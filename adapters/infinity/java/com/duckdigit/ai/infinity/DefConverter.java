/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/ai/infinity/DefConverter.java,v 1.1 2010/10/19 00:43:14 secwind Exp $
==================================================================================================*/
package com.duckdigit.ai.infinity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import com.duckdigit.ai.*;

public class DefConverter extends ISAMConverter
{
	/* Path to def files. */
	private static final String     s_sDefPath = "/home/ai/adapters/infinity/def/";
	private String          m_sClass;

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

	/* Variable names. */
	private ArrayList       m_names;
	/* ISAM variable declarations. */
	private ArrayList       m_ISAMDecls;
	/* Sql field declarations. */
	private ArrayList       m_SQLDecls;
	/* JDBC type codes. */
	private int             m_iSQLTypes[];

	/* Insert query. */
	private String          m_sQuery;

	public DefConverter(
			String      p_sTable )
	{
		m_sClass = p_sTable;

		m_names = new ArrayList();
		m_ISAMDecls = new ArrayList();
		m_SQLDecls = new ArrayList();

		m_Preset = null;
		m_Advset = null;
	}

	private void parseDefFile()
		throws IOException
	{
        FileReader      rf;
		BufferedReader  fr;
		String          sLine;
		StringTokenizer toks;
		String          sTok;
		boolean         bParse;

        fr = null;
        rf = new FileReader(
							s_sDefPath + m_sClass + ".def" );
        try {
          fr = new BufferedReader( rf );

          while (fr. ready())
          {
              sLine = fr. readLine();
              if (null == sLine) break;

              toks = new StringTokenizer( sLine );

              bParse = false;
              if (1 < toks.countTokens())
              {
                  sTok = toks. nextToken();
                  if (0 == "SET".compareToIgnoreCase ( sTok ))
                  {
                      bParse = parseSet( toks );
                  }
                  else if (0 == "LINK".compareToIgnoreCase ( sTok ))
                  {
                      bParse = true;
                  }
                  else if (2 == toks.countTokens())
                  {
                      m_names. add( sTok );
                      m_ISAMDecls. add( toks. nextToken() );
                      m_SQLDecls. add( toks. nextToken() );
                      bParse = true;
                  }
              }

              if (!bParse)
              {
                  if (0 != sLine.length())
                  {
                      System.out.println(
                          "Def file line error: " + sLine + "\n" );
                  }
              }
          }
        }
        finally {
          if (null != fr) fr. close();
          rf.close();
        }

        interpretSQLTypes();
        buildInsertQuery();
	}

	/*
		Interprets RANDOM, INDEX, FILTER, RANGE, SETSIZE, PRESELECT and
		POSTRECORD directives in the .def file.
	 */
	private boolean parseSet(
			StringTokenizer     p_toks )
	{
		boolean             bParse;
		String              sTok;
		int                 nCache;
		StringBuffer        selBuff;

		bParse = false;
		sTok = p_toks. nextToken();
		if (0 == "RANDOM".compareToIgnoreCase( sTok ))
		{
			if (2 == p_toks.countTokens())
			{
				bParse = true;
				m_sFile = p_toks.nextToken();
				m_sLayout = p_toks.nextToken();
			}
		}
		else if (0 == "INDEX".compareToIgnoreCase( sTok ))
		{
			if (2 == p_toks. countTokens())
			{
				bParse = true;
				m_sIndex = p_toks. nextToken();
				m_sKeys = p_toks. nextToken();
			}
		}
		else if (0 == "FILTER".compareToIgnoreCase( sTok ))
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
		else if (0 == "RANGE".compareToIgnoreCase( sTok ))
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
		else if (0 == "SETSIZE".compareToIgnoreCase( sTok ))
		{
			if (1 == p_toks. countTokens())
			{
				try {
					nCache = new Integer(
											p_toks. nextToken() ).
										intValue();
					setCacheSize( nCache );
					bParse = true;
				}
				catch (Exception e) {}
			}
		}
		else if (0 == "PRESELECT".compareToIgnoreCase( sTok ))
		{
			m_Preset = new ArrayList();
			while (0 != p_toks. countTokens())
			{
				m_Preset. add( p_toks. nextToken() );
			}
			bParse = true;
		}
		else if (0 == "POSTRECORD".compareToIgnoreCase( sTok ))
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

	/*
		The current .def file format supports conversion of BASIX data types to
		JDBC CHAR, DATE, DOUBLE, INTEGER and VARCHAR fields. Other types will
		be translated as OBJECT, which will eventually result in a NULL value
		assignment in the insert transaction.
	 */
	private void interpretSQLTypes()
	{
		int         iV;
		int         nVs;
		String      sType;
		int         iParen;

		nVs = m_SQLDecls. size();
		m_iSQLTypes = new int[ nVs ];
		for (iV=0; iV<nVs; iV++)
		{
			sType = (String) m_SQLDecls.get( iV );
			iParen = sType.indexOf( '(' );
			if (-1 != iParen)
			{
				sType = sType.substring( 0, iParen );
			}

			if (0 == "varchar".compareToIgnoreCase( sType ))
			{
				m_iSQLTypes[ iV ] = Types.VARCHAR;
			}
			else if (0 == "char".compareToIgnoreCase( sType ))
			{
				m_iSQLTypes[ iV ] = Types.CHAR;
			}
			else if (0 == "int".compareToIgnoreCase( sType ))
			{
				m_iSQLTypes[ iV ] = Types.INTEGER;
			}
			else if (0 == "date".compareToIgnoreCase( sType ))
			{
				m_iSQLTypes[ iV ] = Types.DATE;
			}
			else if (0 == "float".compareToIgnoreCase( sType ))
			{
				m_iSQLTypes[ iV ] = Types.DOUBLE;
			}
			else
			{
				m_iSQLTypes[ iV ] = Types.OTHER;
			}
		}
	}

	private void buildInsertQuery()
	{
		StringBuffer        insBuf;
		Iterator            itName;
		boolean             bFrst;

		int                 iQ;
		int                 nQs;

		insBuf = new StringBuffer( 1024 );

		insBuf. append( "INSERT INTO " );
		insBuf. append( m_sClass );
		insBuf. append( " (" );

		bFrst = true;
		itName = m_names. iterator();
		while (itName. hasNext())
		{
			if (!bFrst)
			{
				insBuf. append( "," );
			}
			else
			{
				bFrst = false;
			}
			insBuf. append(
						(String) itName. next() );
		}

		insBuf. append( ") VALUES (" );

		bFrst  = true;
		nQs = m_names. size();
		for (iQ=0; iQ<nQs; iQ++)
		{
			if (!bFrst)
			{
				insBuf. append( ",?" );
			}
			else
			{
				insBuf. append( "?" );
				bFrst = false;
			}
		}

		insBuf. append( ")" );

		m_sQuery = new String( insBuf );
	}

	public String getClassString()
	{
		return m_sClass;
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

	public int[] getVarTypes()
	{
		return m_iSQLTypes;
	}

	public String getFilterTemplate()
	{
		return m_sFilter;
	}

	public String getRangeTemplate()
	{
		return m_sRange;
	}

	public ArrayList getVarDecls()
	{
		return m_ISAMDecls;
	}

	public ArrayList getVarNames()
	{
		return m_names;
	}

	public String getInsertQuery()
	{
		return m_sQuery;
	}

	public void initializeIndex()
		throws Exception
	{
		Iterator        itSet;
		String          sAsgn;
		int             iSep;

		if (null == m_Preset) return;

		itSet = m_Preset. iterator();
		while (itSet.hasNext())
		{
			sAsgn = (String) itSet. next();
			iSep = sAsgn. indexOf( '=' );
			if (-1 != iSep)
			{
				setVariable(
					sAsgn. substring( 0, iSep ),
					sAsgn. substring( iSep + 1 ) );
			}
		}
	}

	public boolean advanceIndex()
	{
		Iterator        itSet;
		String          sAsgn;
		int             iSep;

		if ((null == m_Advset) ||
			(0 == m_Advset. size())) return false;

		itSet = m_Advset. iterator();
		while (itSet.hasNext())
		{
			sAsgn = (String) itSet. next();
			iSep = sAsgn. indexOf( '=' );
			if (-1 != iSep)
			{
				try {
					setVariable(
						sAsgn. substring( 0, iSep ),
						sAsgn. substring( iSep + 1 ) );
				}
				catch (Exception ex) {}
			}
		}

		return true;
	}

	public static void main(String[] args)
	{
		DefConverter cnvrtr = new DefConverter( args[0] );

		try {
			cnvrtr. prepareArgs( args );
			cnvrtr. prepareLog();

			cnvrtr. parseDefFile();

			cnvrtr. convert();
		}
		catch (Exception ex) {
			System.out.println( ex. getMessage() );
			ex. printStackTrace();
		}
	}
}
