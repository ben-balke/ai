/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/RnxtRequest.java,v 1.1 2010/10/19 00:44:26 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

import java.util.*;

public class RnxtRequest extends IsamRequest
{
	public RnxtRequest (byte buf [], RandomFile randomFile, int recordsInSet)
		throws IsamException
	{
		super (buf, IsamRequest.RSN);
		this.appendByte (randomFile.m_filechan);
		this.appendInt (recordsInSet);
	}
}
