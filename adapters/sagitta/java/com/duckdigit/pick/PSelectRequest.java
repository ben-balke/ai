
/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/pick/PSelectRequest.java,v 1.2 2010/04/22 18:41:42 secwind Exp $
==================================================================================================*/
package com.duckdigit.pick;

import java.util.*;

public class PSelectRequest extends PickRequest
{
	final public static int SEL_ALL = 	0;
	final public static int SEL_ID = 	1;
	final public static int SEL_BASIC = 2;
	final public static int SEL_MV = 	3;
	
	public PSelectRequest (byte buf [], PickFile pickFile, RecordId recid)
		throws PickException
	{
		super (buf, PickRequest.SEL);
		this.appendNumber (pickFile.m_filechan);
		this.appendByte (SEL_ID);
		this.appendRecordId (recid);
	}
	public PSelectRequest (byte buf [], PickFile pickFile, String pickbasic)
		throws PickException
	{
		super (buf, PickRequest.SEL);
		this.appendNumber (pickFile.m_filechan);
		this.appendByte (SEL_BASIC);
		this.appendString (pickbasic);
	}
	public PSelectRequest (byte buf [], PickFile pickFile, String mvfile, 
			RecordId recid)
		throws PickException
	{
		super (buf, PickRequest.SEL);
		this.appendNumber (pickFile.m_filechan);
		this.appendByte (SEL_MV);
		this.appendString (mvfile);
		this.appendRecordId (recid);
	}
	public PSelectRequest (byte buf [], PickFile pickFile)
		throws PickException
	{
		super (buf, PickRequest.SEL);
		this.appendNumber (pickFile.m_filechan);
		this.appendByte (SEL_ALL);
	}
}
