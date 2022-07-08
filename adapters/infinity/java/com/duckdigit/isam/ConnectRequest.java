/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/ConnectRequest.java,v 1.1 2010/10/19 00:44:26 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

public class ConnectRequest extends IsamRequest
{
	public ConnectRequest (byte buf [], String username, String password, String ipaddress)
		throws IsamException
	{
			/**
			 * Add one to the username and password for
			 * null termination.
			 */
		super (buf, IsamRequest.CON);
		this.appendString (username);
		this.appendString (password);
		this.appendString (ipaddress);
	}
}
