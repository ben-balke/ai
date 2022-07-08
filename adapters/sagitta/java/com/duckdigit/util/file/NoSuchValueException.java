/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/util/file/NoSuchValueException.java,v 1.2 2010/04/22 18:41:45 secwind Exp $
==================================================================================================*/
package com.duckdigit.util.file;

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
