/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/ai/DataExtractor.java,v 1.1 2010/10/19 00:42:53 secwind Exp $
==================================================================================================*/
package com.duckdigit.ai;

import java.io.IOException;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.duckdigit.util.*;

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

	/*  Tag prefix. */
	public static final String TAG_PREFIX = "{sub:";
	/*  Tag suffix. */
	public static final String TAG_SUFFIX = "}";
	/*  Argument provider name. */
	private static final String ARG_PROV_NAME = "arg";
	/*  Key-value separator in argument strings. */
	private static final String VALUE_SEP = "=";

	/* Log path key. */
	private static final String LOG_TAG =
					TAG_PREFIX + "-log" + TAG_SUFFIX;
	/* Database selection key. */
	private static final String DATABASE_TAG =
					TAG_PREFIX + "database" + TAG_SUFFIX;

	/* Autocommit key Y=do autocommit, N=Commit after done. */
	private static final String AUTOCOMMIT_TAG =
					TAG_PREFIX + "autocommit" + TAG_SUFFIX;

	/*  Subsitution engine for command-line parameters. */
	private TagParser       m_parser;

	//===== Logging.
	private static AILogger      s_Log;
	private AILogger             m_log;
	private boolean					m_bAutoCommit = false;

	//===== Source definition.

	SourceDefiner           m_SrcDef;

	//===== SQL Context.

	/* Connection and prepared statement. */
	private String          m_sqlKey;
	private Connection      m_sql;
	private String          m_sInsert;
	private PreparedStatement
							m_insrt;
	private String          m_sDupChk;
	private PreparedStatement
						    m_dupchk;
	private String          m_sUpdate;
	private PreparedStatement
							m_update;

	//===== Fault recovery.

	/*  Allowed attempts to connect. */
	private static final int ALLOWED_TRIES = 8;
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
		StringValueProvider     svp;

		int                     iArg;
		int                     nArgs;
		String                  sArg;
		int                     iSep;
		String                  sKey;
		String                  sVal;

		svp = new StringValueProvider( ARG_PROV_NAME );
		m_parser = new TagParser( TAG_PREFIX, TAG_SUFFIX );
		m_parser. addValueProvider( svp );

		// Add target as a substitution parameter.
		svp. addString(
					"DXTarget",
					args[0] );

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
				svp. addString(
							sKey,
							sVal );
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
		Substitutes values for key tags in the provided string.
		@param String
	 */
	public String substitute(
			String      p_sTmpl )
	{
		if (p_sTmpl == null)
		{
			return null;
		}
		return m_parser.substituteLine( p_sTmpl );
	}

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
	 * @return AILogger          Singleton log.
	 * @throws IOException
	 */
	 private static AILogger getLog(
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
				s_Log = new AILogger();
			}
			else
			{
				s_Log = new AILogger( p_sPath );
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
					substitute( LOG_TAG ),
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

	/**
		Establish the SQL context for the extraction. We need a connection and
		a prepared statement.
		@exception SQLException     If database cannot be reached, or prepared
									statement query is invalid.
	 */
	private void prepareSQL()
		throws SQLException
	{
		m_sqlKey = substitute( DATABASE_TAG );
		m_sql = SqlConnector.getConn( m_sqlKey );

		String	sAutoCommit = substitute( AUTOCOMMIT_TAG );
		if (sAutoCommit != null && sAutoCommit.charAt (0) == 'Y')
		{
			m_bAutoCommit = true;
		}
		else
		{
			m_bAutoCommit = false;
		}
		m_sql. setAutoCommit( m_bAutoCommit );
		buildInsertQuery();
		m_insrt = m_sql. prepareStatement( m_sInsert );

		if (m_SrcDef. checkUpdate())
		{
			buildUpdateQuery();
			m_update = m_sql. prepareStatement( m_sUpdate );
			m_dupchk = m_sql. prepareStatement( m_sDupChk );
		}
	}

	private void buildInsertQuery()
	{
		StringBuffer        insBuf;
		Collection          cNames;
		Iterator            itName;
		boolean             bFrst;

		int                 iQ;
		int                 nQs;

		insBuf = new StringBuffer( 1024 );

		insBuf. append( "INSERT INTO " );
		String schemaPrefix = substitute("office{sub:-office}.");
		if (schemaPrefix != null)
			insBuf. append (schemaPrefix);

		insBuf. append( m_SrcDef. getClassString() );
		insBuf. append( " (" );

		bFrst = true;
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

		insBuf. append( ") VALUES (" );

		bFrst  = true;
		nQs = cNames. size();
		for (iQ=0; iQ<nQs; iQ++)
		{
			if (!bFrst)
			{
				insBuf. append( ",?" );
			}
			else
			{
				insBuf. append( "?" );
				bFrst = false;
			}
		}

		insBuf. append( ")" );

		m_sInsert = new String( insBuf );
	}

	private void buildUpdateQuery()
	{
		StringBuffer        valBuf;
		StringBuffer        cstrBuf;

		String              sIKey = null;
		boolean             bFrstVal;
		boolean             bFrstCstr;

		Collection          cNames;
		Iterator            itName;
		String              sName;

		String              sCstr;

		valBuf = new StringBuffer( 1024 );
		cstrBuf = new StringBuffer( 1024 );

		valBuf. append( "UPDATE " );
		valBuf. append( m_SrcDef. getClassString() );
		valBuf. append( " SET " );

		cstrBuf. append( " WHERE " );

		bFrstVal = true;
		bFrstCstr = true;
		cNames = m_SrcDef. getVarNames();
		itName = cNames. iterator();
		while (itName. hasNext())
		{
			sName = (String) itName. next();
		    if (m_SrcDef. checkKey( sName ))
			{
				if (!bFrstCstr)
				{
					cstrBuf. append( " AND " );
				}
				else
				{
					sIKey = sName;
					bFrstCstr = false;
				}
				cstrBuf. append( sName );
				cstrBuf. append( " = ?" );
			}
			else
			{
				if (!bFrstVal)
				{
					valBuf. append( "," );
				}
				else
				{
					bFrstVal = false;
				}
				valBuf. append( sName );
				valBuf. append( " = ?" );
			}
		}

		sCstr = new String( cstrBuf );
		valBuf. append( sCstr );
		m_sUpdate = new String( valBuf );

		// Now apply constraint to duplicate key detect query.
		m_sDupChk = "SELECT " + sIKey +
					" FROM " + m_SrcDef. getClassString() +
					sCstr;
	}

	/**
		Determines whether the current record already exists, in earlier form,
		in the database.
		@return boolean         <code>true</code> if record found.
	 */
	private boolean checkDuplicate()
		throws SQLException, Exception
	{
		boolean         rslt;
		ResultSet       rs;

		rslt = false;
		if (m_SrcDef. checkUpdate())
		{
			bindDuplicate();
			rs = m_dupchk.executeQuery();
			rslt = rs.next();
			rs. close();
		}

		return rslt;
	}

	/*
		Binds key fields to the duplicate query.
		@exception Exception        If a field is invalid.
		@exception SQLException     If SQL binding fails.
	 */
    private void bindDuplicate()
		throws
			Exception,
			SQLException
    {
		SourceDefiner   def;
		ArrayList       names;
		ArrayList       keys;

		int             iKey;
		int             nKeys;
		String          sKey;
		int             iFld;

		def = getDefiner();
		names = def. getVarNames();
		keys = def. getKeyNames();
		nKeys = keys. size();
		for (iKey=0; iKey<nKeys; iKey++)
		{
			sKey = (String) keys. get( iKey );
			iFld = names. indexOf( sKey );
			bindValue(
						m_dupchk,
						iFld,
						iKey+1 );
		}
    }

	/*
		Binds source fields to SQL parameters.
		@exception Exception        If a field is invalid.
		@exception SQLException     If SQL binding fails.
	 */
	private void bindInsert()
		throws
			SQLException,
			Exception
	{
		int             iFld;
		int             nFlds;

		nFlds = getDefiner(). getVarCount();
		for (iFld=0; iFld<nFlds; iFld++)
		{
			bindValue(
						m_insrt,
						iFld,
						iFld+1 );
		}
	}

	/*
		Binds source fields to SQL parameters in update.
		@exception Exception        If a field is invalid.
		@exception SQLException     If SQL binding fails.
	 */
    public void bindUpdate()
		throws
			Exception,
			SQLException
	{
		int             iTypes[];
		ArrayList       names;
		int             iFld;
		int             nFlds;
		String          sName;
		int             iBnd;

		nFlds = getDefiner(). getVarCount();
		names = getDefiner(). getVarNames();
		iTypes = getDefiner(). getVarTypes();

		for (iFld=0; iFld<nFlds; iFld++)
		{
			sName = (String) names. get( iFld );
			iBnd = getDefiner(). getUpdateIndex( sName );

			bindValue(
						m_update,
						iFld,
						iBnd );
		}
    }

	public abstract void bindValue(
		    PreparedStatement       p_stmt,
			int                     p_iFld,
			int                     p_iBind )
		throws
			SQLException,
			Exception;

	/**
		Concludes JDBC operations. If the transfer was incomplete, we roll back
		changes.
		@param boolean          <code>true</code> if transfer is complete.
	 */
	private void terminateSQL(
			boolean     p_bComplete )
	{
		try {
			if (m_bAutoCommit == false)
			{
				if (p_bComplete)
				{
					m_sql. commit();
				}
				else
				{
					m_sql. rollback();
				}
			}
			SqlConnector.closeConn( m_sqlKey );
		}
		catch (SQLException sqle) {}
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
			accessLog( args[0] );
			convert();
		}
		catch (Exception ex) {
			System.out.println( ex. getMessage() );
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

		// Establish JDBC context.
		try {
			prepareSQL();
		}
		catch (SQLException sqle)
		{
			
			reportFault( sqle );
			return;
		}

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
				System.out.println( "NO RECORDS" );
			}
			else
			{
				long dT = new java.util.Date(). getTime() - start.getTime();
				System.out.println(
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
			SQLException,
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

	public abstract boolean processRecords()
		throws SourceException;

	public void postRecord()
	{
		String      sStmt = null;
		PreparedStatement		ps = null;

		try {
			if (!m_bSkip)
			{
				// Attempt to upload record to SQL.
				if (m_SrcDef. checkUpdate() &&
					checkDuplicate())
				{
					sStmt = m_sUpdate;
					bindUpdate();
					ps = m_update;
				}
				else
				{
					sStmt = m_sInsert;
					bindInsert();
					ps = m_insrt;
				}
				ps.execute();
				m_iRecords++;
				if (null != m_log)
				{
					m_log.m_records++;
				}
			}

			// Process remaining records.
			m_bSkip = false;
		}
		catch (SQLException sqle) {
			//Dump data for later analysis.
			if (null != m_log)
			{
				m_log.m_errors++;
				if (10 > m_log.m_errors)
				{
					SqlConnector. dumpSqlExceptions(
								sStmt,
								sqle );
				}
			}
		    else
			{
				SqlConnector. dumpSqlExceptions(
							sStmt,
							sqle );
			}
			dumpRecord();
		}
		catch (Exception e) {
			// Diagnostics for other errors.
			e. printStackTrace();
			if (null != m_log) m_log. m_errors++;
		}

		// Periodically write status checkpoint to the log, and monitor
		// for operator-requested termination.
		if ((null != m_log) &&
			(0 == (m_log. m_records % 1000)))
		{
			updateLog();
		}
	}

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

		System.out.println(
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

	/* Check for external stop request. */
	public abstract File getStopfilePath();

	public boolean checkContinue()
	{
		if ((null != m_log) &&
			(0 == (m_log. m_records % 1000)))
		{
			if (getStopfilePath(). exists())
			{
				reportFault(
					"== ABORTED AT OPERATOR REQUEST ==\n" );
				return false;
			}
		}
		return true;
	}
}
