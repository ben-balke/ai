
/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/pick/ConnectRequest.java,v 1.2 2010/04/22 18:41:42 secwind Exp $
==================================================================================================*/
package com.duckdigit.pick;

public class ConnectRequest extends PickRequest
{
	public ConnectRequest (byte buf [], String username, String password, String ipaddress, int inbufsize, int outbufsize,
		String database)
		throws PickException
	{
			/**
			 * Add one to the username and password for
			 * null termination.
			 */
		super (buf, PickRequest.CON);
		this.appendString (username);
		this.appendString (password);
		this.appendString (ipaddress);
			/**
			 * Reverse the output and input buffer sizes so that
			 * they match the client buffers.
			 */
		this.appendInt (outbufsize);
		this.appendInt (inbufsize);
		this.appendString (database);
	}
}
