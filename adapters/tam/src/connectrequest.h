/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/connectrequest.h,v 1.1 2009/09/20 07:15:31 secwind Exp $
******************************************************************/
#ifndef CONNECTREQUEST_H_
#define CONNECTREQUEST_H_

#include <dbclient.h>
#include <dbrequest.h>

class ConnectRequest_ : public DbRequest_
{
public:
	ConnectRequest_ (char *buf, char *username, char *password,
		char *ipaddress);
};

#endif 
