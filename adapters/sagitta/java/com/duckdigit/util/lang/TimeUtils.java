/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/util/lang/TimeUtils.java,v 1.2 2010/04/22 18:41:48 secwind Exp $
==================================================================================================*/
package com.duckdigit.util.lang;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TimeUtils {

	public static Date getToday()
	{
		GregorianCalendar   cal;

		cal = new GregorianCalendar();

		cal.set(
			    cal.get( Calendar.YEAR ),
			    cal.get( Calendar.MONTH ),
			    cal.get( Calendar.DATE ),
				0, 0, 0 );

		return cal. getTime();
	}
}
