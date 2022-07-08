/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/ai/adapters/sagitta/SourceException.java,v 1.2 2010/04/22 18:41:39 secwind Exp $
==================================================================================================*/
package com.duckdigit.ai.adapters.sagitta;

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
