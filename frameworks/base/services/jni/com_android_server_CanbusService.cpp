#define LOG_TAG "CanbusService10"
#define LOG_NDEBUG 0
//only used when build entire android source code
//#include <cutils/Log.h>
//if you build with ndk or mm/mmm, use android/log.h
//#include <cutils/Log.h>
//#include <utils/Log.h>

#include <android/log.h>
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGF(...)  fprintf(fd_log, __VA_ARGS__)

#include "jni.h"
#include "JNIHelp.h"
#include "android_runtime/AndroidRuntime.h"
#include <utils/misc.h>

/**
 * NO HAL
#include <hardware/hardware.h>
#include <hardware/hello.h>
*/
#include <stdio.h>

#include <unistd.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>

#include <termio.h>

#include <pthread.h>

//#define DEVICE_NAME "/dev/canbus"
#define DEVICE_NAME "/dev/ttyHSL1"
#define MODULE_NAME "Canbus"
#define MODULE_AUTHOR "tyholiu@ijidou.com"

char RESERVED 	= 0x80;
char BLUETOOTH 	= 0x40;
char RADAR 		= 0x20;
char KEY_IN		= 0x10;
char PARK		= 0x08;
char REV		= 0x04;
char ILL		= 0x02;
char ACC		= 0x01;

//TODO: ioctl to set brand rate
//TODO: get/set complicate class
//TODO: add listener

namespace android
{

  pthread_t canbus_read_t;
  char active_info;

  int setup_port(int fd, int baud, int databits, int parity, int stopbits);
  int setup_port2(int fd, char *device_name);

  void *read_canbus(void *arg) {

	int fd;
	FILE *fd_log;

  	char device_name[] = "/dev/ttyHSL1";
//  	char device_name[] = "/data/user/abc";

  	fd_log = fopen("/data/user/out_jjww", "w");
  	if (fd_log == NULL) {
  	   return NULL;
  	}


  	fprintf(fd_log, "CanbusService10 child thread start..\n");
  	fflush(fd_log);
  	LOGE("child thread start..\n");


  	 if ((fd = open(device_name, O_RDWR)) == -1) {
  		fprintf(fd_log, "Canbus JNI native: failed to open %s -- %s", device_name, strerror(errno));
  		  		fflush(fd_log);
  		 LOGF("Canbus JNI native: failed to open %s -- %s", device_name, strerror(errno));

  	    return NULL;
  	 }

  	fprintf(fd_log, "open file ok ..\n");
  	fflush(fd_log);
  	LOGE("open file ok ..\n");

//  	setup_port(fd, 115200, 8, 0, 1);
  	setup_port2(fd, device_name);

  	 char buf[1024];
  	 int len = 1024;
  	 int count = 0;
  	 int i;

     while (1) {
    	 fprintf(fd_log, "read_canbus: reading ..\n");
    	 fflush(fd_log);
        LOGE("read_canbus: reading ..\n");

  	    count = read(fd, buf, 32);
  	    fprintf(fd_log, "read_canbus: Read %d from %s\n", count, device_name);
  	    fflush(fd_log);
  	    LOGE("read_canbus: Read %d from %s\n", count, device_name);

  		for (i = 0; i < count; i++) {
  			fprintf(fd_log, "%X ", buf[i] & 0x000000FF);
  			fflush(fd_log);
  			LOGE("%X ", buf[i] & 0x000000FF);
  		}
  		fprintf(fd_log, "\n");
  		fflush(fd_log);
  		LOGE("\n");

  		int com_id = buf[3];

  		switch (com_id) {
  			case 0x72:
  				active_info = buf[4];
  		  	    fprintf(fd_log, "Recevie: 0x72\n");
  		  	    fflush(fd_log);
  				break;
  			case 0x73:
  				break;
  		}

  		sleep(5);
      }

      close(fd);

      fclose(fd_log);

      return NULL;
  }

  void close() {
//	  pthread_join(canbus_read_t, NULL);
  }

  //TODO: ioctl
  static jboolean canbus_init(JNIEnv *env, jclass clazz) {

	  	LOGE("stderr canbus_init  ..\n");
	  	LOGE("LOGE canbus_init...!\n");
	  	int ret = 0;

	    ret = pthread_create(&canbus_read_t, NULL, read_canbus, NULL);
//	  	read_canbus(NULL);
	    if (ret != 0){
	        LOGE("Creat pthread error!\n");
	        exit(-EINVAL);
	    }

	    return 0;
  }

  static jboolean canbus_get_bluetooth(JNIEnv *env, jobject clazz){
    int val = 0;

    LOGE("Canbus JNI native active_info=%d", active_info);
    return (active_info & BLUETOOTH);
  }

  static jboolean canbus_is_active_bluetooth(JNIEnv *env, jobject clazz){
	  return canbus_get_bluetooth(env, clazz);
  }


  //FIXME: check me
  int setup_port(int fd, int baud, int databits, int parity, int stopbits)

  {

  	struct termio term_attr;

  	/* Get current setting */

  	if (ioctl(fd, TCGETA, &term_attr) < 0) {
  		LOGE("ioctl(fd, TCGETA, &term_attr\n");
  		return -1;
  	}

  	/* Backup old setting */

  //	memcpy(&oterm_attr, &term_attr, sizeof(struct termio));

  	term_attr.c_iflag &= ~(INLCR | IGNCR | ICRNL | ISTRIP);
  	term_attr.c_oflag &= ~(OPOST | ONLCR | OCRNL);
  	term_attr.c_lflag &= ~(ISIG  | ECHO  | ICANON | NOFLSH);
  	term_attr.c_cflag &= ~CBAUD;

  //	term_attr.c_cflag |= CREAD | speed_to_flag(baud);
//  	speed_to_flag(baud);
  	term_attr.c_cflag |= (CREAD | B115200);

  	/* Set databits */

  	term_attr.c_cflag &= ~(CSIZE);
  	switch (databits) {
  	case 5:

  		term_attr.c_cflag |= CS5;
  		break;

  	case 6:

  		term_attr.c_cflag |= CS6;

  		break;

  	case 7:

  		term_attr.c_cflag |= CS7;

  		break;

  	case 8:

  	default:

  		term_attr.c_cflag |= CS8;

  		break;

  	}

  	/* Set parity */

  	switch (parity) {

  	case 1: /* Odd parity */

  		term_attr.c_cflag |= (PARENB | PARODD);

  		break;

  	case 2: /* Even parity */

  		term_attr.c_cflag |= PARENB;

  		term_attr.c_cflag &= ~(PARODD);

  		break;

  	case 0: /* None parity */

  	default:

  		term_attr.c_cflag &= ~(PARENB);

  		break;

  	}

  	/* Set stopbits */

  	switch (stopbits) {

  	case 2: /* 2 stopbits */

  		term_attr.c_cflag |= CSTOPB;

  		break;

  	case 1: /* 1 stopbits */

  	default:

  		term_attr.c_cflag &= ~CSTOPB;

  		break;

  	}

  	term_attr.c_cc[VMIN] = 1;

  	term_attr.c_cc[VTIME] = 0;

  	if (ioctl(fd, TCSETAW, &term_attr) < 0) {
  		LOGE("ioctl(fd, TCSETAW, &term_attr) < 0\n");
  		return -1;

  	}

  	if (ioctl(fd, TCFLSH, 2) < 0) {
  		LOGE("ioctl(fd, TCFLSH, 2) < 0\n");
  		return -1;

  	}

  	return 0;

  }

  int setup_port2(int fd, char *device_name) {

  	struct termios config;

  	if(!isatty(fd)) {
  	  fprintf(stderr, "Not a tty %s -- %s", device_name, strerror(errno));
  	  return -1;
  	}

  	if(tcgetattr(fd, &config) < 0) {
  		fprintf(stderr, "Can not get config %s -- %s", device_name, strerror(errno));
  		return -1;
  	}

  	//Input flags - Turn off input processing
  //	config.c_iflag &= ~(IGNBRK | BRKINT | ICRNL |
  //	                     INLCR | PARMRK | INPCK | ISTRIP | IXON);
  	config.c_iflag = 0;

  	//Output flags - Turn off output processing
  	config.c_oflag = 0;

  	//local mode flag
  	//No line processing
  //	config.c_lflag &= ~(ECHO | ECHONL | ICANON | IEXTEN | ISIG);
  	config.c_lflag = 0;

  	//#define CSIZE 0000060  	// byte with mask
  	//#define CSTOPB 0000100	//2 stop bits

  	//#define CPARENB 0000400  // 开启输出时产生奇偶位、输入时进行奇偶校验。
  	//#define CPARODD 0001000  // 输入/输入校验是奇校验。

  	//#define PARENB CPARENB  // 开启输出时产生奇偶位、输入时进行奇偶校验。
  	//#define PARODD CPARODD  // 输入/输入校验是奇校验。

  //	config.c_cflag &= ~CSIZE;	//no "byte with mask"

  	config.c_cflag &= ~PARENB;	//no parity
  	config.c_cflag &= ~CSTOPB;	//1bit stop
  	config.c_cflag |= CS8;		//8bits data

  //	config.c_cc[VMIN]  = 1;
  //	config.c_cc[VTIME] = 0;

  	 if(cfsetispeed(&config, B115200) < 0 || cfsetospeed(&config, B115200) < 0) {
  		 fprintf(stderr, "Failed  cfsetispeed %s -- %s", device_name, strerror(errno));
  		 return -1;
  	 }

  	 if(tcsetattr(fd, TCSAFLUSH, &config) < 0) {
  		 fprintf(stderr, "Failed  tcsetattr %s -- %s", device_name, strerror(errno));
  		 return -1;
  	 }

  	 return 0;
  }

/*
V      void            void
Z       jboolean     boolean
I        jint              int
J       jlong            long
D      jdouble       double
F      jfloat            float
B      jbyte            byte
C      jchar           char
S      jshort          short

Array
[I       jintArray      int[]
[F     jfloatArray    float[]
[B     jbyteArray    byte[]
[C    jcharArray    char[]
[S    jshortArray   short[]
[D    jdoubleArray double[]
[J     jlongArray     long[]
[Z    jbooleanArray boolean[]
	*/

  static const JNINativeMethod method_table[] = {
	{"canbus_init1",		"()Z", (void *)canbus_init},
    {"get_bluetooth", 		"()Z", (void *)canbus_get_bluetooth},
	{"is_active_bluetooth", "()Z", (void *)canbus_is_active_bluetooth},
  };

  int register_android_server_CanbusService(JNIEnv *env) {
    return jniRegisterNativeMethods(env, "com/android/server/CanbusService", method_table, NELEM(method_table));
  }
};
