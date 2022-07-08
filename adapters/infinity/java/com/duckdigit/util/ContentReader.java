/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/util/ContentReader.java,v 1.1 2010/10/19 00:43:59 secwind Exp $
==================================================================================================*/
package com.duckdigit.util;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;


/**
	<p>Title: ContentReader</p>
	<p>Description: This role delimits bookmarked sections of a content file
	for tag substitution. Sections are identified by the sequence
	"ZBK:[section tag]." After selecting the section by tag, the client provides
	the reader to a SubstitutionFilter}. The reader will
	present the characters following the tag line, and until the beginning
	of the next tag line, or the end of the file.</p>
	@version 1.0
 */
public class ContentReader extends Reader
{
	public static final String   ZML_BKMK_TAG        = "ZML:bookmark";

	private String              m_sFile;
	private String              m_sSection;

	private RandomAccessFile    m_File;
	private HashMap             m_secMap;
	private LinkedList          m_Ends;

	private long                m_lEnd;

    public ContentReader(
			String          p_sFile )
    {
		m_sFile = p_sFile;
		m_secMap = null;
    }

	private void openSource()
		throws IOException
	{
		m_File = new RandomAccessFile( m_sFile, "r" );
	}

	public boolean selectSection(
		    String          p_sSection )
		throws IOException
	{
		long        lLoc;
		int         iEnd;

		Iterator    itEnd;
		long        lEnd;

		m_sSection = p_sSection;
		lLoc = locateSection();

		if (-1 == lLoc) return false;

		m_File. seek( lLoc );
		itEnd = m_Ends. iterator();
		while (itEnd. hasNext())
		{
			lEnd = ((Long) itEnd. next()). longValue();
			if (lEnd >= lLoc)
			{
				m_lEnd = lEnd;
				break;
			}
		}

		return true;
	}

	private long locateSection()
		throws IOException
	{
		Long            loc;
		long            fLoc;
		int             iEnd;

		if (null == m_secMap) loadMap();

		loc = (Long) m_secMap.get( m_sSection );
		if (null == loc) return -1;

		return loc. longValue();
	}

	private void loadMap()
		throws IOException
	{
		long        nBy;
		long        lEnd;
		String      sIn;

		int         jStub;
		String      sSt;
		int         iSt;
		int         nSt;
		char        cSt;
		String      sTag;
		Long        loc;

		openSource();
		m_secMap = new HashMap();
		m_Ends = new LinkedList();

		nBy = m_File. length();
		while (nBy != m_File. getFilePointer())
		{
			lEnd = m_File. getFilePointer();

			sIn = m_File. readLine();
			jStub = sIn.indexOf( ZML_BKMK_TAG );
			if (-1 != jStub)
			{
				m_Ends. addLast( new Long( lEnd ) );
				jStub += ZML_BKMK_TAG. length();
				sSt = sIn. substring( jStub );
				sSt = sSt. trim();
				nSt = sSt. length();
				sTag = null;
				for (iSt=0; iSt<nSt; iSt++)
				{
					cSt = sSt.charAt( iSt );
					if (!Character. isJavaIdentifierPart( cSt ))
					{
						sTag = sSt. substring( 0, iSt );
						break;
					}
				}
				if (null == sTag) sTag = sSt;

				loc = new Long( m_File. getFilePointer() );
				m_secMap. put(
							sTag,
							loc	);
			}
		}

		m_Ends. add(
				    new Long( m_File. length() ) );
	}

    public int read(
		    char[]      p_cBuff,
			int         p_iSt,
			int         p_iLen )
		throws IOException
    {
		long        lFP;
		int         iToRd;
		byte        byBf[];
		int         nRd;

		int         iRd;
		int         iCh;

		if (null == m_File)
		{
			openSource();
			m_lEnd = m_File. length();
		}

		lFP = m_File. getFilePointer();
		if (lFP == m_lEnd) return -1;

		iToRd = Math.min( p_iLen, (int) (m_lEnd - lFP) );

		byBf = new byte[ iToRd ];
		nRd = m_File.read( byBf );

		iCh = p_iSt;
		for (iRd=0; iRd<nRd; iRd++)
		{
			p_cBuff[ iCh ] = (char) byBf[ iRd ];
			iCh++;
		}

		return nRd;
    }

	/**
		Do nothing, so that file can be re-used across calls to the
		SubstitutionFilter.
	 * @throws IOException
	 */
    public void close()
		throws IOException
    {
    }

    public void finalClose()
		throws IOException
    {
        m_File. close();
    }

	public static void main(
		    String      args[] )
	{
		ContentReader       cr;
		LineNumberReader    lnr;
		String              sIn;

		try {
			cr = new ContentReader( args[0] );
			cr. selectSection( "head" );
			lnr = new LineNumberReader( cr );
			do {
				sIn = lnr. readLine();
				System.out.println( sIn );
			} while (null != sIn);

			cr. selectSection( "content" );
			lnr = new LineNumberReader( cr );
			do {
				sIn = lnr. readLine();
				System.out.println( sIn );
			} while (null != sIn);

			cr. selectSection( "tail" );
			lnr = new LineNumberReader( cr );
			do {
				sIn = lnr. readLine();
				System.out.println( sIn );
			} while (null != sIn);
		}
		catch (IOException ioe) {
			System.out.println(
						    ioe. getMessage() );
		}
	}
}
