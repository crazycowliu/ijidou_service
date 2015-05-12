/*
 * 说明：SPI通讯实现
 *     方式一： 同时发送与接收实现函数： SPI_Transfer()
 *     方式二：发送与接收分开来实现
 *     SPI_Write() 只发送
 *     SPI_Read() 只接收
 *     两种方式不同之处：方式一，在发的过程中也在接收，第二种方式，收与发单独进行
 * Created on: 2013-5-28
 * Author: lzy
 */

#include <stdint.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <getopt.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <linux/types.h>
//#include <linux/spi/spidev.h>
#include </home/liufeng/repo/kernel/include/uapi/linux/spi/spidev.h>

#define pr_err(...)   printf(__VA_ARGS__)
#define pr_debug(...) printf(__VA_ARGS__)

//#include "Debug.h"
#define SPI_DEBUG 1
//

static const char *device = "/dev/spidev0.0";
											//mode 3: CPOL=1, CPHA＝1
static uint8_t mode = 3; 					/* SPI通信使用全双工，设置CPOL＝0，CPHA＝0。 */
static uint8_t bits = 8; 					/* ８ｂiｔｓ读写，MSB first。*/
//static uint32_t speed = 12 * 1000 * 1000;	/* 设置12M传输速度 */
static uint32_t speed = 1 * 1000 * 1000;
											//receive: 16MHz/4 = 4M
											//send here is: 3.2M, 4MHz > 3.2MHz, ok
static uint8_t lsb = 0;						//LSB_FIRST=0, MSB
static uint16_t delay = 0;

static int g_SPI_Fd = 0;

static void pabort(const char *s)
{
    perror(s);
    abort();
}

/**
 * 功 能：同步数据传输
 * 入口参数 ：
 *             TxBuf -> 发送数据首地址
 *             len -> 交换数据的长度
 * 出口参数：
 *             RxBuf -> 接收数据缓冲区
 * 返回值：0 成功
 * 开发人员：Lzy 2013－5－22
 */
int SPI_Transfer(const uint8_t *TxBuf, uint8_t *RxBuf, int len)
{
    int ret;
    int fd = g_SPI_Fd;

    struct spi_ioc_transfer tr =    {
            .tx_buf = (unsigned long) TxBuf,
            .rx_buf = (unsigned long) RxBuf,
            .len =    len,
            .delay_usecs = delay,
    };

    ret = ioctl(fd, SPI_IOC_MESSAGE(1), &tr);
    if (ret < 1)
        pr_err("can't send spi message");
    else
    {
#if SPI_DEBUG
        int i;
        pr_debug("nsend spi message Succeed");
        pr_debug("nSPI Send [Len:%d]: ", len);
        for (i = 0; i < len; i++)
        {
            if (i % 8 == 0)
            printf("nt");
            printf("0x%02X ", TxBuf[i]);
        }
        printf("n");

        pr_debug("SPI Receive [len:%d]:", len);
        for (i = 0; i < len; i++)
        {
            if (i % 8 == 0)
            printf("nt");
            printf("0x%02X ", RxBuf[i]);
        }
        printf("n");
#endif
    }
    return ret;
}

/**
 * 功 能：发送数据
 * 入口参数 ：
 *             TxBuf -> 发送数据首地址
 ＊            len -> 发送与长度
 ＊返回值：0 成功
 * 开发人员：Lzy 2013－5－22
 */
int SPI_Write(uint8_t *TxBuf, int len, char *str, char *t)
{
    int ret;
    int fd = g_SPI_Fd;
    int i = 0;
    int j = 0;

//    ret = write(fd, TxBuf, len);
    char buf[] = "Hello world!";

    len = strlen(str);
    printf("str=%s\n", str);
    printf("len=%d\n", len);
    fflush(stdout);

    if (t == NULL) {
        while (1) {
            for (i = 0; i < len; i++) {
    			ret = write(fd, str+i, 1);
    			if (ret < 0)
    				pr_err("SPI Write error\n");
    			else {
    				//				printf("0x%02X ", str[i]);
    				printf("%c ", str[i]);
    				fflush(stdout);
    			}

    			sleep(1);
            }
        }
    } else {
        while (1) {
			ret = write(fd, str, len + 1);
			if (ret < 0)
				pr_err("SPI Write error\n");
			else {
				printf("%s ", str);
				fflush(stdout);
			}

			sleep(1);
        }
    }

    return ret;
}

/**
 * 功 能：接收数据
 * 出口参数：
 *         RxBuf -> 接收数据缓冲区
 *         rtn -> 接收到的长度
 * 返回值：>=0 成功
 * 开发人员：Lzy 2013－5－22
 */
int SPI_Read(uint8_t *RxBuf, int len)
{
    int ret;
    int fd = g_SPI_Fd;
    ret = read(fd, RxBuf, len);
    if (ret < 0)
        pr_err("SPI Read error\n");
    else
    {
#if SPI_DEBUG
        int i;
        pr_debug("SPI Read [len:%d]:", len);
        for (i = 0; i < len; i++)
        {
            if (i % 8 == 0)
            printf("nt");
            printf("0x%02X ", RxBuf[i]);
        }
        printf("n");
#endif
    }

    return ret;
}

/**
 * 功 能：打开设备 并初始化设备
 * 入口参数 ：
 * 出口参数：
 * 返回值：0 表示已打开 0XF1 表示SPI已打开 其它出错
 * 开发人员：Lzy 2013－5－22
 */
int SPI_Open(void)
{
    int fd;
    int ret = 0;

    if (g_SPI_Fd != 0) /* 设备已打开 */
        return 0xF1;

    fd = open(device, O_RDWR);
    if (fd < 0)
        pabort("can't open device");
    else
        pr_debug("SPI - Open Succeed. Start Init SPI...\n");

    g_SPI_Fd = fd;
    /*
     * spi mode
     */
    ret = ioctl(fd, SPI_IOC_WR_MODE, &mode);
    if (ret == -1)
        pabort("can't set spi mode");

    ret = ioctl(fd, SPI_IOC_RD_MODE, &mode);
    if (ret == -1)
        pabort("can't get spi mode");

    /*
     * bits per word
     */
    ret = ioctl(fd, SPI_IOC_WR_BITS_PER_WORD, &bits);
    if (ret == -1)
        pabort("can't set bits per word");

    ret = ioctl(fd, SPI_IOC_RD_BITS_PER_WORD, &bits);
    if (ret == -1)
        pabort("can't get bits per word");

    /*
     * max speed hz
     */
    ret = ioctl(fd, SPI_IOC_WR_MAX_SPEED_HZ, &speed);
    if (ret == -1)
        pabort("can't set max speed hz");

    ret = ioctl(fd, SPI_IOC_RD_MAX_SPEED_HZ, &speed);
    if (ret == -1)
        pabort("can't get max speed hz");

    ret = ioctl(fd, SPI_IOC_WR_LSB_FIRST, &lsb);
    if (ret == -1)
        pabort("can't set lsb");

    ret = ioctl(fd, SPI_IOC_RD_LSB_FIRST, &lsb);
    if (ret == -1)
        pabort("can't get lsb");

    pr_debug("spi lsb:  %d\n", lsb);
    pr_debug("spi mode: %d\n", mode);
    pr_debug("bits per word: %d\n", bits);
    pr_debug("max speed: %d KHz (%d MHz)\n", speed / 1000, speed / 1000 / 1000);

    return ret;
}

/**
 * 功 能：关闭SPI模块
 */
int SPI_Close(void)
{
    int fd = g_SPI_Fd;

    if (fd == 0) /* SPI是否已经打开*/
        return 0;
    close(fd);
    g_SPI_Fd = 0;

    return 0;
}

/**
 * 功 能：自发自收测试程序
 *         接收到的数据与发送的数据如果不一样 ，则失败
 * 说明：
 *         在硬件上需要把输入与输出引脚短跑
 * 开发人员：Lzy 2013－5－22
 */
int SPI_LookBackTest(void)
{
    int ret, i;
    const int BufSize = 16;
    uint8_t tx[BufSize], rx[BufSize];

//    bzero(rx, sizeof(rx));
    for (i = 0; i < BufSize; i++) {
    	tx[i] = i;
    	rx[i] = 0;
    }

    pr_debug("nSPI - LookBack Mode Test...\n");
    ret = SPI_Transfer(tx, rx, BufSize);
    if (ret > 1)
    {
        ret = memcmp(tx, rx, BufSize);
        if (ret != 0)
        {
            pr_err("LookBack Mode Test error\n");
//            pabort("error");
        }
        else
            pr_debug("SPI - LookBack Mode OK\n");
    }

    return ret;
}

int main(int argc, char **argv) {

	if (!strcmp(argv[1], "self")) {
		SPI_LookBackTest();
	} else if(!strcmp(argv[1], "rw")) {
//		uint8_t TxBuf[] = {0X1F, 0X20};
		unsigned char TxBuf[] = "AB";
		SPI_Open();
		if (argc == 3) {
			SPI_Write(TxBuf, 2, argv[2], NULL);
		} else {
			SPI_Write(TxBuf, 2, argv[2], argv[3]);
		}

		SPI_Close();
	}


	return 0;
}

