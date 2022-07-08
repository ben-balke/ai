/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/IndexFile.java,v 1.1 2010/10/19 00:44:26 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

public class IndexFile extends Object
{
	public String		m_name;
	public String		m_fields;
	BasixVarList		m_varlist;
	public IndexFile (String name, String fields)
	{
		m_name = name;
		m_fields = fields;
	}
	public void setVarList (BasixVarList varlist)
	{
		m_varlist = varlist;
	}
	public BasixVarList getVarList ()
	{
		return m_varlist;
	}
}
