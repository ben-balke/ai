/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/tcpclient.h,v 1.1 2009/09/20 07:15:31 secwind Exp $
*****************************************************************/

#ifndef TCPCLIENT_H_
#define TCPCLIENT_H_

#include <stdio.h>
#include <sys/types.h>
#include <errno.h>
#include <netdb.h>
#include <netinet/in.h>
#include <string.h>
#include <fcntl.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <strings.h>
#include <sys/time.h>
#include <sys/types.h>
#include "defines.h"

#define soread(a,b,c) recv(a,b,c,0)
#define sowrite(a,b,c) send(a,b,c,0)
//#define soread(a,b,c) read(a,b,c)
//#define sowrite(a,b,c) write(a,b,c)
#define soclose(a) close(a)
#define soperror(a) perror(a)

#ifndef	INADDR_NONE
#	define	INADDR_NONE		0xffffffff
#endif
#define		SOCKET		int
#define		SOCKET_ERROR		(-1)

#define     TCP_GOOD                    (0)
#define     TCP_FAIL                    (-1)
#define     TCP_SERVICE_NOT_KNOWN       (-2)
#define     TCP_BAD_PORT                (-3)
#define     TCP_CANT_CREATE_SOCKET      (-4)
#define     TCP_BIND_FAIL               (-5)
#define     TCP_LISTEN_FAIL             (-6)
#define     TCP_ACCEPT_FAIL             (-7)
#define     TCP_SOCKET_CLOSED           (-8)
#define     TCP_NEED_SERVICE_OR_PORT    (-9)
#define     TCP_CLOSED                  (-10)
#define     TCP_NO_DATA                 (-11)

#define     TCP_POLL_SEC            0   /* select period in seconds. */
#define     TCP_POLL_USEC           0   /* select period in milliseconds. */



class CTcpClient  
{
	SOCKET					SocketFD;
	char					m_szHostName [128];
public:
	CTcpClient();
	virtual ~CTcpClient();

	static BOOL TcpInit();

	BOOL WriteBytes (char* buf, int len);
	BOOL ReadBytes (char* buf, int nbytes, int sec, int micro);
	BOOL Disconnect();
	BOOL Connect(char* ServerName, int ServicePort);
	BOOL IsConnected () { return SocketFD != (SOCKET)SOCKET_ERROR; }
	BOOL CheckForRead (int sec, int micro);
	int Read (char *buf, int maxlen);
};

#endif
