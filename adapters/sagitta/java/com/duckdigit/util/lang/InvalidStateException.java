/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/util/lang/InvalidStateException.java,v 1.2 2010/04/22 18:41:48 secwind Exp $
==================================================================================================*/
package com.duckdigit.util.lang;

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
