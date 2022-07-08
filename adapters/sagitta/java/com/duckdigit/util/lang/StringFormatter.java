/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/util/lang/StringFormatter.java,v 1.2 2010/04/22 18:41:48 secwind Exp $
==================================================================================================*/
package com.duckdigit.util.lang;

import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * This class provides generic string formatting utilities for things like zipcodes and
 * phone numbers.
 */
public class StringFormatter
{
	/**
	 * Converts a 5 or 9 digit zip code string into a
	 * correctly formated string using the '-' character
	 * between the 5th and 6th character.
	 * @param	zip 	the source string to convert.
	 * @returns Formatted zipcode string
	 */
	public static String formatZipcode (String zip)
	{
		String rslt;
		if (zip.length () > 5)
		{
			rslt = zip.substring (0, 5) + "-" + zip.substring (5);
		}
		else
		{
			rslt = zip;
		}
		return rslt;
	}

	/**
	 * Formats a string into a phone number using (###)###-####.
	 * If the phone number is does not include an area code
	 * the ###-#### notation is used.  All characters are turned into numbers
	 * based on the standard phone letter mapping.  All non-numbers and 
	 * characters are stripped before any formatting is done.
	 * @param	phone String containing the unformated phone number
	 * @return	Formatted phone number string.
	 */
	public static String formatPhone (String phone)
	{
		String 	rslt;
		int		len = phone.length ();

		String 		newphone = "";
		for (int i = 0; i < len; i++)
		{
			char		ch;
			int			num;
			ch = phone.charAt (i);
			if (ch >= '0' && ch <= '9')
			{
				newphone += ch;
			}
			else
			{	
				char		adjust = '\0';
				if (ch >= 'A' && ch <= 'Z')
				{
					adjust = 'A';
				}
				else if (ch >= 'a' && ch <= 'z')
				{
					adjust = 'a';
				}
				if (adjust != '\0')
				{
					num = ((int)(ch - adjust) / 3) + 2;
					if (num > 9)
					{
						num = 9;
					}
					newphone += ('0' + num);
				}
			}
		}
		len = newphone.length ();
		
		if (len < 4)
		{
			return phone;
		}
		phone = newphone;
		if (phone.length () <= 7)
		{
			rslt = phone.substring (0, 3) + "-" + phone.substring (3);
		}
		else
		{
			rslt = "(" + phone.substring (0, 3) + ")" + phone.substring (3, 6) +
				"-" + phone.substring (6);
		}
		return rslt;
	}

}
