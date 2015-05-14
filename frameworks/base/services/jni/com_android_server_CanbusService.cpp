#define LOG_TAG "CanbusService_jni"
#define LOG_NDEBUG 0
#include <android/log.h>
//only used when build entire android source code
//#include <cutils/Log.h>
//if you build with ndk or mm/mmm, use android/log.h
//#include <cutils/Log.h>
//#include <utils/Log.h>

#define CANBUS_EBUG

#ifdef CANBUS_EBUG
  #define LOGF(...)  fprintf(f_log, __VA_ARGS__);fflush(f_log);
#else
  #define LOGF(...)
#endif


#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)


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

#define FILE_LOG     "/data/user/log_canbus_service"
#define CANBUS_SERVICE   "com/android/server/CanbusService"

namespace android
{
  extern JavaVM *cachedJVM;

  FILE *f_log;

  static pthread_t canbus_read_t;

  static int g_attached = 0;

  static jobject g_obj = NULL;

  static int setup_port(int fd_uart, char *device_name);

  /**
   * Attach the JNI environment to the pthread
   */
  static JNIEnv* getJNIEnv()
  {
    if (cachedJVM == NULL) {
      LOGF("Failed to get JNIEnv. JniHelper::getJavaVM() is NULL\n");
      return NULL;
    }

    JNIEnv *env = NULL;
    jint ret = cachedJVM->GetEnv((void**)&env, JNI_VERSION_1_4);

    switch (ret) {
          case JNI_OK :
          LOGF("getenv successA\n");
          return env;

          case JNI_EDETACHED :
          LOGF("thread not attached\n");
            if (cachedJVM->AttachCurrentThread(&env, NULL) < 0) {
            LOGF("Failed to get the environment using AttachCurrentThread()\n");
            return NULL;
            } else {
              // Success : Attached and obtained JNIEnv!
            LOGF("getenv successB\n");
            g_attached = 1;
            return env;
            }
          case JNI_EVERSION :
          LOGF("JNI interface version 1.4 not supported\n");
          default :
          LOGF("Failed to get the environment using GetEnv()\n");
          return NULL;
    }
  }

  static void DetachCurrent()
  {
      if(g_attached) {
        cachedJVM->DetachCurrentThread();
      }
  }

  void *read_canbus(void *arg)
  {
    int fd_uart;
    JNIEnv *env;

    LOGE("child thread start..\n");
    LOGF("child thread start..\n");

      //prepare JNI environment
    LOGE("Check JNIEnv\n");
    LOGF("Check JNIEnv\n");

    env = getJNIEnv();
    if (env == NULL) {
      LOGE("JNI env is NULL\n");
      LOGF("JNI env is NULL\n");
      return NULL;
    }

    if (g_obj == NULL) {
      LOGE("JNI g_obj is NULL\n");
      LOGF("JNI g_obj is NULL\n");
      return NULL;
    }

    jclass clazz = env->GetObjectClass(g_obj);
    jmethodID method2 = env->GetMethodID(clazz, "notifyListeners", "(I[BI)V");

    if(method2 == NULL){
      LOGE("find method notifyListeners error\n");
      LOGF("find method notifyListeners error\n");
      DetachCurrent();
      return NULL;
    }
    LOGE("find method notifyListeners\n");
    LOGF("find method notifyListeners\n");

    if ((fd_uart = open(DEVICE_NAME, O_RDWR)) == -1) {
      LOGE("Canbus JNI native: failed to open %s -- %s", DEVICE_NAME, strerror(errno));
      LOGF("Canbus JNI native: failed to open %s -- %s", DEVICE_NAME, strerror(errno));
      DetachCurrent();
      return NULL;
    }
    LOGE("open file ok ..\n");
    LOGF("open file ok ..\n");

    setup_port(fd_uart, DEVICE_NAME);

    char buf[1024];
    int len = 1024;
    int count = 0;
    int i;

    jbyteArray bytes = env->NewByteArray(1024);
    if (bytes == NULL) {
      LOGF("Canbus JNI native: can not alloc byte array\n");
      DetachCurrent();
      return NULL;
    }
    jbyte *byte_array = env->GetByteArrayElements(bytes, NULL);

     while (1) {
        LOGE("read_canbus: reading ..\n");
        LOGF("read_canbus: reading ..\n");

        //FIXME: message might be read by multiple reads
        //FIXME: synchronized with write if possible
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

        LOGF("len = %d...\n", len);
        memcpy(byte_array, buf, len);

        LOGF("Call back method 2 memcpy...\n");
        env->CallVoidMethod(g_obj, method2, com_id, bytes, len);
        LOGF("Call back method 2 succeed...\n");
      }

      env->DeleteLocalRef(bytes);

      close(fd_uart);
      LOGF("close fd_uart succeed...\n");

      env->DeleteGlobalRef(g_obj);
      LOGF("close delete g_obj succeed...\n");

      DetachCurrent();
      LOGF("DetachCurrent succeed...\n");

      return NULL;
  }

  void close() {
    fclose(f_log);
  }

  static jboolean canbus_init(JNIEnv *env, jobject obj) {

      f_log = fopen(FILE_LOG, "w+");
      if (f_log == NULL) {
       LOGE("%s open log file failed\n", LOG_TAG);
       LOGF("%s open log file failed\n", LOG_TAG);
         return -1;
      }

      LOGE("%s canbus_init\n", LOG_TAG);
      LOGF("%s canbus_init\n", LOG_TAG);

      LOGE("%s env = %p\n", LOG_TAG, env);
      LOGF("%s env = %p\n", LOG_TAG, env);

      g_obj = env->NewGlobalRef(obj);

      int ret = pthread_create(&canbus_read_t, NULL, read_canbus, NULL);

      if (ret != 0){
          LOGE("Creat pthread error!\n");
          LOGF("Creat pthread error!\n");

          env->DeleteGlobalRef(g_obj);
          return -EINVAL;
      }

      return 0;
  }

  /**
   * Not implemented yet, Hiworld do not provide query interface
   */
  static jbyteArray canbus_query_message(JNIEnv *env, jobject obj, jint comId, jbyteArray queryMsg) {
    jbyteArray bytes = env->NewByteArray(0);
    return bytes; //do not need to rlease bytes
  }

  int setup_port(int fd_uart, char *device_name) {

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
    //  config.c_iflag &= ~(IGNBRK | BRKINT | ICRNL |
    //                       INLCR | PARMRK | INPCK | ISTRIP | IXON);
    config.c_iflag = 0;

    //Output flags - Turn off output processing
    config.c_oflag = 0;

    //local mode flag
    //No line processing
    //  config.c_lflag &= ~(ECHO | ECHONL | ICANON | IEXTEN | ISIG);
    config.c_lflag = 0;

    //#define CSIZE 0000060   // byte with mask
    //#define CSTOPB 0000100  //2 stop bits

    //#define CPARENB 0000400
    //#define CPARODD 0001000

    //#define PARENB CPARENB
    //#define PARODD CPARODD

    //  config.c_cflag &= ~CSIZE;  //no "byte with mask"

    config.c_cflag &= ~PARENB;  //no parity
    config.c_cflag &= ~CSTOPB;  //1bit stop
    config.c_cflag |= CS8;    //8bits data

    //  config.c_cc[VMIN]  = 1;
    //  config.c_cc[VTIME] = 0;

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

  static const JNINativeMethod method_table[] = {
      {"native_init",            "()Z", (void *)canbus_init},
      {"native_query_message",   "(I[B)[B", (void *)canbus_query_message},
  };

  int register_android_server_CanbusService(JNIEnv *env) {
    return jniRegisterNativeMethods(env, CANBUS_SERVICE, method_table, NELEM(method_table));
  }
};
