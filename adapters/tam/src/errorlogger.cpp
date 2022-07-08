/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/errorlogger.cpp,v 1.1 2009/09/20 07:15:31 secwind Exp $
*****************************************************************/
#include <dbclient.h>

static char buf [512];

static DbReply_		*activeReply = NULL;

void ErrorLogger_::logError (eError err, char *errorText, ...)
{
	va_list ap;
	char	*b = buf;
	b += sprintf (buf, "ERROR[%d]: ", err);
	va_start (ap, errorText);     /* Initialize variable arguments. */
	vsprintf (b, errorText, ap);
	va_end( ap );              /* Reset variable arguments.      */

	fprintf (stderr, "%s\n", buf);
	if (activeReply)
	{
		activeReply->setFailure (err, b);
		activeReply = NULL;
	}
}

void ErrorLogger_::setActiveReply (DbReply_ *dbReply)
{
	activeReply = dbReply;
}

void ErrorLogger_::clearActiveReply ()
{
	activeReply = NULL;
}
