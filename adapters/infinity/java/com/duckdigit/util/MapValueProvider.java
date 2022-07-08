/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/util/MapValueProvider.java,v 1.1 2010/10/19 00:43:59 secwind Exp $
==================================================================================================*/
package com.duckdigit.util;

import java.util.*;
import java.io.*;

public class MapValueProvider extends AbstractValueProvider
{
	Map			m_values;
	public MapValueProvider (String name, Map values)
	{
		super (name);
		m_values = values;
	}

	public Iterator getKeys()
	{
		return m_values.keySet().iterator();
	}

	public Object getValue (String key)
	{
		return m_values.get (key);
	}

	public void setValue(String key, Object value)
		throws ReadOnlyValueProviderException
	{
		if (null == value)
		{
			m_values. remove( key );
		}
		else
		{
			m_values. put( key, value );
		}
	}
}
