/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/RandomFile.java,v 1.1 2010/10/19 00:44:26 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

import java.util.*;

import com.duckdigit.util.StringUtils;

public class RandomFile extends Object
{
	public static final int			NOINDEX = -1;
	public String					m_name = null;
	public String					m_fields = null;
	public ArrayList				m_indicies = null;
	public int						m_filechan = -1;
	public BasixVarList				m_varlist = null;
	public int						m_activeIndex = NOINDEX;
	public byte						m_boundBuffer [] = null;
	public byte						m_recordSetBuffer [] = null;
	public IsamClient				m_client = null;
	private int						m_rcdnos [] = null;
	private int						m_rcdno;
	public boolean					m_eof = false;
	private int						m_recordSetSize = 1;
	private int						m_currentRecordInSet = 0;
	private int						m_eofRecord = -1;

	public RandomFile (IsamClient client, String name, String fields,
		int recordSetSize) throws IsamException
	{
		m_client = client;
		m_name = name;
		m_fields = fields;
		if (recordSetSize <= 0)
		{
			throw new IsamException ("Record set must be greater than zero.");
		}
		m_recordSetSize = recordSetSize;
	}

/*	public void finalize()
	{
		try {
			close();
		} catch (Exception e) {}
	}
*/
	public byte [] getRecordSetBuffer ()
	{
		if (m_recordSetSize == 1)
			return m_boundBuffer;
		return m_recordSetBuffer;
	}
	public int getRecordSetOffset ()
	{
		if (m_recordSetSize == 1)
			return 0;
		return m_currentRecordInSet * m_boundBuffer.length;
	}


	public void addIndex (String filename, String fields)
	{
		if (m_indicies == null)
		{
			m_indicies = new ArrayList ();
		}
		m_indicies.add (new IndexFile (filename, fields));
	}

	public BVar makeVar (String vardec)
		throws BasixDeclException
	{
		BVar		bv = m_varlist.createSubVar (vardec, m_boundBuffer);
		return bv;
	}

	public void open () throws IsamException
	{
		synchronized (m_client)
		{
			RopenRequest ropen = new RopenRequest (m_client.getRequestBuf (), this);
			m_client.sendRequest (ropen);
			RopenReply	reply = new RopenReply (m_client.getReplyBuf (), this);
			m_client.getReply (reply, IsamClient.READ_TIMEOUT);
		}
	}
	public void select (int index, String rangeClause, String whereClause) throws IsamException
	{
		this.select (index, rangeClause, whereClause, null);
	}
	public void select (String indexfile, String rangeClause, String whereClause) throws IsamException
	{
		this.select (findIndex (indexfile), rangeClause, whereClause, null);
	}
	public void select (int index, String rangeClause, String whereClause, String orderby) throws IsamException
	{
		synchronized (m_client)
		{
			this.setActiveIndexNo (index);
			R1stRequest r1st = new R1stRequest (m_client.getRequestBuf (), this, rangeClause, whereClause, orderby);
			m_client.sendRequest (r1st);
			R1stReply	reply = new R1stReply (m_client.getReplyBuf (), this);
			m_client.getReply (reply, IsamClient.READ_TIMEOUT);
			m_eof = false;
			m_currentRecordInSet = m_recordSetSize;
			m_eofRecord = -1;
		}
	}
	public void select (String indexfile, String rangeClause, String whereClause, String orderby) throws IsamException
	{
		this.select (findIndex (indexfile), rangeClause, whereClause, orderby);
	}

	public void select (int rcdno) throws IsamException
	{
		synchronized (m_client)
		{
			m_eof = false;
			RrrdRequest rrrd = new RrrdRequest (m_client.getRequestBuf (), this,
												rcdno);
			m_client.sendRequest (rrrd);
			RowReply	reply = new RowReply (m_client.getReplyBuf (), this);
			m_client.getReply (reply, IsamClient.READ_TIMEOUT);
			m_activeIndex = NOINDEX;
			m_eof = reply.isEof ();
		}
	}
	/**
	 * Next moves forward one record.  If the record set is larger
	 * than 1, n records are read into a holding buffer or an EOF occurs before
	 * the method returns.  This means that if you want to terminate a next operation
	 * based on a value in a record then be advised that the entire record set
	 * is returned before you will receive the first record in the set.
	 * @return returns false if an EOF occors; else true.
	 */
	public boolean next () throws IsamException
	{
		return this.next (true);
	}
	/**
	 * Next moves backwards one record.  If the record set is larger
	 * than 1, n records are read into a holding buffer or an EOF occurs before
	 * the method returns.  This means that if you want to terminate a previous operation
	 * based on a value in a record then be advised that the entire record set
	 * is returned before you will receive the first record in the set.
	 * @return returns false if an EOF occors; else true.
	 */
	public boolean previous () throws IsamException
	{
		return this.next (false);
	}

	/**
	 * Next moves forward or backward one record.  If the record set is larger
	 * than 1, n records are read into a holding buffer or an EOF occurs before
	 * the method returns.  This means that if you want to terminate a next operation
	 * based on a value in a record then be advised that the entire record set
	 * is returned before you will receive the first record in the set.
	 * @param forward	true to move forward on the index; else moves backwards.
	 */
	private boolean next (boolean forward) throws IsamException
	{
		synchronized (m_client)
		{
			if (m_activeIndex != NOINDEX)
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
						IsamRequest rqst;
						if (forward == true)
						{
							rqst = new RnxtRequest (m_client.getRequestBuf (),
								this, m_recordSetSize);
						}
						else
						{
							rqst = new RprevRequest (m_client.getRequestBuf (),
								this, m_recordSetSize);
						}

						m_client.sendRequest (rqst);

						m_eofRecord = -1;
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
							m_client.getReply (reply, IsamClient.READ_TIMEOUT);
							if (reply.isEof ())
							{
								m_eofRecord = m_currentRecordInSet;
								break;
							}
						}
						m_currentRecordInSet = 0;
					}
					if (m_eofRecord == m_currentRecordInSet)
					{
						m_eof = true;
						return false;
					}
						/**
						 * Copy the buffered record into bound buffer.
						 */
					System.arraycopy (
						m_recordSetBuffer, m_currentRecordInSet * m_boundBuffer.length,
						m_boundBuffer, 0,
						m_boundBuffer.length);
					m_rcdno = m_rcdnos [m_currentRecordInSet];
					m_currentRecordInSet++;
					m_eof = false;
				}
				else
				{
					IsamRequest rqst;
					m_currentRecordInSet = 0;
					if (forward == true)
					{
						rqst = new RnxtRequest (m_client.getRequestBuf (),
							this, m_recordSetSize);
					}
					else
					{
						rqst = new RprevRequest (m_client.getRequestBuf (),
							this, m_recordSetSize);
					}

					m_client.sendRequest (rqst);

					RowReply	reply = new RowReply (m_client.getReplyBuf (), this);
					m_client.getReply (reply, IsamClient.READ_TIMEOUT);
					m_eof = reply.isEof ();
				}
				return !m_eof;
			}

				/**
				 * On record number read, only allow a single read
				 * before returning an eof.  Note that the select
				 * actually reads the RowReply.
				 */
			boolean		eofrslt = m_eof;
			m_eof = true;
			return !eofrslt;
		}
	}

	public boolean isEof ()
	{
		return m_eof;
	}
	public void delete (int rcdno) throws IsamException
	{
		synchronized (m_client)
		{
			System.out.println ("Deleting record " + rcdno);
			RdelRequest rdel = new RdelRequest (m_client.getRequestBuf (), this,
												rcdno);
			m_client.sendRequest (rdel);
			RdelReply	reply = new RdelReply (m_client.getReplyBuf ());
			m_client.getReply (reply, IsamClient.READ_TIMEOUT);
		}
	}
	public void update (UpdateRequest upr) throws IsamDuplicateKeyException, IsamException
	{
		synchronized (m_client)
		{
			RwrtRequest rwrt = new RwrtRequest (m_client.getRequestBuf (), this,
												upr);
			m_client.sendRequest (rwrt);
			RwrtReply	reply = new RwrtReply (m_client.getReplyBuf ());
			m_client.getReply (reply, IsamClient.READ_TIMEOUT);
		}
	}
	public void insert () throws IsamDuplicateKeyException, IsamException
	{
		synchronized (m_client)
		{
			RinsRequest rins = new RinsRequest (m_client.getRequestBuf (), this,
												m_boundBuffer);
			m_client.sendRequest (rins);
			RinsReply	reply = new RinsReply (m_client.getReplyBuf ());
			m_client.getReply (reply, IsamClient.READ_TIMEOUT);
		}
	}

	public void close () throws IsamException
	{
		synchronized (m_client)
		{
			RclsRequest rcls = new RclsRequest (m_client.getRequestBuf (), this);
			m_client.sendRequest (rcls);
			RclsReply	reply = new RclsReply (m_client.getReplyBuf ());
			m_client.getReply (reply, IsamClient.READ_TIMEOUT);
		}

	}
	public IndexFile getActiveIndex ()
	{
		return (IndexFile) m_indicies.get (m_activeIndex);
	}
	public void setFileChan (int filechan)
	{
		m_filechan = filechan;
	}
	public void setVarList (BasixVarList varlist)
	{
		m_varlist = varlist;
		m_boundBuffer = new byte [varlist.getRecordLength ()];
		if (m_recordSetSize > 1)
		{
			m_recordSetBuffer = new byte [m_recordSetSize *
				m_boundBuffer.length];
		}
		m_rcdnos = new int [m_recordSetSize];
	}
	public int getActiveIndexNo ()
	{
		return m_activeIndex;
	}
	public void setActiveIndexNo (int iidx)
	{
		m_activeIndex = iidx;
	}
	public void setIndexVarList (int iidx, BasixVarList varlist)
	{
		if (m_indicies != null)
			((IndexFile) m_indicies.get (iidx)).setVarList (varlist);
	}
	public void setRcdNo (int rcdno)
	{
		m_rcdnos [m_currentRecordInSet] = rcdno;
		m_rcdno = rcdno;
	}
	public int getRcdNo ()
	{
		return m_rcdno;
	}
	public String dumpBuf ()
	{
		if (m_boundBuffer == null)
			return null;
		return StringUtils.hexDumpToString (m_boundBuffer, m_boundBuffer.length);
	}
	public int findIndex (String indexfile)
	{
		if (m_indicies != null)
		{
			for (int iidx = 0; iidx < m_indicies.size (); iidx++)
			{
				if (((IndexFile) m_indicies.get (iidx)).m_name.equals (indexfile))
				{
					return iidx;
				}
			}
		}
		return -1;
	}

}
