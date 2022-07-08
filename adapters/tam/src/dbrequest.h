/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/dbrequest.h,v 1.2 2009/09/21 18:09:35 secwind Exp $
*****************************************************************/
#ifndef DBREQUEST_H_
#define DBREQUEST_H_

#include <dbclient.h>

enum eResult
{
	I_OK=0,
	I_FAIL=1
};

/**
 * This class is used to construct reply packets that are
 * returned from the server to the client after a request is processed.
 * Failures are handled by the ErrorLogger_::logError service.
 */
class DbRequest_
{
	byte_			*m_buf;
	byte_			*m_curpos;
	eCommand		m_command;
public:
		/**
		 * First set up the buffer to use for replys.  Make sure
		 * the it is unique to each thread.
		 */
	DbRequest_ (char *buf, eCommand command);
	~DbRequest_ ();
	void setFailure (byte_ err, char *errText);
	void setOk ();
	char *getBytes () { return (char *) m_buf; }
	int length () { return m_curpos - m_buf; }
	void appendData (byte_ *data, int len);
	void appendString (char *str);
	void appendNumber (int num);
	static int appendNumber (char *buf, int num);
	void appendByte (byte_ num);
	void appendInt (int num);
	virtual void prepare ();
		/**
		 * Use the next two to save a position that is filled by 
		 * other means and skip the appropriate bytes.  This basically
		 * reserves bytes.
		 */
	char *getCurPos ();
	void skipBytes (int nbytes);
};

#endif
