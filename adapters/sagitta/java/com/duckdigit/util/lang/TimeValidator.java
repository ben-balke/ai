/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/util/lang/TimeValidator.java,v 1.2 2010/04/22 18:41:48 secwind Exp $
==================================================================================================*/
package com.duckdigit.util.lang;

import java.text.*;
import java.util.*;
import java.lang.*;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class TimeValidator
{
	// The "lenient" logic does not seem to work for time stamps. These must be
	// ordered from most to least complex, as otherwise the "HH:mm" format
	// processes every format, ignoring AM/PM and other modifiers. Unfortunately,
	// this means that we will have to fail seven times before succeeding with
	// the most common input format...
	static SimpleDateFormat	timeFormats [] =
	{
		new SimpleDateFormat ("hh:mm:ss a z"),
		new SimpleDateFormat ("HH:mm:ss z"),
		new SimpleDateFormat ("hh:mm a z"),
		new SimpleDateFormat ("HH:mm z"),
		new SimpleDateFormat ("hh:mm:ss a"),
		new SimpleDateFormat ("HH:mm:ss"),
		new SimpleDateFormat ("hh:mm a"),
		new SimpleDateFormat ("HH:mm"),
	};

	static SimpleDateFormat hrFrmt = new SimpleDateFormat("HH:mm:ss");

	public static Time validate(String inputTime)
	{
		java.util.Date			rsltDate = null;
		if (inputTime == null)
			return(null);
		if (inputTime.equals ("now"))
		{
			return Time.valueOf(
							hrFrmt.format( new java.util.Date() ) );
		}
		for (int i = 0; i < timeFormats.length; i++)
		{
			timeFormats [i].setLenient(false);
			try
			{
				rsltDate = timeFormats [i].parse(inputTime);
				if (rsltDate != null)
				{
					break;
				}
			}
			catch (ParseException ignore)
			{
			}
		}
		if (null == rsltDate) return null;
		return new Time( rsltDate. getTime() );
	}
	public static void main (String Argv[])
	{
		String		inp = null;
		Time		ret = null;
		int			len = 0;

		inp = "11:15 AM"; ret = TimeValidator.validate (inp);
		System.out.println ("Input: " + inp + " Output: " + ret );
		inp = "23:15"; ret = TimeValidator.validate(inp);
		System.out.println ("Input: " + inp + " Output: " + ret );
		inp = "11:15 PM PST"; ret = TimeValidator.validate(inp);
		System.out.println ("Input: " + inp + " Output: " + ret );
		inp = "11:15:25 GMT"; ret = TimeValidator.validate(inp);
		System.out.println ("Input: " + inp + " Output: " + ret );
		inp = "now"; ret = TimeValidator.validate (inp);
		System.out.println ("Input: " + inp + " Output: " + ret );
		return;
	}
}

