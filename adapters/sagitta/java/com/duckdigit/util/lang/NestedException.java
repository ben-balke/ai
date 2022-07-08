/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/util/lang/NestedException.java,v 1.2 2010/04/22 18:41:48 secwind Exp $
==================================================================================================*/
package com.duckdigit.util.lang;

public class NestedException extends Exception
{
	public NestedException (String message, Exception ex)
	{
		super (message, ex);
	}
	public String getNestedStackTrace ()
	{
		return StringUtils.stackTraceToString (this);
	}
	public String getMessageStack ()
	{
		return this.getMessage ();
	}
}
