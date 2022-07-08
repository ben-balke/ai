/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/IsamRequest.java,v 1.1 2010/10/19 00:44:26 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

import java.io.*;
import java.util.*;

public abstract class IsamRequest
{
	static final byte 	NOSZ [] = { (byte) 'N', (byte) 'O' };

		/**
 		 * The following must match isamserver.h on UNIX platform.
		 */
    public static final int CON=0;
    public static final int ROP=1;  // Random Open
    public static final int RS1=2;  // Rancom Search First
    public static final int RSN=3;  // Random Search Next
    public static final int ROW=4;  // Random Search Next
    public static final int RSI=5;  // Random Search Insert
    public static final int RIN=6;  // Random Insert
    public static final int RDL=7;  // Random Delete
    public static final int RRD=8;  // Random Read
    public static final int RWR=9;  // Random Write
    public static final int DIS=10;  // Disconnect
	public static final int	LIV=11;	// Is it alive?
	public static final int	CLS=12;	// Close the file.
	public static final	int	SEL=13;	// Select statement.
	public static final int SOPN=14; // Shell command open.
	public static final int RSP=15;	// Random Search Previous
	public static final int PRC=16;	// Procedure Execution.
	public static final int FTP=17;	// File Transfer.
	public static final int MAX=16;

	public static final int		NUMLEN = 2;
	protected static int		PACKETHDR_SIZE = 2;

	byte			m_buf [] = null;
	int				m_count = 0;

	public IsamRequest (byte buf [], int command) throws IsamException
	{
		m_buf = buf;
		this.write (NOSZ);
		this.write ((byte) command);
	}

	private void write (byte b)
	{
		m_buf [m_count++] = b;
	}
	public void write (byte buf [], int offset, int nbytes)
	{
		int		end = offset + nbytes;
		for (int i = offset; i < end; i++)
		{
			m_buf [m_count++] = buf [i];
		}
	}
	public void write (byte buf [])
	{
		int		end = buf.length;
		for (int i = 0; i < end; i++)
		{
			m_buf [m_count++] = buf [i];
		}
	}

	public static boolean isCommandValid (int command)
	{
		if (command >= CON && command <= MAX)
		{
			return true;
		}
		return false;
	}

	/**
	 * Prepares the packet for transmission to the server by
	 * assigning the byte count in the header.
	 * @return The number of bytes in the packet.
	 */
	public int prepare () throws IsamException
	{
		int			num = m_count - (PACKETHDR_SIZE + 1);
		m_buf [0] = (byte) ((num >> 8) & 0xff);
		m_buf [1] = (byte) (num & 0xff);
		return m_count;

	}

	/**
	 * Sets a range of bytes in the middle of the current buffer.
	 * @param pbuf
	 */
	private void setBytes (byte pbuf [], int bufoffset, int offset, int len)
	{
		for (int i = 0; i < len; i++)
		{
			m_buf [offset + i] = pbuf [bufoffset + i];
		}
	}
	/**
	 * Write a string into the packet.  First a number is written
	 * and then the string itself followed by a null byte.
	 */
	public void appendString (String str) throws IsamException
	{
		int		len = str.length ();

		appendNumber (len + 1);
		if (len > 0)
		{
			this.write (str.getBytes ());
		}
		this.write ((byte) 0);
	}
	public void appendNumber (int n) throws IsamException
	{
		try
		{
			short	num = (short) n;
			this.write ((byte) ((num >> 8) & 0xff));
			this.write ((byte) (num & 0xff));
		}
		catch (Exception ex)
		{
			throw new IsamException (ex.getMessage ());
		}
	}
	public void appendByte (int n) throws IsamException
	{
		this.write ((byte) n);
	}

	public void appendMasterValues (byte masterbuf [], BasixVarList varlist)
		throws IsamException
	{
		appendNumber (varlist.getRecordLength ());
		Iterator                varIter;
		BasixVar			    var;
		varIter = varlist.iterator();
		while (varIter.hasNext())
		{
			var = (BasixVar) varIter.next();
			this.write (masterbuf, var.getMasterOffset (), var.getSize ());
		}
	}
	void appendInt (int num)
	{
	    this.write ((byte) (num >> 24));
	    this.write ((byte) ((num >> 16) & 0xff));
	    this.write ((byte) ((num >> 8) & 0xff));
	    this.write ((byte) (num & 0xff));
	}

	public void appendBytes (int len, byte b) throws IsamException
	{
		try
		{
			for (int i = 0; i < len; i++)
			{
				this.write (b);
			}
		}
		catch (Exception ex)
		{
			throw new IsamException (ex.getMessage ());
		}
	}
}

