#define LOG_TAG "HelloService"
#include "jni.h"
#include "JNIHelp.h"
#include "android_runtime/AndroidRuntime.h"
#include <utils/misc.h>
#include <utils/Log.h>
#include <hardware/hardware.h>
#include <hardware/hello.h>
#include <stdio.h>

namespace android
{
  struct hello_device_t *hello_device = NULL;

  static void hello_setVal(JNIEnv *env, jobject clazz, jint value, jchar num) {
    int val = value;
    ALOGI("Hello JNI: set value %d to device val.", val);
    if (!hello_device) {
      ALOGI("Hello JNI: device is not open.");
      return;
    }
    hello_device->set_val(hello_device, val, num);
  }

  static jint hello_getVal(JNIEnv *env, jobject clazz, jchar num){
    int val = 0;
    if (!hello_device) {
      ALOGI("Hello JNI: device is not open.");
      return val;
    }
    hello_device->get_val(hello_device, &val, num);
    ALOGI("Hello JNI: get value %d from device val.", val);
    return val;
  }

  static inline int hello_device_open(const hw_module_t *module, struct hello_device_t **device) {
    return module->methods->open(module, HELLO_HARDWARE_MODULE_ID, (struct hw_device_t **)device);
  }

  static jboolean hello_init(JNIEnv *env, jclass clazz) {
    hello_module_t *module;
    ALOGI("Hello JNI: initializing...");

    if(hw_get_module(HELLO_HARDWARE_MODULE_ID, (const struct hw_module_t **)&module) == 0) {
       ALOGI("Hello JNI: Hello Stub found.");
       if (hello_device_open(&(module->common), &hello_device) == 0) {
         ALOGI("Hello JNI: hello device is open.");
         return 0;
       }
       ALOGE("Hello JNI: failed to open hello device");
       return -1;
    }
    ALOGE("Hello JNI: failed to get hello stub module.");
    return -1;
  }

  static const JNINativeMethod method_table[] = {
    {"init_native", "()Z", (void *)hello_init},
    {"setVal_native", "(IC)V", (void *)hello_setVal},
    {"getVal_native", "(C)I", (void *)hello_getVal},
  };

  int register_android_server_HelloService(JNIEnv *env) {
    return jniRegisterNativeMethods(env, "com/android/server/HelloService", method_table, NELEM(method_table));
  }
};
