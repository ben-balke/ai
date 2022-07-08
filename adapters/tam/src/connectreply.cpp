/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/connectreply.cpp,v 1.1 2009/09/20 07:15:31 secwind Exp $
******************************************************************/
#include <dbclient.h>
#include <connectreply.h>

ConnectReply_::ConnectReply_ (char *buf) : DbReply_ (buf)
{
}

