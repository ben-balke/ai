
/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/pick/PickReply.java,v 1.2 2010/04/22 18:41:42 secwind Exp $
==================================================================================================*/
package com.duckdigit.pick;

import java.io.*;
public abstract class PickReply
{
	public static final byte	OK = 0;
	public static final byte	FAIL = 1;
	private PickFailure		m_pickFailure = null;
	private int			m_command;
	private byte			m_buf [];
	private int			m_rslt; // OK or FAIL
	private int			m_offset;

	public PickReply (int command, byte buf [])
	{
		m_buf = buf;
		m_offset = 0;
		m_command = command;
	}

	public void reset ()
	{
		m_offset = 0;
	}
	public void parseReply (int len)
		throws PickException
	{
		int command = parseByte ();
		if (!PickRequest.isCommandValid (command))
		{
			m_rslt = FAIL;
			m_pickFailure = new PickFailure (PickFailure.BADPACKET,
					"Pick packet command is not recognized.");
		}
		else
		{
			if (m_command != command)
			{
				m_rslt = FAIL;
				m_pickFailure = new PickFailure (PickFailure.UNEXPECTED_COMMAND,
						"Pick packet command " + m_command +
						" expected.  Got a " + command + " instead.");
				throw new PickException (m_pickFailure.toString ());
			}
			m_rslt = parseByte ();
			switch (m_rslt)
			{
			case OK:
				parseReplyBody (len - m_offset);
				break;
			case FAIL:
				m_pickFailure = new PickFailure (this);
				throw new PickException (m_pickFailure.toString ());
			}
		}
	}
	public RecordId parseRecordId ()
	{
		return new RecordId (parseString ());
	}
	public String parseString ()
	{
		int len = parseNumber ();
		int offset = m_offset;
		m_offset += len;
			//
			// Don't include the null character.  It
			// is required by the win32 stuff.
			//
		return new String (m_buf, offset, len - 1);
	}
	public int parseInt ()
	{
		int		num = 0;
		num |= (m_buf [m_offset++] << 24);
		num |= (m_buf [m_offset++] << 16) & 0x00ff0000;
		num |= (m_buf [m_offset++] << 8) & 0x0000ff00;
		num |= (m_buf [m_offset++]) & 0xff;
		return num;
	}
	public int parseNumber ()
	{
			return parseInt ();
	}
	public int parseBuf (byte buf [], int offset)
	{
		int		size = parseNumber ();
		System.arraycopy (m_buf, m_offset, buf, offset, size);
		m_offset += size;
		return size;
	}
	public void writeBuf (OutputStream out)
		throws PickException
	{
		try
		{
			int		size = parseNumber ();
			out.write (m_buf, m_offset, size);
			m_offset += size;
		}
		catch (Exception ex)
		{
			throw new PickException ("PickReply: Writing buffer", ex);
		}
	}

	public int parsePacketSize ()
	{
		return parseInt ();
	}
	public String parseString (int len)
	{
		String		newString = new String (m_buf, m_offset, len);
		m_offset += len;
		return newString.trim ();
	}
	public int parseByte ()
	{
		return (int) m_buf [m_offset++];
	}
/*	public BasixVarList parseVarList ()
		throws IsamException
	{
		String	name;
		int		type;
		int		start;
		int		array;
		int		offset;
		int		masteroffset;
		BasixVarList	varlist;

		int		fields = parseNumber ();
		varlist = new BasixVarList ();
		for (int i = 0; i < fields; i++)
		{
			try
			{
				name = parseString (4);
				type = parseByte ();
				start = parseNumber ();
				array = parseNumber ();
				offset = parseNumber ();
				masteroffset = parseNumber ();

				varlist.addField (new BasixVar (
											  name, type, start, array,
											  offset, masteroffset));
			}
			catch (Exception ex)
			{
				ex.printStackTrace ();
				throw new IsamException ("parseVarList: error", ex);
			}
		}
		return varlist;
	}
*/

	protected abstract void parseReplyBody (int len)
		throws PickException;

	public int getCommand ()
	{
		return m_command;
	}
	PickFailure getFailure () { return m_pickFailure; }
}
