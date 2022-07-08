/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/RdelReply.java,v 1.1 2010/10/19 00:44:26 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

public class RdelReply extends IsamReply
{
	public RdelReply (byte buf [])
		throws IsamException
	{
		super (IsamRequest.RDL, buf);
	}
	protected void parseReplyBody (int len)
		throws IsamException
	{
	}

}
