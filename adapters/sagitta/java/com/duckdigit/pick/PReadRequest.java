
/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/pick/PReadRequest.java,v 1.2 2010/04/22 18:41:42 secwind Exp $
==================================================================================================*/
package com.duckdigit.pick;

import java.util.*;

public class PReadRequest extends PickRequest
{
	public PReadRequest (byte buf [], PickFile pickFile, RecordId rcdno)
		throws PickException
	{
		super (buf, PickRequest.RED);
		this.appendNumber (pickFile.m_filechan);
		this.appendRecordId (rcdno);
	}
}
