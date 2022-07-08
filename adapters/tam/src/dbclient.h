/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/dbclient.h,v 1.1 2009/09/20 07:15:31 secwind Exp $
******************************************************************/
#ifndef DBCLIENT_H_
#define DBCLIENT_H_

#include <stdio.h>
#include <sys/types.h>
#include <fcntl.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <time.h>

#define         MAXPACKET   (1024 * 3)
#define         MAXROWREPLY (1024 * 2)
#define         PACKETHDR_SIZE  2
#define         KEEPALIVE_INTERVAL  5




typedef enum eCommand
{
    CON=0,
    DIS=5, // Disconnect
    FTP=9, // File Transfer
    CMD_FAIL=-1 // Failure has occured.
} eCommand;

typedef char PacketLen_ [4];
typedef char UserName_ [14];
typedef char Password_ [14];
typedef char IpAddress_ [4];
typedef char Filename_ [8];
typedef char FileChan;
typedef unsigned char byte_;

#include "dataxlogger.h"
#include "Crypt.h"
#include "tcpclient.h"
#include "dbrequest.h"
#include "dbreply.h"
#include "connectrequest.h"
#include "disconnectrequest.h"
#include "connectreply.h"
#include "rftprequest.h"
#include "rftpreply.h"
#include "fileprocessor.h"

class DbClient_
{
	CTcpClient			*m_tcp;
	char				*m_buf;
	int					m_bufsize;
public:
	DbClient_ ();
	~DbClient_ ();
	int connect (char *server, int port);
	int disconnect ();
	char *getPacket ();
	int writeRequest (DbRequest_ *request);
	eCommand readPacket (int *len);
	int getFile (char *path, FileProcessor_ *pProcessor);

};

#endif

