
/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/pick/PSkipToRequest.java,v 1.2 2010/04/22 18:41:42 secwind Exp $
==================================================================================================*/
package com.duckdigit.pick;

import java.util.*;

public class PSkipToRequest extends PickRequest
{
	public PSkipToRequest (byte buf [], PickFile pickFile, RecordId recid, int interval)
		throws PickException
	{
		super (buf, PickRequest.SKP);
		this.appendNumber (pickFile.m_filechan);
		this.appendRecordId (recid);
		this.appendNumber (interval);
	}
}
