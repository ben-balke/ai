/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/util/lang/StringUtils.java,v 1.2 2010/04/22 18:41:48 secwind Exp $
==================================================================================================*/
package com.duckdigit.util.lang;

import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Iterator;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

/**
 * This class provides generic string utilities.
 */
public class StringUtils
{
	private static String 	m_spaces = "                                   ";
	private static String 	m_zeros = "00000000000000000000000000000000000";

	public static String [] toStringArray (List list)
	{
		String	rslt [] = new String [list.size ()];
		Iterator	itor = list.iterator ();
		for (int s = 0; s < rslt.length; s++)
		{
			rslt [s] = (String) itor.next ();
		}
		return rslt;
	}

	/**
	 * Creates a string from number left padded with spaces.  The
	 * difference in the width and number width is filled with spaces.
	 * @param	number	integer number to format.
	 * @param	width	number of total characters.
	 * @returns A string containing the formated number.
	 */
	public static String spacePadDecLeft (int number, int width)
	{
		return spacePadTextLeft (Integer.toString (number), width);
	}

	/**
	 * Creates a string from number right padded with spaces.
	 * @param	number	integer number to format.
	 * @param	width	number of total characters.
	 * @returns A string containing the formated number.
	 */
	public static String spacePadDecRight (int number, int width)
	{
		return spacePadTextRight (Integer.toString (number), width);
	}

	/**
	 * Formats an integer number into hexidecimal left padded with
	 * the character '0'.
	 * @param	number	integer number to format.
	 * @param	width	number of total characters.
	 * @returns A string containing the formated number.
	 */
	public static String zeroPadHex (int number, int width)
	{
		return zeroPadTextLeft (Integer.toHexString (number), width);
	}

	/**
	 * Formats an integer number left padded with the character '0'.
	 */
	public static String zeroPadDecLeft (int number, int width)
	{
		return zeroPadTextLeft (Integer.toString (number), width);
	}

	/**
	 * Space pads a string from the left.
	 * @param	str		string to format.
	 * @param	width	number of total characters.
	 * @returns A string containing the formated text.
	 */
	public static String spacePadTextLeft (String str, int width)
	{
		if (str == null)
		{
			return createSpaceString (width);
		}
		if (width - str.length () < 1)
			return str;
		return (createSpaceString (width - str.length ()) + str);
	}

	/**
	 * Space pads a string on the right.
	 * @param	str		string to format.
	 * @param	width	number of total characters.
	 * @returns A string containing the formated text.
	 */
	public static String spacePadTextRight (String str, int width)
	{
		if (str == null)
		{
			return createSpaceString (width);
		}
		if (width - str.length () < 1)
			return str;
		return str + createSpaceString (width - str.length ());
	}

	/**
	 * Space pades a string with the center of a width.
	 * @param str		string to format.
	 * @param	width	total number of characters.
	 * @return	A string containing the formatted text.
	 */
	public static String spacePadTextCenter (String str, int width)
	{

		if (str == null)
		{
			return createSpaceString (width);
		}
		int		padlen = width - str.length ();
		if (padlen < 1)
			return str;
		int		padleft = padlen / 2;
		return createSpaceString (padleft) + str + createSpaceString (padlen - padleft);
	}


	/**
	 * Zero pads a string on the right.
	 * @param	str		string to format.
	 * @param	width	number of total characters.
	 * @returns A string containing the formated text.
	 */
	public static String zeroPadTextRight (String str, int width)
	{
		if (str == null)
		{
			return createZeroString (width);
		}
		if (width - str.length () < 1)
			return str;
		return str + createZeroString (width - str.length ());
	}

	/**
	 * Creates a string of length filled with a character.
	 * @param	width	length of string to create.
	 * @param	c		character to fill the string with.
	 * @return A string filled with the provided character.
	 */

	public static String createFilledString (int width, char c)
	{
		char			fill [] = new char [width];
		for (int i = 0; i < width; i++)
		{
			fill [i] = c;
		}
		return new String (fill, 0, width);
	}

	public static String createSpaceString (int width)
	{
		if (width > m_spaces.length ())
		{
			return createFilledString (width, ' ');
		}
		return m_spaces.substring (0, width);
	}

	public static String createZeroString (int width)
	{
		if (width > m_zeros.length ())
		{
			return createFilledString (width, '0');
		}
		return m_zeros.substring (0, width);
	}
	/**
	 * Zero pads a string on the left.
	 * @param	str		string to format.
	 * @param	width	number of total characters.
	 * @returns A string containing the formated text.
	 */
	public static String zeroPadTextLeft (String str, int width)
	{
		if (str == null)
		{
			return createZeroString (width);
		}
		if (width - str.length () < 1)
			return str;
		return (createZeroString (width - str.length ()) + str);
	}

	/**
		 * Determines if a byte is printable as a char
		 * @param	b	a byte.
		 * @returns	A boolean value.
	 */
	public static boolean isPrint (byte b)
	{
		if ( ( (int) b > 31) && ( (int) b < 126))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Creates a string that represents the provided data as
	 * a hex dump.
	 * @param	data	byte array containing the data to dump.
	 * @returns	A string containing the hex dump text.
	 */
	 public static String hexDumpToString (byte data [], int count)
		{
			int address = 0;
			StringBuffer sb;
			String s;
			StringBuffer tmp;

			sb = new StringBuffer ();
			tmp = new StringBuffer ();

			sb.append (zeroPadHex (address, 4));
			sb.append (": ");

			for(int i=0; i<count; i++)
			{
				sb.append (zeroPadHex (((int) data[i]) & 0xff, 2));
				sb.append (' ');
				address++;
				if (isPrint (data[i]))
				{
					tmp.append ((char) data[i]);
				}
				else
				{
					tmp.append ('.');
				}
				if (((address % 16) == 0) && (i + 1<count) && (address > 0))
				{
					sb.append (' ');
					sb.append (tmp);
					tmp.setLength (0);
					sb.append ('\n');
					sb.append (zeroPadHex (address, 4));
					sb.append (": ");
				}
			} // for i ...

			if ( (address % 16) > 0 )
			{
				for(int k=16; k--> (address % 16);)
				{
					sb.append ("   ");
				}
				sb.append (' ');
				sb.append (tmp);
			}
			else
			{
				sb.append (' ');
				sb.append (tmp);
			}
			s = new String (sb);
			return s;
	}

	/**
	 * Converts an exception's stack trace to a string.
	 * @param	e 	Exception to get the stack trace from.
	 * @return	String containing the stack trace.
	 */
	public static String stackTraceToString (Exception e)
	{
		ByteArrayOutputStream	bytes = new ByteArrayOutputStream ();
		PrintWriter writer = new PrintWriter (bytes, true);
		e.printStackTrace (writer);
		return bytes.toString ();
	}

	/**
	 * Converts an exception's stack trace to a string.
	 * @param	e 	Exception to get the stack trace from.
	 * @return	String containing the stack trace.
	 */
	public static String messageStackToString (Exception e)
	{
		StringBuffer        buf;
		Throwable           cex;
		Throwable           nex;

		buf = new StringBuffer();
		buf. append( e.getMessage() );
		cex = e;
		nex = cex. getCause();
		while ((null != nex) &&
			    (nex != cex))
		{
			cex = nex;
			buf. append( '\n' );
			buf. append( cex. getMessage() );
			nex = cex. getCause();
		}

		return buf. toString();
	}

	/** 
	 * Searches an exception and all its causes message strings
	 * for text.  Returns the matching message or null.
	 */
	public static String searchMessageStack (Exception e, String text)
	{
		Throwable           cex;
		cex = e;
		while (cex != null)
		{
			String			msg = cex.getMessage ();
			if (msg.indexOf (text) != -1)
			{
				return msg;
			}
			if (cex == cex.getCause ())
			{
				break;
			}
			cex = cex. getCause();
		}
		return null;
	}

	/**
	 * Generates a random string of characters whose values are
	 * between low and hi.
	 * @param len	length of string to create.
	 * @param low	the low character in the range.
	 */
	public static String makeRandomString (int len, char low, char hi)
	{
		StringBuffer		sb = new StringBuffer (len);
		Random	r = new Random ();
		char	c;
		int		n;
		int		range = hi + 1 - low;

		for (int i = 0; i < len; i++)
		{
			n = ((r.nextInt () & 0x0000ffff) % range);
			c = (char) (low + (char) n);

			//System.out.println (c + " " + n);
			sb.append (c);
		}
		return sb.toString ();
	}
	/**
	 * Trims any white space from the end of a string.  This
	 * should be used instead of String.trim().
	 * @param str	string to trim.
	 */
	public static String trimEnd (String str)
	{
		int			i = str.length () - 1;
		while (i >= 0)
		{
			if (str.charAt (i) > ' ')
			{
				if (i == (str.length () - 1))
				{
					return str;
				}
				return str.substring (0, i + 1);
			}
			i--;
		}
		return "";
	}

	public static String generateJavascriptString (String src)
	{
		if (src == null || src.length () == 0)
		{
			return "";
		}
		StringBuffer	    rslt = new StringBuffer ("'");
		int				     index = 0;

		for (int i = 0; i < src.length (); i++)
		{
			char	    ch = src.charAt (i);
			if (ch >= '\u1000')
			{
				rslt.append ("\\u").append (Integer.toHexString ((int)ch));
			}
			else if (ch >= '\u0100')
			{
				rslt.append ("\\u0").append (Integer.toHexString ((int)ch));
			}
			else if (ch >= 128)
			{
				rslt.append ("\\u00").append (Integer.toHexString ((int)ch));
			}
			else
			{
				switch (ch)
				{
					case '\r':
						//rslt.append ("\\r");
						break;
					case '\n':
						rslt.append ("\\n'+\n'");
						index++;
						break;
					case '\'': rslt.append ("\\047"); break;
					case '"': rslt.append ("\\042"); break;
					default:
						rslt.append (ch);
						break;
				}
			}
		}
		rslt.append ('\'');
		return rslt.toString ();
	}

	public static String generateHtmlAttributeString (String src)
	{
		StringBuffer	    rslt = new StringBuffer ();

		for (int i = 0; i < src.length (); i++)
		{
			char	    ch = src.charAt (i);
			if (ch >= 128)
			{
				rslt.append ("&#").append ((int)ch).append (';');
			}
			else
			{
				switch (ch)
				{
					case '"': rslt.append ("&#034;"); break;
					default:
						rslt.append (ch);
						break;
				}
			}
		}
		return rslt.toString ();
	}
	public static String generateHtmlEscapedString (String src)
	{
		StringBuffer	    rslt = new StringBuffer ();

		for (int i = 0; i < src.length (); i++)
		{
			char	    ch = src.charAt (i);
			if (ch >= 128)
			{
				rslt.append ("&#").append ((int)ch).append (';');
			}
			else
			{
				switch (ch)
				{
					case '&': rslt.append ("&amp;"); break;
					case '<': rslt.append ("&lt;"); break;
					case '>': rslt.append ("&gt;"); break;
					default:
						rslt.append (ch);
						break;
				}
			}
		}
		return rslt.toString ();
	}
	public static String generateHtmlNoteString (String src)
	{
		StringBuffer	    rslt = new StringBuffer ();

		for (int i = 0; i < src.length (); i++)
		{
			char	    ch = src.charAt (i);
			if (ch >= 128)
			{
				rslt.append ("&#").append ((int)ch).append (';');
			}
			else
			{
				switch (ch)
				{
					case '&': rslt.append ("&amp;"); break;
					case '<': rslt.append ("&lt;"); break;
					case '>': rslt.append ("&gt;"); break;
					case '\n': rslt.append ("<br>"); break;
					case '\r': break;
					default:
						rslt.append (ch);
						break;
				}
			}
		}
		return rslt.toString ();
	}
	public static String generateXMLEscapedString (String src)
	{
		StringBuffer	    rslt = new StringBuffer ();

		for (int i = 0; i < src.length (); i++)
		{
			char	    ch = src.charAt (i);
			if (ch >= 128)
			{
				rslt.append ("&#").append ((int)ch).append (';');
			}
			else
			{
				switch (ch)
				{
					case '&': rslt.append ("&amp;"); break;
					case '<': rslt.append ("&lt;"); break;
					case '"': rslt.append ("&quot;"); break;
					case '\'': rslt.append ("&apos;"); break;
					case '>': rslt.append ("&gt;"); break;
					case '\n': rslt.append ("&#10;"); break;
					case '\r': rslt.append ("&#13;"); break;
					default:
						rslt.append (ch);
						break;
				}
			}
		}
		return rslt.toString ();
	}


	public static String extractLine (String src, int line)
	{
		StringTokenizer		st = new StringTokenizer (src, "\n");
		for (int i = 0; st.hasMoreTokens(); i++)
		{
			String val = st.nextToken();
			if (i == line)
			{
				return val;
			}
		}
		return null;
	}

	public static String removeChars (String src, String toremove)
	{
		StringBuffer		dest = new StringBuffer ();
		int					len = src.length ();
		for (int i = 0; i < len; i++)
		{
			char		ch = src.charAt (i);
			if (toremove.indexOf (ch) == -1)
			{
				dest.append (ch);
			}
		}
		return dest.toString ();
	}
	public static String removeChar (String src, char toremove)
	{
		StringBuffer		dest = new StringBuffer ();
		int					len = src.length ();
		for (int i = 0; i < len; i++)
		{
			char		ch = src.charAt (i);
			if (ch != toremove)
			{
				dest.append (ch);
			}
		}
		return dest.toString ();
	}
	public static String terminateAtChar (String src, String termChars)
	{
		int				len = src.length ();
		int				termlen = termChars.length ();
		for (int i = 0; i < termlen; i++)
		{
			int idx = src.indexOf (termChars.charAt (i));
			if (idx != -1 && idx + 1 < len)
			{
				len = idx;
			}
		}
		return src.substring (0, len);
	}
	public static String parseEscapes (String src)
	{
		StringBuffer	sb = new StringBuffer ();
		boolean			inescape = false;
		for (int i = 0; i < src.length (); i++)
		{
			char		ch = src.charAt (i);
			if (inescape == false)
			{
				if (ch == '\\')
				{
					inescape = true;
				}
				else
				{
					sb.append (ch);
				}
			}
			else
			{
				switch (ch)
				{
				case '\\':
					ch = '\\';
					break;
				case 'n':
					ch = '\n';
					break;
				case 't':
					ch = '\t';
					break;
				case 'b':
					ch = '\b';
					break;
				case 'r':
					ch = '\r';
					break;
				case 'f':
					ch = '\f';
					break;
				}
				sb.append (ch);
				inescape = false;
			}
		}
		return sb.toString ();
	}
/*
 * Test program...
 */
	public static void main (String argv [])
	{
		System.out.println (makeRandomString (6, '0', '9'));
		System.out.println (StringUtils.spacePadTextCenter ("12345", 5) + "]");
		System.out.println (StringUtils.spacePadTextCenter ("1234", 5) + "]");
		System.out.println (StringUtils.spacePadTextCenter ("123", 5) + "]");
		System.out.println (StringUtils.spacePadTextCenter ("12", 5) + "]");
		System.out.println (StringUtils.spacePadTextCenter ("1", 5) + "]");
		System.out.println (StringUtils.spacePadTextCenter ("", 5) + "]");
		System.out.println (StringUtils.generateJavascriptString ("A\nBB\n\"CC\"\n\u00ae\u0325\u2984\n"));
		System.out.println( StringUtils.generateHtmlAttributeString(
					"A pig in a \"p\u00a0\u0200\u3000\"." ));
		System.out.println( StringUtils.generateHtmlEscapedString(
					"Bra&ket notation looks like \"<\u00a0|\u2000>\". " ));
		System.out.println( StringUtils.generateHtmlNoteString(
				"What a great\r\n'line'\r\nshe said.\nCan I show you my <&|&>?\n" +
				"Or better yet, \"<\u00a0|\u2000>\"."));
		System.out.println( StringUtils.generateXMLEscapedString(
				"What a great\r\n'line'\r\nshe said.\nCan I show you my <&|&>?\n" +
				"Or better yet, \"<\u00a0|\u2000>\"."));
	}
}
