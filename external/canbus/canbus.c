#include <unistd.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>

#include <stdio.h>
#include <stdlib.h>

#include <errno.h>
#include <string.h>

#define DEVICE_NAME "/dev/ttyS2"

//NOTE: do not align
typedef struct struct_message {
	unsigned char sof1;

	unsigned char sof2;

	unsigned char length;	//length of data

	unsigned char comID;

	unsigned char data[256];

	unsigned char checksum;	//(length + comID + data[0] + ... + data[length - 1]) & 0xFF - 1

} str_msg;

void read_msg ();
void write_msg ();
unsigned char check_sum (str_msg *m);

int main(int argc, char **argv) {

	if (!strcmp(argv[2], "write")) {
		write_msg(argv[1]);
	} else {
		read_msg(argv[1]);
	}

	return 0;
}

void read_msg (char *device_name) {
	int fd;
	int i;
	if ((fd = open(device_name, O_RDWR)) == -1) {
	  fprintf(stderr, "Canbus JNI native: failed to open %s -- %s", device_name, strerror(errno));
	  return ;
	}

	char buf[1024];
	char *buf_p = buf;
	int len = 1024;
	int count = 0;
	while ((count = read(fd, buf_p, len)) != -1) {

		printf("Read %d from /dev/ttyS2\n", count);
		for (i = 0; i < count; i++) {
			printf("%X ", buf_p[i] & 0x000000FF);
		}
		printf("\n");

		buf_p += count;
		len -= count;
	}

	close(fd);
}


void write_msg (char *device_name) {
	int fd;
	int i;

	if ((fd = open(device_name, O_RDWR)) == -1) {
	  fprintf(stderr, "Canbus JNI native: failed to open %s -- %s", device_name, strerror(errno));
	  return ;
	}

	str_msg msg;

	msg.sof1 = 0xAA;
	msg.sof2 = 0x55;

	msg.comID = 0xD2;
	msg.data[0] = 0x01;
	sprintf(&msg.data[1], "%s", "92.5MHz     ");
	msg.length = 1 + 12;

//	msg.comID = 0xE2;
//	sprintf(&msg.data[0], "%s", "92.5MHz      ");
//	msg.length = 13;

	msg.checksum = check_sum(&msg);

	printf("Write %d to %s\n", (msg.length + 4 + 1), DEVICE_NAME);

	printf("%X ", msg.sof1);
	printf("%X ", msg.sof2);
	printf("%X ", msg.length);
	printf("%X ", msg.comID);

	for (i = 0; i < msg.length; i++) {
		printf("%X ", msg.data[i]);
	}
	printf("%X ", msg.checksum);

	printf("\n");


	//Do not consider partial write
//	write(fd, msg, 4);
	int count = -1;
	count = write(fd, &msg.sof1, 1);
	printf("write count = %d\n", count);
	write(fd, &msg.sof2, 1);
	write(fd, &msg.length, 1);
	write(fd, &msg.comID, 1);
	write(fd, msg.data, msg.length);
	write(fd, &msg.checksum, 1);

	close(fd);
}

unsigned char check_sum (str_msg *m) {
	unsigned char sum = 0;
	sum += m->length;
	printf("%X ", sum);

	sum += m->comID;
	printf("%X ", sum);

	int i = 0;
	for (i = 0; i < m->length; i++) {
		sum += m->data[i];
		printf("%X ", sum);
	}
	sum = sum & 0xFF;
	printf("%X ", sum);

	sum -= 1;
	printf("%X ", sum);

	return sum;
}
