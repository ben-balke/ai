/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/dbclient.cpp,v 1.2 2009/09/21 18:09:35 secwind Exp $
******************************************************************/
#include <sys/types.h>
#include <fcntl.h>
#include <stdio.h>
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <time.h>
#include "tcpclient.h"
#include "dbclient.h"
#include "dbrequest.h"
#include "dbreply.h"
#include "connectrequest.h"
#include "disconnectrequest.h"
#include "connectreply.h"
#include "fileprocessor.h"



#define			DATESLEN		26
#define			READ_TIMEOUT	5
				/* Number of seconds to timeout before a read is abandoned.
				 */


DbClient_::DbClient_ () 
{
	m_buf = new char [MAXPACKET];
	m_bufsize = MAXPACKET;
	m_tcp = new CTcpClient;
}

DbClient_::~DbClient_ ()
{
	m_tcp->Disconnect ();
	delete m_buf;
	delete m_tcp;
}

int DbClient_::disconnect ()
{
	DisconnectRequest_ disconnectRequest (m_buf);
	writeRequest (&disconnectRequest);
}

int DbClient_::connect (char *server, int port)
{
	int 			nbytes;
	int				rslt = FALSE;

	if (m_tcp->Connect (server, port))
	{
		ConnectRequest_ ConnectRequest  (m_buf, (char *) "bbalke", (char *) "bbalke", (char *) "192.168.22.1");
		if (writeRequest (&ConnectRequest))
		{
			if (DbClient_::readPacket (&nbytes) != CON)
			{
				fprintf (stderr, "DbClient_::negotiateConnection: expecting a CON packet.\n");
			}
			else
			{
				ConnectReply_ reply (m_buf);
				rslt = TRUE;
			}
		}
	}
	return rslt;
}
/*
 * Returns the start of the packet just after the header.
 */
char *DbClient_::getPacket ()
{
	return m_buf + PACKETHDR_SIZE + 1;
}

int DbClient_::writeRequest (DbRequest_ *request)
{
	int		len;
	request->prepare ();
	len = request->length ();
	Crypt_::encrypt (m_buf, 0, len);
	if (m_tcp->WriteBytes (m_buf, len) == FALSE)
	{
		fprintf (stderr, "DbClient_::writeRequest: Write socket failed: len: %d, errno: %d\n",
			len, errno);
		return FALSE;
	}
	return TRUE;
}
/*
 * Reads the data into the TAM buffer m_buf.  Returns the eCommand and the
 * length of the packet.
 */
eCommand DbClient_::readPacket (int *len)
{
	eCommand 	cmd;

		/*
		 * First read the packet header which contains the packet size,
		 * and the command.  We capture the command, and NULL terminate
		 * the size so we can convert it from ascii to int.
		 */
	if (!m_tcp->ReadBytes (m_buf, PACKETHDR_SIZE + 1, 12, 0))
	{
		fprintf (stderr, "DbClient_::readPacket: Read of header failed");
		return CMD_FAIL;
	}
	Crypt_::decrypt (m_buf, 0, PACKETHDR_SIZE + 1);
	cmd = (eCommand) (m_buf [PACKETHDR_SIZE]);
		/*
		 * Null terminate the string by discarding the command.
		 */
	*len = (m_buf [0] << 8) & 0x0000ff00;
	*len |= (m_buf [1] & 0x000000ff);

		/*
		 * Do some size testing so we don't overwrite the buffer etc.
		 */
	if (*len > MAXPACKET)
	{
		fprintf (stderr, "DbClient_::readPacker: Packet length provided is too long");
		return CMD_FAIL;
	}
	if (*len == 0)
	{
		return cmd;
	}
	if (!m_tcp->ReadBytes ((char *) m_buf + PACKETHDR_SIZE + 1, *len, 12, 0))
	{
		fprintf (stderr, "DbClient_::readPacket: Read of body failed packet len:%d", *len);
		return CMD_FAIL;
	}
		/*
		 * Decrypt the remainder of the packer from relative to the header.
		 * This is required because the incoming packet is completely encrypted
		 * as a whole.  The Crypt_ algorithm will fail otherwise.
		 */
	Crypt_::decrypt (m_buf, PACKETHDR_SIZE + 1, *len);
	return cmd;
}

int DbClient_::getFile (char *path, FileProcessor_ *pProcessor)
{
	int			nbytes;
	int			rslt = FALSE;
	RftpRequest_ RftpRequest  (m_buf, path);
	if (writeRequest (&RftpRequest))
	{
		if (DbClient_::readPacket (&nbytes) != FTP)
		{
			fprintf (stderr, "DbClient_::getFile: expecting a FTP packet.\n");
		}
		else
		{
			RftpReply_ reply (m_buf);
			int nleft = reply.m_filesize;
			while (nleft > 0)
			{
				int nwanted = pProcessor->getBytesNeeded ();
				if (nwanted > nleft)
				{
					nwanted = nleft;
				}
				if (!m_tcp->ReadBytes (pProcessor->getBuffer (), nwanted, 12, 0))
				{

					fprintf (stderr, "DbClient_::getFile: did not get all the bytes:%d", 
						nwanted);
					break;
				}
	       		if (!pProcessor->process (nwanted))
				{
					break;
				}
				nleft -= nwanted;
			}
			if (nleft <= 0)
			{
	       		rslt = pProcessor->processEof ();
			}
		}
	}
	return rslt;
}
