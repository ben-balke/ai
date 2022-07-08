/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/util/lang/EnumIterator.java,v 1.2 2010/04/22 18:41:48 secwind Exp $
==================================================================================================*/
package com.duckdigit.util.lang;

import java.util.*;

public class EnumIterator implements Iterator
{
	private Enumeration m_enum;

	public EnumIterator(
			Enumeration     p_enum )
	{
		m_enum = p_enum;
	}

	public boolean hasNext()
	{
		return m_enum.hasMoreElements();
	}

	public Object next()
	{
		return m_enum.nextElement();
	}

	public void remove()
	{
	}
}
