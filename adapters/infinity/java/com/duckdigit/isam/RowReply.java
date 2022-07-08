/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/RowReply.java,v 1.1 2010/10/19 00:44:26 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

import java.io.*;

public class RowReply extends IsamReply
{
	RandomFile			m_randomFile = null;
	OutputStream		m_out = null;
	boolean				m_eof = false;
	public RowReply (byte buf [], RandomFile randomFile)
		throws IsamException
	{
		super (IsamRequest.ROW, buf);
		m_randomFile = randomFile;
	}
	public RowReply (byte buf [], OutputStream out)
		throws IsamException
	{
		super (IsamRequest.ROW, buf);
		m_out = out;
		m_randomFile = null;
	}
	protected void parseReplyBody (int len)
		throws IsamException
	{
			//
			// Test for EOF in the first byte.
			//
		if (parseByte () == 1)
		{
			m_eof = true;
			// BBB System.out.println ("EOF in RowReply");
		}
		else if (m_randomFile != null)
		{
			parseBuf (m_randomFile.getRecordSetBuffer (),
				m_randomFile.getRecordSetOffset ());
			m_randomFile.setRcdNo (parseInt ());
			// BBB System.out.println ("rcdno: " + m_randomFile.getRcdNo ());
		}
		else if (m_out != null)
		{
			writeBuf (m_out);
			parseInt ();	// ignore the record number.
		}
	}
	public boolean isEof ()
	{
		return m_eof;
	}

}
