/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/util/net/PacketTransporter.java,v 1.2 2010/04/22 18:41:50 secwind Exp $
==================================================================================================*/
package com.duckdigit.util.net;

import java.net.Socket;
import java.io.PrintStream;
import java.io.OutputStream;
import java.io.InputStream;


public class PacketTransporter
{
		/**
		 * Size of the command byte and the length of the remaining packet.
		 */
	private int READ_TIMEOUT = 10000;

	byte				m_inbuf [] = null;
	byte				m_outbuf [] = null;

	OutputStream		m_out = null;
	InputStream			m_in = null;
	Socket				m_socket = null;

	public PacketTransporter (Socket socket, int inbufsize, int outbufsize,
		int readtimeout) throws Exception
	{
		m_socket = socket;
		m_out = socket.getOutputStream ();
		m_in = socket.getInputStream ();
		m_inbuf = new byte [inbufsize];
		m_outbuf = new byte [outbufsize];
		READ_TIMEOUT = readtimeout;
		m_socket.setSoLinger (true, 10);
		m_socket.setSoTimeout (READ_TIMEOUT);

	}
	public void resetTimeout ()
		throws Exception
	{
		m_socket.setSoTimeout (READ_TIMEOUT);
	}
	public void setTimeout (int timeout)
		throws Exception
	{
		m_socket.setSoTimeout (timeout);
	}
	public OutputStream getOutputStream ()
	{
		return m_out;
	}

	public byte [] getInBuf ()
	{
		return m_inbuf;
	}

	public byte [] getOutBuf ()
	{
		return m_outbuf;
	}

	public void readBytes (byte buf [], int offset, int len)
		throws Exception
	{
		int readsize = len;
		int nbytes;
			//
			// Read the actual data.
			//
		do
		{
			nbytes = m_in.read (buf, offset, readsize);
			if (nbytes < 0)
			{
				throw new PacketCloseException (
					"Did not receive expected number of bytes: " +
						nbytes + " instead of " + readsize + ".");
			}
			readsize -= nbytes;
			offset += nbytes;
		} while (readsize > 0);
	}

	public PacketHeader readPacket () throws Exception
	{
		int 	nbytes;
		readBytes (m_inbuf, 0, Packet.PACKETHDR_SIZE);
		PacketHeader		ph = new PacketHeader (m_inbuf);
		readBytes (m_inbuf, Packet.PACKETHDR_SIZE, ph.m_size);
		return ph;
	}

	public void readPacket (Packet p) throws Exception
	{
		PacketHeader ph = readPacket ();
		p.parsePacket ();
	}
	public void sendPacket (Packet dp) throws Exception
	{
		try
		{
			int count = dp.prepare ();
			m_out.write (m_outbuf, 0, count);
			m_out.flush ();
		}
		catch (Exception ex)
		{
			throw new PacketException ("Error in fileDocument", ex);
		}
	}

	public void terminate ()
	{
		try { m_out.close (); } catch (Exception ignore) {}
		try { m_in.close (); } catch (Exception ignore) {}
		try { m_socket.close (); } catch (Exception ignore) {}
	}
}
