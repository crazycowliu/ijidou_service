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

//#define DEVICE_NAME "/dev/canbus"
#define DEVICE_NAME "/dev/ttyS0"
#define MODULE_NAME "Canbus"
#define MODULE_AUTHOR "tyholiu@ijidou.com"

namespace android
{

  int fd;

  static jboolean canbus_init(JNIEnv *env, jclass clazz) {

  if ((fd = open(DEVICE_NAME, O_RDWR)) == -1) {
	    ALOGE("Canbus JNI native: failed to open %s -- %s", DEVICE_NAME, strerror(errno));
	    return -EINVAL;
  }

	  return 0;
  }

  static jboolean canbus_get_bluetooth(JNIEnv *env, jobject clazz, jchar num){
    int val = 0;

    if (fd == -1) {
      ALOGI("Canbus JNI: device is not open.");
      return 0;
    }

    read(fd, &val, 1);

    return val;
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
    {"get_bluetooth", "()Z", (void *)canbus_get_bluetooth},
  };

  int register_android_server_CanbusService(JNIEnv *env) {
    return jniRegisterNativeMethods(env, "com/jidou/server/CanbusService", method_table, NELEM(method_table));
  }


};
