/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/RwrtReply.java,v 1.1 2010/10/19 00:44:26 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

public class RwrtReply extends IsamReply
{
	public RwrtReply (byte buf [])
		throws IsamException
	{
		super (IsamRequest.RWR, buf);
	}
	protected void parseReplyBody (int len)
		throws IsamException
	{
	}

}
