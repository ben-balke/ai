/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/rftprequest.h,v 1.1 2009/09/20 07:15:31 secwind Exp $
*****************************************************************/
#ifndef RFTPREQUEST_H_
#define RFTPREQUEST_H_

#include <dbclient.h>

class RftpRequest_ : public DbRequest_
{
	public:
		RftpRequest_ (char *buf, char *filename);
};

#endif 
