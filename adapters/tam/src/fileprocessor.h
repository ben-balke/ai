/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/fileprocessor.h,v 1.1 2009/09/20 07:15:31 secwind Exp $
*****************************************************************/
#ifndef FILEPROCESSOR_H_
#define FILEPROCESSOR_H_

class FileProcessor_
{
public:
	virtual int getBytesNeeded () = 0;
	virtual char *getBuffer () = 0;
	virtual int process (int nbytes) = 0;
	virtual int processEof () = 0;
};

#endif
