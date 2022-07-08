
/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/pick/TextFormatter.java,v 1.2 2010/04/22 18:41:42 secwind Exp $
==================================================================================================*/
package com.duckdigit.pick;


/**
 * TextFormatter formats strings based on a defined mask.  It is used
 * to format fields such as phone numbers and ssn.
 */
public class TextFormatter
{
	public static final char		FIELDSEP = (char) -6;
	public static final char		VALUESEP = (char) -3;
	public static final char		VALUESEPALT = (char) -4;
	String			m_mask = null;
	String			m_changeFS = null;

	public TextFormatter (String mask, String changeFS)
	{
		initialize (mask);
		m_changeFS = changeFS;	
	}
	public TextFormatter (String mask)
	{
		initialize (mask);
	}
	private void initialize (String mask)
	{
		if (mask == null)
		{
			m_mask = null;
		}
		else if (mask.equals ("zipcode"))
		{
			m_mask = "#####-####";
		}
		else if (mask.equals ("phone"))
		{
			m_mask = "(###)###-####";
		}
		else
		{
			m_mask = mask;
		}
	}
	public String addFormat (String src)
	{
		char			ch;
		int 			sidx = 0;
		StringBuffer	dest = new StringBuffer ();
		int				srclen = src.length ();

		if (m_mask == null)
		{
			if (m_changeFS != null)
			{
				for (sidx = 0; sidx < srclen; sidx++)
				{
					ch = src.charAt (sidx);
	
					if (ch == VALUESEP || ch == VALUESEPALT)
					{
						if (m_changeFS.length () > 0)
						{
							dest.append (m_changeFS);
						}
					}
					else
					{
						dest.append (ch);
					}
				}
				return dest.toString ();
			}
			return src;
		}

		for (int midx = 0; midx < m_mask.length (); midx++)
		{
			if (m_changeFS != null)
			{
				while (sidx < srclen && (src.charAt (sidx) == VALUESEP || src.charAt (sidx) == VALUESEPALT))
				{
					sidx++;
					if (m_changeFS.length () > 0)
					{
						dest.append (m_changeFS);
					}
				}
			}
			char mch = m_mask.charAt (midx);
			switch (mch)
			{
			case '#':
				if (sidx < srclen)
				{
					dest.append (src.charAt (sidx++));
				}
				break;
			case '*':
				if (sidx < srclen)
				{
					dest.append (dest.substring (sidx));
					sidx = src.length ();
				}
				break;
			default:
				if (sidx < srclen)
				{
					dest.append (mch);
				}
				break;
				
			}
		}
		return dest.toString ();
	}
	public static void main (String args [])
	{
		TextFormatter	tf;

		if (args.length == 2)
		{
			tf = new TextFormatter (args [0]);
		}
		else if (args.length == 3)
		{
			tf = new TextFormatter (args [0], args [2]);
		}
		else
		{
			System.out.println ("Usage: <mask> <value> [ <changeFS> ]");
			return;
		}
		System.out.println (tf.addFormat (args[1].replace ('^', VALUESEP)));
	}
}
