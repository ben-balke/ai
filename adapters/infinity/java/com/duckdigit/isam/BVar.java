/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/BVar.java,v 1.1 2010/10/19 00:44:25 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

import java.util.*;
import java.text.*;
import java.util.GregorianCalendar;

import com.duckdigit.util.StringUtils;
//import com.duckdigit.zml.ZmlData;
//import com.duckdigit.zml.rh.IsamRecordField;
//import com.duckdigit.zml.rh.RecordField;

public class BVar extends BasixVar 
{
	private BasixData	m_basixdata = null;
		/**
		 * When getting the data move the bytes as it where one
		 * of the other types.  Then convert the data.
		 */
	private int			m_moveAs;
	private int			m_moveOffset;
	private int			m_moveSize;

	public BVar (BasixVar mastervar, int idx1, int idx2, byte buf [])
		throws BasixDeclException
	{
		super (mastervar, idx1, idx2);
		m_basixdata = makeBasixData (buf, m_type);
	}

	public BVar (BasixVar mastervar, int idx1, int idx2, byte buf [],
				 int moveAs)
		throws BasixDeclException
	{
		super (mastervar, idx1, idx2);
		m_basixdata = makeBasixData (buf, m_type);
		if (moveAs != m_type)
		{
			m_moveAs = moveAs;
			m_moveOffset = 0;
			m_moveSize = getSize ();
		}
	}
	public BVar (BasixVar mastervar, int idx1, int idx2, byte buf [],
				 int moveAs, int moveoffset, int movesize)
		throws BasixDeclException
	{
		super (mastervar, idx1, idx2);
		m_basixdata = makeBasixData (buf, m_type);
		if (moveAs != m_type)
		{
			m_moveAs = moveAs;
			if (moveoffset + movesize > getSize ())
			{
				throw new BasixDeclException ("Move offset and size is larger than the base variable");
			}
			m_moveOffset = moveoffset;
			m_moveSize = movesize;
		}
	}

	private static BasixData makeBasixData (byte buf [], int type)
		throws BasixDeclException
	{
		BasixData       basixdata = null;
		switch (type)
		{
		case TYPE_BCD4:
			basixdata = new BasixDataBcd4 (buf);
			break;
		case TYPE_BCD8:
			basixdata = new BasixDataBcd8 (buf);
			break;
		case TYPE_SHORT:
			basixdata = new BasixDataShort (buf);
			break;
		case TYPE_STRING:
			basixdata = new BasixDataString (buf);
			break;
		}

		return basixdata;
	}

	public void set (int i)
		throws Exception
	{
		m_basixdata.set (i, m_masteroffset, getSize ());
	}

	public void set (double d)
		throws Exception
	{
		m_basixdata.set (d, m_masteroffset, getSize ());
	}


	public void set (String str)
		throws Exception
	{
		m_basixdata.set (str, m_masteroffset, getSize ());
	}

	public int getInt ()
		throws Exception
	{
		if (m_moveAs != TYPE_UNKNOWN)
		{
			byte [] bytes = m_basixdata.getBytes (m_masteroffset + m_moveOffset,
								m_moveSize);
			BasixData	basixdata = makeBasixData (bytes, m_moveAs);
			return basixdata.getInt (0, bytes.length);
		}
		return m_basixdata.getInt (m_masteroffset, getSize ());
	}
	public double getDouble ()
		throws Exception
	{
		if (m_moveAs != TYPE_UNKNOWN)
		{
			byte [] bytes = m_basixdata.getBytes (m_masteroffset + m_moveOffset,
								m_moveSize);
			BasixData	basixdata = makeBasixData (bytes, m_moveAs);
			return basixdata.getDouble (0, bytes.length);
		}
		return m_basixdata.getDouble (m_masteroffset, getSize ());
	}
	public String getString ()
		throws Exception
	{
		if (m_moveAs != TYPE_UNKNOWN)
		{
			byte [] bytes = m_basixdata.getBytes (m_masteroffset + m_moveOffset,
							m_moveSize);
			BasixData	basixdata = makeBasixData (bytes, m_moveAs);
			return basixdata.getString (0, bytes.length);
		}

		return m_basixdata.getString (m_masteroffset, getSize ());
	}
	// BBB we may not have any standard way that time values
	// are stored in the ISAM files.  This is going to cause trouble.
	private Date getTime ()
		throws Exception
	{
		double d = m_basixdata.getDouble (m_masteroffset,
				getSize ());
		int hour = (int)d / 10000;
		int min = (int)d % 10000 / 100;
		GregorianCalendar	cal = new GregorianCalendar ();
		cal.set (Calendar.HOUR_OF_DAY, hour);
		cal.set (Calendar.MINUTE, min);
		return cal.getTime ();
	}


	public static int dateToJulian (Date d)
	{
		if (d == null)
		{
			return 0;
		}
		GregorianCalendar	cal = new GregorianCalendar ();
		cal.setTime (d);
		int juldate = cal.get (Calendar.DAY_OF_YEAR);
		juldate += (cal.get (Calendar.YEAR) - 1900) * 1000;
		return juldate;
	}
	public static int dateToJulian (String dateString, String format)
	{
		if (dateString.length () == 0)
		{
			return 0;
		}
		SimpleDateFormat formatter
			= new SimpleDateFormat (format);
		ParsePosition pos = new ParsePosition(0);
		Date d = formatter.parse (dateString, pos);
		GregorianCalendar	cal = new GregorianCalendar ();
		cal.setTime (d);
		int juldate = cal.get (Calendar.DAY_OF_YEAR);
		juldate += (cal.get (Calendar.YEAR) - 1900) * 1000;
		return juldate;
	}
	public Date julianToDate ()
		throws Exception
	{
		return julianToDate (getInt ());
	}
	public static Date julianToDate (int jul)
	{
		// BBB This is questionable.
		if (jul == 0)
		{
			return null;
		}
		int		year = jul / 1000;
		if (year < 21)
		{
			year += 2000;
		}
		else
		{
			year += 1900;
		}
		GregorianCalendar	cal = new GregorianCalendar ();
		cal.setTime (new Date (0));
		cal.set (Calendar.DAY_OF_YEAR, jul % 1000);
		cal.set (Calendar.YEAR, year);
		return cal.getTime ();
	}

	public byte [] getBytes ()
		throws Exception
	{
		return m_basixdata.getBytes (m_masteroffset, getSize ());
	}
}
