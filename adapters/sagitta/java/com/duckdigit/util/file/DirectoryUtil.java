/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/util/file/DirectoryUtil.java,v 1.2 2010/04/22 18:41:45 secwind Exp $
==================================================================================================*/
package com.duckdigit.util.file;

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
		Identifies the directory containing 2ndWind ini (setup) files.
		@returns String     Relative path, including a terminal path separator.
		@see com.duckdigit.zml.rh.SqlRecordHandler
	 */
	public static String getHome ()
	{
		if (m_Home == null)
		{
			m_Home = System.getProperty (
							"SECWIND",
							File.separatorChar + "home" +
									File.separatorChar + "duckdigit" +
									File.separatorChar + "www");
			//System.err.println ("BBB SECWIND=" + m_Home);
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
		@see com.duckdigit.zml.rh.SqlRecordHandler
	 */
	public static String getDatabaseIniPath ()
	{
		return getHome () + "conf" + File.separatorChar + "database.ini";
	}

	/**
		Provides the path for the file that defines the installation-specific
		agency management context.
		@returns String     Fully resolved path to <b>agency.ini</b>.
		@see com.duckdigit.agency
	 */
	public static String getAgencyIniPath ()
	{
		return getHome () + "conf" + File.separatorChar + "agency.ini";
	}

	/**
		Provides the path for the file that defines the connection parameters
		to the installation's document servers.
		@returns String     Fully resolved path to <b>document.ini</b>.
		@see com.duckdigit.docsrv.DocumentClient
	 */
	public static String getDocumentIniPath ()
	{
		return getHome () + "conf" + File.separatorChar + "document.ini";
	}
	
	public static String getSqlCacheIniPath ()
	{
		return getHome () + "conf" + File.separatorChar + "sqlcache.ini";
	}
	/**
	 * Returns the path which the pickcache.ini file is kept for 
	 * com.duckdigit.zml.PickLookupCacheController.
	 */
	public static String getPickCacheIniPath ()
	{
		return getHome () + "conf" + File.separatorChar + "pickcache.ini";
	}
	/**
		Provides the path of the TDPC/Secwind configuration director.
		This is where the properties and initiation files should be 
		kept.  The return value includes a file separator character on
		the end of the string.
		@returns String     Fully resolved path to the conf dir.  Usually /home/duckdigit/www/conf/.
	**/
	public static String getConfDir ()
	{
		return getHome () + "conf" + File.separatorChar;
	}

	/**
		Provides the path of TDPC/Secwind log directory.
		This is where all exceptions are logged.
	**/
	public static String getLogDir ()
	{
		return getHome () + "log" + File.separatorChar;
	}
}
