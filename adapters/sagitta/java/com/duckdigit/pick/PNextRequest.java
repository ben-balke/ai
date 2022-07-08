
/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/pick/PNextRequest.java,v 1.2 2010/04/22 18:41:42 secwind Exp $
==================================================================================================*/
package com.duckdigit.pick;

import java.util.*;

public class PNextRequest extends PickRequest
{
	public PNextRequest (byte buf [], PickFile pickFile, int rcds)
		throws PickException
	{
		super (buf, PickRequest.NXT);
		this.appendByte (pickFile.m_filechan);
		this.appendInt (rcds);
	}
}
