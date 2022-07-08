/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/strutil.cpp,v 1.1 2009/09/20 07:15:31 secwind Exp $
*****************************************************************/
#include <stdio.h>
#include <ctype.h>
#include "strutil.h"

unsigned int StrUtil_::isinteger(char *buff)
{
	char	   *i = buff;
	int			num = 0;
	while (*i && isspace (*i))
	{
		i++;
	}
			
	while (*i != '\0')
	{
		if (i == buff)
			if ((*i == '-') ||
				(*i == '+'))
			{
				i++;
				continue;
			}
		if (!isdigit((unsigned char) *i))
			return 0;
		if (!isspace (*i))
		{
			num = 1;
		}
		i++;
	}
	return num;
}

void StrUtil_::strtoupper(char *string)
{
	while (*string != '\0')
	{
		*string = toupper((unsigned char) *string);
		string++;
	}
}

void StrUtil_::strtolower(char *string)
{
	while (*string != '\0')
	{
		*string = tolower((unsigned char) *string);
		string++;
	}
}

static char        escape_buff[8192];
/* FIXME: should this check for overflow? */
char * StrUtil_::Escape(char *string)
{
	char	   *foo,
			   *bar;

	foo = escape_buff;

	bar = string;
	while (*bar != '\0')
	{
		if ((*bar == '\t') ||
			(*bar == '\n') ||
			(*bar == '\\') ||
			(*bar == '\b') ||
			(*bar == '\r'))
			*foo++ = '\\';
		*foo++ = *bar++;
	}
	*foo = '\0';

	return escape_buff;
}
