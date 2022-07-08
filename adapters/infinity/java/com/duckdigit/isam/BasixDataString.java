/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/BasixDataString.java,v 1.1 2010/10/19 00:44:25 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

public class BasixDataString implements BasixData
{
	byte		m_buf [];
	public BasixDataString (byte rcdbuf [])
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
		String	str = Integer.toString (i);
		set (str, offset, size);
	}

	public void set (double d, int offset, int size)
		throws BasixDataException
	{
		String	str = Double.toString (d);
		set (str, offset, size);
	}
	public void set (String str, int offset, int size)
		throws BasixDataException
	{
		int		len;
		if (str == null)
		{
			str = "";
		}
		len = Math.min (str.length (), size);
		for (int i = 0; i < len; i++)
		{
			m_buf [offset + i] = (byte) str.charAt (i);
		}
		if (len < size)
		{
			m_buf [offset + len] = (byte) 0;
		}
	}
	public int getInt (int offset, int size)
		throws BasixDataException
	{
		return Integer.parseInt (new String (m_buf, offset, size));
	}
	public double getDouble (int offset, int size)
		throws BasixDataException
	{
		return Double.parseDouble (new String (m_buf, offset, size));
	}
	public String getString (int offset, int size)
		throws BasixDataException
	{
		int			s;
		for (s = 0; s < size; s++)
		{
			if (m_buf [offset + s] == 0)
			{
				break;
			}
		}
			/**
			 * Trim off any white space from the end of the
			 * string.
			 */
		for (int i = s; i > 0; i--)
		{
			if (m_buf [offset + (i - 1)] > (byte) ' ')
			{
				return new String (m_buf, offset, i);
			}
		}
		return "";
	}
}
