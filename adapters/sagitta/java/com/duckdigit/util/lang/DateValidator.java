/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/util/lang/DateValidator.java,v 1.2 2010/04/22 18:41:48 secwind Exp $
==================================================================================================*/
package com.duckdigit.util.lang;

import java.text.*;
import java.util.*;
import java.lang.*;

public class DateValidator
{
	static SimpleDateFormat	dateFormats [] =
	{
		new SimpleDateFormat ("dd-MMM-yyyy"),
		new SimpleDateFormat ("MM/dd/yyyy"),
		new SimpleDateFormat ("MM/dd/yy"),
		new SimpleDateFormat ("MM-dd-yyyy"),
		new SimpleDateFormat ("MM-dd-yy"),
		new SimpleDateFormat ("MM dd yyyy"),
		new SimpleDateFormat ("MM dd yy"),
		new SimpleDateFormat ("dd-MMM-yy"),
		new SimpleDateFormat ("yyyy-MM-dd"),
		new SimpleDateFormat ("EEE MMM dd HH:mm:ss zzz yyyy")
	};

	public static Date validate(String inputDate)
	{
		Date			rsltDate = null;
		if (inputDate == null)
			return(null);
		if (inputDate.equals ("now"))
		{
			return new Date ();
		}
		for (int i = 0; i < dateFormats.length; i++)
		{
			dateFormats [i].setLenient(false);
			try
			{
				rsltDate = dateFormats [i].parse(inputDate);
				if (rsltDate != null)
				{
					break;
				}
			}
			catch (ParseException ignore)
			{
			}
		}
		return rsltDate;
	}
	public static void main (String Argv[])
	{
		String		inp = null;
		Date		ret = null;
		int			len = 0;

		inp = "01/10/99"; ret = DateValidator.validate (inp);
		System.out.println ("Input: " + inp + " Output: " + ret);
		inp = "29-FEB-2000"; ret = DateValidator.validate(inp);
		System.out.println ("Input: " + inp + " Output: " + ret);
		inp = "12-01-2000"; ret = DateValidator.validate(inp);
		System.out.println ("Input: " + inp + " Output: " + ret);
		inp = "12-32-1993"; ret = DateValidator.validate(inp);
		System.out.println ("Input: " + inp + " Output: " + ret);
		return;
	}
}

