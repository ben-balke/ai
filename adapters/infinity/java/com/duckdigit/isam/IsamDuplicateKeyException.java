/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/IsamDuplicateKeyException.java,v 1.1 2010/10/19 00:44:26 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

public class IsamDuplicateKeyException extends IsamException
{
	public IsamDuplicateKeyException (String message)
	{
		super (message, null);
	}
	public IsamDuplicateKeyException (String message, Exception ex)
	{
		super (message, ex);
	}
}
