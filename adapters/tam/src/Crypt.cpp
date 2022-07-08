/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/Crypt.cpp,v 1.1 2009/09/20 07:15:31 secwind Exp $
******************************************************************/
#include <stdio.h>
#include <fcntl.h>
#include <unistd.h>
#include <Crypt.h>

int cryptit = 1;

void Crypt_::encrypt (char *buf, int offset, int len)
{
	if (!cryptit)
		return;
	unsigned char		*ubuf = (unsigned char *) buf;
	int		fnl = offset + len;
	for (int i = offset; i < fnl; i++)
	{
		unsigned char m = (unsigned char) ((i % 7) + 1);
		ubuf [i] = (unsigned char)
			((~(ubuf [i] << m) & (0xff << m)) | 
			((~(ubuf [i] >> (8 - m))) & (0xff >> (8 - m))));
	}
}


void Crypt_::decrypt (char *buf, int offset, int len)
{
	if (!cryptit)
		return;
	unsigned char		*ubuf = (unsigned char *) buf;
	int		fnl = offset + len;
	for (int i = offset; i < fnl; i++)
	{
		unsigned char m = (unsigned char) ((i % 7) + 1);
		ubuf [i] = (unsigned char)
			((~(ubuf [i] << (8 - m)) & (0xff << (8 - m))) | 
			((~(ubuf [i] >> m)) & (0xff >> m)));
	}
}
