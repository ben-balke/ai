
/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/pick/ConnectReply.java,v 1.2 2010/04/22 18:41:42 secwind Exp $
==================================================================================================*/
package com.duckdigit.pick;

public class ConnectReply extends PickReply
{
	public ConnectReply (byte buf [])
		throws PickException
	{
		super (PickRequest.CON, buf);
	}
	protected void parseReplyBody (int len)
		throws PickException
	{
	}

}
