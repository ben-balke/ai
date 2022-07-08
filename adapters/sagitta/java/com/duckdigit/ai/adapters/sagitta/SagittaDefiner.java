/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/ai/adapters/sagitta/SagittaDefiner.java,v 1.2 2010/04/22 18:41:39 secwind Exp $
==================================================================================================*/
package com.duckdigit.ai.adapters.sagitta;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class SagittaDefiner
	extends DefParser
	implements PickDefiner
{
	/* Path to def files. */
	private static final String     s_sDefPath = "/home/ai/adapters/sagitta/defs/";

	/* File descriptions. */
	private String          m_sFile;

	/* Selection criteria. */
	private String          m_sFilter;

	/* Transfer limit. */
	private int             m_iCacheSize = 200;

	private int             m_iMaxRecordSize = 1024;

    public SagittaDefiner (
			String      p_sTable )
	{
		super( p_sTable );
    }

    public String getDefFilePath()
	{
		return s_sDefPath;
    }
	public String parseName (String sName, String sSrc)
	{
		if (sName.equals ("BLANK"))
		{
			return sName + sSrc;
		}
		return sName;
	}

    public boolean parseSet(
		    String          p_sKey,
			StringTokenizer p_toks )
	{
		boolean             bParse;
		String              sTok;
		int                 nCache;
		StringBuffer        selBuff;

		bParse = false;
		if (0 == "FILE".compareToIgnoreCase( p_sKey ))
		{
			if (1 == p_toks.countTokens())
			{
				bParse = true;
				m_sFile = p_toks.nextToken();
			}
		}
		else if (0 == "FILTER".compareToIgnoreCase( p_sKey ))
		{
			bParse = true;
			selBuff = new StringBuffer( 512 );
			while (0 != p_toks. countTokens())
			{
				selBuff. append( p_toks. nextToken() );
				if (0 != p_toks. countTokens())
				{
					selBuff. append( " " );
				}
			}
			m_sFilter = new String( selBuff );
		}
		else if (0 == "SETSIZE".compareToIgnoreCase( p_sKey ))
		{
			if (1 == p_toks. countTokens())
			{
				try {
					m_iCacheSize = new Integer(
											p_toks. nextToken() ).
										intValue();
					bParse = true;
				}
				catch (Exception e) {}
			}
		}
		else if (0 == "MAXRECORD".compareToIgnoreCase( p_sKey ))
		{
			if (1 == p_toks. countTokens())
			{
				try {
					m_iMaxRecordSize = new Integer(
											p_toks. nextToken() ).
										intValue();
					bParse = true;
				}
				catch (Exception e) {}
			}
		}
		return bParse;
    }

	public String getFile()
	{
		return m_sFile;
	}

	public String getFilterTemplate()
	{
		return m_sFilter;
	}

	public int getCacheSize()
	{
		return m_iCacheSize;
    }

	public int getMaxRecordSize()
	{
		return m_iMaxRecordSize;
	}
}
