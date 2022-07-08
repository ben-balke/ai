
/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/pick/PickFile.java,v 1.2 2010/04/22 18:41:42 secwind Exp $
==================================================================================================*/
package com.duckdigit.pick;

import java.util.*;

import com.duckdigit.util.lang.StringUtils;

public class PickFile extends Object
{
	public static final int			SKIP_EOF = 1;
	public static final int			SKIP_ERROR = -1;
	public static final int			SKIP_FOUND = 0;
	public static final int			SKIP_KEEP_GOING = 2;

	private static RecordId			NOT_SINGLE_ROW = null;
	public String					m_name = null;
	public int						m_filechan = -1;
	public PickVarList				m_varlist = new PickVarList ();
	public byte						m_recordSetBuffer [] = null;
	public PickClient				m_client = null;
	private RecordId				m_rcdnos [] = null;
	private RecordId				m_rcdno;
	private int						m_rcdsizes [] = null;
	private int						m_rcdsize;
	public boolean					m_eof = false;
	private int						m_recordSetSize = 1;
	private int						m_currentRecordInSet = 0;
	private int						m_currentOffsetInSet = 0;
	private int						m_eofRecord = -1;
	private int						m_maxRecordSize = 0;
	private boolean					m_is_file_open = false;
	private RecordId				m_singleRow = NOT_SINGLE_ROW;

	public PickFile (PickClient client, String name, int maxRecordSize,
		int recordSetSize) throws PickException
	{
		m_client = client;
		m_name = name;
		if (maxRecordSize <= 0)
		{
			throw new PickException ("Max record length needs to be greater than zero.");
		}
		m_maxRecordSize = maxRecordSize;
		
		if (recordSetSize <= 0)
		{
			throw new PickException ("Record set must be greater than zero.");
		}
		m_recordSetSize = recordSetSize;
		m_recordSetBuffer = new byte [m_recordSetSize *
			m_maxRecordSize];
		m_rcdnos = new RecordId [m_recordSetSize];
		m_rcdsizes = new int [m_recordSetSize];
	}

	public void finalize()
	{
		try {
			close();
		} catch (Exception e) {}
	}

	public byte [] getRecordSetBuffer ()
	{
		return m_recordSetBuffer;
	}
	public int getRecordSetOffset ()
	{
		if (m_recordSetSize == 1)
			return 0;
		return m_currentOffsetInSet;
	}

	public PVar makeVar (int fieldPos) throws PickException
	{
		if (m_is_file_open)
		{
			throw new PickException("Pick file has already been opened. You can not add variables to an open pick file. They must be defined before the open.");
		}
		
		return m_varlist.createField(fieldPos);
	}
	public PVar makeVar (int fieldPos, int subFieldPos) throws PickException
	{
		if (m_is_file_open)
		{
			throw new PickException("Pick file has already been opened. You can not add variables to an open pick file. They must be defined before the open.");
		}
		
		return m_varlist.createField(fieldPos, subFieldPos);
	}

	public void open () throws PickException
	{
		synchronized (m_client)
		{
			PopenRequest popen = new PopenRequest (m_client.getRequestBuf (), this);
			m_client.sendRequest (popen);
			PopenReply	reply = new PopenReply (m_client.getReplyBuf (), this);
			m_client.getReply (reply, PickClient.READ_TIMEOUT);

			m_is_file_open = true;
		}
	} 

	public void select (RecordId rowid)
	{
		m_singleRow = rowid;
	}

	/**
	 * Lowlevel select routine.  Synchronizes with the other client 
	 * calls.
	 */
	private void select (PSelectRequest psel) throws PickException
	{
		synchronized (m_client)
		{

			m_client.sendRequest (psel);
			PSelectReply	reply = new PSelectReply (m_client.getReplyBuf (), this);
			m_client.getReply (reply, PickClient.READ_TIMEOUT * 10);
			m_eof = false;
			m_currentRecordInSet = m_recordSetSize;
			m_eofRecord = -1;
		}
	}

	/**
	 * Select All records from the pick file.  Use next to retrieve the 
	 * records. 
	 */
	public void select() throws PickException
	{
		select (new PSelectRequest(m_client.getRequestBuf(), this));
	}

	/**
	 * After a select ALL, use skipTo to advance the record to the rowid
	 * specified.  
	 * Returns true is the rowid was found false if EOF
	 */
	public int skipTo (RecordId rowid, int interval) throws PickException
	{
		//System.err.println ("PickFile:skipTo():: rowid " + rowid + " interval: " + interval);
		PSkipToRequest pskip = new PSkipToRequest (m_client.getRequestBuf (), this, rowid, interval);
		m_client.sendRequest (pskip);
		PSkipToReply	reply = new PSkipToReply (m_client.getReplyBuf (), this);
		//System.err.println ("PickFile:skipTo() to in loop reading Reply");
		m_client.getReply (reply, PickClient.READ_TIMEOUT);
		//System.err.println ("PickFile:skipTo() got reply eof: " + reply.isEof () + " foundit: " + reply.foundIt ());
		return reply.getResult ();
	}

	/**
	 * Reads a single record from the pick file based on row id.  
	 * The fields are populated after this call.
	 * @return Returns false when the recordid is not in file.
	 */
	public boolean read (RecordId recordid) throws PickException
	{
		if (recordid.length () == 0)
			throw new PickException ("Invalid key ID for selection: " + recordid);
		PReadRequest		rr = new PReadRequest (m_client.getRequestBuf(), 
									this, recordid);

		boolean		eof;
		synchronized (m_client)
		{
			m_client.sendRequest (rr);

			m_currentRecordInSet = 0;
			m_currentOffsetInSet = 0;
			RowReply	reply = new RowReply (m_client.getReplyBuf (), this);
			m_client.getReply (reply, PickClient.READ_TIMEOUT);
			eof = reply.isEof ();
			if (!eof)
			{
				distributeFieldData (m_recordSetBuffer, 0);
			}
		}
		return !eof;
	}

	/**
	 * Select a set of records matching the criteria of the pick basic provided.
	 * Use next to retrieve the records. 
	 */
	public void select (String basic) throws PickException
	{
		if (basic == null || basic.length () == 0)
			throw new PickException ("Basic search string was empty.");
				
		select (new PSelectRequest (m_client.getRequestBuf(), this, basic));
	}

	/**
	 * Select a set of records idenfied in the sole multivalue field located 
	 * the mvfile at recordid location.  Use next to retrieve the records. 
	 */
	public void select (String mvfile, RecordId recordid) throws PickException
	{
		if (mvfile == null || mvfile.length () == 0)
			throw new PickException ("Multi value file name was not specified.");

		if (recordid.length () == 0)
			throw new PickException ("Invalid key ID for multi value selection: " + recordid);
				
		select (new PSelectRequest(
			m_client.getRequestBuf (), this, mvfile, recordid));
	}

	/**
	 * Distributes the pick data buffer at offset location into the 
	 * field list.  Each field data is terminated by a decimal 254 byte.
	 * @return The number of bytes copyied from the buffer.
	 */
	private int distributeFieldData (byte buf [], int offset)
	{
		int		curoset = offset;
		int		fstartoset;
		for (int i = 0; i < m_varlist.size (); i++)
		{
			PVar		pv = (PVar) m_varlist.get (i);
			if (pv.getFieldPosition () == PVar.PICK_FIELD_ROWID)
			{
				pv.set (getRcdNo ());
			}
			else
			{
				fstartoset = curoset;
				while (buf [curoset] != (byte) 254)
				{
					curoset++;
				}
				pv.set (buf, fstartoset, curoset - fstartoset);
				curoset++;
			}
		}
		return curoset - offset;
	}
	
	/**
	 * Next moves forward one record.  If the record set is larger
	 * than 1, n variable length records are read and packed into a holding 
	 * buffer or an EOF occurs before the method returns.  This means that if 
	 * you want to terminate a next operation based on a value in a record 
	 * then be advised that the entire record set is returned before you 
	 * will receive the first record in the set.
	 * @return returns false if an EOF occors; else true.
	 */
	public boolean next () throws PickException
	{
		if (m_singleRow != NOT_SINGLE_ROW)
		{
			if (m_eof == true)
			{
				m_singleRow = NOT_SINGLE_ROW;
				return false;
			}
			boolean rslt = read (m_singleRow);
			m_eof = true;
			return rslt;
		}
		synchronized (m_client)
		{
				/**
			 	* See if we are buffering records.
			 	*/
			if (m_recordSetSize > 1)
			{
					/**
				 	* Check to see if we need to start collecting more
				 	* records from the server.  Otherwise use the spooled
				 	* records.
				 	*/
				if (m_currentRecordInSet == m_recordSetSize)
				{
					PNextRequest 	rqst;
					rqst = new PNextRequest (m_client.getRequestBuf (),
						this, m_recordSetSize);
	
					m_client.sendRequest (rqst);
	
					m_eofRecord = -1;

						/**
						 * Read a record set from the server.  We 
						 * write each variable length record into the 
						 * allocated buffer.  The offset is incremented
						 * for each record when the RowReply is parsed 
						 * through the setRcdNoAndSize() callback.
						 */
					m_currentOffsetInSet = 0;
					for (m_currentRecordInSet = 0; m_currentRecordInSet <
						m_recordSetSize; m_currentRecordInSet++)
					{
							/**
						 	* RowReply uses the
						 	* getRecordSetBuffer, getRecordSetOffset,
						 	* and setRcdNo to place the data and set the r
						 	* record number for the buffered record.
						 	*/
						RowReply	reply = new RowReply (
							m_client.getReplyBuf (), this);
						m_client.getReply (reply, PickClient.READ_TIMEOUT);
							/**
							 * If we have reached EOF from the server then
							 * track the record in this set that will cause
							 * us to notify the caller.
							 */
						if (reply.isEof ())
						{
							m_eofRecord = m_currentRecordInSet;
							break;
						}
						m_currentOffsetInSet += m_rcdsize;
					}
						/**
						 * Now reset the record and offset so we can
						 * distribute each record to the fields.
						 */
					m_currentRecordInSet = 0;
					m_currentOffsetInSet = 0;
				}
				if (m_eofRecord == m_currentRecordInSet)
				{
					m_eof = true;
					return false;
				}
					/**
					 * Set the current state variables.
					 */
				m_rcdno = m_rcdnos [m_currentRecordInSet];
				m_rcdsize = m_rcdsizes [m_currentRecordInSet];
					/**
				 	* Distribute the stored buffer into the fields.
				 	*/
				distributeFieldData (m_recordSetBuffer, m_currentOffsetInSet);

					/**
					 * Increment the record set the offset for the 
					 * next variables.
					 */
				m_currentRecordInSet++;
				m_currentOffsetInSet += m_rcdsize;
				m_eof = false;
			}
			else
			{
					/**
					 * We are reading one record at a time.  Do everything
					 * through the 0 record.
					 */
				PNextRequest rqst;
				m_currentRecordInSet = 0;
				m_currentOffsetInSet = 0;
				rqst = new PNextRequest (m_client.getRequestBuf (),
					this, 1);
	
				m_client.sendRequest (rqst);
	
				RowReply	reply = new RowReply (m_client.getReplyBuf (), this);
				m_client.getReply (reply, PickClient.READ_TIMEOUT);
				m_eof = reply.isEof ();
				if (!m_eof)
				{
					distributeFieldData (m_recordSetBuffer, 0);
				}
			}
			return !m_eof;
		}
	}

	public void close () throws PickException
	{
		synchronized (m_client)
		{
			if (m_is_file_open)
			{
				PclsRequest pcls = new PclsRequest (m_client.getRequestBuf (), this);
				m_client.sendRequest (pcls);
				PclsReply	reply = new PclsReply (m_client.getReplyBuf ());
				m_client.getReply (reply, PickClient.READ_TIMEOUT);
	
				m_is_file_open = false;
			}
		}

	}

	/*
	setFileChan is called from PopenReply and stores
	the file channel from the server.
	*/
	public void setFileChan (int filechan)
	{
		m_filechan = filechan;
	}

	public void setRcdNoAndSize (RecordId rcdno, int size)
	{
		m_rcdnos [m_currentRecordInSet] = rcdno;
		m_rcdno = rcdno;
		m_rcdsizes [m_currentRecordInSet] = size;
		m_rcdsize = size;
	}
	public RecordId getRcdNo ()
	{
		return m_rcdno;
	}
	public int getRcdSize ()
	{
		return m_rcdsize;
	}
	public boolean isEof ()
	{
		return m_eof;
	}
	public String dumpBuf ()
	{
		return StringUtils.hexDumpToString (m_recordSetBuffer, m_rcdsize);
	}
}	
