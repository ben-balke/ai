/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/connectrequest.cpp,v 1.2 2009/09/21 18:09:35 secwind Exp $
******************************************************************/
#include <dbclient.h>

ConnectRequest_::ConnectRequest_ (char *buf, char *username, char *password, 
	char *ipaddress) : DbRequest_ (buf, CON)

{
	DbRequest_::appendString (username);
	DbRequest_::appendString (password);
	DbRequest_::appendString (ipaddress);
	DbRequest_::appendInt (0);
	DbRequest_::appendInt (0);
	DbRequest_::appendString ((char *) "tam");

}
