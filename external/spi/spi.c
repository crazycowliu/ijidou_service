/*
 * ˵����SPIͨѶʵ��
 *     ��ʽһ�� ͬʱ���������ʵ�ֺ����� SPI_Transfer()
 *     ��ʽ������������շֿ���ʵ��
 *     SPI_Write() ֻ����
 *     SPI_Read() ֻ����
 *     ���ַ�ʽ��֮ͬ������ʽһ���ڷ��Ĺ�����Ҳ�ڽ��գ��ڶ��ַ�ʽ�����뷢��������
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
#include <linux/spi/spidev.h>

#define pr_err(...)   printf(__VA_ARGS__)
#define pr_debug(...) printf(__VA_ARGS__)

//#include "Debug.h"
#define SPI_DEBUG 0

static const char *device = "/dev/spidev0.0";
static uint8_t mode = 0; 	/* SPIͨ��ʹ��ȫ˫��������CPOL��0��CPHA��0�� */
static uint8_t bits = 8; /* ����i�����д��MSB first��*/
static uint32_t speed = 12 * 1000 * 1000;/* ����12M�����ٶ� */
static uint16_t delay = 0;
static int g_SPI_Fd = 0;

static void pabort(const char *s)
{
    perror(s);
    abort();
}

/**
 * �� �ܣ�ͬ�����ݴ���
 * ��ڲ��� ��
 *             TxBuf -> ���������׵�ַ
 *             len -> �������ݵĳ���
 * ���ڲ�����
 *             RxBuf -> �������ݻ�����
 * ����ֵ��0 �ɹ�
 * ������Ա��Lzy 2013��5��22
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
 * �� �ܣ���������
 * ��ڲ��� ��
 *             TxBuf -> ���������׵�ַ
 ��            len -> �����볤��
 ������ֵ��0 �ɹ�
 * ������Ա��Lzy 2013��5��22
 */
int SPI_Write(uint8_t *TxBuf, int len)
{
    int ret;
    int fd = g_SPI_Fd;

    ret = write(fd, TxBuf, len);
    if (ret < 0)
        pr_err("SPI Write errorn");
    else
    {
#if SPI_DEBUG
        int i;
        pr_debug("nSPI Write [Len:%d]: ", len);
        for (i = 0; i < len; i++)
        {
            if (i % 8 == 0)
            printf("nt");
            printf("0x%02X ", TxBuf[i]);
        }
        printf("n");

#endif
    }

    return ret;
}

/**
 * �� �ܣ���������
 * ���ڲ�����
 *         RxBuf -> �������ݻ�����
 *         rtn -> ���յ��ĳ���
 * ����ֵ��>=0 �ɹ�
 * ������Ա��Lzy 2013��5��22
 */
int SPI_Read(uint8_t *RxBuf, int len)
{
    int ret;
    int fd = g_SPI_Fd;
    ret = read(fd, RxBuf, len);
    if (ret < 0)
        pr_err("SPI Read errorn");
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
 * �� �ܣ����豸 ����ʼ���豸
 * ��ڲ��� ��
 * ���ڲ�����
 * ����ֵ��0 ��ʾ�Ѵ� 0XF1 ��ʾSPI�Ѵ� ��������
 * ������Ա��Lzy 2013��5��22
 */
int SPI_Open(void)
{
    int fd;
    int ret = 0;

    if (g_SPI_Fd != 0) /* �豸�Ѵ� */
        return 0xF1;

    fd = open(device, O_RDWR);
    if (fd < 0)
        pabort("can't open device");
    else
        pr_debug("SPI - Open Succeed. Start Init SPI...n");

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

    pr_debug("spi mode: %dn", mode);
    pr_debug("bits per word: %dn", bits);
    pr_debug("max speed: %d KHz (%d MHz)n", speed / 1000, speed / 1000 / 1000);

    return ret;
}

/**
 * �� �ܣ��ر�SPIģ��
 */
int SPI_Close(void)
{
    int fd = g_SPI_Fd;

    if (fd == 0) /* SPI�Ƿ��Ѿ���*/
        return 0;
    close(fd);
    g_SPI_Fd = 0;

    return 0;
}

/**
 * �� �ܣ��Է����ղ��Գ���
 *         ���յ��������뷢�͵����������һ�� ����ʧ��
 * ˵����
 *         ��Ӳ������Ҫ��������������Ŷ���
 * ������Ա��Lzy 2013��5��22
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

    pr_debug("nSPI - LookBack Mode Test...n");
    ret = SPI_Transfer(tx, rx, BufSize);
    if (ret > 1)
    {
        ret = memcmp(tx, rx, BufSize);
        if (ret != 0)
        {
            pr_err("LookBack Mode Test errorn");
//            pabort("error");
        }
        else
            pr_debug("SPI - LookBack Mode OKn");
    }

    return ret;
}

int main(int argc, char **argv) {

	if (!strcmp(argv[1], "self")) {
		SPI_LookBackTest();
	} else if(!strcmp(argv[1], "rw")) {

	}


	return 0;
}

