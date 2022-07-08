/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/util/lang/StringParser.java,v 1.2 2010/04/22 18:41:48 secwind Exp $
==================================================================================================*/
package com.duckdigit.util.lang;

import java.util.*;

public class StringParser
{
	public static String before(String complete, String lookfor)
		throws Exception
	{
		int soffset = complete.indexOf(lookfor);

		if (soffset == 0)
		{
			return complete;
		}
		return complete.substring(0,soffset);
	}

	public static String after(String complete, String lookfor)
		throws Exception
	{
		int soffset = complete.indexOf(lookfor);

		if (soffset == 0)
		{
			return complete;
		}
		return complete.substring(soffset);
	}

	/**
	 * Turns a string into an array of strings.
	 * @param	str 	the source string to convert.
	 * @param	delimiters	the characters used as separators.
	 * @returns array of strings.
	 */
	public static String [] buildArgList (String str, String delimiters)
	{
		StringTokenizer 	st = new StringTokenizer (str, delimiters);
		int					count = st.countTokens ();
		String [] 			argv = new String [count];
		for (int i = 0; i < count; i++)
		{
			argv [i] = st.nextToken ();
		}
		return argv;
	}

	/**
	 * Creates an array list form a string using a StringTokenizer
	 * with its default delimiters.
	 * @param	String containing the list.
	 * @return ArrayList containing Strings for each parsed fields.
	 */
	public static ArrayList parseStringFields (String str)
	{
		StringTokenizer 	st = new StringTokenizer (str);
		ArrayList	list = new ArrayList ();
		while (st.hasMoreTokens ())
		{
			list.add (st.nextToken ());
		}
		return list;
	}
	
	/**
	 * Creates an array list form a string using a StringTokenizer
	 * with the provided delimiters.
	 * @param	String containing the list.
	 * @return ArrayList containing Strings for each parsed fields.
	 */
	public static ArrayList parseStringFields (String str, String delimiters)
	{
		StringTokenizer 	st = new StringTokenizer (str, delimiters);
		ArrayList	list = new ArrayList ();
		while (st.hasMoreTokens ())
		{
			list.add (st.nextToken ());
		}
		return list;
	}
	/**
	 * Extacts an array of strings from a comma separated list.
	 * The individual strings can be optionaly double quoted.
	 * All white space is preserved.
	 * Example: "Alan","Peter",Jack Willis,"BEN BALKE"
	 * @param	String containing the list.
	 * @return	An array of Strings containing the parsed fields.
	 */
	public static ArrayList parseQuotedStrings (String str)
	{
		return parseQuotedStrings (str, ',');
	}

		/**
	 * Extacts an array of strings from a comma separated list.
	 * The individual strings can be optionaly double quoted.
	 * All white space is preserved.
	 * Example: "Alan","Peter",Jack Willis,"BEN BALKE"
	 * @param	String containing the list.
	 * @return	An array of Strings containing the parsed fields.
	 */
	public static ArrayList parseQuotedStrings (String str, char delimiter)
	{
		ArrayList	list = new ArrayList ();
		boolean 	inQuote = false;
		int			start = -1;
		int			i;
		char		lastch = 0;
		char		ch;

		if (str.length () == 0)
		{
			return null;
		}
		for (i = 0; i < str.length (); i++)
		{
			ch = str.charAt (i);
			if (ch == '"')
			{
				if (inQuote)
				{
					if (start == -1)
						start = i;
					list.add (str.substring (start, i));
					inQuote = false;
					start = -1;
				}
				else
				{
					inQuote = true;
				}
			}
			else if (ch == delimiter)
			{
				if (!inQuote)
				{
					if (start == -1)
					{
						if (lastch == delimiter || i == 0)
							list.add ("");
					}
					else
					{
						list.add (str.substring (start, i));
					}
					start = -1;
				}
			}
			else
			{
				if (start == -1)
				{
					start = i;
				}
			}
			lastch = ch;
		}
		if (start != -1)
		{
			list.add (str.substring (start, i));
		}
		if (lastch == delimiter)
		{
			list.add ("");
		}
		return list;
	}

	/**
	 * Extacts an array of strings from a comma separated list.
	 * Quotes are ignored
	 * All white space is preserved.
	 * Example: Alan,Peter,Jack Willis,BEN BALKE
	 * @param	String containing the list.
	 * @param	char	identifies the delimiting character.
	 * @return	An array of Strings containing the parsed fields.
	 */
	public static ArrayList parseDelimitedStrings (String str, char delimiter)
	{
		ArrayList	list = new ArrayList ();
		int			start = -1;
		int			i;
		char		lastch = 0;
		char		ch;

		if (str.length () == 0)
		{
			return null;
		}
		for (i = 0; i < str.length (); i++)
		{
			ch = str.charAt (i);
			if (ch == delimiter)
			{
				if (start == -1)
				{
					if (lastch == delimiter || i == 0)
						list.add ("");
				}
				else
				{
					list.add (str.substring (start, i));
				}
				start = -1;
			}
			else
			{
				if (start == -1)
				{
					start = i;
				}
			}
			lastch = ch;
		}
		if (start != -1)
		{
			list.add (str.substring (start, i));
		}
		if (lastch == delimiter)
		{
			list.add ("");
		}
		return list;
	}
	
	
	
	
	/**
	 * Escapes all the occurances of delim in str by prepending the
	 * escape character.
	 * @param str	source string to escape.
	 * @param targetString character to search and escape.
	 * @param escape string to replace with.
	 */
	public static String escapeStringEx (String str, String targetString, String escape)
	{
		char		targetChar = 0;
		if (targetString.length () > 1 && targetString.charAt (0) == '\\')
		{
			switch (targetString.charAt (1))
			{
			case 'n':
				targetChar = '\n';
				break;
			case 't':
				targetChar = '\t';
				break;
			case 'b':
				targetChar = '\b';
				break;
			case 'r':
				targetChar = '\r';
				break;
			case 'f':
				targetChar = '\f';
				break;
			case '\\':
				break;
			default:
				int		octalnum = 0;
				int len = targetString.length () - 1;
				for (int i = 0; i < len; i++)
				{
					char	ch = targetString.charAt (1 + i);
					if (Character.isDigit (targetString.charAt (1 + i)))
					{
						octalnum *= 8;
						octalnum += ch - '0'; 
					}
					else
					{
						break;
					}
				}
				targetChar = (char) octalnum;
				break;
			}
		}
		else
		{
			targetChar = targetString.charAt (0);
		}
		return escapeString (str, targetChar, escape);
	}

	
	/**
	 * Escapes all the occurances of delim in str by prepending the
	 * escape character.
	 * @param str	source string to escape.
	 * @param targetchar character to search and escape.
	 * @param escape string to replace with.
	 */
	public static String escapeString (String str, char targetchar, String escape)
	{
		int		idx = 0;
		int		next;
		StringBuffer	dest = new StringBuffer ();
		while ((next = str.indexOf (targetchar, idx)) != -1)
		{
			dest.append (str.substring (idx, next) + escape);
			idx = next + 1;
		}
		dest.append (str.substring (idx));
		return dest.toString ();
	}

	/**
	 * Takes a newline separated list of <key>=<value> pairs and creates
	 * a Hashtable from them.
	 * @param	str	Source string to parse.
	 * @return Hashtable
	 */
	public static Hashtable parseProperties (String str, String delimiters)
	{
		Hashtable			ht = new Hashtable ();
		StringTokenizer 	st = new StringTokenizer (str, delimiters);
		int					count = st.countTokens ();
		String [] 			argv = new String [count];
		for (int i = 0; i < count; i++)
		{
			String 		s = st.nextToken ();
			int			idx;
			idx = s.indexOf ('=');
			if (idx != -1)
			{
				ht.put (s.substring (0, idx), s.substring (idx + 1));
			}
		}
		return ht;
	}
/*
 * Test program...
 */
	public static void main (String argv [])
	{
		List l;
		String t = "\"\",,,\"this is a test\",\"\",jack,ben ,jack,\"joe,brad\",";
		l = parseQuotedStrings (t);
		String [] s = StringUtils.toStringArray (l);

		System.out.println ("count: " + s.length);
		for (int i = 0; i < s.length; i++)
		{
			System.out.println ("String: " + s [i]);
		}

		System.out.println (StringParser.escapeString ("This''s is a test''", '\'', "\\'"));
		System.out.println (StringParser.escapeStringEx ("This \nis\ta test", "\\n", "<br>"));
		System.out.println (StringParser.parseProperties ("option1=value1,,key2=,key3=value3", ","));


	}
}
