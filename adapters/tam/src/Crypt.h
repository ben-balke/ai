/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/Crypt.h,v 1.1 2009/09/20 07:15:31 secwind Exp $
******************************************************************/
#ifndef CRYPT_H_
#define CRYPT_H_
#include <stdio.h>
#include <fcntl.h>
#include <unistd.h>

class Crypt_
{
	public: 
		static void encrypt (char *buf, int offset, int len);
		static void decrypt (char *buf, int offset, int len);
};

#endif
