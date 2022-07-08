/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/R1stReply.java,v 1.1 2010/10/19 00:44:26 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

public class R1stReply extends IsamReply
{
	RandomFile			m_randomFile;
	public R1stReply (byte buf [], RandomFile randomFile)
		throws IsamException
	{
		super (IsamRequest.RS1, buf);
		m_randomFile = randomFile;
	}
	protected void parseReplyBody (int len)
		throws IsamException
	{
	}

}
