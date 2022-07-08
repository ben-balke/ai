/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/util/SubstitutionFilter.java,v 1.1 2010/10/19 00:43:59 secwind Exp $
==================================================================================================*/
package com.duckdigit.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

public class SubstitutionFilter
{
	private TagParser           m_parser;
	private static final String LINE_SEP = "\n";

		// Used to locate insertion files.
	PathResolver                        m_locator;

	private HashMap             m_Sections;
	private Reader              m_source;
	private LineNumberReader    m_LineSrc;

	private StringBuffer        m_buffer;
	private PrintWriter         m_target;

		/**
		 * The following two strings identify the beginning and end
		 * of a group of lines that are combined and parsed as a single
		 * line.  This allows SkipLineExceptions to exclude multiple lines
		 * of HTML with having the HTML designer to join lines.  BBB Eventually this
		 * sould be done by a pre-process when loading the html files.
		 */
	public static String	GROUP_BEGIN = "<!--ZML:begin-->";
	public static String	FORM_BEGIN = "<!--ZML:begin";
	public static String	INSERT_BEGIN = "<!--ZML:insert";
	public static String    REF_END = "-->";
	public static String	GROUP_END = "<!--ZML:end-->";


	private boolean         m_bGrouped;
	private boolean         m_bPreProc;
	private boolean         m_bNoParse;
	private boolean         m_bSkipGroup;

	private String          m_sPreText;
	private String          m_sPostText;

    public SubstitutionFilter(
		    TagParser       p_parser,
			PathResolver    p_locator )
	{
		this( p_parser, p_locator, null );
	}

    public SubstitutionFilter(
		    TagParser       p_parser,
			PathResolver    p_locator,
			HashMap         p_Sections )
	{
		m_parser = p_parser;
		m_locator = p_locator;
		if (null == p_Sections)
		{
			m_Sections = new HashMap();
		}
		else
		{
			m_Sections = p_Sections;
		}

		m_bGrouped = false;
		m_bPreProc = false;
		m_bNoParse = false;
    }

	public void setPreprocessing()
	{
		m_bPreProc = true;
	}

	/*==========================================================================
	==== Source and destination configuration.
	==========================================================================*/

	/**
	    Select a file as the substitution input.
		@param String   Comma-delimited list of files.
	 */
	public void prepareFileSource(
		    String      p_sFileList )
		throws IOException
	{
		int                 iBkmk;
		String              sFile;
		String              sBkmk;
		ContentReader       cr;

		File            f;
		String			filePath;

		iBkmk = p_sFileList. indexOf( '#' );
		if (-1 == iBkmk)
		{
			sFile = p_sFileList;
			sBkmk = null;
		}
		else
		{
			sFile = p_sFileList. substring( 0, iBkmk );
			sBkmk = p_sFileList. substring( iBkmk + 1 );
		}

		m_source = null;
		sFile = m_locator. locateFile( sFile );
		if (null != sFile)
		{
			if (null == sBkmk)
			{
				f = new File( sFile );
				if (f. exists())
				{
					defineSource( new FileReader( f ) );
				}
			}
			else
			{
				try {
					cr = getSection(
								sFile,
								sBkmk );
					if (null != cr)
					{
						defineSource( cr );
					}
				}
				catch (Exception ex) {}
			}
		}

		if (null == m_source)
		{
			throw new IOException( "No file found from " + p_sFileList );
		}
	}

	/*
		Prepare a ContentReader to begin processing input at the indicated
		section mark.
	 */
	private ContentReader getSection(
		    String      p_sFile,
			String      p_sSec )
		throws Exception
	{
		String          sFile;
		ContentReader   cr;

		if (null != m_locator)
		{
			sFile = m_locator. locateFile( p_sFile );
		}
		else
		{
			sFile = p_sFile;
		}

		cr = (ContentReader) m_Sections. get( sFile );
		if (null == cr)
		{
			cr = new ContentReader( sFile );
			m_Sections. put( sFile, cr );
		}

		if (cr. selectSection(
					m_parser. parseSimple( p_sSec ) ))
		{
			return cr;
		}

		return null;
	}

	public void clear()
	{
		Iterator        itCRs;
		ContentReader   cr;

		itCRs = m_Sections. values(). iterator();
		while (itCRs.hasNext())
		{
			cr = (ContentReader) itCRs. next();
			try {
				cr. finalClose();
			}
			catch (Exception ex) {}
		}
		m_Sections. clear();
		m_Sections = null;
	}

	/**
		Define the reader for the substitution transfer.
		@param Reader           Substitution input.
	 */
	public void defineSource(
		    Reader          p_source )
	{
		m_source = p_source;

		m_LineSrc = new LineNumberReader( m_source );
	}

	/**
		Wrap a stream as the output target for the filter.
		@param OutputStream     Filter target.
	 */
	public void selectStream(
		    OutputStream     p_target )
		throws IOException
	{
		selectTarget( new PrintWriter( p_target, true ) );
	}

	/**
		Select a PrintWriter as the filter target.
		@param PrintWriter      Filter target.
	 */
	public void selectTarget(
		    PrintWriter     p_target )
	{
		m_target = p_target;
		m_buffer = null;
	}

	/**
		Select a StringBuffer as the filter target.
	 */
	public void selectBuffer(
		    StringBuffer     p_buffer )
	{
		m_buffer = p_buffer;
		m_target = null;
	}

/*==============================================================================
===== Translate source to produce target.
==============================================================================*/


	public void parseStream()
		throws Exception
	{
		boolean                 bOutLast = true;
		int						eidx;
		int                     fidx;
		int						gidx;
		int                     iidx;
		int                     ridx;
		String					subform = null;
		String                  subFile = null;
		String					line = null;

		/* Recursive processor for groups and includes. */
		SubstitutionFilter      subFilter;

		// Maintain grouping if we are in pre-processing mode.
		if (m_bGrouped && m_bPreProc)
		{
			postLine( GROUP_BEGIN );
		}

		line = m_sPreText;
		if (null == line)
		{
			line = m_LineSrc. readLine();
		}

		// If content begins with a GROUP, suppress final return.
		if (line != null && line. indexOf( GROUP_BEGIN ) == 0)
		{
			bOutLast = false;
		}

		/**
		 * BBB Someday we will need to support embedded groups.
		 */
		while (null != line)
		{
			ridx = 0;       // For compiler.
			eidx = getPositionOf( line, GROUP_END );
			gidx = getPositionOf( line, GROUP_BEGIN );
			fidx = getPositionOf( line, FORM_BEGIN );
			if (checkInside( line, fidx) && (fidx == gidx))
			{
				fidx = getPositionOf( line. substring( gidx + 1), FORM_BEGIN );
			}
			if (checkInside( line, fidx ))
			{
				ridx = line. substring(fidx). indexOf( REF_END ) + fidx;
			}
			iidx = getPositionOf( line, INSERT_BEGIN );
			if (iidx < fidx )
			{
				ridx = line. substring(iidx). indexOf( REF_END ) + iidx;
			}
			if (m_bGrouped
				    && checkInside( line, eidx )
					&& (eidx < fidx)
					&& (eidx < gidx)
					&& (eidx < iidx))
			{
				/*==============================================================
				===== Group termination.
				==============================================================*/

				if (eidx + GROUP_END.length () < line.length ())
				{
					m_sPostText = line.substring (eidx + GROUP_END.length ());
				}
				if (eidx != 0)
				{
					try {
	    				postTaggedLine( line. substring( 0, eidx ) );
	    			} catch (SkipLineException sle) {
	    				line = null;
	    			}
				}
				// If pre-processing, preserve the grouping.
				if (m_bPreProc)
				{
					postLine( GROUP_END );
				}
				return;
			}
			else if (checkInside( line, gidx)
						&& (gidx < fidx)
						&& (gidx < iidx))
			{
				/*==============================================================
				===== Group initation.
				==============================================================*/
				if (gidx != 0)
				{
					try {
						postTaggedLine( line. substring( 0, gidx ) );
					} catch (SkipLineException sle) {
						line = null;
					}
				}

				if (line != null)
				{
					gidx += GROUP_BEGIN.length ();
					subFilter = new SubstitutionFilter(
												m_parser,
												m_locator,
												m_Sections );
					subFilter. m_LineSrc  =     this. m_LineSrc;
					subFilter. selectBuffer(    new StringBuffer() );

					subFilter. m_sPreText =     line. substring( gidx );
					subFilter. m_bGrouped =     true;
					subFilter. m_bPreProc =     this. m_bPreProc;
					subFilter. m_bNoParse =     this. m_bNoParse;

					subFilter. parseStream();

					if (!subFilter. m_bSkipGroup)
					{
						postLine( subFilter. m_buffer. toString() );
					}
					line = subFilter. m_sPostText;
					if (!checkContent( line ) && bOutLast)
					{
						postLine( LINE_SEP );
					}
				}
			}
			else if (checkInside( line, fidx ) && (ridx > fidx)
						&& (fidx < iidx))
			{
				/*==============================================================
				===== Embedded Subforms.
				==============================================================*/
				if (fidx != 0)
				{
					try {
						postTaggedLine( line. substring( 0, fidx ) );
					} catch (SkipLineException sle) {
						line = null;
					}
				}

				if (null != line)
				{
					subform = line. substring(
										fidx + FORM_BEGIN.length(),
										ridx ). trim();

					subFilter = new SubstitutionFilter(
												m_parser,
												m_locator,
												m_Sections );
					subFilter. m_sPreText =
								line. substring( ridx + REF_END.length() );
					subFilter. m_LineSrc  =     this. m_LineSrc;
					subFilter. selectBuffer( new StringBuffer() );

					subFilter. m_bGrouped =     true;
					subFilter. m_bPreProc =     true;
					subFilter. m_bNoParse =     true;

					subFilter. parseStream();

					// As the embedded sub-form will presumable re-use the
					// output stream in its own SubstitutionFilter, we need
					// to post the existing output at this point.
					postOutput();

					// Now present the grouped text for processing by the
					// sub-form.
					if (!subFilter. m_bSkipGroup)
					{
						StringBuffer        embedCtnt;

						// Assume the embeded form will provide its own content...
						embedCtnt = null;
						if (subFilter. m_buffer. toString(). trim(). length() >
								(GROUP_BEGIN.length() + GROUP_END.length() + 1))
						{
							// ...unless there was significant content before
							// the GROUP_END.
							embedCtnt = subFilter. m_buffer;
						}
					}

					// Resume processing with trailing text.
					line = subFilter. m_sPostText;
				}
			}
			else if (checkInside( line, iidx ) && (ridx > iidx ))
			{
				/*==============================================================
				===== Content insertion.
				==============================================================*/
				if (iidx != 0)
				{
					try {
						postTaggedLine( line. substring( 0, iidx ) );
					} catch (SkipLineException sle) {
						line = null;
					}
				}

				if (null != line)
				{
					subFile = line. substring(
										iidx + INSERT_BEGIN.length(),
										ridx ). trim();
					subFile = m_parser.substituteLine (subFile);
					if (subFile != null && subFile.length () != 0)
					{
						subFilter = new SubstitutionFilter(
												m_parser,
												m_locator,
												m_Sections );
						subFilter. m_sPreText =     null;
						subFilter. m_buffer   =     this. m_buffer;
						subFilter. m_target   =     this. m_target;

						subFilter. m_bGrouped =     this. m_bGrouped;
						subFilter. m_bPreProc =     this. m_bPreProc;
						subFilter. m_bNoParse =     this. m_bNoParse;

						subFilter. prepareFileSource( subFile );
						subFilter. parseStream();

						// Save the skip group status so that it can be propagated up if
						// the insert is in a group. Note that this only holds if we are
						// grouped ourselves: otherwise, section skips are treated as
						// line skips.
						this. m_bSkipGroup = subFilter. m_bSkipGroup;
					}

					line = line. substring( ridx + REF_END.length() );
				}
			}
			else {
				try {
					postTaggedLine( line );
				    postLine( LINE_SEP );
				} catch (SkipLineException sle) {}
				line = null;
			}

			if (!checkContent( line ))
			{
				line = m_LineSrc. readLine();
			}
		}

		//pout.println ("<!--DONE-->");
		if (!m_bGrouped)
		{
			postOutput();
			m_LineSrc. close();
		}
	}

	private void postTaggedLine(
		    String          p_sLine )
		throws
			SkipLineException,
			IOException
	{
		String      parsedString;

		if (m_bNoParse)
		{
			postLine( p_sLine );
			return;
		}

		try {
			parsedString = m_parser. processTags( p_sLine );
			if (parsedString != null)
			{
				postLine( parsedString );
			}
		}
		catch (SkipSectionException sse) {
			if (m_bGrouped) {
				m_bSkipGroup = true;
			}
			else
			{
				throw new SkipLineException();
			}
		}
	}


	/**
		Post line to target.
	 */
	private void postLine(
		    String      p_sLine )
		throws IOException
	{
		if (null != m_target)
		{
			m_target. print( p_sLine );
		}
		if (null != m_buffer)
		{
			m_buffer. append( p_sLine );
		}
	}

	private void postEmbeddedContent()
		throws IOException
	{
	}

	/**
		Send output to target.
	 */
	private void postOutput()
		throws IOException
	{
		if (null != m_target)
		{
			m_target. flush();
		}
	}

	private boolean checkContent(
		    String      p_sLine )
	{
		if (null == p_sLine) return false;

		if (0 == p_sLine. trim(). length())
		{
			return false;
		}

		return true;
	}

	private int getPositionOf(
		    String          p_sLine,
			String          p_sText )
	{
		int             idx;

		idx = p_sLine. indexOf( p_sText );
		if (-1 == idx) idx = p_sLine. length();

		return idx;
	}

	private boolean checkInside(
		    String          p_sLine,
			int             p_iIndex )
	{
		return ((0 <= p_iIndex) && (p_iIndex < p_sLine. length()));
	}
}
