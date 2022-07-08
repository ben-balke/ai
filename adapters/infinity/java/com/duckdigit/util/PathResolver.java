/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/util/PathResolver.java,v 1.1 2010/10/19 00:43:59 secwind Exp $
==================================================================================================*/
package com.duckdigit.util;

import java.io.File;
import java.util.StringTokenizer;

/**
 * Searches directories on a path string for a file. The provided file name
 * may include comma-delimited alternates, and substitution tags.
 */
public class PathResolver
{
	TagParser       m_Parser;
	String          m_sPath;
	String          m_sExt;

	public PathResolver()
	{
		m_Parser = null;
		m_sPath = null;
	}

	public void setPathRoot(
			String      p_sPath )
	{
		m_sPath = p_sPath;
	}

	public void setFileExt(
			String      p_sExt )
	{
		m_sExt = p_sExt;
	}
	public void assignParser(
			TagParser   p_Parser )
	{
		m_Parser = p_Parser;
	}

	/**
		Locate a file on the current path. The input can be a comma-delimited
		list of files. We search for each alternate on all branches of the
		path before trying the next.
		@param String       Comma-delimited list of alternate files.
	 */
	public String locateFile(
			String          p_sFiles )
	{
		String              sFiles;
		StringTokenizer     paths;
		StringTokenizer     files;
		String              file;
		String              path;
		File                test;

		// Prepare parser for file alternates.
		if (null != m_Parser)
		{
			sFiles = m_Parser. parseSimple( p_sFiles );
		}
		else
		{
			sFiles = p_sFiles;
		}
		if (null == sFiles) return null;

		files = new StringTokenizer( sFiles, "," );

		// Try next file.
		while (files. hasMoreTokens())
		{
			file = files .nextToken(). trim();
			if ((null != m_sExt) && (-1 == file.indexOf( '.' )))
			{
				file += "." + m_sExt;
			}
			test = new File( file );
			// Absolute file spec or null path - try raw file name.
			if (test. isAbsolute() || (null == m_sPath))
			{
				if (test. isFile())
				{
					return file;
				}
				continue;
			}

			// Append the file to each of the paths.
			paths = new StringTokenizer( m_sPath, "," );
			while (paths. hasMoreTokens())
			{
				path = paths .nextToken() + "/" + file;
				test = new File( path );
				if (test .isFile())
				{
					return test. getAbsolutePath();
				}
			}
		}

		// Garbage return if file could not be found.
		return p_sFiles;
	}

	public static void main( String args[] )
	{
		PathResolver        loc;

		loc = new PathResolver();
		loc.setPathRoot( args[0] );
		System.out.println( loc.locateFile( args[1] ) );

	}
}
