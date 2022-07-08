
/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/pick/PopenReply.java,v 1.2 2010/04/22 18:41:42 secwind Exp $
==================================================================================================*/
package com.duckdigit.pick;

public class PopenReply extends PickReply
{
	PickFile		m_pickFile;

	public PopenReply (byte buf [], PickFile pickFile)
		throws PickException
	{
		super (PickRequest.OPN, buf);
		m_pickFile = pickFile;
	}
	protected void parseReplyBody (int len)
		throws PickException
	{
		m_pickFile.setFileChan (parseByte ());
	}

}
