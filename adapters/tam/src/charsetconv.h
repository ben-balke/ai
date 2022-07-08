/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/charsetconv.h,v 1.1 2009/09/20 07:15:31 secwind Exp $
******************************************************************/
#ifndef CHARSETCONV_H_
#define CHARSETCONV_H_

#include <iconv.h>

class CharSetConv_
{
	char	   *m_charset_from;
	char	   *m_charset_to;
	iconv_t		m_iconv_d;
	char		m_convert_charset_buff[8192];
public:
	CharSetConv_ ();
	~CharSetConv_ ();
	char *convert(char *string);
	int initconv();
	int closeconv();
};

#endif
