#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>

#define DEVICE_NAME "/dev/hello"

int main(int argc, char **argv) {
  int fd = -1;
  int val = 0;
  int i;
  fd = open(DEVICE_NAME, O_RDWR);
  if (fd == -1) {
    printf ("Failed to open device %s.\n", DEVICE_NAME);
    return -1;
  }

  printf("Read original value \n");
  read(fd, &val, 0);
  printf("val = %d.\n", val);

  val = 100;
  printf("Write value %d to %s\n", val, DEVICE_NAME);
  write(fd, &val, 0);

  printf("Read original value again\n");
  read(fd, &val, 0);
  printf("val = %d.\n", val);

  close(fd);

  return 0;
}
