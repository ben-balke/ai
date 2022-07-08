/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/RopenReply.java,v 1.1 2010/10/19 00:44:26 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

public class RopenReply extends IsamReply
{
	RandomFile		m_randomFile;

	public RopenReply (byte buf [], RandomFile randomFile)
		throws IsamException
	{
		super (IsamRequest.ROP, buf);
		m_randomFile = randomFile;
	}
	protected void parseReplyBody (int len)
		throws IsamException
	{
		m_randomFile.setFileChan (parseByte ());
		m_randomFile.setVarList (parseVarList ());
		int			indexcount = parseByte ();
		for (int i = 0; i < indexcount; i++)
		{
			m_randomFile.setIndexVarList (i, parseVarList ());
		}
	}

}
