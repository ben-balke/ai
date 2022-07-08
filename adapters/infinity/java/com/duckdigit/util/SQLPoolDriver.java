/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/util/SQLPoolDriver.java,v 1.1 2010/10/19 00:43:59 secwind Exp $
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
   Test driver for the SQL connection pool.
 
   The SQL connection pool utilizes the 2ndWinds ResourceLoadRegulator. This is 
   a mechanism that allows multiple threads to share a connection resource, and
   attempts to automatically recover a connection after it has been dropped.
 
   We use to SqlConnector to establish the database connections, which means 
   that the pool configuration will be found in 
            /home/ai/conf/database.ini.
 
   A comprehensive test program should vary the ratio of
	  * threads, defined here as a input parameter, to 
   `  * pool size, defined in the pool configuration.
   The ratio of:
	  * SQL access time, defined here as an input paramter, to
	  * connection polling interval, defined in the pool configuration
 
   The test menu allows the user to start and stop threads, with a separate
   option to terminate the test.
 
   Parameters for the main routine:
	  Active count - number of threads to start to exercise the SQL pool.
	  Access interval - Time for each thread to wait between SQL queries.
	  Idle count - number of threads to claim connection without routine use.
 
   @author Brian K. E. Balke
 */
public class SQLPoolDriver
	  extends Thread
{
   // SQL configuration entry.
   static private String
				  s_sSQLProt = "sql.pooltest";
   
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
   public SQLPoolDriver (
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
			System.out.println( "  Queried DB from thread " + m_iID );
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
	  SQLPoolDriver	 drv;
	  ArrayList		 threads;
	  
	  BufferedReader rdr;
	  int			 iState;
	  String		 sFunc;
	  int			 iFnc;
	  
	  if (2 != args.length)
	  {
		 System.out.println( "Usage:\n" );
		 System.out.println(
			   "  SQLPoolDriver <# Active> <Interval> <# Idle> " );
		 System.out.println(
			   "    <# Active> - Number of threads with regular queries" );
		 System.out.println(
			   "    <Interval> - Seconds between queries" );
		 System.out.println(
			   "    <# Idle> - Number of idle connections to establish" );
	  }
	  
	  try {
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
	  
	  try {
	  	  nInterval = Integer.valueOf( args[ 1 ] ). intValue();
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
	  
	  try {
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
						drv = new SQLPoolDriver(
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
						drv = (SQLPoolDriver) threads. get( iTh );
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
