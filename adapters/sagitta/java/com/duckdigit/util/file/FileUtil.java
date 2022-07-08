/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/util/file/FileUtil.java,v 1.2 2010/04/22 18:41:45 secwind Exp $
==================================================================================================*/
package com.duckdigit.util.file;

import java.io.*;
import java.rmi.server.UID;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

//import com.oroinc.text.regex.*;	// Perl Regular Expression Stuff from oroinc.
//import com.oroinc.text.perl.*;	// Perl Regular Expression Stuff from oroinc.

import com.duckdigit.util.lang.LineFilter;

public class FileUtil
{
	/**
	 * Creates a file from a string.
	 * @param targetPath	path of the file to move.
	 * @param data	String containing the data to write.
	 * @returns true if the more was successful; else false.
	 */
	public static void createFromString (String targetPath, String data) throws IOException
	{
		boolean 		result = true;

		File		target = new File (targetPath);

		FileWriter			out = new FileWriter (target);
		out.write (data, 0, data.length ());
		out.flush ();
		out.close ();
	}

	/**
	 * Deletes all files on the path, starting at the root. The root is not
	 * deleted.
	 * @param p_sRoot
	 * @throws IOException
	 */
	public static void deleteTree(
		    File      p_fRoot )
		throws IOException
	{
		File[]      cnts;
		File        fChld;
		int         iF;
		int         nF;

		if (!p_fRoot.isDirectory())
		{
			return;
		}

		cnts = p_fRoot. listFiles();
		nF = cnts. length;
		for (	iF = 0;
			    iF < nF;
				iF++ )
		{
			fChld = cnts[iF];
			if (fChld. isDirectory())
			{
				deleteTree( fChld );
			}
			fChld. delete();
		}
	}

	/**
	 * Moves the specified file to the localArchive directory.
	 * First attempts to rename the file and then trys to copy the data from
	 * the srcPath to the targetPath.
	 * @param path	path of the file to move.
	 * @returns true if the more was successful; else false.
	 */
	public static void moveFile (String srcPath, String targetPath) throws IOException
	{
		boolean 		result = true;

		char		buf [] = new char [1024];
		int			nbytes;
		File		src = new File (srcPath);
		File		target = new File (targetPath);



		if (!src.renameTo (target))
		{
			FileReader			in = new FileReader (src);
			FileWriter			out = new FileWriter (target);

			while ((nbytes = in.read (buf, 0, buf.length)) > 0)
			{
				System.out.println (buf);
				out.write (buf, 0, nbytes);
			}
			out.flush ();
			in.close ();
			out.close ();
			src.delete ();
		}
	}


	/**
	 * Changes the extension of a file name.}
	 * @param filePath	Full pathname.
	 * @param newExtension	Replacement extension.  The extension must include
	 *						the '.' character.
	 * @return	String to new path.
	 */
	public static String changeExtension (String fullPath, String newExtension)
	{
		int 		dotPosition = fullPath.lastIndexOf ('.');
		if (dotPosition == -1)
		{
			return fullPath + newExtension;
		}
		return fullPath.substring (0, dotPosition) + newExtension;
	}
	
	/**
	 * Gets the suffix following the . in a filename.
	 */
	public static String getExtension (String fullPath)
	{
		int 		dotPosition = fullPath.lastIndexOf ('.');
		if (dotPosition == -1)
		{
			return "";
		}
		return fullPath.substring (dotPosition + 1);
	}

	/**
	 * Returns the base name of the file.
	 * @param filePath	Full pathname of the file.
	 * @return String to base name without path and extension.
	 */
	public static String getBaseName (String fullPath)
	{
		String		name = FileUtil.getName (fullPath);
		int 		dotPosition = name.lastIndexOf ('.');
		if (dotPosition == -1)
		{
			return name;
		}
		return name.substring (0, dotPosition);
	}

		/**
	 * Returns the base name of the file.
	 * @param filePath	Full pathname of the file.
	 * @return String to base name without path and extension.
	 */
	public static String getName (String fullPath)
	{
		int 		startPosition = fullPath.lastIndexOf ('/');
		int 		backslashPosition = fullPath.lastIndexOf ('\\');
		int 		colonPosition = fullPath.lastIndexOf (':');
		startPosition = Math.max (backslashPosition, 
						Math.max (colonPosition, startPosition));
		if (startPosition == -1)
			return fullPath;
		return fullPath.substring (startPosition + 1);
	}


	public static class RegExpFilter implements FilenameFilter
	{
		private String		m_regExp;
        Pattern             m_ptrn;
//		Perl5Util			perl = new Perl5Util ();

		RegExpFilter (String exp) throws FileUtilException
		{
			try
			{
//				perl.match (exp, "");
                m_ptrn = Pattern.compile(
                                    exp,
                                    Pattern.CASE_INSENSITIVE );
			}
//			catch (com.oroinc.text.perl.MalformedPerl5PatternException ex)
			catch (PatternSyntaxException ex)
			{
				throw new FileUtilException (ex.toString ());
			}
			m_regExp = exp;
		}
		/**
		* FilenameFilter implementation method determines whether a file
		* is a header file.
		* @param	dir		Directory being searched.
		* @param	name	Candidate file.
		* @return	true if the file is a header file; else false.
		*/
		public boolean accept (File dir, String name)
		{
            Matcher     filter;
                
			try
			{
                filter = m_ptrn. matcher( name );
                return filter. matches();
//				return perl.match (m_regExp, name);
			}
			catch (Exception ex)
			{
				return false;
			}

		}
	}


	/**
	 * Lists the files in a directory that match the provided regular
	 * experssion ala perl.<p>
	 *
	 * The Perl5 syntax is as follows:
	 * <ul>
	 * <li> [m]/pattern/[i][m][s][x]
	 * </ul>
	 * <p>As with Perl, any non-alphanumeric character can be used in lieu of
	 * the slashes.
	 * @param	path	Directory path to search.
	 * @param 	perlregexp	Perl style regular expersion.
	 * @throws	FileUtilException when the regular expression is invalid.
	 */
	public static File [] ListFiles (String path, String perlregexp)
		throws FileUtilException
	{
		File		dir = new File (path);

		FileUtil.RegExpFilter ref = new FileUtil.RegExpFilter (perlregexp);
		File		files [] = dir.listFiles (ref);
		return files;
	}


	/**
	 * Redirects a disk file to the provided output stream.
	 * Opens path and sends its output to out.
	 * @param path	path of the file to open.
	 * @param out	OutputStream to send the file to.
	 * @return number of bytes redirected.
	 */
	public static int redirectFile (String path, OutputStream out) throws
		IOException, FileNotFoundException
	{
		int					totalBytes = 0;
		int					nbytes;
		byte				buf [] = new byte [2048];
		FileInputStream		fin = new FileInputStream (new File (path));

		while ((nbytes = fin.read (buf)) > 0)
		{
			out.write (buf, 0, nbytes);
			totalBytes += nbytes;
		}
		fin.close ();
		return totalBytes;
	}
	/**
	 * Redirects a disk file to the provided output stream.
	 * Opens path and sends its output to out.
	 * @param path	path of the file to open.
	 * @param out	OutputStream to send the file to.
	 * @return number of bytes redirected.
	 */
	public static void redirectFile (String path, OutputStream out, LineFilter filter) throws
		IOException, FileNotFoundException
	{
		//FileInputStream		fin = new FileInputStream (new File (path));
		LineNumberReader		lin;
		PrintStream				pout;

		lin = new LineNumberReader (
				new InputStreamReader (
				new FileInputStream (new File (path))));
		pout = new PrintStream (out);
		String		line;
		while ((line = lin.readLine ()) != null)
		{
			pout.println (filter.substituteLine (line));
		}
		lin.close ();
	}
	/**
	 * Redirects a disk file to a String Buffer
	 * Opens path and sends its output to out.
	 * @param path	path of the file to open.
	 * @param out	OutputStream to send the file to.
	 * @return number of bytes redirected.
	 */
	public static StringBuffer redirectFile (String path, LineFilter filter) throws
		IOException, FileNotFoundException
	{
		LineNumberReader		lin;
		PrintStream				pout;

		lin = new LineNumberReader (
				new InputStreamReader (
				new FileInputStream (new File (path))));
		StringBuffer	sb = new StringBuffer ();
		String		line;
		while ((line = lin.readLine ()) != null)
		{
			if (filter != null)
			{
				sb .append (filter.substituteLine (line));
			}
			else
			{
				sb .append (line);
			}
			sb.append ('\n');
		}
		lin.close ();
		return sb;
	}

	/**
		Create a unique temporary file name as a string of hex characters.
		<p>
		We use the RMI UID generator, which produces a value presented as three
		segments: <code>XXXXXX:XXXXXXXXXX:(-)8XXX</code>. The last segment
		is a counter running from -8000 to 7FFF. The first segment is a machine
		identifier, the second is a time stamp with 1 second resolution, and the
		last segment is a counter running from -8000 to 7FFE. The ID is
		is guaranteed to be unique so long as fewer than 64K suffixes are
		generated in a second.
		<p>
		Since the sign may not be an allowed file character, we need to add
		0x8000 to the value encoded in the third segment.
	 */
	public static String getUniquePrefix()
	{
		String  sUid;
		int     iCln1;
		int     iCln2;
		String  sCnt;
		String  sSuffix;

		sUid = new UID(). toString();

		// Extract first two segments.
		iCln1 = sUid.indexOf( ":" );
		sSuffix = sUid.substring( 0, iCln1 );
		iCln2 = sUid.indexOf( ":", iCln1 + 1 );
		sSuffix += sUid.substring( iCln1 + 1, iCln2 );

		// Now translate counter.
		sCnt = Integer. toHexString(
						Integer.parseInt( sUid.substring( iCln2 + 1 ), 16 )
							+ 0x8000 );
		for (int i=0; i<4-sCnt.length(); i++)
		{
			sSuffix += "0";
		}
		sSuffix += sCnt;

		return sSuffix;
	}
}
