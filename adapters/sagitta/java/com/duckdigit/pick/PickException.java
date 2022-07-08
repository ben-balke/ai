
/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/pick/PickException.java,v 1.2 2010/04/22 18:41:42 secwind Exp $
==================================================================================================*/
package com.duckdigit.pick;

public class PickException extends Exception
{
	public PickException (String message)
	{
		super (message, null);
	}
	public PickException (String message, Exception ex)
	{
		super (message, ex);
	}
}
