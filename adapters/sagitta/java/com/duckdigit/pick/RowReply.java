
/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/pick/RowReply.java,v 1.2 2010/04/22 18:41:42 secwind Exp $
==================================================================================================*/
package com.duckdigit.pick;

import java.io.*;

public class RowReply extends PickReply
{
	PickFile			m_pickFile = null;
	OutputStream		m_out = null;
	boolean				m_eof = false;
	public RowReply (byte buf [], PickFile pickFile)
		throws PickException
	{
		super (PickRequest.ROW, buf);
		m_pickFile = pickFile;
	}
	/*
	public RowReply (byte buf [], OutputStream out)
		throws PickException
	{
		super (PickRequest.ROW, buf);
		m_out = out;
		m_pickFile = null;
	}
	*/
	protected void parseReplyBody (int len)
		throws PickException
	{
			//
			// Test for EOF in the first byte.
			//
		if (parseByte () == 1)
		{
			m_eof = true;
			//System.out.println ("EOF in RowReply");
		}
		else if (m_pickFile != null)
		{
			int size;
			size = parseBuf (m_pickFile.getRecordSetBuffer (),
				m_pickFile.getRecordSetOffset ());
			RecordId recno = parseRecordId ();
			//System.out.println ("recno: " + recno);
			m_pickFile.setRcdNoAndSize (recno, size);
			// BBB System.out.println ("rcdno: " + m_pickFile.getRcdNo ());
		}
		else if (m_out != null)
		{
			writeBuf (m_out);
			parseRecordId ();	// ignore the record number.
		}
	}
	public boolean isEof ()
	{
		return m_eof;
	}

}
