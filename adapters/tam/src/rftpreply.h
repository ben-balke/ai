/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/rftpreply.h,v 1.1 2009/09/20 07:15:31 secwind Exp $
*****************************************************************/
#ifndef RFTPREPLY_H_
#define RFTPREPLY_H_

#include <dbclient.h>

class RftpReply_ : public DbReply_
{
public:
	int			m_size;
	byte_		m_cmd;
	byte_		m_rslt;
	int			m_filesize;
	RftpReply_ (char *buf);
};


#endif
