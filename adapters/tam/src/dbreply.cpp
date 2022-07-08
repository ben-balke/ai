/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/dbreply.cpp,v 1.1 2009/09/20 07:15:31 secwind Exp $
*****************************************************************/
#include <dbclient.h>
#include <dbreply.h>

DbReply_::DbReply_ (char *buf)
{
	m_buf = (byte_*) buf;
	m_curpos = (byte_*) buf;
}

char *DbReply_::getString ()
{
	int 	len = getNumber ();
	char	*str = (char *) m_curpos;
	m_curpos += len;
	return (str);
}

char *DbReply_::getBuf (int *plen)
{
	*plen = getNumber ();
	char	*pbuf = (char *) m_curpos;
	m_curpos += *plen;
	return ((char *) pbuf);
}

int DbReply_::getNumber ()
{
	short num = 0;
	num |= (*(m_curpos++) << 8) & 0x0000ff00;
	num |= (*(m_curpos++)) & 0x000000ff;
	return (int) num;
}

int DbReply_::getInt ()
{
	int		num = 0;
	num |= (*(m_curpos++) << 24);
	num |= (*(m_curpos++) << 16) & 0x00ff0000;
	num |= (*(m_curpos++) << 8) & 0x0000ff00;
	num |= (*(m_curpos++)) & 0xff;
	return num;
}


int DbReply_::getByte ()
{
	return (int)(*(m_curpos++));
}


int DbReply_::bytesLeft (int size)
{
	return size - (m_curpos - m_buf);
}
