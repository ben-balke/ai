/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/disconnectrequest.h,v 1.1 2009/09/20 07:15:31 secwind Exp $
*****************************************************************/
#ifndef DISCONNECTREQUEST_H_
#define DISCONNECTREQUEST_H_

#include <dbclient.h>
#include <dbrequest.h>

	// USERSZ must match the size setting in the client code.
	//

class DisconnectRequest_ : public DbRequest_
{
	public:

		DisconnectRequest_ (char *buf);
};

#endif 
