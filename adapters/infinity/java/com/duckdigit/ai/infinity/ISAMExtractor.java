package com.duckdigit.ai.infinity;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.duckdigit.isam.*;
import com.duckdigit.util.*;
import com.duckdigit.ai.*;

public class ISAMExtractor extends DataExtractor
{
	//===== Custom configuration.

	/* Cono key. */
	private static final String CONO_TAG = TAG_PREFIX + "@cono" + TAG_SUFFIX;
	/* Pcno key. */
	private static final String PCNO_TAG = TAG_PREFIX + "@pcno" + TAG_SUFFIX;
	/* Iserve key. */
	private static final String ISERVE_TAG = TAG_PREFIX + "-iserve" + TAG_SUFFIX;
	/* Office TAG */
	private static final String OFFICE_TAG = TAG_PREFIX + "-office" + TAG_SUFFIX;

	static final int 		PCLEN = 6;
	static final int 		COLEN = 6;

	ISAMDefiner     m_ISAMDef;

	//===== ISAM context.

	/*  Connection parameters. */
	private static final String HOST_NAME = "iserve";
	private static final int PORT_NUMBER = 3123;
	private static final String USER_NAME = "bbalke";
	private static final String PASSWORD = "bbalke";

	/*  ISAM Connection. */
	private IsamClient      m_Client;
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

    public ISAMExtractor() {
		m_Client = null;
		m_rtnVars = null;
    }

	public void identifyDefiner(
		    ISAMDefiner     p_ISAMDef )
	{
		m_ISAMDef = p_ISAMDef;
	}

	public String constructLogHeader()
	{

		return StringUtils. spacePadTextLeft(
						substitute( CONO_TAG ), COLEN ) +
				StringUtils. spacePadTextLeft(
					    substitute( PCNO_TAG ), PCLEN );
	}

	/* Check for external stop request. */
	private File m_stopFile = new File( "/home/duckdigit/stop" );

    public File getStopfilePath()
	{
		return m_stopFile;
    }

	/*==========================================================================
	===== Source Management.
	==========================================================================*/

    public void establishConnection()
		throws SourceException
	{
		try {
			String iserve = substitute( ISERVE_TAG );
			if (iserve == null || iserve.length () == 0)
			{
				iserve = HOST_NAME;	
			}

			if (null == m_Client)
			{
	    	    m_Client = new IsamClient( iserve, PORT_NUMBER );
			}

			m_Client. connect(
							USER_NAME,
							PASSWORD,
							iserve );
		}
		catch (IsamException ise) {
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
		catch (IsamException ise) {
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
			m_rf = m_Client. getRandomFile(
									m_ISAMDef. getFile(),
									m_ISAMDef. getLayout(),
									m_ISAMDef. getCacheSize() );
			m_rf. addIndex(
						m_ISAMDef. getIndexFile(),
						m_ISAMDef. getIndexLayout() );
			m_rf. open();
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
            if (null != m_rf) m_rf. close();
        }
        catch (IsamException ise) {}
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
		BVar            bvar;

		m_vars = new ArrayList();
		m_varMap = new HashMap();

		itNames = p_SrcDef. getVarNames(). iterator();
		itDecls = p_SrcDef. getVarDecls(). iterator();

		while (itDecls. hasNext())
		{

			bvar = m_rf. makeVar(
							(String) itDecls. next() );
			m_vars. add( bvar );
			m_varMap. put(
							(String) itNames. next(),
							bvar );
		}
	}

    public void cacheRecoveryContext()
	{
		m_rtnFile = m_rf;
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
		BVar            bvar;
		int             iVar;
		int             iType[];

		msg = new StringBuffer( 20 * m_vars.size() );
		msg. append( "Data: " );

		iType = getDefiner(). getVarTypes();

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
	===== Query Processing.
	==========================================================================*/

	public void initializeIndex()
		throws Exception
	{
		ArrayList       cPresets;
		Iterator        itSet;
		String          sAsgn;
		int             iSep;

		cPresets = m_ISAMDef. getPresets();
		if (null == cPresets) return;

		itSet = cPresets. iterator();
		while (itSet.hasNext())
		{
			sAsgn = (String) itSet. next();
			iSep = sAsgn. indexOf( '=' );
			if (-1 != iSep)
			{
				setVariable(
					sAsgn. substring( 0, iSep ),
					sAsgn. substring( iSep + 1 ) );
			}
		}
	}

	public boolean advanceIndex()
	{
		Iterator        itSet;
		String          sAsgn;
		int             iSep;

		if ((null == m_ISAMDef. getAdvsets()) ||
			(0 == m_ISAMDef. getAdvsets(). size())) return false;

		itSet = m_ISAMDef. getAdvsets(). iterator();
		while (itSet.hasNext())
		{
			sAsgn = (String) itSet. next();
			iSep = sAsgn. indexOf( '=' );
			if (-1 != iSep)
			{
				try {
					setVariable(
						sAsgn. substring( 0, iSep ),
						sAsgn. substring( iSep + 1 ) );
				}
				catch (Exception ex) {}
			}
		}

		return true;
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
			bvar. set( substitute( p_val ) );
		}
	}

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

		enableSkip();
    }

    public boolean selectRecords()
	{
		try {
			m_rf. select(
						m_ISAMDef. getIndexFile(),
						substitute( m_ISAMDef. getRangeTemplate() ),
						substitute( m_ISAMDef. getFilterTemplate() ) );
		}
		catch (IsamException ie) {
			return false;
		}

		return true;
    }

	public boolean processRecords()
		throws SourceException
	{
		boolean     bRcd;

		bRcd = checkRecord();
		while (bRcd && checkContinue())
		{
			postRecord();

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
		}

		return !bRcd;
	}

    public boolean checkRecord()
		throws SourceException
	{
		try {
			return m_rf. next();
		}
		catch (IsamException ise) {
		    throw new SourceException(
						ise. getMessage(),
						ise );
		}
     }

    public void bindInsert(
		    PreparedStatement   p_Insrt)
		throws
			Exception,
			SQLException
	{
		int             iVar;
		Iterator        itVars;

		iVar = 1;
		itVars = m_vars. iterator();
		while (itVars. hasNext())
		{
			bindValue(
				    p_Insrt,
					iVar - 1,
					iVar );
			iVar++;
		}
    }

    public void bindUpdate(
		    PreparedStatement   p_Update)
		throws
			Exception,
			SQLException
	{
		ArrayList       names;
		int             iFld;
		String          sName;
		int             iBnd;

		names = getDefiner(). getVarNames();

		for (iFld=0; iFld<names.size(); iFld++)
		{
			sName = (String) names. get( iFld );
			iBnd = getDefiner(). getUpdateIndex( sName );
			bindValue(
				    p_Update,
					iFld,
					iBnd );
		}
    }

	public void bindValue(
		    PreparedStatement       p_Stmt,
			int                     p_iFld,
			int                     p_iBnd )
		throws
			SQLException,
			Exception
	{
		boolean         bNull;

		String          sName;
		int             iBnd;
		BVar            bvar;
		java.util.Date   jul;

		int             iTypes[];

		iTypes = getDefiner(). getVarTypes();
		bvar = (BVar) m_vars. get( p_iFld );
		switch (iTypes[ p_iFld])
		{
		case Types.INTEGER:
			p_Stmt.setInt( p_iBnd, bvar. getInt() );
			break;
		case Types.DOUBLE:
			p_Stmt.setDouble( p_iBnd, bvar. getDouble() );
			break;
		case Types.VARCHAR:
		case Types.CHAR:
			p_Stmt.setString( p_iBnd, bvar. getString() );
			break;
		case Types.DATE:
			jul = bvar. julianToDate();
			if (null == jul)
			{
				p_Stmt.setNull( p_iBnd, Types.DATE );
			}
			else
			{
				p_Stmt.setDate(
							p_iBnd,
							new java.sql.Date(
										jul. getTime() ) );
			}
			break;
		default:
			// Will probably throws an exception.
			p_Stmt.setNull( p_iBnd, Types.OTHER );
		}
	}

	public static void main(
		    String      args[] )
	{
		ISAMExtractor        IExt;
		ISAMDefParser        SDef;

		IExt = new ISAMExtractor();
		SDef = new ISAMDefParser( args[0] );
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
