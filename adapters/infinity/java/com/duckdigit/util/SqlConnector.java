/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/util/SqlConnector.java,v 1.1 2010/10/19 00:43:59 secwind Exp $
==================================================================================================*/
package com.duckdigit.util;

import java.sql.*;
import java.util.*;
import java.io.IOException;


/**
 * The SqlConnector provides a connection to the an SQL database
 * using standard settings from the sql.ini file.  This static class
 * is the preferred mechanism for establishing a connection.  The parameters
 * for the SQL connection are obtained from the sql.ini file found in the
 * <users home directory> + /sql.ini.  The user, password, driver, and url
 * are used to connect to the database.
 *
 * @version 1.00
 * @author Ben B. Balke
 */

public class SqlConnector
{
	static Integer		m_syncobject = new Integer (1);
	static ArrayList    m_conns = null;
	static SqlConnectProtocol
						m_default = null;
	static boolean      s_bPoll = false;
	static SqlPoller    s_Poller = null;
	static char			LEX_SEP = '.';

	public static Connection getConn ()
		throws java.sql.SQLException
	{
		return getConn ( null, false) ;
	}

	public static Connection getConn (
			String      p_sConn )
		throws java.sql.SQLException
	{
		return getConn ( p_sConn, false) ;
	}

	/**
	* Provides the connection for JDBC actions.
	* @param	p_sConn		String containing the database identifier.
	* @param dedicated If true, then produce a dedicated connection.
	* @return JDBC connection or null if the connection has not established.
	* @exception
	*/
	public static Connection getConn (
			String  p_sConn,
			boolean dedicated )
		throws java.sql.SQLException
	{
		SqlConnectProtocol  prot;
		prot = getProtocol( p_sConn );
		if (null == prot) return null;
		return prot. getConn( dedicated );
	}

	public static void closeConn(
		    String      p_sConn )
		throws java.sql.SQLException
	{
		SqlConnectProtocol  prot;
		prot = getProtocol( p_sConn );
		if (null == prot) return;
		prot. close();
	}

	/**
	* Gets the user name used to connect to the database.
	* @return	String containing the database username.  null is returned if no user name is available.
	*/
/*	public static String getUsername ()
	{
		loadConnectParams (false);
		return m_user;
	}
*/
	/**
	* Gets the user name used to connect to the database.
	* @return	String containing the database username.  null is returned if no password is available.
	*/
/*	public static String getPassword ()
	{
		loadConnectParams (false);
		return m_password;
	}
*/

	/**
	 * Loads the connection parameters using
	 * the parameters from the SQL.ini file.  The ini file's path is derived
	 * by appending sql.ini to the "user.home" property.
	 * @return false when the connect parameters are inaccessible or not
	 * complete.
	 */
	private static boolean loadConnectParams ()
	{
		Ini                 ini;
		LinkedList          stanzas;
		Iterator            itor;
		String              stnza;
		SqlConnectProtocol  prot;
		boolean				result = true;

		if (m_conns == null)
		{
			m_conns = new ArrayList();
			try
			{
				String			iniPath = DirectoryUtil.getDatabaseIniPath ();

				ini = new Ini ();
				stanzas = Ini. queryStanzas( iniPath );
				if (!ini.loadStanza (iniPath, null))
				{
					System.err.println ("database.ini failed to load.  Using " + iniPath);
				}

				itor = stanzas. iterator();
				while (itor. hasNext())
				{
					stnza = itor. next(). toString();
					if (-1 != stnza. indexOf( "sql" ))
					{
						prot = new SqlConnectProtocol();
						ini.setStanza( stnza );
						prot. interpret(
									ini. getStanzaEntries() );
						registerProtocol( prot );
					}
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace ();
				result = false;
			}

			if ((null == m_default) &&
				(0 != m_conns. size()))
			{
				m_default = (SqlConnectProtocol) m_conns.get( 0 );
			}
		}
		return result;
	}

	private static SqlConnectProtocol getProtocol(
			String      p_sProt )
		throws SQLException
	{
		Iterator            conns;
		SqlConnectProtocol  prot;
		SqlConnectProtocol  tProt;
		boolean				bKnown;

		if (!loadConnectParams())
		{
			throw new java.sql.SQLException ("Failed to Load the SQL connect parameters.");
		}

		bKnown = false;
		
		prot = null;
		if (null != p_sProt)
		{
			conns = m_conns. iterator();
			while (conns. hasNext())
			{
				tProt = (SqlConnectProtocol) conns. next();
				if (0 == p_sProt. compareToIgnoreCase(
											tProt.getName() ))
				{
					bKnown = true;
					prot = tProt;
					if (!prot. isDynamic() || prot. hasHost()) return prot;
							// End processing for normal connection.
					break;
				}
			}
			if (null == prot)
			{
				if (null == getServerName( p_sProt ))
				
				{
					throw new SQLException ("Cannot locate SQL stanza for " + p_sProt);
				}
				
				prot = new SqlConnectProtocol();
				prot. setDynamic();
			}

			// Wait for poller to signal retry, if connection is unavailable.
			if (!prot.checkHost())
			{
				throw new SQLException (
						"Database for " + p_sProt + " appears to be down." );
//				return null;
			}

 			if (queryProtocol( p_sProt, prot ))
			{
				if (!bKnown)
				{
					registerProtocol( prot );
				}
			}
		}
		else
		{
			prot = m_default;
		}
		return prot;
	}
	
	private static void registerProtocol(
			SqlConnectProtocol		p_Prot )
	{
		m_conns. add( p_Prot );
		if (s_bPoll)
		{
			if (null == s_Poller)
			{
				startPolling();
			}
			s_Poller.addProtocol( p_Prot );
		}
		if (null == m_default)
		{
			if (p_Prot. checkDefault())
			{
				m_default = p_Prot;
			}
		}
	}

	private static String getServerName(
			String			p_sProt )
	{
		if (p_sProt.indexOf(LEX_SEP) == p_sProt.lastIndexOf(LEX_SEP))
		{
			return null;
		}
		
		return p_sProt.substring( 0, p_sProt.lastIndexOf( LEX_SEP ) );
	}

	/**
		Dynamically loads a connection protocol.
	 */
	private static boolean queryProtocol(
			String				p_sProt,
			SqlConnectProtocol	p_Prot )
	{
		String				sPrSrv;
		String				sDB;
		
		Connection			conn;
		Statement			qry;
		ResultSet			rs;
		ResultSetMetaData	rsm;
		
		StateProperties		config;
		
		p_Prot. quarantineHost();
		
		sPrSrv = getServerName( p_sProt );
		try {
			conn = getConn( sPrSrv );
			if (null == conn) return false;

			sDB = p_sProt. substring(
								p_sProt. lastIndexOf( LEX_SEP ) + 1 );
			qry = conn.createStatement();
			try {
				rs = qry.executeQuery(
							"SELECT * FROM sw_dbserver WHERE name='sql." + sDB + "'" );
				if (rs.next())
				{
					rsm = rs.getMetaData();
					config = new StateProperties();
					for (int iC=1; iC<=rsm.getColumnCount(); iC++)
					{
						/*
						 * If the property is not set in the database then skip it
						 * as one would do in the database.ini file.  If will 
						 * recieve the default value in interpret below...
						 */
						Object		o = rs.getObject( iC );
						if (o != null)
						{
							config.setProperty(
									rsm. getColumnName( iC ),
									o. toString() );
						}
					}
					config. setProperty( "name", p_sProt );
					try {
							/*
							 * This is where were the parameters are processed
							 * using the standard state interpreter.
							 */
						p_Prot.interpret( config );
					}
					catch (InvalidStateException ise) {
						System.out.println(
								"Invalid configuration for database " + p_sProt );
						p_Prot. quarantineHost();
						return false;
					}

					return true;
				}
			}
			finally {
				qry. close();
			}
		}
		catch (SQLException se) {
			config = new StateProperties();
			config. setProperty(
						"name", p_sProt );
			config. setProperty(
						"pollmins", "1" );
			try {
				p_Prot. interpret( config );
			} catch (InvalidStateException ise) {}
			p_Prot. quarantineHost();
		}
		
		return false;
	}
	

	/*==========================================================================
	===== Metadata exposure.
	==========================================================================*/

	public static List getTables (String unused, String dbname)
	{
		ArrayList			list = new ArrayList ();
        try
        {
            Connection conn = SqlConnector.getConn (dbname);
            DatabaseMetaData dmd = conn.getMetaData();
            ResultSet       rs = dmd.getTables ("",null,null,null);
            if (rs != null)
            {
				try {
					while (rs.next ())
					{
						if (rs.getString ("TABLE_TYPE").equals ("TABLE"))
						{
							list.add (rs.getString ("TABLE_NAME"));
						}
					}
				}
				finally {
	                rs.close ();
				}
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace ();
        }
		return list;
	}

	public static List getColumns (String unused, String dbname, String table)
	{
		ArrayList			list = new ArrayList ();
        try
        {
            Connection conn = SqlConnector.getConn (dbname);
            DatabaseMetaData dmd = conn.getMetaData();
            ResultSet       rs = dmd.getColumns ("",null,table,null);
            if (rs != null)
            {
				try {
					while (rs.next ())
					{
						list.add (rs.getString ("COLUMN_NAME"));
					}
				}
				finally {
					rs.close ();
				}
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace ();
        }
		return list;
	}


	/*==========================================================================
	===== Connection polling.
	==========================================================================*/

	/**
		Enable polling.
	 */
	public static void enablePolling()
	{
		s_bPoll = true;
	}

	/**
		Establish the polling facility.
	 */
	public static void startPolling()
	{
		s_Poller = new SqlPoller();
		s_Poller. start();
	}

	/**
		Establish the polling facility.
	 */
	public static void finishPolling()
	{
        if (null != s_Poller)
        {
    		s_Poller. terminate();
        	s_Poller = null;
        }
	}

	/*==========================================================================
	===== Test interface.
	==========================================================================*/

	public static void main (String args [])
	{
		if (args.length == 1)
		{
			try
			{
				SqlConnector.getConn (args [0]);
			}
			catch (SQLException s)
			{
				SqlConnector.dumpSqlExceptions ("connect", s);
			}
		}
	}

	/*==========================================================================
	===== Exception Processing
	==========================================================================*/

	/**
	* Dumps linked SQL exceptions to standard output.
	* @param 	Statement 	Sql statement displayed as a heading.
	* @param	s			SQLException to traverse.
	*/
	public static void dumpSqlExceptions (String Statement, SQLException s)
	{
		System.err.println ("SQL Exception: " + Statement + "\n");
		do
		{
			s.printStackTrace ();
			s = s.getNextException ();
		} while (s != null);
	}

	/**
	 * Evaluates a SQLException to see if it is a duplicate key
	 * exception.
	 * @param p_sProt	A string containing the protocol stanza.
	 * @param sex		The SQLException in question.
	 * @return	boolean	true if the protocol believes the exception is
	 *			a duplicate. false otherwise.  The database.ini configuration
	 *			file provides information to the protocol to determine this.
	 */
	public static boolean isExceptionDuplicateKey (String p_sProt,
			SQLException sex)
	{
		boolean			rslt = false;
		try
		{
			rslt = getProtocol (p_sProt).isExceptionDuplicateKey (sex);
		}
		catch (Exception ignore)
		{
			ignore.printStackTrace ();
			dumpSqlExceptions ("Cannot Locate Protocol", sex);
		}
		return rslt;
	}
}
