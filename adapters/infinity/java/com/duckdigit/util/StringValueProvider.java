/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/util/StringValueProvider.java,v 1.1 2010/10/19 00:43:59 secwind Exp $
==================================================================================================*/
package com.duckdigit.util;

import java.util.*;
import java.io.*;

public class StringValueProvider extends AbstractValueProvider
{
	Hashtable			m_values;
	public StringValueProvider (String name)
	{
		super (name);
		m_values = new Hashtable ();
	}
	public Iterator getKeys()
	{
		return m_values.keySet().iterator();
	}

	public boolean addString (String equalString)
	{
		int		idx = equalString.indexOf ('=');
		if (idx == -1)
		{
			return false;
		}
		m_values.put (equalString.substring (0, idx),
			equalString.substring (idx + 1));
		return true;
	}

	public void addString (String key, String value)
	{
		m_values.put (key, value);
	}

	public Object getValue (String key)
	{
		return m_values.get (key);
	}
}
