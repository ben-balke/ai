/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/util/net/PacketCloseException.java,v 1.2 2010/04/22 18:41:50 secwind Exp $
==================================================================================================*/
package com.duckdigit.util.net;

/**
 * Exception that is thrown when the input stream of the PacketTransporter is closed.
 */
public class PacketCloseException extends Exception
{
	public PacketCloseException (String message)
	{
		super (message, null);
	}
	public PacketCloseException (String message, Exception ex)
	{
		super (message, ex);
	}
}

