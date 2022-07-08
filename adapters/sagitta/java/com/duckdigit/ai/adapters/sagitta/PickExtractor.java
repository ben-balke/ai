/***********************************************************************************
*** PICK EXTRACTOR
*** Copyright (c) DuckDigit Technologies, 2009
*** Description:
***********************************************************************************/
package com.duckdigit.ai.adapters.sagitta;

import java.io.File;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.duckdigit.pick.PVar;
import com.duckdigit.pick.PickClient;
import com.duckdigit.pick.PickFile;
import com.duckdigit.pick.PickException;
import com.duckdigit.util.lang.StringUtils;

/**
 *	Generates postgres copy command from a pick file and writes to standard out.
 *	It assume the table exists and does not drop or create.  Fields are truncated,
 *	with warnings.  Three arguments can be passed to the extractor:
 *  <ul>
 *		<li>-hostname <div>pick or universe database server to connect to.</div></li>
 *		<li>-inbufsize <div>size of the input buffer to allocate for incoming 
 			communications with the server.  This buffer must be as big as the 
			largest record * the number of records transfer with each next call.  
			If this buffer is too small an array out of bounds exception will occur.  
			Recommended size is 200K at least.</div></li>
 *		<li>-outbufsize  <div>.</li>
 *	</ul>
 **/
public class PickExtractor extends DataExtractor implements com.duckdigit.pick.ConversionObserver
{
		/** 
		 * Argument token that identifies the hostname to connect too.
		 **/
	private static final String HOSTNAME_TAG = "hostname";
	private static final String OFFICE_TAG = "office";
		/** 
		 * Argument token that identifies the hostname to connect too.
		 **/
	private static final String OUTBUFSIZE_TAG = "outbufsize";
	private static final String INBUFSIZE_TAG = "inbufsize";

		/** 
		 * Holds the name of the current field being parsed from the Def file.
		 **/
	public String		 	m_curField = null;
		/**
		 * Size of maximum length of the current field.
		 */
	private int		 		m_curMaxLen = 0;
	public int getMaxSize () { return m_curMaxLen; }

		/**
		 * Pick Definer parses the gen file with the field definitions in it.
		 */
	private PickDefiner     m_PickDef;

		/*  
		 * Default Connection parameters.
		 */
	private static final String HOST_NAME = "pickserve";
	private static final int 	PORT_NUMBER = 5234;
	private static final String USER_NAME = "pickserve";
	private static final String PASSWORD = "version9";

		/**
		 * Active pick connection. */
	private PickClient      m_Client;
		/**
		 * Active pick file created through the Pickclient 
		 */
	private PickFile      	m_pf;
	
		/*
		 * Field definitions mapped for search.by name.
		 */
	private HashMap         m_varMap;
	
		/**
		 * Field list loaded from the gen file
		 **/
	private ArrayList       m_vars;

	/*  Context retained following access failure */
	private PickFile      	m_rtnFile;
	private HashMap         m_rtnVarMap;
	private ArrayList       m_rtnVars;
		/**
		 * input buffer size.  Default is 100K.  This determines the size of the 
		 * buffer allocated to receive data from the pick server.
		 */
	private int 			m_inBufSize = 100 * 1024;
		/**
		 * output buffer size.  Default is 100K.  This determines the size of the 
		 * buffer allocated to construct packets to be written to the pick server.
		 */
	private int 			m_outBufSize = 100 * 1024;

	/*****************************************************
	*** ConversionObserver methods are over written here.
	******************************************************/
		/**
		 * Reports a truncation to stderr.
		 */
	public void reportTruncation (String sValue) 
	{ 
		System.err.println ("WARNING: " + m_curField + " [" + sValue + "] trunctated " + sValue.length () + " to " + m_curMaxLen);
		recordWarning ();
	}
		/**
		 * Reports a truncation to stderr.
		 */
	public void reportBadNumber (String sValue, String msg) 
	{ 
		if (sValue.equals ("-") || sValue.equals (""))
				return;
		System.err.println ("WARNING: " + m_curField + " [" + sValue + "] bad number " + msg );
		recordWarning ();
	}
		


		/**
		 * Constructor.  Does nothing but initialize a couple of internal variables
		 */
    public PickExtractor() 
	{
		m_Client = null;
		m_rtnVars = null;
    }

		/**
		 * Identifys the PickDefiner to use.  The definer provides the list 
		 * of fields to process.
		 */
	public void identifyDefiner(
		    PickDefiner     p_PickDef )
	{
		m_PickDef = p_PickDef;
	}

	public String constructLogHeader()
	{
		return "";
	}


	/*==========================================================================
	===== Source Management.
	==========================================================================*/

    public void establishConnection()
		throws SourceException
	{
		int			size;
		try {
			String pickserve = m_properties.getProperty ( HOSTNAME_TAG );
			if (pickserve == null || pickserve.length () == 0)
			{
				pickserve = HOST_NAME;	
			}

			String sInSize = m_properties.getProperty ( INBUFSIZE_TAG );
			if (sInSize != null && sInSize.length () > 0)
			{
				try
				{
					size = Integer.parseInt (sInSize);
					m_inBufSize = size;
				}
				catch (Exception ex) {}
			}

			String sOutSize = m_properties.getProperty ( OUTBUFSIZE_TAG );
			if (sOutSize != null && sOutSize.length () > 0)
			{
				try
				{
					size = Integer.parseInt (sOutSize);
					m_outBufSize = size;
				}
				catch (Exception ex) {}
			}

			if (null == m_Client)
			{
					/* 
					 * We swap the in and out buffer sizes so that the pick server
					 * has corresponding buffers to receive and send packets.
					 */
	    	    m_Client = new PickClient( pickserve, PORT_NUMBER, m_inBufSize, m_outBufSize);
			}
			System.err.println ("Connecting to: " + pickserve + " OutBufSize: " + m_outBufSize + " InBufSize: " + m_inBufSize);

			m_Client. connect(
							USER_NAME,
							PASSWORD,
							pickserve );
		}
		catch (PickException ise) 
		{
			ise.printStackTrace ();
		    throw new SourceException(
						    ise. getMessage(),
							ise );
		}
    }

    public void recoverConnection()
		throws SourceException
	{
		try {
			if (null != m_Client)
			{
			    m_Client. reconnect();
			}
		}
		catch (PickException ise) {
		    throw new SourceException(
						    ise. getMessage(),
							ise );
		}
    }

    public void closeConnection()
	{
		if (null != m_Client)
		{
			m_Client. disconnect();
		}
    }

    public void prepareSource()
		throws SourceException
	{
		try {
			m_pf = m_Client. getPickFile(
									m_PickDef. getFile(),
									m_PickDef. getMaxRecordSize(),
									m_PickDef. getCacheSize() );
		}
		catch (Exception ex) {
			throw new SourceException(
							ex.getMessage(),
							ex );
		}
    }

    public void terminateSource()
	{
        try {
            if (null != m_pf) m_pf. close();
        }
        catch (PickException ise) {}
    }

	/*==========================================================================
	===== Field Operations.
	==========================================================================*/

	public void createFields(
		    SourceDefiner       p_SrcDef )
		throws Exception
	{
		Iterator        itNames;
		Iterator        itDecls;
		PVar            pvar;
		int				subFieldPos = 0;

		m_vars = new ArrayList();
		m_varMap = new HashMap();

		itNames = p_SrcDef. getVarNames(). iterator();
		itDecls = p_SrcDef. getVarDecls(). iterator();

		while (itDecls. hasNext())
		{
			subFieldPos = 0;
			String 	sFieldPos =  (String) itDecls.next ();
			int		nFieldPos;
			if (sFieldPos.equals ("rowid"))
			{
				nFieldPos = PVar.PICK_FIELD_ROWID;
			}
			else
			{
				int subidx = sFieldPos.indexOf ('.');
				if (subidx != -1)
				{
					nFieldPos = Integer.parseInt (sFieldPos.substring (0, subidx)) - 1;
					subFieldPos = Integer.parseInt (sFieldPos.substring (subidx + 1));
				}
				else
					nFieldPos = Integer.parseInt (sFieldPos) - 1;
			}
			//System.err.println ("Adding Field: " + nFieldPos + " sub: " + subFieldPos);
			pvar = m_pf. makeVar (nFieldPos, subFieldPos);
			m_vars. add( pvar );
			m_varMap. put(
							(String) itNames. next(),
							pvar );
		}
		m_pf. open();
	}

    public void cacheRecoveryContext()
	{
		m_rtnFile = m_pf;
		m_rtnVars = m_vars;
		m_rtnVarMap = m_varMap;
    }

	public boolean checkCache()
	{
		return null != m_rtnVars;
	}

    public void dumpRecord()
	{
		StringBuffer        msg;
		Iterator        itVars;
		PVar            pvar;
		int             iVar;
		int             iType[];

		msg = new StringBuffer( 20 * m_vars.size() );
		msg. append( "Data: " );

		iType = getDefiner(). getVarTypes();

		itVars = m_vars. iterator();
		iVar = 0;
		while (itVars. hasNext())
		{
			pvar = (PVar) itVars. next();
			try {
				switch (iType[ iVar ])
				{
				case Types.INTEGER:
					msg. append( pvar. getInt() );
					break;
				case Types.DOUBLE:
					msg. append( pvar. getDouble() );
					break;
				case Types.VARCHAR:
				case Types.CHAR:
					msg. append( pvar. getString() );
					break;
				case Types.DATE:
					msg. append( pvar. getString() );
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
		//System.err.println( msg. toString() );
    }

	/*==========================================================================
	===== Query Processing.
	==========================================================================*/

	public void initializeIndex()
		throws Exception
	{
	}

	public boolean advanceIndex()
	{
		return false;
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
		PVar        pvar;

		pvar = (PVar) m_varMap. get( p_name );
		if (null != pvar)
		{
			pvar. set( m_properties.getProperty ( p_val ), null );
		}
	}

    public void continueRange()
		throws Exception
	{
		Iterator        itRtn;
		Iterator        itVar;
		PVar            rtnPvar;
		PVar            pvar;

		itRtn = m_rtnVars. iterator();
		itVar = m_vars. iterator();

		while (itRtn.hasNext())
		{
			rtnPvar = (PVar) itRtn. next();
			pvar = (PVar) itVar. next();
			pvar.set( rtnPvar. getString() );
		}

		enableSkip();
    }

    public boolean selectRecords()
	{
		try {
		/* BBB Need to work with filters. */
		/*
		 *	String		sBasic = m_properties.getProperty ( m_PickDef. getFilterTemplate() ) );
		 *	if (sBasic != null)
		 *	{
		 *		m_pf. select(sBasic);
		 *	}
		 *	else
		 *	{
		 *	}
		 */
			m_pf. select();
		}
		catch (PickException ie) {
			return false;
		}

		return true;
    }

	public boolean processRecords()
		throws SourceException,Exception
	{
		boolean     bRcd;

		bRcd = checkRecord();
		while (bRcd && checkContinue())
		{
			outputRecord();

			// Skip over secondary records.
			if (advanceIndex())
			{
				if (!selectRecords())
				{
					// Source processing problem.
					cacheRecoveryContext();
					return false;
				}
			}

			bRcd = checkRecord();
			logRecord ();
		}

		return !bRcd;
	}

    public boolean checkRecord()
		throws SourceException
	{
		try {
			return m_pf. next();
		}
		catch (PickException ise) {
		    throw new SourceException(
						ise. getMessage(),
						ise );
		}
     }

	/*
	 * This binds a field to the prepared statement.  Multivalue fields are
	 * bound as arrays strings.
	 */
	public String getBulkValue(
			int                     p_iFld,
			int                     p_iBnd )
		throws
			Exception
	{
		boolean         bNull;

		int             iBnd;
		PVar            pvar;
		String			other;
						/* Formatting etc that follows sql type.
						 */

		int             iType;

		m_curField = getDefiner(). getVarNames().get (p_iFld).toString ();
		iType = getDefiner(). getVarTypes() [p_iFld];
		m_curMaxLen = getDefiner(). getVarLengths() [p_iFld]; 
		pvar = (PVar) m_vars. get( p_iFld );

			/*
			 * Check for a multivalue field.  Make an SQL array representation
			 * of the values and bind it as a string.  
			 * BBB This is currently only compatible with Postgres (i think).
			 */
		other = (String) ((DefParser) getDefiner ()).getOtherDecls().get (p_iFld);
		//System.err.println ("bindValue[" + p_iFld + ": " + other);
		if (other != null && other.indexOf ("MV") != -1)
		{
			int			ptype;
			switch (iType)
			{
			case Types.VARCHAR: 	ptype = PVar.PICK_STRING; break;
			case Types.INTEGER: 	ptype = PVar.PICK_INT; break;
			case Types.DOUBLE: 		ptype = PVar.PICK_REAL; break;
			case Types.CHAR: 		ptype = PVar.PICK_STRING; break;
			case Types.DATE: 		ptype = PVar.PICK_DATE; break;
			default:
					// Will probably throw an exception.
				return "\\N";
			}
			if (pvar.getString ().trim().length () == 0)
			{
				return "\\N";
			}
			else
			{
					/*
					 * Pass this object as the TruncationObserver 
					 * so that we can record the trunction.
					 */
				return pvar.makeArray (ptype, other, this);
			}
		}
		else
		{
			String			sValue;
			StringBuffer	sb;
				/*
				 * Just bind it as a single field.
				 */
			switch (iType)
			{
			case Types.INTEGER:
				return new Integer (pvar.getInt ()).toString ();
			case Types.DOUBLE:
				return new Double (pvar.getDouble ()).toString ();
			case Types.VARCHAR:
			case Types.CHAR:
				sValue = pvar.getString ();
				if (sValue.indexOf (com.duckdigit.pick.TextFormatter.VALUESEP) != -1)
				{
						/**
						 * If the field is configured to specifically
						 * strip value separators then do it without warning.
						 */
					if (other == null || other.indexOf ("STVS") == -1)
					{
						System.err.println ("WARNING: NON MV field " + m_curField + " stripped value separators: " + sValue);
						recordWarning ();
					}
						/**
						 * Strip any value separators.
						 */
					sValue = StringUtils.removeChar (sValue, 
						com.duckdigit.pick.TextFormatter.VALUESEP);
				}
				if (sValue.length () > m_curMaxLen)
				{
					reportTruncation (sValue);
					sValue = sValue.substring (0, m_curMaxLen - 1);
				}
				return sValue;
			case Types.DATE:
				try
				{
					Date	d = pvar.getDate ();
					if (d == null)
					{
						return "\\N";
					}
					GregorianCalendar calendar = new GregorianCalendar();
					calendar.setTime (d);
					int year = calendar.get(Calendar.YEAR);
					if (year > 2100 || year < 1200)
					{
						System.err.println ("WARNING: date year out of range " + year);
					}
					return StringUtils.zeroPadDecLeft (calendar.get (Calendar.YEAR), 4) + "-" + (calendar.get (Calendar.MONTH) + 1) + "-" + calendar.get (Calendar.DAY_OF_MONTH);
				}
				catch (Exception ex)
				{
					ex.printStackTrace ();
					throw ex;
				}
			default:
				// All others return a null.
				return "\\N";
			}
		}
	}

	public static void main(
		    String      args[] )
	{
		PickExtractor        IExt;
		SagittaDefiner        SDef;

		PVar.enableCopyOutput (true);
		IExt = new PickExtractor();
		SDef = new SagittaDefiner( args[0] );
		IExt. identifyDefiner( SDef );

		try {
		    SDef. parseDefFile();
			IExt. extract( args, SDef );
			IExt. finishLog();
		}
		catch (Exception ex) {
			System.err.println( ex. getMessage() );
			ex. printStackTrace();
		}
	}
}
