/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/BasixDataBcd8.java,v 1.1 2010/10/19 00:44:25 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

public class BasixDataBcd8 implements BasixData
{
	byte		m_buf [];

	public BasixDataBcd8 (byte buf [])
	{
		m_buf = buf;
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
		try
		{
			Bcd8 bcd = new Bcd8 (i);
			System.arraycopy (bcd.getBytes (), 0, m_buf, offset, size);
		}
		catch (BcdException bex)
		{
			throw new BasixDataException (bex.getMessage () + " Trying to set BCD 8 to " + i);
		}
	}
	public void set (double d, int offset, int size)
		throws BasixDataException
	{
		try
		{
			Bcd8 bcd = new Bcd8 (d);
			System.arraycopy (bcd.getBytes (), 0, m_buf, offset, size);
		}
		catch (BcdException bex)
		{
			throw new BasixDataException (bex.getMessage () + " Trying to set BCD 8 to " + d);
		}
	}
	public void set (String str, int offset, int size)
		throws BasixDataException
	{
		try
		{
			Bcd8 bcd = new Bcd8 (str);
			System.arraycopy (bcd.getBytes (), 0, m_buf, offset, size);
		}
		catch (BcdException bex)
		{
			throw new BasixDataException (bex.getMessage () + " Trying to set BCD 8 to " + str);
		}
	}
	public int getInt (int offset, int size)
		throws BasixDataException
	{
		try
		{
			Bcd8	bcd = new Bcd8 (m_buf, offset);
			return bcd.getIntegerValue ();
		}
		catch (BcdException bex)
		{
			throw new BasixDataException (bex.getMessage ());
		}
	}
	public double getDouble (int offset, int size)
		throws BasixDataException
	{
		try
		{
			Bcd8	bcd = new Bcd8 (m_buf, offset);
			return bcd.getDoubleValue ();
		}
		catch (BcdException bex)
		{
			throw new BasixDataException (bex.getMessage ());
		}
	}
	public String getString (int offset, int size)
		throws BasixDataException
	{
		try
		{
			Bcd8	bcd = new Bcd8 (m_buf, offset);
			return bcd.toText (null);
		}
		catch (BcdException bex)
		{
			throw new BasixDataException (bex.getMessage ());
		}
	}
}
