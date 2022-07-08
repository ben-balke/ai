/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/errorlogger.h,v 1.2 2009/09/21 18:09:35 secwind Exp $
*****************************************************************/
#ifndef ERRORLOGGER_H
#define ERRORLOGGER_H

#include <stdarg.h>
#include <dbreply.h>

enum	eError
{
	ER_BAD_CON_PACKET		= 1,
	ER_UNEXPECTED_CLOSE		= 2,
	ER_BAD_PACKET			= 3,
	ER_LOGIN_REQUIRED		= 10,
	ER_LOGIN_FAILED			= 11,
	ER_OPEN_LIMIT			= 12,
	ER_SYNTAX_ERROR			= 13,
	ER_NO_VARIABLE			= 14,
	ER_BAD_FILECHANNEL		= 15,
	ER_ISAM					= 16,
	ER_VARLIST_WRONG_SIZE 	= 17,
	ER_KEY_WRONG_SIZE 		= 18,
	ER_INVALID_INDEX 		= 19,
	ER_TIMEOUT				= 20,
	ER_SHELL_ERROR			= 21,
	ER_LICENSE_EXPIRE		= 22,
	ER_DUP_INDEX			= 23,
	ER_PROCNOTFOUND			= 24,
	ER_PROCERROR			= 25,
	ER_SORTER				= 26,
};

class ErrorLogger_
{
	public:
		static void logError (eError err, char *errorText, ...);
};

#endif
