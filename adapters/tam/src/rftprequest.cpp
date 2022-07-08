/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/rftprequest.cpp,v 1.1 2009/09/20 07:15:31 secwind Exp $
*****************************************************************/
#include <dbclient.h>

/**
 * The constructor extracts out the elements of the request from 
 * incoming byte array.  The string pointers are no longer valid 
 * after the buf is changed so don't rely after process is called.
 */
RftpRequest_::RftpRequest_ (char *buf, char *filename)
	: DbRequest_ (buf, FTP)
{
	appendString (filename);
}
