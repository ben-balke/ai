/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/util/SQLConnectionRegulator.java,v 1.1 2010/10/19 00:43:59 secwind Exp $
==================================================================================================*/
package com.duckdigit.util;

import java.sql.Connection;
import java.sql.SQLException;


public class SQLConnectionRegulator
	extends ResourceLoadRegulator
{
	SqlConnectProtocol      m_Client;

    public SQLConnectionRegulator(
		    SqlConnectProtocol      p_Client,
            Object                  p_oInstance,
		    int                     p_iSize,
			int                     p_iRateLim )
	{
		super( p_oInstance, p_iSize, p_iRateLim );
		m_Client = p_Client;
    }

    public Object allocateResource()
		throws Exception
	{
		Connection      conn;
		conn = m_Client.connect( 
                            (String) describeInstance(),
                            true );
System.err.println( "Allocating SQL connection " + conn );
		return conn;
    }

	public boolean checkValid(
		    Object      p_rsrc )
	{
		Connection      conn;

		conn = (Connection) p_rsrc;
		try {
		    return !conn.isClosed();
		}
		catch (SQLException sqle) {
			return false;
		}
	}

    public void freeResource(
		    Object p_Rsrc )
	{
		try {
			((Connection) p_Rsrc). close();
		}
		catch (SQLException sqle) {}
    }
}
