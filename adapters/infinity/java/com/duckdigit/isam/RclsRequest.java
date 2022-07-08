/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/RclsRequest.java,v 1.1 2010/10/19 00:44:26 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

import java.util.*;

public class RclsRequest extends IsamRequest
{
	public RclsRequest (byte buf [], RandomFile randomFile)
		throws IsamException
	{
		super (buf, IsamRequest.CLS);
		this.appendByte (randomFile.m_filechan);
	}
}
