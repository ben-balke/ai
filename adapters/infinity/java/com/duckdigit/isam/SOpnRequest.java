/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/SOpnRequest.java,v 1.1 2010/10/19 00:44:26 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

public class SOpnRequest extends IsamRequest
{
	public SOpnRequest (byte buf [], String command)
		throws IsamException
	{
			/**
			 * Add one to the username and password for
			 * null termination.
			 */
		super (buf, IsamRequest.SOPN);
		this.appendString (command);
	}
}
