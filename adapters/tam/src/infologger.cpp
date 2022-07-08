/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/infologger.cpp,v 1.1 2009/09/20 07:15:31 secwind Exp $
*****************************************************************/
#include <infologger.h>
#include <stdio.h>
#include <time.h>
#include <fcntl.h>

static char buf [512];

int		InfoLogger_::s_errorLevel = 1;

void InfoLogger_::log (int errorLevel, char *format, ...)
{
	if (errorLevel <= s_errorLevel)
	{
		struct tm	*newtime;
		time_t		long_time;
		time (&long_time);
		newtime = localtime (&long_time);
		va_list ap;
		char	*b = buf;
		b += sprintf (buf, "%.19s [%d]: ", asctime (newtime), getpid ());
		va_start (ap, format);     /* Initialize variable arguments. */
		b += vsprintf (b, format, ap);
		va_end( ap );              /* Reset variable arguments.      */
		*b++ = '\n';
		write (1, buf, b - buf);
	}
}

