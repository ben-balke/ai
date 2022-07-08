
/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/pick/PclsRequest.java,v 1.2 2010/04/22 18:41:42 secwind Exp $
==================================================================================================*/
package com.duckdigit.pick;

import java.util.*;

public class PclsRequest extends PickRequest
{
	public PclsRequest (byte buf [], PickFile pickFile)
		throws PickException
	{
		super (buf, PickRequest.CLS);
		this.appendByte (pickFile.m_filechan);
	}
}
