/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/ProcReply.java,v 1.1 2010/10/19 00:44:26 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;
import java.util.*;

public class ProcReply extends IsamReply
{
	public HashMap		m_values;
	
	public ProcReply (byte buf [], HashMap values)
		throws IsamException
	{
		super (IsamRequest.PRC, buf);
		m_values = values;
	}
	protected void parseReplyBody (int len)
		throws IsamException
	{
		String		name;
		String		value;
		int retcount = parseNumber ();
		for (int i = 0; i < retcount; i++)
		{
			name = parseString ();
			value = parseString ();
			m_values.put (name, value);
		}
	}
	public HashMap getResults () 
	{
		return m_values;
	}
}
