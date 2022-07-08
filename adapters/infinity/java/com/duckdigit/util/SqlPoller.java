/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/util/SqlPoller.java,v 1.1 2010/10/19 00:43:59 secwind Exp $
==================================================================================================*/
package com.duckdigit.util;

import java.util.Iterator;
import java.util.LinkedList;

public class SqlPoller
	extends Thread
{
	LinkedList      m_Prots;
	boolean         m_bRun;

	/**
		Defualt constructor.
	 */
    public SqlPoller()
	{
		m_Prots = new LinkedList();
    }

	/**
		Register a connection manager with the poller.
		@param SqlConnectProtocol
	 */
	public void addProtocol(
		    SqlConnectProtocol      p_Prot )
	{
System.out.println( "Adding connection manager." );
		m_Prots. add( p_Prot );
	}

	/**
		Thread function that wakes up every minute and prompts connection
		managers to signal the database as needed to maintain connections.
	 */
	public void run()
	{
		Iterator            itProts;
		SqlConnectProtocol  prot;

		m_bRun = true;
		while (m_bRun)
		{
			try {
				Thread.sleep( 60000 );  // Wake up once a minute.
				itProts = m_Prots. iterator();
				while (itProts. hasNext())
				{
					prot = (SqlConnectProtocol) itProts. next();
					prot. pollConnections();
				}
			}
			catch (InterruptedException iex) {}
		}

		itProts = m_Prots. iterator();
		while (itProts. hasNext())
		{
			prot = (SqlConnectProtocol) itProts. next();
			prot. closeAll();
		}
	}

	/**
		Out-of-thread service to terminate polling.
	 */
	public void terminate()
	{
		m_bRun = false;
		this. interrupt();
		try {
			this. join();
		}
		catch (InterruptedException iex) {}
	}
}
