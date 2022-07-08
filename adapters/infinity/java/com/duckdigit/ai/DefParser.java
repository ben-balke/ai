/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/ai/DefParser.java,v 1.1 2010/10/19 00:42:54 secwind Exp $
==================================================================================================*/
package com.duckdigit.ai;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

public abstract class DefParser
	implements SourceDefiner
{
	/* Def file. */
	private String          m_sFile;
	/* Def class. */
	private String          m_sClass;

	/* Variable names. */
	private ArrayList       m_names;
	/* Source field declarations. */
	private ArrayList       m_SrcDecls;
	/* Sql field declarations. */
	private ArrayList       m_SQLDecls;
	/* Other field information declarations specialized by extending class. */
	private ArrayList       m_OtherDecls;
	/* JDBC type codes. */
	private int             m_iSQLTypes[];
	/* SQL field lengths. */
	private int             m_iSQLLengths[];

	/* Key list. */
	private ArrayList       m_Keys;
	private int             m_iKey;

	public DefParser(
			String      p_sFile )
	{
		m_sFile = p_sFile;
		m_sClass = p_sFile;
			// May be over-ridden by SOURCE field in parseSet.

		m_names = new ArrayList();
		m_SrcDecls = new ArrayList();
		m_SQLDecls = new ArrayList();
		m_OtherDecls = new ArrayList();
	}

	public abstract String getDefFilePath();

	public String parseName (String name, String src)
	{
		return name;
	}

	public void parseDefFile()
		throws IOException
	{
        FileReader      rf;
		BufferedReader  fr;
		String          sLine;
		StringTokenizer toks;
		String          sTok;
		boolean         bParse;
		String			sName;
		String			sSrc;

        fr = null;
        rf = new FileReader(
							getDefFilePath() + m_sFile + ".def" );
        try {
          fr = new BufferedReader( rf );

          while (fr. ready())
          {
              sLine = fr. readLine();
              if (null == sLine) break;
			  if ((0 == sLine. length()) ||
				    ('#' == sLine. charAt( 0 ))) continue;

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
                  else 
				  {
				  	int		remain = toks.countTokens ();

				  	if (remain >= 2)
                  	{
			  		  sName = sTok;
					  sSrc = toks. nextToken();
                      m_names. add (parseName (sName, sSrc));
                      m_SrcDecls. add (sSrc);
                      m_SQLDecls. add( toks. nextToken() );
                  	  if (remain >= 3)
					  {
					  	String 		otherDecls = "";
						while (toks.hasMoreTokens ())
						{
							otherDecls = otherDecls + toks. nextToken();
						}
                      	m_OtherDecls. add (otherDecls);
					  }
					  else
					  {
                      	m_OtherDecls. add(null);
					  }
                      bParse = true;
					}
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
	}

	/*
		Interprets SET directives in the .def file.
	 */
	public boolean parseSet(
			StringTokenizer     p_toks )
	{
		boolean         bParse;
		String          sTok;

		int             iKey;
		String          sKey;

		bParse = false;

		sTok = p_toks. nextToken();
		if (0 == "SOURCE".compareToIgnoreCase( sTok ))
		{
			if (1 == p_toks. countTokens())
			{
				m_sClass = p_toks. nextToken();
				bParse = true;
			}
		}
		else if ((0 == "KEY".compareToIgnoreCase( sTok )) &&
					// Must come after name list.
			     (0 != m_names. size()))
		{
			m_Keys = new ArrayList(
						    p_toks. countTokens() );
			while (p_toks.hasMoreTokens())
			{
				sTok = p_toks. nextToken();
				for (iKey = 0; iKey < m_Keys. size(); iKey++)
				{
					sKey = (String) m_Keys. get( iKey );
					if (m_names. indexOf( sKey ) >  m_names.indexOf( sTok ))
					{
						m_Keys.add( iKey, sTok );
						bParse = true;
						break;
					}
				}

				if (!bParse && m_names.contains( sTok))
				{
					m_Keys. add( sTok );
					bParse = true;
				}

				// bParse = false means we could not place the key field.
			}
		}
		else
		{
			bParse = parseSet( sTok, p_toks );
		}

		return bParse;
	}

	public abstract boolean parseSet(
		    String          p_sKey,
			StringTokenizer p_toks );

	/*
		The current .def file format supports conversion of data to
		JDBC CHAR, DATE, DOUBLE, INTEGER and VARCHAR fields. Other types will
		be translated as OBJECT.
	 */
	private void interpretSQLTypes()
	{
		int         iV;
		int         nVs;
		String      sDecl;
		String      sType;
		int         iParen;

		nVs = m_SQLDecls. size();
		m_iSQLTypes = new int[ nVs ];
		m_iSQLLengths = new int[ nVs ];
		for (iV=0; iV<nVs; iV++)
		{
			sDecl = (String) m_SQLDecls.get( iV );
			iParen = sDecl.indexOf( '(' );
			if (-1 != iParen)
			{
				sType = sDecl.substring( 0, iParen );
			}
			else
			{
				sType = sDecl;
			}

			if (0 == "varchar".compareToIgnoreCase( sType ))
			{
				m_iSQLTypes[ iV ] = Types.VARCHAR;
				m_iSQLLengths[ iV ] = parseLength( sDecl );
			}
			else if (0 == "char".compareToIgnoreCase( sType ))
			{
				m_iSQLTypes[ iV ] = Types.CHAR;
				m_iSQLLengths[ iV ] = parseLength( sDecl );
			}
			else if (0 == "int".compareToIgnoreCase( sType ))
			{
				m_iSQLTypes[ iV ] = Types.INTEGER;
				m_iSQLLengths[ iV ] = 4;
			}
			else if (0 == "date".compareToIgnoreCase( sType ))
			{
				m_iSQLTypes[ iV ] = Types.DATE;
				m_iSQLLengths[ iV ] = 8;
			}
			else if (0 == "float".compareToIgnoreCase( sType ))
			{
				m_iSQLTypes[ iV ] = Types.DOUBLE;
				m_iSQLLengths[ iV ] = 4;
			}
			else
			{
				m_iSQLTypes[ iV ] = Types.OTHER;
				m_iSQLLengths[ iV ] = -1;
			}
		}
	}

	private int parseLength(
		    String      sType )
	{
		int         iLP;
		int         iRP;

		iLP = sType.indexOf( '(' );
		iRP = sType.indexOf( ')' );
		if ((-1 == iLP) || (-1 == iRP))
		{
			return 1;
		}
		return Integer. decode(
					sType.substring( iLP + 1, iRP ) ). intValue();
	}

	public String getClassString()
	{
		return m_sClass;
	}

	public int getVarCount()
	{
		return m_iSQLTypes. length;
	}

	public int[] getVarTypes()
	{
		return m_iSQLTypes;
	}

	public int[] getVarLengths()
	{
		return m_iSQLLengths;
	}

	public ArrayList getVarDecls()
	{
		return m_SrcDecls;
	}

	public ArrayList getOtherDecls()
	{
		return m_OtherDecls;
	}

	public ArrayList getVarNames()
	{
		return m_names;
	}

	/*==========================================================================
	===== Update operations.
	==========================================================================*/

	public boolean checkUpdate()
	{
		return null != m_Keys;
	}

	public ArrayList getKeyNames()
	{
		return m_Keys;
	}

	public boolean checkKey(
		    String          p_sField )
	{
		Iterator            iterFld;
		String              sFld;

		if (!checkUpdate()) return false;

/*		iterFld = m_names. iterator();
		m_iKey = 0;
		while (iterFld. hasNext())
		{
			sFld = (String) iterFld. next();
			if (0 == sFld. compareToIgnoreCase( p_sField ))
			{
				return true;
			}
			m_iKey ++;
		}*/

		return m_Keys.contains( p_sField );
	}

	public int getUpdateIndex(
		    String      p_sField )
	{
		int         iFld;
		int         iKey;
		int         iIdx;

		if (checkKey( p_sField ))
		{
			return m_names. size()
				   - m_Keys. size()
				   + m_Keys.indexOf( p_sField )
				   + 1;
		}

		iFld = m_names.indexOf( p_sField ) + 1;
		iIdx = iFld;
		iKey = 0;
		for (iKey=0; iKey<m_Keys.size(); iKey++)
		{
			if (iFld > m_names.indexOf(
								m_Keys.get( iKey ) ))
			{
				iIdx--;
			}
		}

		return iIdx;
	}
}
