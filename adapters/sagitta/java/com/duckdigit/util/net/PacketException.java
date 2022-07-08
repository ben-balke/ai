/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/util/net/PacketException.java,v 1.2 2010/04/22 18:41:50 secwind Exp $
==================================================================================================*/
package com.duckdigit.util.net;

public class PacketException extends Exception
{
	public PacketException (String message)
	{
		super (message, null);
	}
	public PacketException (String message, Exception ex)
	{
		super (message, ex);
	}
}

