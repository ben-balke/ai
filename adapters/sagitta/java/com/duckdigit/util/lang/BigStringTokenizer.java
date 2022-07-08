/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/util/lang/BigStringTokenizer.java,v 1.2 2010/04/22 18:41:48 secwind Exp $
==================================================================================================*/
package com.duckdigit.util.lang;

import java.util.StringTokenizer;

public class BigStringTokenizer extends StringTokenizer
{
	public BigStringTokenizer (String s, String[] delims)
	{
		super (BigStringTokenizer.replace(s," ",delims)," ");
	}
	private static String replace(String str, String tok, String[] rep)
	{
		String retStr = "";
		for(int x = 0; x < rep.length; x++)
		{
			for(int i = 0, j = 0; (j = str.indexOf(rep[x],i)) > -1 ;
				i = j+rep[x].length())
			{
				retStr += (str.substring(i,j) + tok);
			}
			str = (str.indexOf(rep[x]) == -1) ? str :
			retStr + str.substring (str.lastIndexOf (rep [x]) +
				rep[x].length(),str.length());
			retStr = (x == (rep.length - 1)) ? str : "";
		}
		return retStr;
	}
}
