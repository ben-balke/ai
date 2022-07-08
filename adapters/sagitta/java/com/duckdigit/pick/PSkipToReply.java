
/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/pick/PSkipToReply.java,v 1.2 2010/04/22 18:41:42 secwind Exp $
==================================================================================================*/
package com.duckdigit.pick;

import java.io.*;

public class PSkipToReply extends PickReply
{
	PickFile			m_pickFile = null;
	OutputStream		m_out = null;
	boolean				m_eof = false;
	boolean				m_foundit = false;
	int					m_rslt;
	public PSkipToReply (byte buf [], PickFile pickFile)
		throws PickException
	{
		super (PickRequest.SKP, buf);
		m_pickFile = pickFile;
	}
	protected void parseReplyBody (int len)
		throws PickException
	{
			//
			// Test for EOF in the first byte.
			//
		m_rslt = parseByte ();
		switch (m_rslt)
		{
		case PickFile.SKIP_KEEP_GOING: // 2 means foundit polling.
			//System.err.println ("EOF in PSkipToReply");
			m_foundit = false;
			break;
		case PickFile.SKIP_ERROR:
			//System.err.println ("ERROR in PSkipToReply");
			break;
		case PickFile.SKIP_EOF:
			m_eof = true;
			//System.err.println ("EOF in PSkipToReply");
			break;
		case PickFile.SKIP_FOUND:
			//System.err.println ("Found Record in PSkipToReply");
			m_foundit = true;
			break;
		default:
			System.err.println ("SkipTo: Error!");
			break;
		}
	}
	public boolean isEof ()
	{
		return m_eof;
	}
	public boolean foundIt ()
	{
		return m_foundit;
	}
	public int getResult ()
	{
		return m_rslt;
	}

}
