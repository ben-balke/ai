
/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/pick/PickVarList.java,v 1.2 2010/04/22 18:41:42 secwind Exp $
==================================================================================================*/
package com.duckdigit.pick;

import java.util.ArrayList;

public class PickVarList extends ArrayList
{
	/*
	 * Variable list constructor.
	 */
	public PickVarList()
	{
		super ();
	}

	/**
	 * To add a field to the record declaration.
	 */
	public void addField (PVar var)
	{
		add (var);
	}

	/**
	 * To create a field.
	 */
	public PVar createField (int fieldPos)
	{
		PVar newField = new PVar(fieldPos);
		add(newField);
		return newField;
	}

	/**
	 * To create a field with both position and sub position with in
	 * a multivalue field.
	 */
	public PVar createField (int fieldPos, int subFieldPos)
	{
		PVar newField = new PVar(fieldPos, subFieldPos);
		add(newField);
		return newField;
	}
}
