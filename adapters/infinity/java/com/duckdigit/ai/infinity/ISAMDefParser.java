package com.duckdigit.ai.infinity;

import java.util.ArrayList;
import java.util.StringTokenizer;
import com.duckdigit.ai.*;

public class ISAMDefParser
	extends DefParser
	implements ISAMDefiner
{
	/* Path to def files. */
	private static final String     s_sDefPath = "/home/ai/adapters/infinity/def/";

	/* File descriptions. */
	private String          m_sFile;
	private String          m_sLayout;
	private String          m_sIndex;
	private String          m_sKeys;

	/* Selection criteria. */
	private String          m_sFilter;
	private String          m_sRange;
	private ArrayList       m_Preset;
	private ArrayList       m_Advset;

	/* Transfer limit. */
	private int             m_iCacheSize = 200;

    public ISAMDefParser(
			String      p_sTable )
	{
		super( p_sTable );
    }

    public String getDefFilePath()
	{
		return s_sDefPath;
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
		if (0 == "RANDOM".compareToIgnoreCase( p_sKey ))
		{
			if (2 == p_toks.countTokens())
			{
				bParse = true;
				m_sFile = p_toks.nextToken();
				m_sLayout = p_toks.nextToken();
			}
		}
		else if (0 == "INDEX".compareToIgnoreCase( p_sKey ))
		{
			if (2 == p_toks. countTokens())
			{
				bParse = true;
				m_sIndex = p_toks. nextToken();
				m_sKeys = p_toks. nextToken();
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
		else if (0 == "RANGE".compareToIgnoreCase( p_sKey ))
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
			m_sRange = new String( selBuff );
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
		else if (0 == "PRESELECT".compareToIgnoreCase( p_sKey ))
		{
			m_Preset = new ArrayList();
			while (0 != p_toks. countTokens())
			{
				m_Preset. add( p_toks. nextToken() );
			}
			bParse = true;
		}
		else if (0 == "POSTRECORD".compareToIgnoreCase( p_sKey ))
		{
			m_Advset = new ArrayList();
			while (0 != p_toks. countTokens())
			{
				m_Advset. add( p_toks. nextToken() );
			}
			bParse = true;
		}

		return bParse;
    }

	public String getFile()
	{
		return m_sFile;
	}

	public String getLayout()
	{
		return m_sLayout;
	}

	public String getIndexFile()
	{
		return m_sIndex;
	}

	public String getIndexLayout()
	{
		return m_sKeys;
	}

	public String getFilterTemplate()
	{
		return m_sFilter;
	}

	public String getRangeTemplate()
	{
		return m_sRange;
	}

	public ArrayList getPresets()
	{
		return m_Preset;
    }

	public ArrayList getAdvsets()
	{
		return m_Advset;
    }

	public int getCacheSize()
	{
		return m_iCacheSize;
    }
}
