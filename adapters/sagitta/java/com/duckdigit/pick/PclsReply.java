
/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/pick/PclsReply.java,v 1.2 2010/04/22 18:41:42 secwind Exp $
==================================================================================================*/
package com.duckdigit.pick;

public class PclsReply extends PickReply
{
	public PclsReply (byte buf [])
		throws PickException
	{
		super (PickRequest.CLS, buf);
	}
	protected void parseReplyBody (int len)
		throws PickException
	{
	}

}
