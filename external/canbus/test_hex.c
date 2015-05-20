#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int hex_string_to_binary_array(char buf[], int buf_len, char *message);

int main(int argc, char **argv) {
	char buf[12];
	int i = 0;
	int j = hex_string_to_binary_array(buf, 12, argv[1]);
	for (i = 0; i < j; i++) {
		printf("%X ", (buf[i] & 0x000000FF));
	}
	printf("\n");
}


int hex_string_to_binary_array(char buf[], int buf_len, char *message) {
	int i = 0;
	int j = 0;
	int len = strlen(message);
	char *cp = &buf[0];
	char tmp[3];
	int ch_h;
	int ch_l;
	char ch;
	//msg="AB"
	//msg="01 "
	//msg="31 "
	//msg="31 32 33 34"
	//msg="31 32 33 34 "
	//msg="31 32 33 34 31 32 33 34 31 32 33 34 31 32 33 34 "

	int idx = 0;
	for (i = 0; i < len; i++) {
		ch = message[i];
		if (ch == ' ') {
		  if (idx == 0)
		    continue;

		  tmp[idx] = '\0';

		  cp[j++] = (char)strtol(tmp, NULL, 16);
			idx = 0;
			if (j == buf_len) {
				break;
			}
		} else {
		  tmp[idx++] = ch;
		}
	}

	if (j < buf_len) {
		//i == len
		if (message[i-1] != ' ') {
		  if (idx > 0) {
		    cp[j++] = (char)strtol(tmp, NULL, 16);
		  }
		}
	}

	return j;
}
