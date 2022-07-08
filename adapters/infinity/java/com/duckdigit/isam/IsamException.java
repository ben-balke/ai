/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/IsamException.java,v 1.1 2010/10/19 00:44:26 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

public class IsamException extends Exception
{
	public IsamException (String message)
	{
		super (message, null);
	}
	public IsamException (String message, Exception ex)
	{
		super (message, ex);
	}
}
