/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/util/net/Packet.java,v 1.2 2010/04/22 18:41:50 secwind Exp $
==================================================================================================*/
package com.duckdigit.util.net;

import java.io.*;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.text.SimpleDateFormat;

import com.duckdigit.util.lang.DateValidator;
import com.duckdigit.util.lang.DateTimeValidator;
import com.duckdigit.util.lang.TimeValidator;

public abstract class Packet
{
	static final byte 	NOSZ [] = { (byte) 'N', (byte) 'O' };
	public static final byte	OK = 0;
	public static final byte	FAIL = 1;
	public static final byte	UNLICENSED = 2;
	private ProtocolFailure			m_protFailure = null;
	private int					m_rslt; // OK or FAIL

	public static final int		NUMLEN = 2;
	protected static int		PACKETHDR_SIZE = 4;

	/* Command validator for this application. */
	private static  PacketCommander     s_Cmdr;
	private static	SimpleDateFormat 	s_dateFormat= new SimpleDateFormat ("M-d-yyyy hh:mm:ss");

	protected byte	m_buf [] = null;
	protected int	m_offset = 0;
	protected int	m_command;
	protected int	m_len;

	public static void setApplicationCommander(
			PacketCommander        p_Cmdr )
	{
		s_Cmdr = p_Cmdr;
	}

	public Packet (byte buf [], int command) throws PacketException
	{
		if (null == s_Cmdr)
		{
			throw new PacketException(
						"Application protocol has not been established." );
		}

		m_buf = buf;
		m_command = command;
	}
	public void writeHeader ()
	{
		this.write (NOSZ);
		this.write ((byte) m_command);
		this.write ((byte) OK);
	}

	private void write (byte b)
	{
		m_buf [m_offset++] = b;
	}

	private void write (byte buf [], int offset, int nbytes)
	{
		int		end = offset + nbytes;
		for (int i = offset; i < end; i++)
		{
			m_buf [m_offset++] = buf [i];
		}
	}
	private void write (byte buf [])
	{
		int		end = buf.length;
		for (int i = 0; i < end; i++)
		{
			m_buf [m_offset++] = buf [i];
		}
	}

	public static boolean isCommandValid (int command)
	{
		if (command >= s_Cmdr.getMinCommand() &&
			command <= s_Cmdr.getMaxCommand())
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
	public int prepare () throws PacketException
	{
		int			num = m_offset - (PACKETHDR_SIZE);
		m_buf [0] = (byte) ((num >> 8) & 0xff);
		m_buf [1] = (byte) (num & 0xff);
		return m_offset;

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
	public void appendString (String str) throws PacketException
	{
		int		len = str.length ();

		if (len > 0)
		{
			byte 	bs [] = str.getBytes ();
			appendNumber (bs.length + 1);
			this.write (bs);
		}
		else
		{
			appendNumber (1);
		}
		this.write ((byte) 0);
	}
	public void appendNumber (int n) throws PacketException
	{
		try
		{
			short	num = (short) n;
			this.write ((byte) ((num >> 8) & 0xff));
			this.write ((byte) (num & 0xff));
		}
		catch (Exception ex)
		{
			throw new PacketException (ex.getMessage ());
		}
	}
	public void appendByte (int n) throws PacketException
	{
		this.write ((byte) n);
	}

	public void appendInt (int num)
	{
		this.write ((byte) (num >> 24));
		this.write ((byte) ((num >> 16) & 0xff));
		this.write ((byte) ((num >> 8) & 0xff));
		this.write ((byte) (num & 0xff));
	}

	public void appendBytes (int len, byte b) throws PacketException
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
			throw new PacketException (ex.getMessage ());
		}
	}

	public void appendDouble(
			double      p_dVal )
		throws PacketException
	{
		String      sVal;

		sVal = new Double( p_dVal ). toString();
		appendString( sVal );
	}

	public void appendTime(
			Time        p_Time )
		throws PacketException
	{
		String      sVal;

		sVal = p_Time. toString();
		appendString( sVal );
	}

	public void appendDate(
			Date        p_Time )
		throws PacketException
	{
		String      sVal;

		try
		{
			sVal = s_dateFormat.format (p_Time);
			appendString( sVal );
		}
		catch (Exception ex)
		{
			throw new PacketException ("Appending a Date", ex);
		}
	}

	public void appendTimestamp(
			Timestamp       p_Time )
		throws PacketException
	{
		String      sVal;

		sVal = p_Time. toString();
		appendString( sVal );
	}


	public void reset ()
	{
		m_offset = 0;
	}
	public void parsePacket ()
		throws PacketException
	{
		m_len = parsePacketSize ();
		int command = parseByte ();
		if (!isCommandValid (command))
		{
			m_rslt = FAIL;
			m_protFailure = new ProtocolFailure (ProtocolFailure.BADPACKET,
					"Document packet command is not recognized.");
		}
		else
		{
			if (m_command != command)
			{
				m_rslt = FAIL;
				m_protFailure = new ProtocolFailure (ProtocolFailure.UNEXPECTED_COMMAND,
						"Doc packet command " + m_command +
						" expected.  Got a " + command + " instead.");
				throw new PacketException (m_protFailure.toString ());
			}
			m_rslt = parseByte ();
			switch (m_rslt)
			{
			case OK:
				parsePacketBody ();
				break;
			case FAIL:
				m_protFailure = new ProtocolFailure (this);
				throw new PacketException (m_protFailure.toString ());
			}
		}
	}
	public boolean hasMore()
	{
		return m_offset < m_len + PACKETHDR_SIZE;
	}
	public String parseString ()
	{
		int len = parseNumber ();
		int offset = m_offset;
		m_offset += len;
			//
			// Skip the null terminator.
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
		short num = 0;
		num |= (m_buf [m_offset++] << 8) & 0x0000ff00;
		num |= (m_buf [m_offset++]) & 0xff;
		return ((int) num) & 0x0000ffff;
	}
	public void parseBuf (byte buf [], int offset)
	{
		int		size = parseNumber ();
		System.arraycopy (m_buf, m_offset, buf, offset, size);
		m_offset += size;
	}
	public void writeBuf (OutputStream out)
		throws PacketException
	{
		try
		{
			int		size = parseNumber ();
			out.write (m_buf, m_offset, size);
			m_offset += size;
		}
		catch (Exception ex)
		{
			throw new PacketException ("DocReply: Writing buffer", ex);
		}
	}

	public int parsePacketSize ()
	{
		return parseNumber ();
	}
	public String parseString (int len)
	{
		String		newString = new String (m_buf, m_offset, len);
		m_offset += len;
		return newString.trim ();
	}

	public double parseDouble()
	{
		try {
			return new Double( parseString() ). doubleValue();
		}
		catch (Exception e) {}

		return 0.;
	}

	public Time parseTime()
	{
		String  sDate = null;
		try {
			sDate = parseString();
			return TimeValidator. validate( sDate );
		}
		catch (Exception e) {
			System.out.println( "Could not parse time " + sDate );
		}

		return null;
	}

	public Date parseDate()
	{
		String  sDate = null;
		try {
			sDate = parseString();
			return new java.sql.Date (s_dateFormat.parse (sDate).getTime ());
		}
		catch (Exception e) {
			System.out.println( "Could not parse date " + sDate );
		}

		return null;
	}

	public Timestamp parseTimestamp()
	{
		String  sDate = null;
		try {
			sDate = parseString();
			return DateTimeValidator. validate( sDate );
		}
		catch (Exception e) {
			System.out.println( "Could not parse timestamp " + sDate );
		}

		return null;
	}

	public int parseByte ()
	{
		return (int) m_buf [m_offset++];
	}
	protected abstract void parsePacketBody ()
		throws PacketException;

	public int getCommand ()
	{
		return m_command;
	}
	ProtocolFailure getFailure () { return m_protFailure; }

}


