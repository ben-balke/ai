/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/RopenRequest.java,v 1.1 2010/10/19 00:44:26 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

import java.util.*;

public class RopenRequest extends IsamRequest
{
	public RopenRequest (byte buf [], RandomFile randomFile)
		throws IsamException
	{
		super (buf, IsamRequest.ROP);
		this.appendString (randomFile.m_name);
		this.appendString (randomFile.m_fields);
		if (randomFile.m_indicies != null)
		{
			int			icnt = randomFile.m_indicies.size ();
			this.appendNumber (icnt);
			ListIterator	itor = randomFile.m_indicies.listIterator (0);
			while (itor.hasNext ())
			{
				IndexFile indexFile = (IndexFile) itor.next ();
				this.appendString (indexFile.m_name);
				this.appendString (indexFile.m_fields);
			}
		}
		else
		{
			this.appendNumber (0);
		}
	}
}
