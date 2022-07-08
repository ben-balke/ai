/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/DisconnectRequest.java,v 1.1 2010/10/19 00:44:26 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

public class DisconnectRequest extends IsamRequest
{
	public DisconnectRequest (byte buf [])
		throws IsamException
	{
		super (buf, IsamRequest.DIS);
	}
}
