/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/util/net/PacketHeader.java,v 1.2 2010/04/22 18:41:50 secwind Exp $
==================================================================================================*/
package com.duckdigit.util.net;

public class PacketHeader
{
	public int			m_command;
	public int 			m_size;
	public int			m_rslt;

	PacketHeader (byte buf [])
	{
		m_size = (((int) buf [0]) << 8) & 0x0000ff00;
		m_size |= ((int) buf [1]) & 0x000000ff;
		m_command = buf [2];
		m_rslt = buf [3];
	}
}
