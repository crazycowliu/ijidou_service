#include <unistd.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>

#include <stdio.h>
#include <stdlib.h>

#include <errno.h>
#include <string.h>

#include <pthread.h>

#include <termio.h>

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

static int baudflag_arr[] = {

B921600, B460800, B230400, B115200, B57600, B38400,

B19200, B9600, B4800, B2400, B1800, B1200,

B600, B300, B150, B110, B75, B50

};

static int speed_arr[] = {

921600, 460800, 230400, 115200, 57600, 38400,

19200, 9600, 4800, 2400, 1800, 1200,

600, 300, 150, 110, 75, 50

};

void read_msg ();
void write_msg ();
unsigned char check_sum (str_msg *m);
void *read_canbus(void *arg);
int speed_to_flag(int speed);

int setup_port(int fd, int baud, int databits, int parity, int stopbits);
int setup_port2(int fd, char *device_name);
int wiki(int argc,char** argv);

int main(int argc, char **argv) {


	if (!strcmp(argv[2], "write")) {
		write_msg(argv[1]);
	} else if(!strcmp(argv[2], "read")) {
		read_msg(argv[1]);
	} else if(!strcmp(argv[2], "thread")){

		pthread_t read_t;
	  	int ret = 0;


	    ret = pthread_create(&read_t, NULL, read_canbus, NULL);
	    if (ret != 0){
	    	fprintf(stderr, "Creat pthread error!\n");
	        exit(-EINVAL);
	    }

	    pthread_join(read_t, NULL);
	} else if(!strcmp(argv[2], "wiki")){
		wiki(argc, argv);
	}

	fprintf(stderr, "Main thread finished!\n");

	return 0;
}

void read_msg (char *device_name) {

	int fd;
	int i;

	if ((fd = open(device_name, O_RDWR | O_NOCTTY)) == -1) {
	  fprintf(stderr, "Canbus JNI native: failed to open %s -- %s", device_name, strerror(errno));
	  return ;
	}

	char buf[1024];
	char *buf_p = buf;
	int len = 1024;
	int count = 0;
	while ((count = read(fd, buf_p, len)) != -1) {

		printf("Read %d from %s\n", count, device_name);
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

void *read_canbus(void *arg) {

	char *device_name = "/dev/ttyHSL1";
	int fd;
	char  active_info;

	fprintf(stderr, "child thread start..\n");
	fflush(stderr);

	 if ((fd = open(device_name, O_RDWR)) == -1) {
		  fprintf(stderr, "Canbus JNI native: failed to open %s -- %s", device_name, strerror(errno));
	      return NULL;
	 }

	fprintf(stderr, "open file ok ..\n");

//	setup_port(fd, 115200, 8, 0, 1);
	int rc = setup_port2(fd, device_name);

	if (rc != 0) {
		fprintf(stderr, "setup_port2 failed %s -- %s", device_name, strerror(errno));
	}

	 char buf[1024];
	 int len = 1024;
	 int count = 0;
	 int i;

   while (1) {
	   fprintf(stderr, "reading ..\n");
	   count = read(fd, buf, 1024);
	   fprintf(stderr, "Read %d from %s\n", count, device_name);
		for (i = 0; i < count; i++) {
			fprintf(stderr, "%X ", buf[i] & 0x000000FF);
		}
		fprintf(stderr, "\n");

		int com_id = buf[3];

		switch (com_id) {
			case 0x72:
				active_info = buf[4];
				break;
			case 0x73:
				break;
		}
    }

    close(fd);

    return NULL;
}


int setup_port(int fd, int baud, int databits, int parity, int stopbits)

{

	struct termio term_attr;

	/* Get current setting */

	if (ioctl(fd, TCGETA, &term_attr) < 0) {
		fprintf(stderr, "ioctl(fd, TCGETA, &term_attr\n");
		return -1;
	}

	/* Backup old setting */

//	memcpy(&oterm_attr, &term_attr, sizeof(struct termio));

	term_attr.c_iflag &= ~(INLCR | IGNCR | ICRNL | ISTRIP);
	term_attr.c_oflag &= ~(OPOST | ONLCR | OCRNL);
	term_attr.c_lflag &= ~(ISIG | ECHO | ICANON | NOFLSH);
	term_attr.c_cflag &= ~CBAUD;

//	term_attr.c_cflag |= CREAD | speed_to_flag(baud);
	speed_to_flag(baud);
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
	//#define CSTOPB 0000100

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
		fprintf(stderr, "ioctl(fd, TCSETAW, &term_attr) < 0\n");
		return -1;

	}

	if (ioctl(fd, TCFLSH, 2) < 0) {
		fprintf(stderr, "ioctl(fd, TCFLSH, 2) < 0\n");
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


int speed_to_flag(int speed)
{

	int i;

	fprintf(stderr, "sizeof(speed_arr) / sizeof(int) = %d!\n", sizeof(speed_arr) / sizeof(int));
	for (i = 0; i < sizeof(speed_arr) / sizeof(int); i++) {

		if (speed == speed_arr[i]) {
			fprintf(stderr, " baudrate = %X!\n", baudflag_arr[i]);
			return baudflag_arr[i];

		}

	}

	fprintf(stderr, "Unsupported baudrate, use 9600 instead!\n");

	return B9600;

}


int wiki(int argc, char** argv)
{
        struct termios tio;
        struct termios stdio;
        int tty_fd;
        fd_set rdset;

        unsigned char c='D';

        printf("Please start with %s /dev/ttyS1 (for example)\n",argv[0]);
        memset(&stdio,0,sizeof(stdio));
        stdio.c_iflag=0;
        stdio.c_oflag=0;
        stdio.c_cflag=0;
        stdio.c_lflag=0;
        stdio.c_cc[VMIN]=1;
        stdio.c_cc[VTIME]=0;
        tcsetattr(STDOUT_FILENO,TCSANOW,&stdio);
        tcsetattr(STDOUT_FILENO,TCSAFLUSH,&stdio);
        fcntl(STDIN_FILENO, F_SETFL, O_NONBLOCK);       // make the reads non-blocking

        memset(&tio,0,sizeof(tio));
        tio.c_iflag=0;
        tio.c_oflag=0;
        tio.c_cflag=CS8|CREAD|CLOCAL;           // 8n1, see termios.h for more information
        tio.c_lflag=0;
        tio.c_cc[VMIN]=1;
        tio.c_cc[VTIME]=5;

        tty_fd=open(argv[1], O_RDWR | O_NONBLOCK);
        cfsetospeed(&tio,B115200);            // 115200 baud
        cfsetispeed(&tio,B115200);            // 115200 baud

        tcsetattr(tty_fd,TCSANOW,&tio);
        while (c!='q')
        {
                if (read(tty_fd,&c,1)>0)        write(STDOUT_FILENO,&c,1);              // if new data is available on the serial port, print it out
                if (read(STDIN_FILENO,&c,1)>0)  write(tty_fd,&c,1);                     // if new data is available on the console, send it to the serial port
        }

        close(tty_fd);

        return 0;
}
