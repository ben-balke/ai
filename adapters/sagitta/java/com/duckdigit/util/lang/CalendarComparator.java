/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/util/lang/CalendarComparator.java,v 1.2 2010/04/22 18:41:48 secwind Exp $
==================================================================================================*/
package com.duckdigit.util.lang;

import java.util.Calendar;
import java.util.Comparator;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class CalendarComparator implements Comparator {

    public CalendarComparator() {
    }

    public int compare(
		    Object o1, Object o2 )
	{
		long        lMillis;

		lMillis = ((Calendar) o1).getTimeInMillis() -
					((Calendar) o2).getTimeInMillis();
		if (lMillis > Integer.MAX_VALUE)
		{
			lMillis = Integer. MAX_VALUE;
		}
		else if (lMillis < Integer.MIN_VALUE)
		{
			lMillis = Integer. MIN_VALUE;
		}
		return (int) lMillis;
    }

    public boolean equals(Object obj)
	{
		return obj instanceof CalendarComparator;
    }
}
