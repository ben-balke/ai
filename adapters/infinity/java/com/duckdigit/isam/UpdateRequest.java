/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/UpdateRequest.java,v 1.1 2010/10/19 00:44:26 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

import java.util.*;

public class UpdateRequest
{
	int				m_datalen = 0;
	int				m_rcdno = 0;
	ArrayList		m_fields = new ArrayList ();
	public UpdateRequest ()
	{
	}
	public void setRcdNo (int rcdno)
	{
		m_rcdno = rcdno;
	}
	public int getRcdNo ()
	{
		return m_rcdno;
	}
	public void addField (BVar bvar)
	{
		m_fields.add (bvar);
		m_datalen += bvar.getSize ();
	}
	public int getOffset (int varidx)
		throws Exception
	{
		BVar		bvar = (BVar) m_fields.get (varidx);
		return bvar.getMasterOffset ();
	}
	public int getSize (int varidx)
		throws Exception
	{
		BVar		bvar = (BVar) m_fields.get (varidx);
		return bvar.getSize ();
	}
	
	public byte [] getBytes (int varidx)
		throws Exception
	{
		BVar		bvar = (BVar) m_fields.get (varidx);
		return bvar.getBytes ();
		
	}
	public int getCount ()
	{
		return m_fields.size ();
	}
	public int getDataLen ()
	{
		return m_datalen;
	}
}
