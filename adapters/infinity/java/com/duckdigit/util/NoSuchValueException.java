/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/util/NoSuchValueException.java,v 1.1 2010/10/19 00:43:59 secwind Exp $
==================================================================================================*/
package com.duckdigit.util;

public class NoSuchValueException extends Exception
{
	NoSuchValueException ()
	{
	}
	NoSuchValueException (String ErrorString)
	{
		super (ErrorString, null);
	}
	NoSuchValueException (String ErrorString, Exception ex)
	{
		super (ErrorString, ex);
	}
}
