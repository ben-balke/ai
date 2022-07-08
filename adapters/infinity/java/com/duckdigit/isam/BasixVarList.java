/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/BasixVarList.java,v 1.1 2010/10/19 00:44:25 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

public class BasixVarList extends ArrayList
{
	private int			m_recordLength = 0;

	/*
	 * Variable list constructor.
	 */
	public BasixVarList()
	{
		super ();
	}

	/**
	 * To add a field to the record declaration.
	 */
	public void addField (BasixVar var)
	{
		add (var);
		m_recordLength += var.getSize ();
	}

	/**
	 * To calculate the length of the subject record.
	 * This is simply the offset of the last record, plus its length.
	 */
	public int getRecordLength()
	{
		return m_recordLength;
	}

	/**
	 * To identify the field identified by the value.
	 * The parameter must be intern'd against the field identifiers, as the
	 * search uses the boolean comparator, rather than equals().Pm
	 */
	public BVar createSubVar (String vardec, byte buf [])
		throws BasixDeclException
	{
		int			idx1 = -1;
		int			idx2 = -1;
		String		name;
		int			moveAs = BasixVar.TYPE_UNKNOWN;
		int			asIdx;

		if ((asIdx = vardec.indexOf ("as")) != -1)
		{
			if (vardec.indexOf ("dim") != -1)
			{
				moveAs = BasixVar.TYPE_BCD4;
			}
			else if (vardec.indexOf ("short") != -1)
			{
				moveAs = BasixVar.TYPE_SHORT;
			}
			else if (vardec.indexOf ("long") != -1)
			{
				moveAs = BasixVar.TYPE_BCD8;
			}
			else if (vardec.indexOf ("string") != -1)
			{
				moveAs = BasixVar.TYPE_STRING;
				int i = vardec.indexOf ("(", asIdx);
			}
			vardec = vardec.substring (0, asIdx);
		}
		StringTokenizer	stk = new StringTokenizer (vardec.trim (), "(),");
		int		tokens = stk.countTokens ();
		if (tokens < 1 && tokens > 3)
		{
			return null;
		}
		name = stk.nextToken ();
		if (tokens > 1)
		{
			idx1 = Integer.parseInt (stk.nextToken ());
			if (tokens > 2)
			{
				idx2 = Integer.parseInt (stk.nextToken ());
			}
		}

		Iterator                varIter;
		BasixVar			    var;
		varIter = this.iterator();
		while (varIter.hasNext())
		{
			var = (BasixVar) varIter.next();
			if (var.nameEquals (name))
			{
				return new BVar (var, idx1, idx2, buf, moveAs);
			}
		}
		return null;
	}

}
