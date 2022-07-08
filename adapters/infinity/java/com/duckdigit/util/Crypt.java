/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/util/Crypt.java,v 1.1 2010/10/19 00:43:59 secwind Exp $
==================================================================================================*/
package com.duckdigit.util;

/**
 * Provides a simple cyrtographic algorithm for masking the data values.
 * It uses a bit shifting mechanism and is really not that good, but
 * defeats the typical network sniffer.
 */
public class Crypt
{
	static boolean		crypt = true;

	/**
	 * Encrypt the provided buffer inline.  Segments of a
	 * buffer can be encrypted separately because the offset
	 * is used as the seed of the algorithm.
	 * @param buf	buffer to encrypt.
	 * @param offset offset into the buffer to encrypt.
	 * @param len	number of bytes to encrypte.
	 */
	public static void encrypt (byte buf [], int offset, int len)
	{
		if (!crypt)
			return;
		int		fnl = offset + len;
		for (int i = offset; i < fnl; i++)
		{
			byte m = (byte) ((i % 7) + 1);
			buf [i] = (byte)
				((~(buf [i] << m) & (0xff << m)) |
				((~(buf [i] >> (8 - m))) & (0xff >> (8 - m))));
		}
	}


	/**
	 * Decrypt the provided buffer inline.  Segments of a
	 * buffer can be decrypted separately because the offset
	 * is used as the seed of the algorithm.
	 * @param buf	buffer to encrypt.
	 * @param offset offset into the buffer to encrypt.
	 * @param len	number of bytes to encrypte.
	 */
	public static void decrypt (byte buf [], int offset, int len)
	{
		if (!crypt)
			return;
		int		fnl = offset + len;
		for (int i = offset; i < fnl; i++)
		{
			byte m = (byte) ((i % 7) + 1);
			buf [i] = (byte)
				((~(buf [i] << (8 - m)) & (0xff << (8 - m))) |
				((~(buf [i] >> m)) & (0xff >> m)));
		}
	}
}
