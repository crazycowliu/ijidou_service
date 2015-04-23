#define LOG_TAG "CanbusService"
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

#include <pthread.h>

//#define DEVICE_NAME "/dev/canbus"
#define DEVICE_NAME "/dev/ttyHSL1"
#define MODULE_NAME "Canbus"
#define MODULE_AUTHOR "tyholliu@ijidou.com"

char RESERVED 	= 0x80;
char BLUETOOTH 	= 0x40;
char RADAR 		= 0x20;
char KEY_IN		= 0x10;
char PARK		= 0x08;
char REV		= 0x04;
char ILL		= 0x02;
char ACC		= 0x01;

namespace android
{
  int fd;
  pthread_t read_t;
  char active_info;

  void *read_canbus(void *arg) {

	 if ((fd = open(DEVICE_NAME, O_RDWR)) == -1) {
		  ALOGE("Canbus JNI native: failed to open %s -- %s", DEVICE_NAME, strerror(errno));
	      return NULL;
	 }

	 //TODO: ioctl to set brand rate

	 char buf[1024];
	 int len = 1024;
	 int count = 0;
	 int i;
     while (1){
    	count = read(fd, buf, 1024);
		printf("Read %d from %s\n", count, DEVICE_NAME);
		for (i = 0; i < count; i++) {
			printf("%X ", buf[i] & 0x000000FF);
		}
		printf("\n");

		int com_id = buf[3];

		switch (com_id) {
			case 0x72:
				active_info = buf[4];
				break;
			case 0x73:
				break;
		}
      }

      //
      close(fd);

      return NULL;
  }

  void close() {
	  pthread_join(read_t, NULL);
  }

  //TODO: ioctl
  static jboolean canbus_init(JNIEnv *env, jclass clazz) {

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

    return (active_info & BLUETOOTH);
  }

  static jboolean canbus_is_active_bluetooth(JNIEnv *env, jobject clazz){
	  return canbus_get_bluetooth(env, clazz);
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
	{"init", "()Z", (void *)canbus_init},
    {"get_bluetooth", 		"()Z", (void *)canbus_get_bluetooth},
	{"is_active_bluetooth", "()Z", (void *)canbus_is_active_bluetooth},
  };

  int register_android_server_CanbusService(JNIEnv *env) {
    return jniRegisterNativeMethods(env, "com/android/server/CanbusService", method_table, NELEM(method_table));
  }
};
