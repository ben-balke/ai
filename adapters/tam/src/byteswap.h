/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/byteswap.h,v 1.1 2009/09/20 07:15:31 secwind Exp $
******************************************************************/
#ifndef BYTESWAP_H_
#define BYTESWAP_H_

#include <sys/types.h>
/*
 * routine to change little endian long to host long
 */
class byteswap_
{
public:
	static long get_long(u_char *cp);
	static void put_long(u_char *cp, long lval);
	static short get_short(u_char *cp);
	static void put_short(u_char *cp, short sval);
};

#endif
