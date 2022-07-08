/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/ai/infinity/ISAMConverter.java,v 1.1 2010/10/19 00:43:14 secwind Exp $
==================================================================================================*/
package com.duckdigit.ai.infinity;

import java.io.IOException;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.duckdigit.isam.*;
import com.duckdigit.util.*;
import com.duckdigit.ai.*;

/**
	This tabulated role defines the mechanisms used to transfer data from a
	BASIX ISAM record file to a SQL database.
	<p>
	Specializations of this role supply the following data, necessary to the
	conversion:
	<OL><LI>
	File and index file names.</LI><LI>
	Record layouts for both index and data files.</LI><LI>
	Range and filter strings used by the ISAM server to select records to
	be processed.</LI><OL>
	While a <code>main</code> method is supplied here, the arguments are simply
	cached as substitution values for a {@lookup com.duckdigit.forms.TagParser}.
	The parameter format is [key]=[balue]. Keys processed by the
	<code>ISAMConverter</code> are:
	<OL><LI>
	A <code>database</code> value, which selects a target database from the
	system database.ini file.</LI><LI>
	An optional <code>log</code> value, specifying an output path for status
	messages.</LI></OL>
	Specializations are expected to establish additional parameter requirements.
	<p>
	Services subject to specialization include:
	<OL>
	<LI>getFile</LI>
	<LI>getLayout</LI>
	<LI>getIndexFile</LI>
	<LI>getIndexLayout</LI>
	<LI>getVarNames</LI>
	<LI>getVarDecls</LI>
	<LI>initializeIndex</LI>
	<LI>getRangeTemplate</LI>
	<LI>getFilterTemplate</LI>
	<LI>getInsertStatement</LI>
	</OL>
 */

public abstract class ISAMConverter
{
	//===== Parameter management.
	static final int 		PCLEN = 6;
	static final int 		COLEN = 6;

	/*  Tag prefix. */
	private static final String TAG_PREFIX = "{sub:";
	/*  Tag suffix. */
	private static final String TAG_SUFFIX = "}";
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
	/* Cono key. */
	private static final String CONO_TAG = TAG_PREFIX + "@cono" + TAG_SUFFIX;
	/* Pcno key. */
	private static final String PCNO_TAG = TAG_PREFIX + "@pcno" + TAG_SUFFIX;

	/*  Subsitution engine for command-line parameters. */
	private TagParser       m_parser;

	//===== Logging.

	private AILogger     m_log;

	//===== SQL Context.

	/* Connection and prepared statement. */
	private Connection      m_sql;
	private PreparedStatement
							m_insrt;

	//===== ISAM context.

	/*  Connection parameters. */
	private static final String HOST_NAME = "iserve";
	private static final int PORT_NUMBER = 3123;
	private static final String SERVER_IP = "iserve";
	private static final String USER_NAME = "bbalke";
	private static final String PASSWORD = "bbalke";
	/*  Allowed attempts to connect. */
	private static final int ALLOWED_TRIES = 8;
	/*  Baseline delay between tries. This is the root of a geometric
		progression limited by MAX_WAIT_MILLIS. */
	private static final int SEED_WAIT_MILLIS = 30 * 1000; //2 * 1000;
	/* Maximum wait time. */
	private static final int MAX_WAIT_MILLIS = 5 * 60 * 1000;
	/* ISAM failure counter. */
	private int n_iISAMFailures = 0;
	/* Active wait interval. */
	private int m_nxtWait;

	/*  Active random file. */
	private RandomFile      m_rf;
	/*  Field definitions. */
	private HashMap         m_varMap;
	/*  Field list. */
	private ArrayList       m_vars;
	/*  Context retained following access failure */
	private RandomFile      m_rtnFile;
	private HashMap         m_rtnVarMap;
	private ArrayList       m_rtnVars;

	private int             m_iCacheSize = 200;

	/*==========================================================================
	===== Configuration
	==========================================================================*/

	/**
		Prepare conversion context.
	 */
	public ISAMConverter()
	{
        m_rf = null;
		m_rtnVars = null;
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

		svp = new StringValueProvider( ARG_PROV_NAME );
		m_parser = new TagParser( TAG_PREFIX, TAG_SUFFIX );
		m_parser. addValueProvider( svp );

		nArgs = args.length;
		for (iArg=0; iArg<nArgs; iArg++ )
		{
			sArg = args[ iArg ];
			iSep = sArg.indexOf( VALUE_SEP );
			if (-1 != iSep)
			{
				svp. addString(
							sArg. substring( 0, iSep ),
							sArg. substring( iSep + 1 ) );
			}
		}
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
		Provide a table identifier for error processing. Must be defined in
		specializations.
		@return String          Table description.
	 */
	public abstract String getClassString();

	/*==========================================================================
	===== Status logging.
	==========================================================================*/

	/**
		Sets up the log file for the conversion. It assumes the <code>log</code>
		key is assigned, and attempts to access the company and profit center
		keys.
		@exception IOException
	 */
	public void prepareLog()
		throws IOException
	{
		String      sPath;

		sPath = substitute( LOG_TAG );
		if (null == sPath) return;
		if (0 == sPath. length()) return;

		m_log = new AILogger( sPath );
		m_log. setItem(
				StringUtils.spacePadTextLeft(
							substitute( CONO_TAG ),
							COLEN) +
				StringUtils.spacePadTextLeft(
							substitute( PCNO_TAG ),
							PCLEN ),
				getClassString() );
	}

	public void reportFault(
			Exception       p_ex )
	{
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

	public void closeLog()
	{
		updateLog();
		if (null != m_log)
		{
			try {
				m_log. close();
			}
			catch (IOException ioe) {}
		}
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
		m_sql = SqlConnector.getConn(
						substitute( DATABASE_TAG ) );
		m_sql. setAutoCommit( false );
		m_insrt = m_sql. prepareStatement(
						getInsertQuery() );
	}

	/**
		Service template to be defined by specializations to supply the syntax
		for the insert statement, in JDBC format, for data upload to the
		database.
		@returns String         Insert query.
	 */
	public abstract String getInsertQuery();

	/*
		Binds ISAM variables to SQL parameters.
		@exception Exception        If a BVar is invalid.
		@exception SQLException     If SQL binding fails.
	 */
	private void bindSQL()
		throws
			SQLException,
			Exception
	{
		int             iVar;
		Iterator        itVars;
		BVar            bvar;
		int             iType[];
		java.util.Date  jul;

		iType = getVarTypes();

		iVar = 1;
		itVars = m_vars. iterator();
		while (itVars. hasNext())
		{
			bvar = (BVar) itVars. next();
			switch (iType[ iVar - 1 ])
			{
			case Types.INTEGER:
				m_insrt.setInt( iVar, bvar. getInt() );
				break;
			case Types.DOUBLE:
				m_insrt.setDouble( iVar, bvar. getDouble() );
				break;
			case Types.VARCHAR:
			case Types.CHAR:
				m_insrt.setString( iVar, bvar. getString() );
				break;
			case Types.DATE:
				jul = bvar. julianToDate();
				if (null == jul)
				{
					m_insrt.setNull( iVar, Types.DATE );
				}
				else
				{
					m_insrt.setDate(
								iVar,
								new java.sql.Date(
											jul. getTime() ) );
				}
				break;
			default:
				// Will probably throws an exception.
				m_insrt.setNull( iVar, Types.OTHER );
			}
			iVar++;
		}
	}

	/**
		Concludes JDBC operations. If the transfer was incomplete, we roll back
		changes.
		@param boolean          <code>true</code> if transfer is complete.
	 */
	private void terminateSQL(
			boolean     p_bComplete )
	{
		try {
			if (p_bComplete)
			{
				m_sql. commit();
			}
			else
			{
				m_sql. rollback();
			}
			m_sql. close();
		}
		catch (SQLException sqle) {}
	}

	/*==========================================================================
	===== ISAM Operations.
	==========================================================================*/

	/**
		Over-ride the record cache size.
	 */
	public void setCacheSize(
			int         p_iSize )
	{
		m_iCacheSize = p_iSize;
	}

	/*
		Open ISAM file and map its fields.
		@param IsamClient       Connection to iserve.
		@returns RandomFile     <code>null</code> if preparation fails.
	 */
	private RandomFile prepareRandomFile(
			IsamClient      p_Clt )
		throws BasixDeclException
	{
		boolean     bOpen;

		bOpen = false;
		try {
			m_rf = p_Clt. getRandomFile(
									getFile(),
									getLayout(),
									m_iCacheSize );
			m_rf. addIndex(
						getIndexFile(),
						getIndexLayout() );
			m_rf. open();
			bOpen = true;
			establishVariables( m_rf );
		}
		catch (Exception ex) {
			if (bOpen)
			{
				try {
					m_rf. close();
				}
				catch (Exception e) {}
			}
			reportFault( ex );
			return null;
		}

		return m_rf;
	}

    /**
      Closes random file.
     */
    public void terminateISAM()
    {
        try {
            if (null != m_rf) m_rf. close();
        }
        catch (IsamException ise) {}
    }

	/*
		Declares the target ISAM file. To be defined by specializations.
		@param String       File name.
	 */
	public abstract String getFile();

	/*
		Declares the layout of the target file. To be defined by
		specializations.
		@param String       File Layout (J$,J,J1,J2).
	 */
	public abstract String getLayout();

	/*
		Declares the target index file. To be defined by specializations.
	 */
	public abstract String getIndexFile();

	/*
		Declares the index layout. To be defined by specializations.
	 */
	public abstract String getIndexLayout();

	/*
		Allocate ISAM fields and establish initial values for the index
		fields. On first entry, the index fields will be set by the
		specialization. On subsequent entries, the values are copied from the
		last successful record transfer.
		@param RandomFile       Working ISAM file.
		@exception Exception    If variable setup fails.
	 */
	 private void establishVariables(
			RandomFile      p_rf )
		throws
			Exception,
			BasixDeclException
	{
		createVariables( p_rf );
		if (null == m_rtnVars)
		{
			initializeIndex();
		}
		else
		{
			continueRange();
		}
	}

	/*
		Allocate ISAM fields needed to capture transfer data from the ISAM
		records.
		@param RandomFile       Working ISAM file.
	 */
	private void createVariables(
			RandomFile      p_rf )
			throws BasixDeclException
	{
		Iterator        itNames;
		Iterator        itDecls;
		BVar            bvar;

		m_vars = new ArrayList();
		m_varMap = new HashMap();

		itNames = getVarNames(). iterator();
		itDecls = getVarDecls(). iterator();

		while (itDecls. hasNext())
		{

			bvar = p_rf. makeVar(
							(String) itDecls. next() );
			m_vars. add( bvar );
			m_varMap. put(
							(String) itNames. next(),
							bvar );
		}
	}

	/*
		Identifies the transfer fields. To be defined by specializations.
	 */
	public abstract ArrayList getVarNames();

	/*
		Declares the field allocations. To be defined by specializations.
	 */
	public abstract ArrayList getVarDecls();

	/*
		Declares the field types, as per {@lookup java.sql.Types}. To be defined
		by specializations.
	 */
	public abstract int[] getVarTypes();

	/**
		Define the initial index variable values. To be defined by
		specializations.
		@exception Exception
	 */
	public abstract void initializeIndex()
		throws Exception;

	/**
		Update the index to skip nonessential records. To be defined by
		specializations.
		@exception Exception
	 */
	public boolean advanceIndex()
		throws Exception
	{
		return false;
	}

	/*
		Transfer retained ISAM state to the new search variables.
		@exception IsamException
	 */
	 public void continueRange()
		throws Exception
	{
		Iterator        itRtn;
		Iterator        itVar;
		BVar            rtnBvar;
		BVar            bvar;

		itRtn = m_rtnVars. iterator();
		itVar = m_vars. iterator();

		while (itRtn.hasNext())
		{
			rtnBvar = (BVar) itRtn. next();
			bvar = (BVar) itVar. next();
			bvar.set( rtnBvar. getString() );
		}
	}

	/**
		Set a variable value.
		@param String[1]        Variable name.
		@param String[2]        Variable value.
		@exception Exception    Reporting conversion failure.
	 */
	 public void setVariable(
			String      p_name,
			String      p_val )
		throws Exception
	{
		BVar        bvar;

		bvar = (BVar) m_varMap. get( p_name );
		if (null != bvar)
		{
			bvar. set( substitute( p_val ));
		}
	}

	/*
		Dump current ISAM record for diagnosis.
	 */
	private void dumpISAM()
	{
		StringBuffer        msg;
		Iterator        itVars;
		BVar            bvar;
		int             iVar;
		int             iType[];

		msg = new StringBuffer( 20 * m_vars.size() );
		msg. append( "Data: " );

		iType = getVarTypes();

		itVars = m_vars. iterator();
		iVar = 0;
		while (itVars. hasNext())
		{
			bvar = (BVar) itVars. next();
			try {
				switch (iType[ iVar ])
				{
				case Types.INTEGER:
					msg. append( bvar. getInt() );
					break;
				case Types.DOUBLE:
					msg. append( bvar. getDouble() );
					break;
				case Types.VARCHAR:
				case Types.CHAR:
					msg. append( bvar. getString() );
					break;
				case Types.DATE:
					msg. append( bvar. julianToDate() );
					break;
				}
			}
			catch (Exception ex)
			{
				msg. append( "*ERR*" );
			}
			msg. append( " | " );
			iVar++;
		}
		msg. append( "\n" );
		System.out.println( msg. toString() );
	}

	/*==========================================================================
	===== File processing.
	==========================================================================*/

	/**
		Process records in the associated database. Attempt to recover from
		ISAM record access faults, which are assumed to be network access
		failures, up to a limit of ALLOWED_TRIES failures.
	 */
	public void convert()
	{
		IsamClient      ic;
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

		// Setup ISAM connection.
		ic = new IsamClient( HOST_NAME, PORT_NUMBER );

		seedWait();
		bTry = true;
		while (bTry)
		{
			try {
				if (null == m_rtnVars)
				{
					// Full connect first time through.
					ic. connect(
							USER_NAME,
							PASSWORD,
							SERVER_IP );
				}
				else
				{
					// Reconnect on recovery.
					ic. reconnect();
				}
				if (processFile( ic ))
				{
					// Successful completion.
					bCommit = true;
                    terminateISAM();
					break;
				}
			}
			catch (IsamException ise) {
				reportFault( ise );
			}
			catch (Exception ex) {
				reportFault( ex );
				break;
			}

			// Here if ISAM processing failure. Release the comm socket, and
			// wait a while before retesting connection.
			ic. disconnect();
            terminateISAM();
			bTry = waitForRecovery();
			if (!bTry) {
				reportFault(
					"Cannot maintain communications with the ISERVE." );
			}
			updateLog();
		}

		// Clean up context on completion.
		ic. disconnect();
		terminateSQL( bCommit );
		closeLog();

		// Print statistics if job completed.
		if (bCommit)
		{
			if (0 == m_log. m_records)
			{
				System.out.println( "NO RECORDS" );
			}
			else
			{
				long dT = new java.util.Date(). getTime() - start.getTime();
				System.out.println(
						getClassString() + " " +
						m_log. m_records + " " +
						dT + " ms\n" );
			}
		}
	}

	/**
		Retrieve records from the ISAM file and write them to the SQL database.
		<p>
		As records are processed, we monitor for external stop requests. If
		detected, we stop processing with a "finished" status, causing results
		to this point to be saved in the database.
		@param IsamClient       Target file.
		@returns boolean        <code>true</code> if file processing was
									successfully completed.
								<code>false</code> if an ISAM access failure
									occurs during processing.
		@exception SQLException If JDBC operation fails.
	 */
	private boolean processFile(
			IsamClient      p_ic )
		throws
			SQLException,
			Exception
	{
		int             iRecords;
		boolean         bSkip;

		iRecords = 0;

		// Access target ISAM file.
		if (null == prepareRandomFile( p_ic )) return false;

		// Select ISAM records.
		if (!selectISAM()) return false;

		try {
			// If we are resuming after a connection failure, skip the first
			// result record.
			bSkip = null != m_rtnVars;

			while (m_rf. next())
			{
				try {
					if (!bSkip)
					{
						// Attempt to upload record to SQL.
						bindSQL();
						m_insrt.execute();
						iRecords++;
						if (null != m_log)
						{
							m_log.m_records++;
						}
					}

					// Skip over secondary records.
					if (advanceIndex())
					{
						if (!selectISAM())
						{
							// ISAM processing problem.
							cacheRecoveryContext();
							return false;
						}
					}

					// Process remaining records.
					bSkip = false;
				}
				catch (SQLException sqle) {
					// SQL error. Dump data for later analysis.
					if (null != m_log)
					{
						m_log.m_errors++;
					}
					SqlConnector. dumpSqlExceptions(
									getInsertQuery(),
									sqle );
					//dumpISAM();
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
					if (checkExternalStop())
					{
						reportFault(
							"== DATAX ABORTED AT OPERATOR REQUEST ==\n" );
						return true;
					}
				}
			}
		}
		catch (IsamException ie) {
			// Cache recovery context.
			cacheRecoveryContext();
			return false;
		}

		return true;
	}

	/*
		Select target records in the ISAM file.
		@returns boolean        <code>false</code> if select fails.
	 */
	private boolean selectISAM()
	{
		try {
			m_rf. select(
						getIndexFile(),
						substitute( getRangeTemplate() ),
						substitute( getFilterTemplate() ) );
		}
		catch (IsamException ie) {
			return false;
		}

		return true;
	}

	/*
		Save ISAM upload context for recovery when connection is restored.
	 */
	private void cacheRecoveryContext()
	{
		m_rtnFile = m_rf;
		m_rtnVars = m_vars;
		m_rtnVarMap = m_varMap;
	}

	/**
		Define range criteria for the ISAM selection. To be defined by
		specializations.
	 */
	public abstract String getRangeTemplate();

	/**
		Define filter criteria for the ISAM selection. To be defined by
		specializations.
	 */
	public abstract String getFilterTemplate();

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
		n_iISAMFailures++;
		if (ALLOWED_TRIES == n_iISAMFailures) return false;

		try {
			Thread.sleep( m_nxtWait );
		}
		catch (Exception ex) {}

		m_nxtWait = Math.min( 2*m_nxtWait, MAX_WAIT_MILLIS );

		return true;
	}

	/* Check for external stop request. */
	private File m_stopFile = new File( "/home/ai/stop" );

	private boolean checkExternalStop()
	{
		return m_stopFile. exists();
	}
}
