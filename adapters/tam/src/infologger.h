/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/infologger.h,v 1.1 2009/09/20 07:15:31 secwind Exp $
*****************************************************************/
#ifndef INFOLOGGER_H
#define INFOLOGGER_H

#include <stdarg.h>

class InfoLogger_
{
		static int 		s_errorLevel;
	public:
		static void log (int level, char *errorText, ...);
		static void setErrorLevel (int errorLevel)
		{
			s_errorLevel = errorLevel;
		}
};

#endif
