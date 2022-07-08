/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/util/DirectoryUtil.java,v 1.1 2010/10/19 00:43:59 secwind Exp $
==================================================================================================*/
package com.duckdigit.util;

import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.File;


/**
 * Provides general purpose utilities for the DPC software.
 */
public class DirectoryUtil
{
	private static String		m_Home = null;

	/**
		Identifies the directory containing ini (setup) files.
		@returns String     Relative path, including a terminal path separator.
	 */
	public static String getHome ()
	{
		if (m_Home == null)
		{
			m_Home = System.getProperty (
							"SECWIND",
							File.separatorChar + "home" +
									File.separatorChar + "ai");
			if (!m_Home.endsWith (File.separator))
			{
				m_Home += File.separator;
			}
		}
		return m_Home;
	}

	/**
		Provides the path for the file that defines the connection parameters
		to the installation's SQL database servers.
		@returns String     Fully resolved path to <b>database.ini</b>.
	 */
	public static String getDatabaseIniPath ()
	{
		return getHome () + "conf" + File.separatorChar + "database.ini";
	}

	/**
		Provides the path of the configuration director.
		This is where the properties and initiation files should be 
		kept.  The return value includes a file separator character on
		the end of the string.
		@returns String     Fully resolved path to the conf dir.  Usually /home/ai/conf/.
	**/
	public static String getConfDir ()
	{
		return getHome () + "conf" + File.separatorChar;
	}

	/**
		Provides the path of log directory.
		This is where all exceptions are logged.
	**/
	public static String getLogDir ()
	{
		return getHome () + "logs" + File.separatorChar;
	}
}
