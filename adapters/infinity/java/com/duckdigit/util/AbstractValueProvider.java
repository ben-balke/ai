package com.duckdigit.util;

import java.io.*;
import java.util.Iterator;

public abstract class AbstractValueProvider
	implements ValueProvider
{
	String		m_name;
	public AbstractValueProvider (String name)
	{
		m_name = name;
	}

	public String getName ()
	{
		return m_name;
	}

	public void setName (String name)
	{
		m_name = name;
	}

	public abstract Iterator getKeys();
	public abstract Object getValue (String key);

	/**
		Ignores mode, and simply returns the default representation of the
		value.
		@param String           Kay.
		@param int              Mode.
		@return Object
	 */
	public Object getValue(
		    String          key,
			int             p_mode )
	{
		return getValue( key );
	}

	public boolean getValue (String key, PrintStream pout)
	{
		Object	value = getValue (key);
		if (value == null)
			return false;
		pout.print (value.toString ());
		return true;
	}
	public void setValue (String key, Object value)
		throws ReadOnlyValueProviderException
	{
		throw new ReadOnlyValueProviderException ("ValueProvider " + m_name + " cannot set key '" +
				key + " because it is read only.");
	}
	public ValueProvider getNestedProvider (String key)
	{
		Object		obj = getValue (key);
		if (obj != null && obj instanceof ValueProvider)
		{
			return (ValueProvider) obj;
		}
		return null;
	}
}
