
/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/pick/PickRequest.java,v 1.2 2010/04/22 18:41:42 secwind Exp $
==================================================================================================*/
package com.duckdigit.pick;

import java.io.*;
import java.util.*;

public abstract class PickRequest
{
	protected static int		PACKETHDR_SIZE = 4;
	public static final int		NUMLEN = 4;
	static final byte 	NOSZ [] = { (byte) 'N', (byte) 'O', (byte) 'S', (byte) 'Z' };

		/**
 		 * The following must match isamserver.h on UNIX platform.
		 */
	public static final int CON=0;		// Connect
	public static final int OPN=1;		// Open a file
	public static final int CLS=2;		// Close a file
	public static final int SEL=3;		// Read a file record based on record id
	public static final int NXT=4;		// Next Record
	public static final int DIS=5;		// Disconnect
	public static final int LIV=6;		// It it alive?
	public static final int SOP=7;		// Shell Open
	public static final int PRC=8;		// Stored procedure.
	public static final int FTP=9;		// File Transfer
	public static final int ROW=10;		// Row of data follows
	public static final int RED=11;		// Read a Row by recordid
	public static final int SKP=12;		// Skip to a Record after a SEL ALL
	public static final int CMD_FAIL=-1;	// Failure has occured.

	public static final int MAX=12;


	byte			m_buf [] = null;
	int				m_count = 0;

	public PickRequest (byte buf [], int command) throws PickException
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
	public int prepare () throws PickException
	{
		int			num = m_count - (PACKETHDR_SIZE + 1);
	    m_buf [0] = (byte) ((num >> 24) & 0xff);
	    m_buf [1] = (byte) ((num >> 16) & 0xff);
	    m_buf [2] = (byte) ((num >> 8) & 0xff);
	    m_buf [3] = (byte) (num & 0xff);
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
	public void appendRecordId (RecordId rcdno) throws PickException
	{
		appendString (rcdno.toString ());
	}
	/**
	 * Write a string into the packet.  First a number is written
	 * and then the string itself followed by a null byte.
	 */
	public void appendString (String str) throws PickException
	{
		int		len = str.length ();

		appendNumber (len + 1);
		if (len > 0)
		{
			this.write (str.getBytes ());
		}
		this.write ((byte) 0);
	}
	public void appendNumber (int num) throws PickException
	{
	    this.write ((byte) ((num >> 24) & 0xff));
	    this.write ((byte) ((num >> 16) & 0xff));
	    this.write ((byte) ((num >> 8) & 0xff));
	    this.write ((byte) (num & 0xff));
	}
	public void appendByte (int n) throws PickException
	{
		this.write ((byte) n);
	}

	void appendInt (int num)
	{
	    this.write ((byte) ((num >> 24) & 0xff));
	    this.write ((byte) ((num >> 16) & 0xff));
	    this.write ((byte) ((num >> 8) & 0xff));
	    this.write ((byte) (num & 0xff));
	}

	public void appendBytes (int len, byte b) throws PickException
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
			throw new PickException (ex.getMessage ());
		}
	}
}

