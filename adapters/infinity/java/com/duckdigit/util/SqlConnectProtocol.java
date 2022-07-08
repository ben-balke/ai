/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/util/SqlConnectProtocol.java,v 1.1 2010/10/19 00:43:59 secwind Exp $
==================================================================================================*/
package com.duckdigit.util;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Iterator;

/**
	Defines the configuration model for SQL connections.
 */
class SqlConnectProtocol
	implements StateInterpreter
{
	private String              m_name;
	private String		        m_driver;
		//
		// "sun.jdbc.odbc.JdbcOdbcDriver";
		// "oracle.jdbc.driver.OracleDriver";
		//

//	private String		        m_url;
		// EXAMPLES
		// "jdbc:odbc:JDExpress";
		//"jdbc:oracle:oci7:@dbname";
		//"jdbc:oracle:oci7:@(description=(address=(host=hostname)(protocol=tcp)(port=1526))(connect_data=(sid=db0))";
		//"jdbc:oracle:thin:@hostname:1526:dbname";
		//
	
	private boolean				m_bDynamicHost;
		// If host can change. Only the client can signal this.
	private boolean				m_bNoHost;
		// If connection to host is unavailable, requiring refresh of connection
		// parameters. We assume the host is valid, until a poll fails.
	private boolean				m_bTryHost;
		// Pacing variable for connection parameter refresh by SqlConnector.
	private boolean				m_bTryConn;
		// State control variable for connection refresh.

	private String				m_user;
	private String				m_password;
	private boolean             m_default;
	private boolean				m_leaveOpen;
	private String				m_dupKeyString;
	private int                 m_iPoolSize;
	private String              m_sPollQuery;
	private int                 m_iPollMins;
    private String              m_sActQuery;

    SQLDatabaseAlternator       m_Alternates;
	SQLConnectionRegulator      m_ActivePool;
	int                         m_iNextPoll;
    
    boolean                     m_bReportConnectFailure;

	public SqlConnectProtocol()
	{
		m_iNextPoll = 0;
        m_Alternates = new SQLDatabaseAlternator( this );
        m_bReportConnectFailure = true;
		
		m_bDynamicHost = false;
		m_bNoHost = true;
		m_bTryHost = true;
		m_bTryConn = true;
	}
	
	public void setDynamic()
	{
		m_bDynamicHost = true;
	}
	
	public boolean isDynamic()
	{
		return m_bDynamicHost;
	}
	
	public boolean hasHost()
	{
		return !m_bNoHost;
	}
	
	public void quarantineHost()
	{
		m_bTryHost = 0 == m_iPollMins;
	}
	
	public boolean checkHost()
	{
		return m_bTryHost;
	}

	public void interpret(
			StateProperties     p_props )
		throws InvalidStateException
	{
        ArrayList               urls;
        Iterator                itU;
        Object                  oURL;
        SQLConnectionRegulator  pool;
        
		m_name          = p_props. getString ("name", null);
		m_driver        = p_props. getString ("driver", "sql");
		m_user          = p_props. getString ("user", null);
		if (null == m_user)
		{
			m_user      = p_props. getString ("username", "sql");
		}
		m_password      = p_props. getString ("password", "");
		urls            = p_props. getMultiple ( "url" );
        m_sActQuery     = p_props. getString( "actqry", null );
		m_default       = p_props. getBoolean( "default", false );
		m_dupKeyString  = p_props. getString( "dupkey", "duplicate");
		m_iPoolSize     = p_props. getInt( "poolsize", 1 );
		m_sPollQuery    = p_props. getString( "poll", null);
		m_iPollMins     = p_props. getInt( "pollmins", 0 );
        
        if ((1 < urls.size()) &&
              (null == m_sActQuery))
        {
            throw new InvalidStateException(
                    "Database " + m_name + " has " + urls.size() +
                    " but no activation time query." );
        }

		m_iNextPoll = m_iPollMins;
        
        itU = urls.iterator();
        while (itU. hasNext())
        {
            oURL = itU. next();
    		pool = new SQLConnectionRegulator(
                                    this, 
                                    oURL,
                                    m_iPoolSize, 
                                    500 );
            m_Alternates.registerLocation( oURL, pool );
        }
        
        m_ActivePool = (SQLConnectionRegulator) m_Alternates. locateCurrent();
        if (null == m_ActivePool)
        {
            throw new InvalidStateException(
                  "Could not locate the active database for " + m_name );
        }
		
		// We're now configured to connect to the host.
		m_bNoHost = false;
		m_bTryConn = true;
	}

	public String getName()
	{
		return m_name;
	}

	public String getURL()
	{
		return (String) m_ActivePool. describeInstance();
	}

	/**
	* Gets the user name used to connect to the database.
	* @return	String containing the database username.  null is returned if no user name is available.
	*/
	public String getUsername ()
	{
		return m_user;
	}

	/**
	* Gets the user name used to connect to the database.
	* @return	String containing the database username.  null is returned if no password is available.
	*/
	public String getPassword ()
	{
		return m_password;
	}

	public boolean checkDefault()
	{
		return m_default;
	}

	/**
	 * Sets whether the connection should remain open even when
	 * close is called.
	 */
	public void setLeaveOpen (boolean leaveOpen)
	{
		m_leaveOpen = leaveOpen;
	}

	/**
	* Provides the connection for JDBC actions.
	* @param dedicated If true and the SQL connection is not active,
	* 			the connection is automatically made.
	* @return JDBC connection or null if the connection has not established.
	* @exception
	*/
	public synchronized Connection getConn (boolean dedicated)
		throws java.sql.SQLException
	{
		Connection      conn;

		if (m_bNoHost) return null;
		if (!m_bTryConn) return null;
        if (null == m_ActivePool) return null;

        try {
			if (dedicated)
			{
				conn = connect(
                            (String) m_ActivePool.describeInstance(),
                            false );
			}
			else
			{
				conn = (Connection) m_ActivePool. claimResource();
                m_Alternates. updateClaims( m_ActivePool );
			}
			return conn;
		}
		catch (Exception ex)
		{
			if (ex instanceof SQLException)
			{
			// If polling, suppress connection retries until next cycle.
			// Otherwise, we'll drag down the host with connect retries...
				if (0 != m_iPollMins)
				{
					m_bTryConn = false;
					if (m_bDynamicHost) 
					{
						m_bTryHost = false;
						m_bNoHost = true;
					}
				}
				throw (SQLException) ex;
			}
			else
			{
			    ex.printStackTrace();
			}
		}

		return null;
	}

	/**
	* Connect establishes the SQLConnection to the SQL database.
	*
	* @param	printConnectInfo		if true the database connection information
	* 		is displayed to the system output stream.
	* @exception java.sql.SQLException 	thrown when the database driver
	* 		cannot be loaded or the connection cannot be made.
	*/
	/*
		This routine must be unconstrained by connection status variables in
		order to support alternate evaluations (see pollConnections).
	 */
	public Connection connect (
            String p_sURL,
			boolean printConnectInfo )
		throws java.sql.SQLException
	{
		Connection      sqlconn;

		try
		{
			Class.forName (m_driver);
		}
		catch (ClassNotFoundException cnfe)
		{
			m_bTryHost = false;
			m_bTryConn = false;
			System.err.println ("ERROR: SQL driver load failed for: " + m_driver + "\n");
			cnfe.printStackTrace ();
		}

		try
		{
			sqlconn = java.sql.DriverManager.getConnection (
				p_sURL,
				m_user,
				m_password);
			if (printConnectInfo)
			{
				DatabaseMetaData		meta;
				meta = sqlconn. getMetaData ();
				System.err.print ("Database: " + meta.getDatabaseProductName ());
				System.err.println (" version: " + meta.getDatabaseProductVersion ());
				System.err.println ("Database: " + meta.getUserName ());
			}
            // Successful connection. Report errors again.
            m_bReportConnectFailure = true;
		}

		catch (java.sql.SQLException sqle)
		{
            SQLException 		first = sqle;
            if (m_bReportConnectFailure)
            {
                System.err.println ("SQL Exception");
                do
                {
                    sqle.printStackTrace ();
                    sqle = sqle.getNextException ();
                } while (sqle != null);
                m_bReportConnectFailure = false;
            }
			throw first;
		}

		return sqlconn;
	}

	/**
	* Closes a previously open connection to the database.
	* @exception java.sql.SQLException 	thrown when the database driver cannot close.
	*/
	public void close () throws java.sql.SQLException
	{
        if (null == m_ActivePool) return;
		m_ActivePool. releaseClaim();
	}
	/**
	 * Forces a close regardless of the connection leaveOpen status.
	 * This is used to reconnect a broken connection.
	 * @exception java.sql.SQLException thrown when the database driver cannot close.
	 */
	public void forceClose ()
	{
        if (null == m_ActivePool) return;
		m_ActivePool. closeResource();
	}

	public void closeAll()
	{
		LinkedList              conns;
        Iterator                itR;
        ResourceLoadRegulator   pool;
		Iterator        itConn;
		Connection      sqlConn;

		conns = new LinkedList();
        
        itR = m_Alternates. getRegulators();
        while (itR. hasNext())
        {
            pool = (ResourceLoadRegulator) itR. next();
            pool. getResources( conns );
            itConn = conns. iterator();
            while (itConn. hasNext())
            {
                sqlConn = (Connection) itConn. next();
                if (null != sqlConn)
                {
                    try {
                        sqlConn. close();
                    }
                    catch (SQLException sqle) {}
                }
            }
		}

		m_ActivePool = null;
	}

	/*==========================================================================
	===== Connection Polling
	==========================================================================*/

	/**
		Issue the poll query to all connections on the described database, to
		ensure that they are held open beyond any "idle time" limit.
	 */
	public void pollConnections()
	{
		LinkedList                  conns;
		Iterator                    itConns;
		Connection                  conn;
		Statement                   stmt;
        
        SQLConnectionRegulator      reg;

		if (null == m_ActivePool) return;

		if (0 == m_iPollMins) return;

		m_iNextPoll--;
		if (0 != m_iNextPoll) return;
		
		// If host is invalid (see below), prompt connector to try to reload
		// server parameters.
		if (m_bNoHost) m_bTryHost = true;
		// If static host has lost connection, allow connection attempt on
		// this subsequent poll pass.
		if (!m_bTryConn && !m_bDynamicHost) m_bTryConn = true;

		conns = new LinkedList();
		m_ActivePool.getResources( conns );

		itConns = conns. iterator();
		while (itConns. hasNext())
		{
			conn = (Connection) itConns. next();
			if (null == conn) continue;
			try {
				stmt = conn. createStatement();
	    		stmt. execute( m_sPollQuery );
				stmt. close();
			}
			catch (Exception ex) {
//				System.out.println("pollConnection DEBUG: Connection failed. (" + conn.toString() + ")");
//				System.out.println( ex. getMessage() );
				// We should probably assume that the connection is dead if we
				// get here. Removing it from the collection through the iterator
				// is one way of signalling that. For the Protocol to recover
				// gracefully, we'd want to encapsulate all calls to the SQL pool,
				// and verify that the connection is still in the collection
				// before using it. If not, we'd need to close it and create
				// a new connection.
				//
				// Of course, we have no way of guaranteeing that the application
				// is going to come back to us to renew the connection, so maybe
				// that would be a waste of time...
				m_ActivePool. markInvalid( conn );
				
				// For dynamic hosts, recognize the possibility that the host
				// may be invalid. The client will be signalled to attempt to
				// re-establish the connection on next polling pass (see above).
				if (m_bDynamicHost) m_bNoHost = true;
				m_bTryConn = false;
				System.out.println( "SQL polling found invalid connection." );
			}
		}
        
        reg = (SQLConnectionRegulator) m_Alternates. locateCurrent();
        if (null != reg)
        {
            if (reg != m_ActivePool)
            {
                m_ActivePool = reg;
				System.out.println(
                      "Swapping to database " + 
                                    m_ActivePool. describeInstance() );
            }
        }

		m_iNextPoll = m_iPollMins;
	}
    
    public java.util.Date dateLocation(
           Object           p_oLoc )
    {
        Timestamp           tact = null;
        Connection          conn;
        Statement           stmt;
        ResultSet           rslt;
        
        try {
            conn = connect(
                        (String) p_oLoc,
                        false );
			if (null != conn)
			{
				stmt = conn. createStatement();
				try {
					rslt = stmt. executeQuery( m_sActQuery );
					if (rslt.next())
					{
						tact = rslt.getTimestamp( 1 );
					}
				}
				finally {
					stmt. close();
				}
			}
        }
        catch (SQLException sqle) {}
        
        return tact;
    }

	/*==========================================================================
	===== Exception Processing
	==========================================================================*/

	/**
	* Dumps linked SQL exceptions to standard output.
	* @param 	Statement 	Sql statement displayed as a heading.
	* @param	s			SQLException to traverse.
	*/
	public void dumpSqlExceptions (String Statement, SQLException s)
	{
		System.err.println ("SQL Exception: " + Statement + "\n");
		do
		{
			s.printStackTrace ();
			s = s.getNextException ();
		} while (s != null);
	}

	public boolean isExceptionDuplicateKey (SQLException s)
	{
		int 		idx;

		System.out.println ("DupKey: " + m_dupKeyString);
		if (m_dupKeyString.length () == 0)
		{
			return false;
		}
		idx = s.getMessage ().indexOf (m_dupKeyString);
		return (idx > -1);
	}
}
