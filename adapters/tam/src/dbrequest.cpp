/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/dbrequest.cpp,v 1.1 2009/09/20 07:15:31 secwind Exp $
*****************************************************************/
#include <dbclient.h>
#include <dbrequest.h>
#include <errorlogger.h>

DbRequest_::DbRequest_ (char *buf, eCommand command)
{
	m_buf = (byte_ *) buf;
	m_curpos = m_buf + PACKETHDR_SIZE;
	*(m_curpos++) = (byte_) command;
	m_command = command;
}

DbRequest_::~DbRequest_ ()
{
}

/**
 * Prepare an Db Failure to be returned to the client.
 * The existing contents of the packet are destroyed.
 * Do not call ErrorLogger_::logError in here!
 * param	err	the error number.
 * param	errText text fo the error must be less then
 *			1020 or the internal buffer will be overwritten.
 */
void DbRequest_::setFailure (byte_ err, char *errText)
{
	m_curpos = m_buf + PACKETHDR_SIZE;
	*(m_curpos++) = (byte_) m_command;
	*(m_curpos++) = (byte_) I_FAIL;
	*(m_curpos++) = err;
	appendString (errText);
}
void DbRequest_::appendByte (byte_ b)
{
	*(m_curpos++) = b;
}
void DbRequest_::setOk ()
{
	m_curpos = m_buf + PACKETHDR_SIZE;
	*(m_curpos++) = (byte_) m_command;
	*(m_curpos++) = (byte_) I_OK;
}
void DbRequest_::appendData (byte_ *data, int len)
{
	appendNumber (len);
	memcpy (m_curpos, data, len);
	m_curpos += len;
}
void DbRequest_::appendNumber (int num)
{
	short		s = (short) num;
	*(m_curpos++) = (byte_) ((s >> 8) & 0xff);
	*(m_curpos++) = (byte_) (s & 0xff);
}

int DbRequest_::appendNumber (char *buf, int num)
{
    short       s = (short) num;
    buf [0] = (byte_) ((s >> 8) & 0xff);
    buf [1] = (byte_) (s & 0xff);
    return 2;
}

/**
 * Puts the high order bytes first.
 */
void DbRequest_::appendInt (int num)
{
	*(m_curpos++) = (byte_) (num >> 24);
	*(m_curpos++) = (byte_) ((num >> 16) & 0xff);
	*(m_curpos++) = (byte_) ((num >> 8) & 0xff);
	*(m_curpos++) = (byte_) (num & 0xff);
}

/**
 * Adds a string to the packet request.  The text is preceeded by
 * a 3 byte length and terminated with a null char.  It is assumed 
 * the string is less then 999 bytes.
 */
void DbRequest_::appendString (char *str)
{
	int		len = strlen (str) + 1;
	appendData ((byte_ *) str, len);
}

void DbRequest_::prepare ()
{
	short len = length () - (PACKETHDR_SIZE + 1);
	*(m_buf) = (byte_) ((len >> 8) & 0xff);
	*(m_buf + 1) = (byte_) (len & 0xff);
}

char *DbRequest_::getCurPos ()
{
	return (char *) m_curpos;
}

void DbRequest_::skipBytes (int nbytes)
{
	m_curpos += nbytes;
}


