/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/dbreply.h,v 1.1 2009/09/20 07:15:31 secwind Exp $
*****************************************************************/
#ifndef DBREPLY_H_
#define DBREPLY_H_

#include <dbclient.h>

class DbReply_
{
	byte_		*m_buf;
	byte_		*m_curpos;
public:
	DbReply_ (char *buf);
	char *getString ();
	char *getBuf (int *plen);
	int getNumber ();
	int getInt ();
	int getByte ();
	int bytesLeft (int size);
};

#endif
