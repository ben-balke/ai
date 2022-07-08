
/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/pick/PSelectReply.java,v 1.2 2010/04/22 18:41:42 secwind Exp $
==================================================================================================*/
package com.duckdigit.pick;

public class PSelectReply extends PickReply
{

	public PSelectReply (byte buf [], PickFile pickFile)
		throws PickException
	{
		super (PickRequest.SEL, buf);
	}

	protected void parseReplyBody (int len)
		throws PickException
	{
	}
}
