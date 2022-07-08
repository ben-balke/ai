/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/tcpclient.cpp,v 1.1 2009/09/20 07:15:31 secwind Exp $
*****************************************************************/
#include <memory.h>
#include <string.h>
#include <time.h>
#include "tcpclient.h"



//////////////////////////////////////////////////////////////////////
// Construction/Destruction
//////////////////////////////////////////////////////////////////////

CTcpClient::CTcpClient()
{
	SocketFD = (SOCKET)SOCKET_ERROR;
}

CTcpClient::~CTcpClient()
{

}

/*************************************************************
***			Connect ()
*************************************************************/
BOOL CTcpClient::Connect (char* ServerName, int ServicePort)
/* Purpose:
 * 		To connect to a given service (by number) at the
 *		specified host dotted address or host name.
 * Returns:
 *		A open socket handle or SOCKET_ERROR on a failure.
 */
{
	BOOL					bRslt = FALSE;
	struct hostent			*hp;
	long					inaddr;
    struct sockaddr_in  serv_addr;

	int						LastError;
	
	if (SocketFD != SOCKET_ERROR)
	{
		return FALSE;
	}
	strncpy (m_szHostName, ServerName, sizeof (m_szHostName));
	bzero ((char *)&serv_addr, sizeof (serv_addr));
	
	serv_addr.sin_family = PF_INET;

		/*
		 * See if the host name is not a dotted address.
		 */	
	if ((inaddr = inet_addr (ServerName)) == INADDR_NONE)
	{
		if ((hp = gethostbyname (ServerName)) == NULL)
		{
			perror ("No Host");
			return FALSE;
		}
		bcopy (hp->h_addr, (char *) &serv_addr.sin_addr, hp->h_length);
	}    
	else
	{
		bcopy ((char *) &inaddr, (char *) &serv_addr.sin_addr, sizeof (inaddr));
	}
	                                
	SocketFD = socket (PF_INET, SOCK_STREAM, 0);
	if (SocketFD != SOCKET_ERROR)
	{

			/*
			 * Connect to the port using the the EXEC port number.
			 */
		serv_addr.sin_port = htons ((short) ServicePort);
		
		if (connect (SocketFD, (sockaddr *) &serv_addr, sizeof (serv_addr)) < 0)
		{
			LastError = errno;
			perror ("connect");
			Disconnect();
		}
		else
		{
			bRslt = TRUE;
		}
	}
	else
	{
		perror ("Socket Call Failed");
	}
	return bRslt;
}

BOOL CTcpClient::Disconnect()
{
	BOOL		bRslt = TRUE;
	if (IsConnected())
	{
		close (SocketFD);
		SocketFD = (SOCKET)SOCKET_ERROR;
	}
	return bRslt;

}

BOOL CTcpClient::TcpInit()
{
	int					err = 0; 
#ifdef WIN32
	WORD				wVersion;
	WSADATA				wsaData;
	
	wVersion = MAKEWORD (1, 1);
	
	err = WSAStartup (wVersion, &wsaData);
	if (err != 0)
	{
		err = WSAGetLastError ();
	}
#endif
	return err == 0;
}


int CTcpClient::Read (char *buf, int maxlen)
{
	return recv (SocketFD, buf, maxlen, 0);
}

BOOL CTcpClient::ReadBytes(char* buf, int nbytes, int sectimeout, int microtimeout)
{
	int				n;
	fd_set			rdset;
	struct timeval	timeout;

	FD_ZERO (&rdset);
	FD_SET (SocketFD, &rdset);
	timeout.tv_sec = sectimeout;
	timeout.tv_usec = microtimeout;

	while (nbytes)
	{
		if (select (SocketFD + 1, &rdset, (fd_set *) 0, (fd_set *) 0, &timeout) > 0)
		{
			if (FD_ISSET (SocketFD, &rdset))
			{
				if ((n = recv (SocketFD, buf, nbytes, 0)) == nbytes)
				{
					return TRUE;
				}
				else if (n == 0 || SOCKET_ERROR == n)
				{
					break;
				}

				nbytes -= n;
				buf += n;
			}
		}
		else
		{
			perror ("select");
			break;
		}
	}
	return FALSE;
}

BOOL CTcpClient::WriteBytes(char* buf, int len)
{
	int		nbytes;
	if ((nbytes = send (SocketFD, buf, len, 0)) != len)
	{
		return FALSE;
	}
	return TRUE;

}

/*=============================================================================
TCPCHECKFORREAD
=============================================================================*/
int CTcpClient::CheckForRead (int sec, int microsec)
{
    fd_set              readfds;
    int                 rslt = TCP_FAIL;
    int                 SelectRslt;
    struct timeval      seltime;

    seltime.tv_sec = sec;
    seltime.tv_usec = microsec;

    FD_ZERO (&readfds);
    FD_SET (SocketFD, &readfds);
    while (((SelectRslt = select (SocketFD + 1, &readfds, (fd_set *) 0,
        (fd_set *)0, &seltime)) == -1) && (errno == EINTR))
                ;
    if (SelectRslt > 0 && FD_ISSET (SocketFD, &readfds))
    {
        rslt = TCP_GOOD;
    }
    else if (SelectRslt == 0)
    {
        rslt = TCP_NO_DATA;
    }
    return rslt;
}

