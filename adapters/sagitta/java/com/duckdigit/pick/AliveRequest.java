
/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/pick/AliveRequest.java,v 1.2 2010/04/22 18:41:42 secwind Exp $
==================================================================================================*/
package com.duckdigit.pick;

import java.util.*;

/**
 * AliveRequest sends the LIV request to the server to verify the connection.
 */
public class AliveRequest extends PickRequest
{
	public AliveRequest (byte buf [])
		throws PickException
	{
		super (buf, PickRequest.LIV);
	}
}
