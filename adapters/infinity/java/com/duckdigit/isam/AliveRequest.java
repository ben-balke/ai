/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/AliveRequest.java,v 1.1 2010/10/19 00:44:25 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

import java.util.*;

/**
 * AliveRequest sends the LIV request to the server to verify the connection.
 */
public class AliveRequest extends IsamRequest
{
	public AliveRequest (byte buf [])
		throws IsamException
	{
		super (buf, IsamRequest.LIV);
	}
}
