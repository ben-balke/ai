/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/disconnectrequest.cpp,v 1.1 2009/09/20 07:15:31 secwind Exp $
*****************************************************************/
#include <dbclient.h>
#include <disconnectrequest.h>

DisconnectRequest_::DisconnectRequest_ (char *buf)
	: DbRequest_ (buf, DIS)
{
}
