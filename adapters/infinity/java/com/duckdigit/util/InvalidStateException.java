/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/util/InvalidStateException.java,v 1.1 2010/10/19 00:43:59 secwind Exp $
==================================================================================================*/
package com.duckdigit.util;

public class InvalidStateException extends Exception
{
	public InvalidStateException (String message)
	{
		super (message, null);
	}
	public InvalidStateException (String message, Exception ex)
	{
		super (message, ex);
	}
}
