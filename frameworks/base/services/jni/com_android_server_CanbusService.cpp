#define LOG_TAG "CanbusService3"
#include "jni.h"
#include "JNIHelp.h"
#include "android_runtime/AndroidRuntime.h"
#include <utils/misc.h>
#include <utils/Log.h>
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
  int fd;
  pthread_t read_t;
  char active_info;
  int setup_port(int fd, int baud, int databits, int parity, int stopbits);

  void *read_canbus(void *arg) {

  	char device_name[] = "/dev/ttyHSL1";
  	ALOGE("child thread start..\n");
  	fflush(stderr);

  	 if ((fd = open(device_name, O_RDWR)) == -1) {
  		  ALOGE("Canbus JNI native: failed to open %s -- %s", device_name, strerror(errno));
  	      return NULL;
  	 }

  	ALOGE("open file ok ..\n");

  	setup_port(fd, 115200, 8, 0, 1);

  	 char buf[1024];
  	 int len = 1024;
  	 int count = 0;
  	 int i;

     while (1) {
  	   ALOGE("read_canbus: reading ..\n");
  	   count = read(fd, buf, 1024);
  	   ALOGE("read_canbus: Read %d from %s\n", count, device_name);
  		for (i = 0; i < count; i++) {
  			ALOGE("%X ", buf[i] & 0x000000FF);
  		}
  		ALOGE("\n");

  		int com_id = buf[3];

  		switch (com_id) {
  			case 0x72:
  				active_info = buf[4];
  				break;
  			case 0x73:
  				break;
  		}

  		sleep(5);
      }

      close(fd);

      return NULL;
  }

  void close() {
//	  pthread_join(read_t, NULL);
  }

  //TODO: ioctl
  static jboolean canbus_init(JNIEnv *env, jclass clazz) {

	  	ALOGE("stderr canbus_init  ..\n");
	  	ALOGE("ALOGE canbus_init...!\n");
	  	int ret = 0;

	    ret = pthread_create(&read_t, NULL, read_canbus, NULL);
	    if (ret != 0){
	        ALOGE("Creat pthread error!\n");
	        exit(-EINVAL);
	    }

	    return 0;
  }

  static jboolean canbus_get_bluetooth(JNIEnv *env, jobject clazz){
    int val = 0;

    if (fd == -1) {
      ALOGI("Canbus JNI: device is not open.");
      return 0;
    }

    ALOGE("Canbus JNI native active_info=%d", active_info);
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
  		ALOGE("ioctl(fd, TCGETA, &term_attr\n");
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
  		ALOGE("ioctl(fd, TCSETAW, &term_attr) < 0\n");
  		return -1;

  	}

  	if (ioctl(fd, TCFLSH, 2) < 0) {
  		ALOGE("ioctl(fd, TCFLSH, 2) < 0\n");
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
	{"init", 				"()Z", (void *)canbus_init},
    {"get_bluetooth", 		"()Z", (void *)canbus_get_bluetooth},
	{"is_active_bluetooth", "()Z", (void *)canbus_is_active_bluetooth},
  };

  int register_android_server_CanbusService(JNIEnv *env) {
    return jniRegisterNativeMethods(env, "com/android/server/CanbusService", method_table, NELEM(method_table));
  }
};
