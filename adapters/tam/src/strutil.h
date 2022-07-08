/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/strutil.h,v 1.1 2009/09/20 07:15:31 secwind Exp $
*****************************************************************/
#ifndef STRUTIL_H_
#define STRUTIL_H_

class StrUtil_
{	

public:
	static unsigned int isinteger(char *buff);
	static inline void strtoupper(char *string);
	static void strtolower(char *string);
	static char * Escape(char *string);
};

#endif
