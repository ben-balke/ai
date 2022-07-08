
/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/pick/RecordId.java,v 1.2 2010/04/22 18:41:42 secwind Exp $
==================================================================================================*/
package com.duckdigit.pick;

public class RecordId
{
	String			m_val;
	public RecordId (String val)
	{
		set (val);
	}
	public String toString () { return m_val; }
	public int length () { return m_val.length(); }
	public void set (String val) { m_val = val; }
}
