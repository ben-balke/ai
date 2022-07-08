/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/R1stRequest.java,v 1.1 2010/10/19 00:44:26 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

import java.util.*;

public class R1stRequest extends IsamRequest
{
	public R1stRequest (byte buf [], RandomFile randomFile, String rangeClause, String whereClause, String orderby)
		throws IsamException
	{
		super (buf, IsamRequest.RS1);
		this.appendByte (randomFile.m_filechan);
		this.appendByte (randomFile.getActiveIndexNo ());
		this.appendMasterValues (randomFile.m_boundBuffer,
							randomFile.getActiveIndex ().getVarList ());

		this.appendString (rangeClause != null ? rangeClause : "");

		this.appendString (whereClause != null ? whereClause : "");
		
		if (orderby != null)
		{
			this.appendString (orderby);
		}
	}
}
