/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/util/SQLDynSrvDriver.java,v 1.1 2010/10/19 00:43:59 secwind Exp $
==================================================================================================*/
package com.duckdigit.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
   Test driver for the SQL dynamic server feature.
 
   The SQL Connector allows dynamic database connect configuration through a SQL
   table (sw_DBServer) in a host database. This class tests the basic connection
   management features of the implementation.
 
   <ol><li>
   The format for the connection specification is sql.<host>.<server>
   </li><li>
   The connection can be dynamically relocated. The change will be picked up
   on the next status poll.
   </li><li>
   If the server goes down, the connection will be recovered when the server
   recovers.
   </li></ol>
 
   SQLDynSrvDriver does not do anything complicated with thread and connection
   management: those functions are thoroughly tested by SQLPoolDriver. Here we
   simply print out connections status. However, we do inherit the threaded
   architecture from SQLPoolDriver.
 
   @author Brian K. E. Balke
 */
public class SQLDynSrvDriver
	  extends Thread
{
   // SQL configuration entry.
   static private String
				  s_sSQLProt = "sql.hostsrv.dynamic";
   
   // ID of the thread.
   private int			  m_iID;
   // Millis to wait between DB access. Defaults to 1 hour for idle threads.
   private int			  m_nAccessMillis;
   // Flag to gate the thread loop.
   private boolean		  m_bRun;
   
   /** 
	  Initializing constructor.
	
	  @param p_iID			  Thread ID for status output.
	  @param p_nAccessSecs	  Time between SQL queries.
							  If 0, we assume this is an idle thread, and
							  override to 1 hour.
	*/
   public SQLDynSrvDriver (
		 int		 p_iID,
		 int		 p_nAccessSecs )
   {
	  m_iID = p_iID;
	  if (0 != p_nAccessSecs)
	  {
	  	  m_nAccessMillis = 1000 * p_nAccessSecs;
	  }
	  else
	  {
		 // Fixed wait time for idle threads: 1 hour.
		 m_nAccessMillis = 60 * 60 * 1000;
	  }
   }
   
   
   /**
	  Defines the work done by a thread assigned to the SQLPoolDriver.
	
	  The thread loop:
		 * Claims a connection.
		 * Queries the database.
		 * Sleeps until the next scheduled SQL access.
	*/
   public void run()
   {
      boolean           bFirst;
	  Connection		conn;
	  Statement			qry;
	  
      bFirst = true;
	  m_bRun = true;
	  while (m_bRun)
	  {
         if (!bFirst)
         {
             try {
                if (m_bRun) Thread.sleep ( m_nAccessMillis );
             } catch (InterruptedException ie) {}
         }

         try {
			conn = SqlConnector.getConn( s_sSQLProt );
		 }
		 catch (java.sql.SQLException sqle) {
			System.out.println( sqle. getMessage() );
             if (bFirst)
             {
                 // If first connection, try again after 0.1 s(we may be sharing
                 // with a thread that is still connecting).
                 try {
                     Thread.sleep( 100 );
                 } catch (InterruptedException ie) {}
             }
			continue;
		 }
         bFirst = false;

         try {
			qry = conn. createStatement();
			qry. execute( "SELECT current_date;" );
			qry. close();
			System.out.println( 
				"  Queried DB " + conn. getMetaData(). getURL() +
				" from thread " + m_iID );
		 }
		 catch (Exception ex) {
			System.out.println (
					 "  Failed DB from thread " + m_iID );
		 }
	  }
	  
	  try {
		 SqlConnector. closeConn( s_sSQLProt );
	  } catch (SQLException sqle) {}
   }
   
   /**
	  Signal the associated thread to end processing.
	*/
   public void terminate()
   {
	  m_bRun = false;
	  this. interrupt();
	  try {
		 this. join();
	  }
	  catch (InterruptedException ie) {}
   }
   
   public static void main(
		 String		 args[] )
   {
	  int			 nActive;
	  int			 nInterval;
	  int			 nIdle;
	  int			 nThreads;
	  
	  int			 iTh;
	  SQLDynSrvDriver	 drv;
	  ArrayList		 threads;
	  
	  BufferedReader rdr;
	  int			 iState;
	  String		 sFunc;
	  int			 iFnc;
	  
	  if (2 != args.length)
	  {
		 System.out.println( "Usage:\n" );
		 System.out.println(
			   "  SQLDynSrvDriver <Interval>" );
//		 System.out.println(
//			   "    <# Active> - Number of threads with regular queries" );
		 System.out.println(
			   "    <Interval> - Seconds between queries" );
//		 System.out.println(
//			   "    <# Idle> - Number of idle connections to establish" );
	  }

	  nActive = 1;
/*	  try {
	  	  nActive = Integer.valueOf( args[ 0 ] ). intValue();
		  if ((1 > nActive) ||
				(5 <= nActive))
		  {
			 System.out.println( "\nTest with 1 to 5 active threads.");
			 return;
		  }
	  }
	  catch (NumberFormatException nfe) {
		 System.out.println( "\nActive thread count must be an integer." );
		 return;
	  }
*/	  
	  try {
	  	  nInterval = Integer.valueOf( args[ 0 ] ). intValue();
		  if (1 > nInterval)
		  {
			 System.out.println(
				   "\nDatabase query interval must be 1 or more seconds." );
			 return;
		  }
	  }
	  catch (NumberFormatException nfe) {
		 System.out.println( "\nThe access time must be an integer." );
		 return;
	  }
	  
	  nIdle = 0;
/*	  try {
	  	  nIdle = Integer.valueOf( args[ 2 ] ). intValue();
		  if ((0 > nIdle) ||
				(5 <= nIdle))
		  {
			 System.out.println( "\nTest with 0 to 5 idle threads.");
			 return;
		  }
	  }
	  catch (NumberFormatException nfe) {
		 System.out.println( "\nIdle thread count must be an integer." );
		 return;
	  }
*/
	  nThreads = nActive + nIdle;
	  threads = new ArrayList();
	  
	  SqlConnector.enablePolling ();
	  
	  iState = 0;
	  System.out.println( "1 - Start threads" );
	  System.out.println( "2 - Stop threads" );
	  System.out.println( "3 - Stop test" );
	  
	  rdr = new BufferedReader( new InputStreamReader( System. in ) );
	  
	  while (iState != 2)
	  {
		 try {
			System.out.println( "Enter option: " );
			iFnc = Integer.valueOf(
							  rdr. readLine() ). intValue();
			
			switch (iFnc)
			{
			   case 1:
				  if (iState != 0)
				  {
					 System.out.println(
						   "\n  !!!! Threads are already running.\n" );
					 continue;
				  }
				  else
				  {
					 for (iTh=0; iTh<nThreads; iTh++)
					 {
						drv = new SQLDynSrvDriver(
										  iTh + 1, 
										  iTh < nActive ? nInterval : 0 );
						try {
						   drv.start ();
						}
						catch (IllegalThreadStateException itse) {}
						threads. add (
									iTh,
									drv );
					 }
					 iState = 1;
				  }
				  break;
			   case 2:
				  if (1 != iState)
				  {
					 System.out.println(
						   "\n  !!!! Threads have not been started.\n" );
					 continue;
				  }
				  else
				  {
					 for (iTh=0; iTh<nThreads; iTh++)
					 {
						drv = (SQLDynSrvDriver) threads. get( iTh );
						drv. terminate ();
					 }
					 iState = 0;
				  }
				  break;
			   case 3:
				  if (0 != iState)
				  {
					 System.out.println(
						   "  !!!! Stop threads before ending test." );
				  }
				  iState = 2;
				  break;
			}
		 }
		 catch (NumberFormatException nfe) {
			continue;
		 }
		 catch (IOException ioe)
		 {
			iState = 2;
		 }
   	  }
	  
	  SqlConnector. finishPolling();
   }
}
