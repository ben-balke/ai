/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/RrrdRequest.java,v 1.1 2010/10/19 00:44:26 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

import java.util.*;

public class RrrdRequest extends IsamRequest
{
	public RrrdRequest (byte buf [], RandomFile randomFile, int rcdNo)
		throws IsamException
	{
		super (buf, IsamRequest.RRD);
		this.appendByte (randomFile.m_filechan);
		this.appendInt (rcdNo);
	}
}
