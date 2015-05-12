#include <stdio.h>
#include <stdlib.h>
#include <string.h>

void hex_string_to_binary_array();

int main(int argc, char **argv) {
	char buf[12];
	int i = 0;
	hex_string_to_binary_array(buf, 12, argv[1]);
	for (i = 0; i < 12; i++) {
		printf("%X ", (buf[i] & 0x000000FF));
	}
	printf("\n");
}

void hex_string_to_binary_array(char buf[], int buf_len, char *message) {
	int i = 0;
	int j = 0;
	int len = strlen(message);
	char *cp = &buf[0];
	int ch_h;
	int ch_l;
	char ch;
	//msg="AB"
	//msg="01 "
	//msg="31 "
	//msg="31 32 33 34"
	//msg="31 32 33 34 "
	//msg="31 32 33 34 31 32 33 34 31 32 33 34 31 32 33 34 "

	for (i = 0; i < len; i++) {
		ch = message[i];
		if (ch == ' ') {
			if (message[i-2] >= '0' && message[i-2] <= '9') {
				ch_h = message[i-2] - '0';
			} else if (message[i-2] >= 'A' && message[i-2] <= 'Z') {
				ch_h = message[i-2] - 'A' + 10;
			} else if (message[i-2] >= 'a' && message[i-2] <= 'z') {
				ch_h = message[i-2] - 'a' + 10;
			} else {
				ch_h = 0;
			}

			if (message[i-1] >= '0' && message[i-1] <= '9') {
				ch_l = message[i-1] - '0';
			} else if (message[i-1] >= 'A' && message[i-1] <= 'Z') {
				ch_l = message[i-1] - 'A' + 10;
			} else if (message[i-1] >= 'a' && message[i-1] <= 'z') {
				ch_l = message[i-1] - 'a' + 10;
			} else {
				ch_l = 0;
			}

			cp[j++] = ch_h * 16 + ch_l;
			if (j == buf_len) {
				break;
			}
		}
	}

	if (j < buf_len) {
		//i == len
		if (message[i-1] != ' ') {
			if (message[i-2] >= '0' && message[i-2] <= '9') {
				ch_h = message[i-2] - '0';
			} else if (message[i-2] >= 'A' && message[i-2] <= 'Z') {
				ch_h = message[i-2] - 'A' + 10;
			} else if (message[i-2] >= 'a' && message[i-2] <= 'z') {
				ch_h = message[i-2] - 'a' + 10;
			} else {
				ch_h = 0;
			}

			if (message[i-1] >= '0' && message[i-1] <= '9') {
				ch_l = message[i-1] - '0';
			} else if (message[i-1] >= 'A' && message[i-1] <= 'Z') {
				ch_l = message[i-1] - 'A' + 10;
			} else if (message[i-1] >= 'a' && message[i-1] <= 'z') {
				ch_l = message[i-1] - 'a' + 10;
			} else {
				ch_l = 0;
			}
			cp[j++] = ch_h * 16 + ch_l;
		}
	}

	for (; j < buf_len; j++) {
		cp[j] = ' ';
	}
}
