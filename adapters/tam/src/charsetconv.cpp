/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/charsetconv.cpp,v 1.2 2009/09/21 18:09:35 secwind Exp $
******************************************************************/
#include <stdio.h>
#include <iconv.h>
#include <string.h>
#include "charsetconv.h"


CharSetConv_::CharSetConv_ ()
{
	m_iconv_d = (iconv_t)-1;

	m_charset_from = (char *) "ISO-8859-2";
    //charset_from = NULL;
    //charset_to = "ISO-8859-1";
	m_charset_to = (char *) "UTF-8";
}

CharSetConv_::~CharSetConv_ ()
{
	closeconv ();
}

int CharSetConv_::initconv ()
{
	int		rslt = 0;
	if (m_charset_from)
	{
		m_iconv_d = iconv_open(m_charset_to, m_charset_from);
		if (m_iconv_d == (iconv_t) - 1)
		{
			printf("Cannot convert from charset \"%s\" to charset \"%s\".\n",
				   m_charset_from, m_charset_to);
			return 0;
		}
	}
	return 1;
}

int CharSetConv_::closeconv ()
{
	if (m_iconv_d != (iconv_t) -1)
	{
		iconv_close(m_iconv_d);
		m_iconv_d = (iconv_t)-1;
	}
	return 1;
}

char * CharSetConv_::convert (char *string)
{
	size_t		in_size,
				out_size,
				nconv;
	char	   *in_ptr,
			   *out_ptr;

	in_size = strlen(string) + 1;
	out_size = sizeof(m_convert_charset_buff);
	in_ptr = string;
	out_ptr = m_convert_charset_buff;

	iconv(m_iconv_d, NULL, &in_size, &out_ptr, &out_size);		/* necessary to reset
																 * state information */
	while (in_size > 0)
	{
		nconv = iconv(m_iconv_d, &in_ptr, &in_size, &out_ptr, &out_size);
		if (nconv == (size_t) -1)
		{
			printf("WARNING: cannot convert charset of string \"%s\".\n",
				   string);
			strcpy(m_convert_charset_buff, string);
			return m_convert_charset_buff;
		}
	}
	*out_ptr = 0;				/* terminate output string */
	return m_convert_charset_buff;
}
