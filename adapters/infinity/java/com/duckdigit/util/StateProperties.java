/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/util/StateProperties.java,v 1.1 2010/10/19 00:43:59 secwind Exp $
==================================================================================================*/
package com.duckdigit.util;

import java.util.ArrayList;
import java.util.Properties;

/**
	Extends the Properties class with type-safe interfaces for non-String
	values.
 */
public class StateProperties extends Properties
{
	public StateProperties()
	{
	}

	public StateProperties( Properties  p_defaults )
	{
		super( p_defaults );
	}

	public boolean getBoolean( String p_sKey, boolean p_bDefault )
	{
		String	sValue;
		Boolean	bValue;

		sValue = getProperty( p_sKey, null );

		if (sValue != null)
		{
			if ("true".equalsIgnoreCase( sValue ))
			{
				return true;
			}
			else if ("false".equalsIgnoreCase( sValue ))
			{
				return false;
			}
		}

		return p_bDefault;
	}

	public int getInt( String p_sKey, int p_iDefault )
	{
		String	sValue;
		Integer	iValue;

		sValue = getProperty( p_sKey, null );

		if (sValue != null)
		{
			try
			{
				iValue = Integer.valueOf( sValue );
				return iValue.intValue();
			}
			catch (NumberFormatException e) {}
		}

		return p_iDefault;
	}

	public float getFloat( String p_sKey, float p_fDefault )
	{
		String	sValue;
		Float	fValue;

		sValue = getProperty( p_sKey, null );

		if (sValue != null)
		{
			try
			{
				fValue = Float.valueOf( sValue );
				return fValue.floatValue();
			}
			catch (NumberFormatException e) {}
		}

		return p_fDefault;
	}

	public String getString(
			String p_sKey,
			String p_sDefault )
	{
		return getProperty( p_sKey, p_sDefault );
	}

	/**
	 *  getMultiple returns an array list of strings containing multiple entries
	 * that are suffixed by a number.  For example:  value, value1, value2, ... value'n'.
	 * The first entry can have an optional suffix so that if provided 'value' would be the
	 * first item or 'value1' would be the first item if 'value' is missing.
	 * @param prefix of the property entry.
	 * @return ArrayList
	 */
	public ArrayList getMultiple (
			String prefix)
	{
		ArrayList		list = null;
		String		value;
		String		fieldname;
		for (int i = 0; ; i++)
		{
			if (i != 0)
			{
				fieldname = prefix + i;
			}
			else
			{
				fieldname = prefix;
			}
			value = getString (fieldname, null);
			if (value == null)
			{
				if (i == 0)
					continue;
				break;
			}
			if (list == null)
			{
				list = new ArrayList ();
			}
			list.add (value);
		}
		return list;
	}

	/**
	 * Merges entries against this prefix with an existing list.
	 * @param prefix        of the property entry.
	 * @param ArrayList     Pre-defined entries.
	 * @return ArrayList    The merged list.
	 */
	public ArrayList getMultiple (
			String      prefix,
			ArrayList   p_merge )
	{
		ArrayList		list;

		list = getMultiple( prefix );
		if (null == p_merge)    return list;

		if (list != null)
		{
		    p_merge. addAll( list );
		}

		return p_merge;
	}
}
