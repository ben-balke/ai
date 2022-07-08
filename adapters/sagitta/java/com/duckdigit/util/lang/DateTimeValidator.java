/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/util/lang/DateTimeValidator.java,v 1.2 2010/04/22 18:41:48 secwind Exp $
==================================================================================================*/
package com.duckdigit.util.lang;

import java.util.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class DateTimeValidator
{
	public static Timestamp validate(String inputTime)
	{
		int     iSep;
		String  sDate;
		Date    date;
		String  sTime;
		Time    time;
		String  sTimestamp;

		if (null == inputTime) return null;
		if (inputTime.equals ("now"))
		{
			return new Timestamp( new Date(). getTime() );
		}

		sTimestamp = inputTime;

		// Times always contain a ':'. Search for it and back up to the first
		// blank. The right part of the string should be the time, the left
		// the date.
		iSep = inputTime.indexOf( ':' );
		if (-1 == iSep)
		{
			sDate = inputTime;
			sTime = "00:00:00";
		}
		else
		{
			iSep--;
			while (' ' != inputTime.charAt( iSep ))
			{
				iSep--;
			}
			sDate = inputTime.substring( 0, iSep );
			sTime = inputTime.substring( iSep + 1 );
		}
		date = DateValidator. validate( sDate );
		if (null != date)
		{
			time = TimeValidator. validate( sTime );
			if (null != time)
			{
				sTimestamp = new SimpleDateFormat( "yyyy-MM-dd" ). format( date )
						+ " " + new SimpleDateFormat( "HH:mm:ss" ). format( time );
			}
		}

		try {
			return Timestamp.valueOf( sTimestamp );
		}
		catch (Exception e) {}

		return null;
	}

	public static void main (String Argv[])
	{
		String		inp = null;
		Date		ret = null;
		int			len = 0;

		inp = "1/1/2002 12:00:45 AM"; ret = DateTimeValidator.validate (inp);
		System.out.println ("Input: " + inp + " Output: " + ret);
		inp = "01/10/99 22:34"; ret = DateTimeValidator.validate (inp);
		System.out.println ("Input: " + inp + " Output: " + ret);
		inp = "29-FEB-2000"; ret = DateTimeValidator.validate(inp);
		System.out.println ("Input: " + inp + " Output: " + ret);
		inp = "12-01-2000"; ret = DateTimeValidator.validate(inp);
		System.out.println ("Input: " + inp + " Output: " + ret);
		inp = "12-32-1993"; ret = DateTimeValidator.validate(inp);
		System.out.println ("Input: " + inp + " Output: " + ret);
		inp = "2007-06-11 00:00:00"; ret = DateTimeValidator.validate(inp);
		System.out.println ("Input: " + inp + " Output: " + ret);
		return;
	}
}

