
/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/pick/PopenRequest.java,v 1.2 2010/04/22 18:41:42 secwind Exp $
==================================================================================================*/
package com.duckdigit.pick;

import java.util.*;

public class PopenRequest extends PickRequest
{
	public PopenRequest (byte buf [], PickFile pickFile)
		throws PickException
	{
		super (buf, PickRequest.OPN);
		this.appendString (pickFile.m_name);

		// Pass in the number of defined fields and then
		// the field numbers that have been chosen.
		PickVarList varList = pickFile.m_varlist;

		this.appendNumber(varList.size());
		for (int i=0; i<varList.size(); i++)
		{
			PVar pVar = (PVar)varList.get(i);
			if (pVar.getFieldPosition () != PVar.PICK_FIELD_ROWID)
			{
				this.appendNumber(pVar.getFieldPosition());
			}
		}
	}
}
