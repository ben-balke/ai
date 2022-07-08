/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/BasixDataShort.java,v 1.1 2010/10/19 00:44:25 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

public class BasixDataShort implements BasixData
{
	byte		m_buf [];
	public BasixDataShort (byte rcdbuf [])
	{
		m_buf = rcdbuf;
	}
	public byte [] getBytes (int offset, int size)
		throws BasixDataException
	{
		byte [] rslt = new byte [size];
		System.arraycopy (m_buf, offset, rslt, 0, size);
		return rslt;
	}
	public void getBytes (byte dest [], int destoffset, int offset, int size)
		throws BasixDataException
	{
		System.arraycopy (m_buf, offset, dest, destoffset, size);
	}

	public void set (int i, int offset, int size)
		throws BasixDataException
	{
		short	sh = (short)i;
		m_buf [offset] = (byte)(sh >> 8);
		m_buf [offset + 1] = (byte) (sh & 0xff);
	}
	public void set (double d, int offset, int size)
		throws BasixDataException
	{
		set ((int) d, offset, size);
	}
	public void set (String str, int offset, int size)
		throws BasixDataException
	{
		try
		{
			short		sh = Short.parseShort (str);
			set ((int) sh, offset, size);
		}
		catch (NumberFormatException ex)
		{
		}
	}

	public int getInt (int offset, int size)
		throws BasixDataException
	{
		short num = (short) ((m_buf [offset] << 8) & 0xff00);
		num |= (short)(m_buf [offset + 1]) & 0xff;

		return (int)num;
	}
	public double getDouble (int offset, int size)
		throws BasixDataException
	{
		return (double) getInt (offset, size);
	}
	public String getString (int offset, int size)
		throws BasixDataException
	{
		return Integer.toString (getInt (offset, size));
	}
}
