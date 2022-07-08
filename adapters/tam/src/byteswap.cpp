/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/byteswap.cpp,v 1.1 2009/09/20 07:15:31 secwind Exp $
*****************************************************************/
#include <sys/types.h>
#include "byteswap.h"
/*
 * routine to change little byteswap long to host long
 */
long byteswap_::get_long(u_char *cp)
{
	long		ret;

	ret = *cp++;
	ret += ((*cp++) << 8);
	ret += ((*cp++) << 16);
	ret += ((*cp++) << 24);

	return ret;
}

void byteswap_::put_long(u_char *cp, long lval)
{
	cp[0] = lval & 0xff;
	cp[1] = (lval >> 8) & 0xff;
	cp[2] = (lval >> 16) & 0xff;
	cp[3] = (lval >> 24) & 0xff;
}

/*
 * routine to change little byteswap short to host short
 */
short byteswap_::get_short(u_char *cp)
{
	short		ret;

	ret = *cp++;
	ret += ((*cp++) << 8);

	return ret;
}

void byteswap_::put_short(u_char *cp, short sval)
{
	cp[0] = sval & 0xff;
	cp[1] = (sval >> 8) & 0xff;
}
