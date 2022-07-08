/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/ai/adapters/sagitta/DataExtractor.java,v 1.2 2010/04/22 18:41:39 secwind Exp $
==================================================================================================*/
package com.duckdigit.ai.adapters.sagitta;
import java.io.IOException;
import java.io.File;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

/**
	This tabulated role defines the mechanisms used to transfer data from a
	tabular source to a SQL database. It provides a framework for status
	logging, error recovery and SQL processing.
	<p>
	Services subject to specialization include:
	<OL>
	<LI>getVarNames</LI>
	<LI>getVarDecls</LI>
	<LI>getInsertStatement</LI>
	</OL>
 */

public abstract class DataExtractor
{
	//===== Parameter management.

	/*  Argument provider name. */
	private static final String ARG_PROV_NAME = "arg";
	/*  Key-value separator in argument strings. */
	private static final String VALUE_SEP = "=";

	/* Log path key. */
	private static final String LOG_TAG =
					"log";
	/* Database selection key. */
	private static final String DATABASE_TAG =
					"database";
	private static final String OFFICE_TAG =
					"office";

	/* Autocommit key Y=do autocommit, N=Commit after done. */
	private static final String AUTOCOMMIT_TAG =
					"autocommit";

	/*  Subsitution engine for command-line parameters. */

	//===== Logging.
	private static SagittaLogger      s_Log;
	private SagittaLogger             m_log;
	private boolean					m_bAutoCommit = false;

	//===== Source definition.

	SourceDefiner           m_SrcDef;

	//===== Fault recovery.

	/*  Allowed attempts to connect. */
	//private static final int ALLOWED_TRIES = 8;
	private static final int ALLOWED_TRIES = 1;
	/*  Baseline delay between tries. This is the root of a geometric
		progression limited by MAX_WAIT_MILLIS. */
	private static final int SEED_WAIT_MILLIS = 30 * 1000; //2 * 1000;
	/* Maximum wait time. */
	private static final int MAX_WAIT_MILLIS = 5 * 60 * 1000;
	/* ISAM failure counter. */
	private int n_iFaults = 0;
	/* Active wait interval. */
	private int m_nxtWait;

	private int             m_iRecords;
	private boolean         m_bSkip;
	protected Properties	m_properties = new Properties ();

	/*==========================================================================
	===== Configuration
	==========================================================================*/

	/**
		Prepare conversion context.
	 */
	public DataExtractor()
	{
	}

	/**
		Parse argument definitions as key-value pairs for later	substitution
		operations. The arguments are normally provided in through the main
		routine.
		@param String[]     Array of <key>=<value> assignments.
	 */
	public void prepareArgs(
			String[]    args )
	{
		int                     iArg;
		int                     nArgs;
		String                  sArg;
		int                     iSep;
		String                  sKey;
		String                  sVal;

		// Add target as a substitution parameter.
		nArgs = args.length;
		for (iArg=1; iArg<nArgs; iArg++)
		{
			sArg = args[ iArg ];
			iSep = sArg.indexOf( VALUE_SEP );
			if (-1 != iSep)
			{
				sKey = sArg. substring( 0, iSep );
				sVal = sArg. substring( iSep + 1 );
				sVal = convertParam( sKey, sVal );
				m_properties.setProperty (sKey, sVal);
			}
		}
	}

	/**
		Allows specializations to constrain extraction paramters to a specific
		format.
		<p>
		This version simply returns the provided value.
		@param String(1)        Key.
		@param String(2)        Value
		@return String          Value, following conversion, if any.
	 */
	public String convertParam(
	    	String          p_sKey,
		    String		    p_sVal )
	{
		return p_sVal;
	}

	/**
		Substitutes values for key eags in the provided string.
		@param String
	 */
	/**
		Exports source definition to specializations.
		@return SourceDefiner
	 */
	public SourceDefiner   getDefiner()
	{
		return m_SrcDef;
	}

	/*==========================================================================
	===== Status logging.
	==========================================================================*/

	/*
	 * @return SagittaLogger          Singleton log.
	 * @throws IOException
	 */
	 private static SagittaLogger getLog(
		    String      p_sPath,
			String      p_sType,
			String      p_sHeader )
		throws IOException
	{
		if (null == s_Log)
		{
			if ((null == p_sPath) ||
				(0 == p_sPath. length()))
			{
				s_Log = new SagittaLogger();
			}
			else
			{
				s_Log = new SagittaLogger( p_sPath );
			}

			s_Log. setItem( p_sHeader, p_sType );
		}

		return s_Log;
	}

	/*
		Close log after use is complete.
	 */
	private static void closeLog()
	{
		if (null != s_Log)
		{
			try {
				s_Log. close();
			}
			catch (IOException ioe) {}
		}
	}


	/**
		Sets up the log file for the conversion. It assumes the <code>log</code>
		key is assigned, and attempts to access the company and profit center
		keys.
		@param p_sType          Item type designator.
		@exception IOException
	 */
	public void accessLog(
		    String      p_sType )
		throws IOException
	{
		m_log = getLog(
					m_properties.getProperty (LOG_TAG),
					p_sType,
					constructLogHeader()  );
	}

	public abstract String constructLogHeader();

	public void reportFault(
			Exception       p_ex )
	{
		p_ex.printStackTrace ();
		reportFault( p_ex. getMessage() );
	}

	public void reportFault(
			String          p_msg )
	{
		if (null != m_log)
		{
			try {
				m_log. abort( p_msg + '\n' );
			}
			catch (IOException ioe) {}
		}
	}

	public void updateLog()
	{
		if (null != m_log)
		{
			try {
				m_log. update();
			}
			catch (IOException ioe) {}
		}
	}

	public void finishLog()
	{
		updateLog();
		closeLog();
		m_log = null;
	}

	/*==========================================================================
	===== SQL operations.
	==========================================================================*/

	private void buildCopyHeader()
	{
		StringBuffer        insBuf;
		Collection          cNames;
		Iterator            itName;
		boolean             bFrst = true;

		int                 iQ;
		int                 nQs;

		insBuf = new StringBuffer( 1024 );

		insBuf. append( "COPY office" + m_properties.getProperty (OFFICE_TAG) + "." );
		insBuf. append( m_SrcDef. getClassString() );
		insBuf. append( " (");

		cNames = m_SrcDef. getVarNames();
		itName = cNames. iterator();
		while (itName. hasNext())
		{
			if (!bFrst)
			{
				insBuf. append( "," );
			}
			else
			{
				bFrst = false;
			}
			insBuf. append(
						(String) itName. next() );
		}
		insBuf.append ( " ) FROM STDIN;" );
		System.out.println (insBuf.toString ());
	}

	/*
		Binds source fields to SQL parameters.
		@exception Exception        If a field is invalid.
		@exception SQLException     If SQL binding fails.
	 */
	protected void outputRecord()
		throws
			Exception
	{
		int             iFld;
		int             nFlds;
		boolean             bFrst = true;
		StringBuffer	insBuf = new StringBuffer (1024);

		nFlds = getDefiner(). getVarCount();
		for (iFld=0; iFld<nFlds; iFld++)
		{
			if (!bFrst)
			{
				insBuf. append( "\t" );
			}
			else
			{
				bFrst = false;
			}
			insBuf .append (getBulkValue(
						iFld,
						iFld+1));
		}
		System.out.println (insBuf.toString ());
	}

	public abstract String getBulkValue(
			int                     p_iFld,
			int                     p_iBind )
		throws
			Exception;

	/**
		Concludes JDBC operations. If the transfer was incomplete, we roll back
		changes.
		@param boolean          <code>true</code> if transfer is complete.
	 */
	private void terminateSQL(
			boolean     p_bComplete )
	{
		System.out.println ("\\.");
	}

	/*==========================================================================
	===== Source Operations.
	==========================================================================*/

	public abstract void establishConnection()
		throws SourceException;

	public abstract void recoverConnection()
		throws SourceException;

	public abstract void closeConnection();

	/*
		Open source and map its fields.
		<p>
		This assumes a connection object which is retained across attempts.
	 */
	public abstract void prepareSource()
		throws SourceException;

    /**
      Closes source.
     */
    public abstract void terminateSource();

	/**
		Define the initial index variable values. To be defined by
		specializations.
		@exception Exception
	 */
	public abstract void initializeIndex()
		throws Exception;

	/*
		Transfer cached field state to the new search variables.
		@exception IsamException
	 */
	 public abstract void continueRange()
		throws Exception;

	/*
		Dump current record for diagnosis.
	 */
	public abstract void dumpRecord();

	/*==========================================================================
	===== File processing.
	==========================================================================*/

	public void extract(
		    String[]        args,
			SourceDefiner   p_SrcDef )
	{
		m_SrcDef = p_SrcDef;

		try {
			prepareArgs( args );
			m_SrcDef.setOffice (m_properties.getProperty ( OFFICE_TAG ));
			accessLog( args[0] );
			convert();
		}
		catch (Exception ex) {
			System.err.println( ex. getMessage() );
			ex. printStackTrace();
		}
	}

	/**
		Process records in the associated database. Attempt to recover from
		ISAM record access faults, which are assumed to be network access
		failures, up to a limit of ALLOWED_TRIES failures.
	 */
	public void convert()
	{
		boolean         bTry;
		boolean         bCommit;

		bCommit = false;
		java.util.Date  start = new java.util.Date();
		buildCopyHeader ();

		seedWait();
		bTry = true;
		while (bTry)
		{
			try {
				if (!checkCache())
				{
					// Full connect first time through.
				    establishConnection();
				}
				else
				{
					// Reconnect on recovery.
					recoverConnection();
				}
				bCommit = processSource();
 				if (bCommit)
				{
					break;
				}
			}
			catch (SourceException se) {
				reportFault( se );
			}
			catch (Exception ex) {
				reportFault( ex );
				break;
			}

			// Here if source processing failure. Release the connection, and
			// wait a while before retesting connection.
			closeConnection();
            terminateSource();
			bTry = checkContinue();
			if (bTry)
			{
				bTry = waitForRecovery();
				if (!bTry) {
					reportFault(
						"Cannot maintain communications with the server." );
				}
			}
			updateLog();
		}

		// Clean up context on completion.
		if (bTry)
		{
			terminateSource();
		 	closeConnection();
		}
		terminateSQL( bCommit );
		updateLog();

		// Report progress.
		if (bCommit)
		{
			if ((null != m_log) &&
				(0 == m_log. m_records))
			{
				System.err.println( "NO RECORDS" );
			}
			else
			{
				long dT = new java.util.Date(). getTime() - start.getTime();
				System.err.println(
						m_SrcDef. getClassString() + " " +
						m_log. m_records + " " +
						dT + " ms\n" );
			}
		}

		// Print statistics when job completed.
		//### remove to main routine.
//		finishLog();
		// Remove to main routine.
	}

	/**
		Retrieve records from the source and write them to the SQL database.
		<p>
		As records are processed, we monitor for external stop requests. If
		detected, we stop processing with a "finished" status, causing results
		to this point to be saved in the database.
		@returns boolean        <code>true</code> if file processing was
									successfully completed.
								<code>false</code> if an ISAM access failure
									occurs during processing.
		@exception SQLException If JDBC operation fails.
	 */
	private boolean processSource()
		throws
			Exception
	{
		m_iRecords = 0;

		// Access target ISAM file.
		try {
			prepareSource();
			establishVariables();
		}
		catch (Exception ex) {
			reportFault( ex );
			return false;
		}


		// Select records.
		if (!selectRecords()) return false;

		try {
			return processRecords();
		}
		catch (SourceException se) {
			// Cache recovery context.
			System.err.println( se. getMessage() );
			cacheRecoveryContext();
		}

		return false;
	}
	public void recordWarning ()
	{
		m_log.m_warnings++;
	}

	public void logRecord ()
	{
		if (m_log != null)
		{
			m_log.m_records++;
			if (m_log.m_records % 1000 == 0)
			{
				updateLog ();
			}
		}
	}
	public abstract boolean processRecords()
		throws SourceException, Exception;

	/*
		Allocate fields and establish initial values for the index
		fields. On first entry, the index fields will be set by the
		specialization. On subsequent entries, the values are copied from the
		last successful record transfer.
		@exception Exception    If variable setup fails.
	 */
	 private void establishVariables()
		throws Exception
	{
		createFields( m_SrcDef );
		if (!checkCache())
		{
			initializeIndex();
		}
		else
		{
			continueRange();
		}
	}

	/**
		Allow client to establish first record processing policy in
		continueRange();
		@param boolean      <code>true</code> if to skip first record.
	 */
	public void enableSkip()
	{
		m_bSkip = true;
	}

	/*
		Specializations will implement this to create local storage to cache
		values in a format that can be easily converted to JDBC types.
	 */
	public abstract void createFields(
		    SourceDefiner       p_SrcDef )
		throws Exception;

	/*
		Select target records from the source.
		@returns boolean        <code>false</code> if select fails.
	 */
	public abstract boolean selectRecords();

	public boolean checkRecords()
	{
		return 0 != m_iRecords;
	}

	/*
		Save source upload context for recovery when connection is restored.
	 */
	public abstract void cacheRecoveryContext();
	public abstract boolean checkCache();

	/*==========================================================================
	===== Connection failure recovery.
	==========================================================================*/

	/* Set wait time to minimum. */
	private void seedWait()
	{
		m_nxtWait = SEED_WAIT_MILLIS;
	}

	/* Pause configured time for network recovery. */
	private boolean waitForRecovery()
	{
		int     iRmn;
		int     iNxt;

		n_iFaults++;
		if (ALLOWED_TRIES == n_iFaults) return false;

		System.err.println(
					"Waiting for network recovery " +
					(m_nxtWait/2000) + " sec." );

		try {
			iRmn = m_nxtWait;
			while (0 != iRmn)
			{
				iNxt = Math.min( iRmn, 10000 );
				Thread.sleep( iNxt );
				iRmn -= iNxt;
				if (!checkContinue()) return false;
			}
		}
		catch (Exception ex) {}

		m_nxtWait = Math.min( 2*m_nxtWait, MAX_WAIT_MILLIS );

		return true;
	}


	public boolean checkContinue()
	{
		return true;
	}
}
