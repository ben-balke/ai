/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/util/net/ProtocolFailure.java,v 1.2 2010/04/22 18:41:50 secwind Exp $
==================================================================================================*/
package com.duckdigit.util.net;

/**
 * This class holds error information returned from the DocumentServer
 * in failure packets.
 */
public class ProtocolFailure extends Object
{
	private int			m_errorNo;
	private String		m_errorText;

		/**
		 * Internal errors are < 0.  0-255 are errors generated
		 * by the server.
		 */
	public static final int		BADPACKET = -1;
	public static final int		UNEXPECTED_COMMAND = -2;

	public ProtocolFailure (Packet docPacket)
	{
		m_errorNo = docPacket.parseByte ();
		m_errorText = docPacket.parseString ();
	}
	public ProtocolFailure (int errno, String errorText)
	{
		m_errorNo = errno;
		m_errorText = errorText;
	}
	public String toString ()
	{
		return "Protocol Failure: " + m_errorNo + ":" + m_errorText;
	}
}
