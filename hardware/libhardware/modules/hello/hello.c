#define LOG_TAG "HelloStub"

#include <hardware/hardware.h>
#include <hardware/hello.h>
#include <fcntl.h>
#include <errno.h>
#include <cutils/log.h>
#include <cutils/atomic.h>

#define DEVICE_NAME "/dev/hello"
#define MODULE_NAME "Hello"
#define MODULE_AUTHOR "crazycowliu@gmail.com"

static int hello_device_open(const struct hw_module_t *module, const char *name, struct hw_device_t **device);
static int hello_device_close(struct hw_device_t *device);

static int hello_set_val(struct hello_device_t *dev, int val, unsigned char num);
static int hello_get_val(struct hello_device_t *dev, int *val, unsigned char num);

static struct hw_module_methods_t hello_module_methods = {
  open: hello_device_open
};

struct hello_module_t HAL_MODULE_INFO_SYM = {
  common: {
    tag: HARDWARE_MODULE_TAG,
    version_major: 1,
    version_minor: 0,
    id: HELLO_HARDWARE_MODULE_ID,
    name: MODULE_NAME,
    author: MODULE_AUTHOR,
    methods: &hello_module_methods, 
  }
};

static int hello_device_open(const struct hw_module_t *module, const char *name, struct hw_device_t **device) {
  struct hello_device_t *dev;
  dev = (struct hello_device_t *)malloc(sizeof(struct hello_device_t));
  if (!dev) {
    ALOGE("Hello Stub: failed to alloc space");
    return -ENOMEM;
  }

  memset(dev, 0, sizeof(struct hello_device_t));
  dev->common.tag = HARDWARE_DEVICE_TAG;
  dev->common.version = 0;
  dev->common.module = (hw_module_t *)module;
  dev->common.close = hello_device_close;
  dev->set_val = hello_set_val;
  dev->get_val = hello_get_val;

  if ((dev->fd = open(DEVICE_NAME, O_RDWR)) == -1) {
    ALOGE("Hello Stub: failed to open /dev/hello -- %s", strerror(errno));
    free(dev);
    return -EINVAL;
  }

  *device = &(dev->common);
  ALOGI("Hello Stub: open /dev/hello successfully.");

  return 0;
}

static int hello_device_close(struct hw_device_t *device) {
  struct hello_device_t *hello_device = (struct hello_device_t *)device;
  if (hello_device) {
    close(hello_device->fd);
    free(hello_device);
  }
  return 0;
}

static int hello_set_val(struct hello_device_t *dev, int val, unsigned char num) {
  ALOGI("Hello Stub: set value %d to device val.", val);
  write(dev->fd, &val, 4);
  return 0;
}

static int hello_get_val(struct hello_device_t *dev, int *val, unsigned char num) {
  if (!val) {
    ALOGE("Hello Stub: error val pointer");
    return -EINVAL;
  }

  read(dev->fd, val, 4);
  ALOGI("Hello Stub: get value %d from device val", *val);

  return 0;
}
