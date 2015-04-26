#define LOG_TAG "CanbusService11"
#define LOG_NDEBUG 0
//only used when build entire android source code
//#include <cutils/Log.h>
//if you build with ndk or mm/mmm, use android/log.h
//#include <cutils/Log.h>
//#include <utils/Log.h>

#include <android/log.h>
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGF(...)  fprintf(f_log, __VA_ARGS__);fflush(f_log);

#include "jni.h"
#include "JNIHelp.h"
#include "android_runtime/AndroidRuntime.h"
#include <utils/misc.h>

#include <stdio.h>

#include <unistd.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>

#include <termio.h>

#include <pthread.h>

#define DEVICE_NAME "/dev/ttyHSL1"
#define MODULE_NAME "Canbus"
#define MODULE_AUTHOR "tyholiu@ijidou.com"

#define FILE_LOG 		"/data/user/log_canbus_service"
#define CANBUS_SERVICE 	"com/android/server/CanbusService"

char RESERVED 	= 0x80;
char BLUETOOTH 	= 0x40;
char RADAR 		= 0x20;
char KEY_IN		= 0x10;
char PARK		= 0x08;
char REV		= 0x04;
char ILL		= 0x02;
char ACC		= 0x01;

//TODO: get/set complicate class
//TODO: add listener

namespace android
{
  FILE *f_log;

  pthread_t canbus_read_t;

  char active_info;

  int g_bAttatedT = 0;

  extern JavaVM *cachedJVM;

  int setup_port2(int fd_uart, char *device_name);


  static JNIEnv* getJNIEnv()//was JNIEnv
  {
	  //JavaVM* jvm = cocos2d::JniHelper::getJavaVM();
	  if (cachedJVM == NULL) {
		  LOGF("Failed to get JNIEnv. JniHelper::getJavaVM() is NULL\n");
		  return NULL;
	  }

	  JNIEnv *env = NULL;
	  // get jni environment
	  jint ret = cachedJVM->GetEnv((void**)&env, JNI_VERSION_1_4);

	  switch (ret) {
      	  case JNI_OK :
      		  // Success!
      		LOGF("getenv successA\n");
      		  return env;

      	  case JNI_EDETACHED :
      		  // Thread not attached
      		LOGF("thread not attached\n");
      		  // TODO : If calling AttachCurrentThread() on a native thread
      		  // must call DetachCurrentThread() in future.
      		  // see: http://developer.android.com/guide/practices/design/jni.html

      		  if (cachedJVM->AttachCurrentThread(&env, NULL) < 0)
      		  {
      			LOGF("Failed to get the environment using AttachCurrentThread()\n");
      			  return NULL;
      		  } else {
      			  // Success : Attached and obtained JNIEnv!
      			LOGF("getenv successB\n");
      			  g_bAttatedT = 1;
      			  return env;
      		  }
      	  case JNI_EVERSION :
      		  // Cannot recover from this error
      		LOGF("JNI interface version 1.4 not supported\n");
      	  default :
      		LOGF("Failed to get the environment using GetEnv()\n");
      		  return NULL;
	  }
  }

  static void DetachCurrent()
  {
      if(g_bAttatedT)
      {
    	  cachedJVM->DetachCurrentThread();
      }
  }

  void *read_canbus(void *arg) {

	int fd_uart;
	JNIEnv *env;
	jclass clazz;

  	LOGE("child thread start..\n");
  	LOGF("child thread start..\n");


  	//prepare JNI environment
	LOGE("Check JNIEnv\n");
	LOGF("Check JNIEnv\n");

	env = (JNIEnv *)arg;
	if (env == NULL) {
		LOGE("JNI env is NULL\n");
		LOGF("JNI env is NULL\n");
		return NULL;
	}
	env = getJNIEnv();

	LOGE("%s env3 = %p\n", LOG_TAG, env);
	LOGF("%s env3 = %p\n", LOG_TAG, env);

	clazz = env->FindClass(CANBUS_SERVICE);

	if(clazz == NULL){
		LOGE("find class: %s error\n", CANBUS_SERVICE);
		LOGF("find class: %s error\n", CANBUS_SERVICE);
		DetachCurrent();
		return NULL;
	}
	LOGE("find class: %s\n", CANBUS_SERVICE);
	LOGF("find class: %s\n", CANBUS_SERVICE);

	jfieldID msg_id = env->GetStaticFieldID(clazz, "message", "[B");
	LOGF("get msg_id succeed\n");

    jbyteArray msg = (jbyteArray)(env->GetStaticObjectField(clazz, msg_id));
    jsize msg_len = env->GetArrayLength(msg);
    jbyte *byte_msg = env->GetByteArrayElements(msg, NULL);

    LOGF("get byte_msg succeed...\n");

	jmethodID method1 = env->GetStaticMethodID(clazz, "notifyListeners", "(I)V");
	//jmethodID method1 = (*env)->GetMethodID(env, clazz, "notifyListeners", "(I)V");
	if(method1 == NULL){
		LOGE("find notifyListeners error\n");
		LOGF("find notifyListeners error\n");
		DetachCurrent();
		return NULL;
	}
	LOGE("find notifyListeners ");
	LOGF("find notifyListeners ");

	jmethodID method2 = env->GetStaticMethodID(clazz, "notifyListeners2", "(I[BI)V");
	if(method1 == NULL){
		LOGE("find notifyListeners2 error\n");
		LOGF("find notifyListeners2 error\n");
		DetachCurrent();
		return NULL;
	}
	LOGE("find notifyListeners2\n");
	LOGF("find notifyListeners2\n");

  	 if ((fd_uart = open(DEVICE_NAME, O_RDWR)) == -1) {
  		LOGE("Canbus JNI native: failed to open %s -- %s", DEVICE_NAME, strerror(errno));
  		LOGF("Canbus JNI native: failed to open %s -- %s", DEVICE_NAME, strerror(errno));
  		DetachCurrent();
  	    return NULL;
  	 }
  	LOGE("open file ok ..\n");
  	LOGF("open file ok ..\n");

  	setup_port2(fd_uart, DEVICE_NAME);

  	 char buf[1024];
  	 int len = 1024;
  	 int count = 0;
  	 int i;

     while (1) {
        LOGE("read_canbus: reading ..\n");
        LOGF("read_canbus: reading ..\n");

        //FIXME: message might be read by multiple reads
  	    count = read(fd_uart, buf, 32);

  	    LOGE("read_canbus: Read %d from %s\n", count, DEVICE_NAME);
  	    LOGF("read_canbus: Read %d from %s\n", count, DEVICE_NAME);

  		for (i = 0; i < count; i++) {
  			LOGE("%X ", buf[i] & 0x000000FF);
  			LOGF("%X ", buf[i] & 0x000000FF);
  		}
  		LOGE("\n");
  		LOGF("\n");

  		len = (buf[2] & 0x000000FF) + 5;
  		int com_id = (buf[3] & 0x000000FF);

		switch (com_id) {
			case 0x72:
				active_info = buf[4];
				break;
			case 0x73:
				break;
		}

		//stuff static variable
        memcpy(byte_msg, buf, len);
        LOGF("Set static message succeed...\n");


  	    //env->CallStaticVoidMethod(clazz, method1, com_id);
  	    LOGF("Call back method 1 succeed...\n");

  	    //release bytes ?
  	    LOGF("len = %d...\n", len);
  	    jbyteArray bytes = env->NewByteArray(len);
  	    LOGF("Call back method 2 NewByteArray...\n");
  	    jbyte *byte_array = env->GetByteArrayElements(bytes, NULL);
  	    LOGF("Call back method 2 GetByteArrayElements...\n");
  	    memcpy(byte_array, buf, len);
  	    //void (*CallStaticVoidMethod)(JNIEnv*, jclass, jmethodID, ...);

  	    LOGF("Call back method 2 memcpy...\n");
  	    env->CallStaticVoidMethod(clazz, method2, com_id, bytes, len);

  	    LOGF("Call back method 2 succeed...\n");

  		//sleep(5);
      }

      close(fd_uart);
      LOGF("close fd_uart succeed...\n");

      DetachCurrent();
      LOGF("DetachCurrent succeed...\n");

      return NULL;
  }

  void close() {
//	  pthread_join(canbus_read_t, NULL);
	  fclose(f_log);
  }

  static jboolean canbus_init(JNIEnv *env, jobject obj) {

	  	f_log = fopen(FILE_LOG, "a+");
	  	if (f_log == NULL) {
		   LOGE("%s open log file failed\n", LOG_TAG);
		   LOGF("%s open log file failed\n", LOG_TAG);
	  	   return -1;
	  	}

	  	LOGE("%s canbus_init\n", LOG_TAG);
	  	LOGF("%s canbus_init\n", LOG_TAG);


	  	LOGE("%s env = %p\n", LOG_TAG, env);
	  	LOGF("%s env = %p\n", LOG_TAG, env);

	  	int ret = 0;
	  	ret = pthread_create(&canbus_read_t, NULL, read_canbus, env);

	    if (ret != 0){
	        LOGE("Creat pthread error!\n");
	        LOGF("Creat pthread error!\n");
	        return -EINVAL;
	    }

	    return 0;
  }

  //static is jclass
  //non-static is jobject
  static jboolean canbus_get_bluetooth(JNIEnv *env, jobject obj){
    int val = 0;

    LOGE("Canbus JNI native active_info = %d\n", active_info);
    LOGF("Canbus JNI native active_info = %d\n", active_info);
    return (active_info & BLUETOOTH);;
  }

  static jboolean canbus_is_active_bluetooth(JNIEnv *env, jobject obj){
	  return canbus_get_bluetooth(env, obj);
  }

  int setup_port2(int fd_uart, char *device_name) {

  	struct termios config;

  	if(!isatty(fd_uart)) {
  	  fprintf(stderr, "Not a tty %s -- %s", device_name, strerror(errno));
  	  return -1;
  	}

  	if(tcgetattr(fd_uart, &config) < 0) {
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

  	//#define CPARENB 0000400
  	//#define CPARODD 0001000

  	//#define PARENB CPARENB
  	//#define PARODD CPARODD

  //	config.c_cflag &= ~CSIZE;	//no "byte with mask"

  	config.c_cflag &= ~PARENB;	//no parity
  	config.c_cflag &= ~CSTOPB;	//1bit stop
  	config.c_cflag |= CS8;		//8bits data

  //	config.c_cc[VMIN]  = 1;
  //	config.c_cc[VTIME] = 0;

  	 if(cfsetispeed(&config, B115200) < 0 || cfsetospeed(&config, B115200) < 0) {
  		 fprintf(stderr, "Failed  cfsetispeed %s -- %s", DEVICE_NAME, strerror(errno));
  		 return -1;
  	 }

  	 if(tcsetattr(fd_uart, TCSAFLUSH, &config) < 0) {
  		 fprintf(stderr, "Failed  tcsetattr %s -- %s", DEVICE_NAME, strerror(errno));
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
Ljava/lang/String;	String


Array
[I       jintArray      int[]
[F     jfloatArray    float[]
[B     jbyteArray    byte[]
[C    jcharArray    char[]
[S    jshortArray   short[]
[D    jdoubleArray double[]
[J     jlongArray     long[]
[Z    jbooleanArray boolean[]
"[Ljava/lang/Object;"	Object[]

"()Ljava/lang/String;" 	--> String f();

"(ILjava/lang/Class;)J" --> f(int i, Class c);

"([B)V" 				--> void String(byte[] bytes);


"Lxxx/yyy/Zzz;"	class describer
	*/

  static const JNINativeMethod method_table[] = {
	{"canbus_init1",		"()Z", (void *)canbus_init},
    {"get_bluetooth", 		"()Z", (void *)canbus_get_bluetooth},
	{"is_active_bluetooth", "()Z", (void *)canbus_is_active_bluetooth},
  };

  int register_android_server_CanbusService(JNIEnv *env) {
    return jniRegisterNativeMethods(env, CANBUS_SERVICE, method_table, NELEM(method_table));
  }
};
