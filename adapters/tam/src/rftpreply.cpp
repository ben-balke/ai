/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/rftpreply.cpp,v 1.1 2009/09/20 07:15:31 secwind Exp $
*****************************************************************/
#include <dbclient.h>
#include <rftpreply.h>

RftpReply_::RftpReply_ (char *buf) : DbReply_ (buf)
{
	m_size = getNumber ();
	m_cmd = getByte ();
	m_rslt = getByte ();
	if (m_rslt == I_OK)
	{
		m_filesize = getInt ();
	}
	else
	{
	   int failure = getByte ();
	   char *errstr = getString ();
	   m_filesize = 0;
	   fprintf (stderr, "RftpReply_::Constructor packet Failed to Read: %d %s.\n", m_rslt, errstr);
	}
}

