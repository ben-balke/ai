/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/connectreply.h,v 1.1 2009/09/20 07:15:31 secwind Exp $
******************************************************************/
#ifndef CONNECTREPLY_H_
#define CONNECTREPLY_H_

#include <dbclient.h>

class ConnectReply_ : public DbReply_
{
public:
	ConnectReply_ (char *buf);
};


#endif
