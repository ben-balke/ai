/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/AliveReply.java,v 1.1 2010/10/19 00:44:25 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

public class AliveReply extends IsamReply
{
	public AliveReply (byte buf [])
		throws IsamException
	{
		super (IsamRequest.LIV, buf);
	}
	protected void parseReplyBody (int len)
		throws IsamException
	{
	}
}
