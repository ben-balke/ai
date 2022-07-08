/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/ai/SourceException.java,v 1.1 2010/10/19 00:42:54 secwind Exp $
==================================================================================================*/
package com.duckdigit.ai;

public class SourceException extends Exception
{
	public SourceException (String message)
	{
		super (message, null);
	}
	public SourceException (String message, Exception ex)
	{
		super (message, ex);
	}
}
