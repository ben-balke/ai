
/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/pick/PVar.java,v 1.2 2010/04/22 18:41:42 secwind Exp $
==================================================================================================*/
package com.duckdigit.pick;

import java.util.*;
import java.text.*;
import java.util.GregorianCalendar;

import com.duckdigit.util.lang.StringUtils;

public class PVar 
{
	public static final int		PICK_FIELD_ROWID = -1;

	public static final int		PICK_STRING = 0;
	public static final int		PICK_DATE = 1;
	public static final int		PICK_INT = 2;
	public static final int		PICK_REAL = 3;
	public static final int		PICK_TIME = 4;
		/**
		 * Copy output specifies that the fields are output with escaped characters
		 * for postgres bulk copy commands.
		 */
	public static boolean		s_copyOutput = false;
						

	private int			m_fieldPos;
	private int			m_fieldSubPos = 0; // For multi value fields.  Selects a field within. 
										// Zero means entire field.
	String				m_value;
	TextFormatter		m_format = null;

						/*
						 * pickconv is used to format the pick data type
						 * ie, MR2 = real 2 decimals.
						 */
	String				m_pickconv;
			

	public PVar (int fieldPos)
	{
		m_fieldPos = fieldPos;
		m_value = null;
	}
	public PVar (int fieldPos, int fieldSubPos)
	{
		m_fieldPos = fieldPos;
		m_fieldSubPos = fieldSubPos;
		m_value = null;
	}
	public static void enableCopyOutput (boolean bEnable)
	{		
		s_copyOutput = bEnable;
	}
	public void setFormatter (String mask, String changeFS)
	{
		m_format = new TextFormatter (mask, changeFS);
	}

	public int getFieldPosition()
	{
		return m_fieldPos;
	}
	
	public void set (byte buf [], int offset, int len)
	{
		m_value = new String ();
		if (m_fieldSubPos == 0)
		{
			for (int i = offset; i < offset + len; i++)
			{
				m_value += (char) buf[i];
			}
		}
		else
		{
			//System.out.println (new String (buf, offset, len));
			int		idx = 0;
			int		subfield = m_fieldSubPos - 1;
			while (subfield > 0)
			{
				//System.out.println ("Skipping subfield:"  + subfield);
				while (idx < len && (((char)buf [offset + idx]) != com.duckdigit.pick.TextFormatter.VALUESEP && 
					((char)buf [offset + idx]) != com.duckdigit.pick.TextFormatter.VALUESEPALT))
				{
					idx++;
				}
				idx++;
				subfield--;
			}
			for (; idx < len && (((char)buf [offset + idx]) != com.duckdigit.pick.TextFormatter.VALUESEP && 
					((char)buf [offset + idx]) != com.duckdigit.pick.TextFormatter.VALUESEPALT); idx++)
			{
				//System.out.println ("Adding :"  + (char) buf [offset + idx]);
				m_value += (char) buf [offset + idx];
			}
			//System.out.println ("subfield value:"  + m_value);

		}
		if (m_format != null)
		{
			m_value = m_format.addFormat (m_value);
		}
	}
	public void set (String value)
	{
		m_value = value;
	}
	public void set (RecordId value)
	{
		set (value.toString ());
	}
	public void set (int value)
	{
		m_value = new Integer (value).toString ();
	}
	
	public void set (
			Object              obj,
			String         p_Field)
		throws Exception
	{
		if	(obj == null)
		{
			m_value = null;
		}
		m_value = obj.toString ();
		if (m_format != null)
		{
			m_value = m_format.addFormat (m_value);
		}
	}

		/**
		 * Unformated string.
		 */
	public String toString ()
	{
		if (s_copyOutput)
			return getString (); 
		return m_value;
	}

	public String getString ()
	{
		if (s_copyOutput)
		{
			if (m_value == null)
				return null;
			if (m_value.length () == 0)
			{
				return "";
			}
				
			StringBuffer		buf = new StringBuffer ();
			char				ch = 0;
	
			for (int midx = 0; midx < m_value.length (); midx++)
			{
				ch = m_value.charAt (midx);
				switch (ch)
				{
					case '\\':
						buf.append ("\\\\");
						break;
					case '\b':
						buf.append ("\\b");
						break;
					case '\f':
						buf.append ("\\f");
						break;
					case '\n':
						buf.append ("\\n");
						break;
					case '\r':
						buf.append ("\\r");
						break;
					case '\t':
						buf.append ("\\t");
						break;
					case '\013':
						buf.append ("\\v");
						break;
					default:
						buf.append (ch);
						break;
				}	
			}
			return buf.toString ();
		}
		return m_value;
	}

	public double getDouble ()
	{
		return getDouble (m_value);
	}

	public double getDouble (String pickformat, ConversionObserver convObv)
	{
		return getDouble (m_value, pickformat, convObv);
	}

	private double getDouble (String strValue, String pickformat, 
		ConversionObserver convObv)
	{
		strValue = strValue.trim ();
		if (strValue.length () == 0 || strValue.equals ("-"))
		{
			return 0;
		}
		double 		value = 0;
		try {
			strValue = StringUtils.removeChars (strValue, "$,");
			value = Double.parseDouble (strValue);
			int			mrIdx = pickformat.indexOf ("MR");
			if (mrIdx != -1)
			{
				char 	ch = pickformat.charAt (mrIdx + 2);
				if (ch >= '0' && ch <= '9')
				{
					int		factor = (int) (ch - '0');
					for (; factor > 0; factor--)
					{
						value = value * .1;
					}
				}
			}
		}
		catch (Exception ex)
		{
			convObv.reportBadNumber (strValue, ex.getMessage ());
			value = 0;
		}
		return value;
	}

	private double getDouble (String value)
	{
		try 
		{
			return Double.parseDouble (value);
		} 
		catch (Exception ex)
		{
			return 0;
		}
	}

	public int getInt ()
	{
		return getInt (m_value);
	}

	public int getInt (String pickformat, ConversionObserver convObv)
	{
		return getInt (m_value, pickformat, convObv);
	}

	private int getInt (String strValue, String pickformat, 
		ConversionObserver convObv)
	{
		int 		value = 0;
		if (strValue.length () == 0)
		{
			return 0;
		}
		strValue = StringUtils.removeChars (strValue, "$,");
		try {
			value = Integer.parseInt (strValue);
		}
		catch (Exception ex)
		{
			convObv.reportBadNumber (strValue, ex.getMessage ());
			value = 0;
		}
		return value;
	}
	private int getInt (String value)
	{
		try 
		{
			return Integer.parseInt (value);
		} 
		catch (Exception ex)
		{
			return 0;
		}
	}

	public java.util.Date getDate ()
	{
		return getDate (m_value);
	}

	private java.util.Date getDate (String value)
	{
		if (value.trim().length() == 0)
		{
			return null;
		}
		GregorianCalendar newDate = new GregorianCalendar (1967,11,31);
		newDate.add (Calendar.DATE, getInt (value)); 
		return newDate.getTime ();
	}
	private String getDateString (String value)
	{
		if (value.trim().length() == 0)
		{
			return "0001-01-01";
		}
		java.util.Date		d = getDate (value);
		if (d == null)
		{
			return "0001-01-01";
		}
		SimpleDateFormat	sdf = new SimpleDateFormat ("dd-MMM-yyyy");
		return sdf.format (d);
	}

	private void appendSubValue (int picktype, String subValue, StringBuffer dest, ConversionObserver convObv, String pickformat)
	{
		if (picktype == PICK_DATE)
		{
			dest.append (getDateString (subValue.toString ()));
		}
		else
		{
			if (picktype == PICK_INT || picktype == PICK_REAL)
			{
				if (subValue.trim ().length () == 0)
				{
					subValue = "0";
				}
				else
				{
					if (picktype == PICK_INT)
					{
						int		value = getInt (subValue, pickformat,
												convObv);
						subValue = Integer.toString (value);
					}
					else
					{
						double		value = getDouble (subValue, pickformat,
												convObv);
						subValue = Double.toString (value);
					}
				}
			}
			else
			{
				if (convObv != null)
				{
					int		iMaxLen = convObv.getMaxSize ();
					if (subValue.length () > iMaxLen)
					{
						convObv.reportTruncation (subValue);
						subValue = subValue.substring (0, iMaxLen - 1);
					}
				}
			}
			dest.append (subValue);
		}
	}

	public String makeArray (int picktype, String pickformat, ConversionObserver convObv)
	{
//System.err.println ("makeArray: type: " + picktype + " orig: " + getString ());
		if (m_value.length () == 0)
		{
			return null;
		}

		StringBuffer		buf = new StringBuffer ("{\"");
		StringBuffer		subValue = new StringBuffer ();
		char				ch = 0;

		for (int midx = 0; midx < m_value.length (); midx++)
		{
			ch = m_value.charAt (midx);
			if (ch == com.duckdigit.pick.TextFormatter.VALUESEP ||
				ch == com.duckdigit.pick.TextFormatter.VALUESEPALT)
			{
				appendSubValue (picktype, subValue.toString (), buf, convObv,
					pickformat);
				subValue = new StringBuffer ();
				buf.append ("\"");
				if (midx != (m_value.length () - 1))
				{
					buf.append (",\"");
				}
			}
			else if (ch == '"')
			{
				if (s_copyOutput) subValue.append ("\\\\\\\""); else subValue.append ("\\\"");
			}
			else if (ch == '\b')
			{
				if (s_copyOutput) subValue.append ("\\b"); else buf.append ('\b');
			}
			else if (ch == '\f')
			{
				if (s_copyOutput) subValue.append ("\\f"); else buf.append ('\f');
			}
			else if (ch =='\n')
			{
				if (s_copyOutput) subValue.append ("\\n"); else buf.append ('\n');
			}
			else if (ch =='\r')
			{
				if (s_copyOutput) subValue.append ("\\r"); else buf.append ('\r');
			}
			else if (ch =='\t')
			{
				if (s_copyOutput) subValue.append ("\\t"); else buf.append ('\t');
			}
			else if (ch =='\013')
			{
				if (s_copyOutput) subValue.append ("\\v"); else buf.append ('\013');
			}
			else
			{
				subValue.append (ch);
				if (ch == '\'')
				{
					subValue.append (ch);
				}
				else if (ch == '\\')
				{
						/*
						 * We need four of these to work correctly.  Ugh!
						 */
					subValue.append (ch);
					subValue.append (ch);
					subValue.append (ch);
				}
			}
		}
		if (ch != com.duckdigit.pick.TextFormatter.VALUESEP &&
			ch != com.duckdigit.pick.TextFormatter.VALUESEPALT)
		{
			appendSubValue (picktype, subValue.toString (), buf, convObv, 
				pickformat);
			buf.append ("\"");
		}
		buf.append ("}");
		//System.err.println ("makeArray: " + buf.toString ());
		return buf.toString ();
	}
}
