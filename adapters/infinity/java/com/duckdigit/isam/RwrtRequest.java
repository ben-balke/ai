/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/RwrtRequest.java,v 1.1 2010/10/19 00:44:26 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;
import java.util.*;

public class RwrtRequest extends IsamRequest
{
	public RwrtRequest (byte buf [], RandomFile randomFile, UpdateRequest upreq)
		throws IsamException
	{
		super (buf, IsamRequest.RWR);
		try
		{
			this.appendByte (randomFile.m_filechan);
			this.appendInt (upreq.getRcdNo ());
			int		fldcount = upreq.getCount ();
			this.appendNumber (fldcount);
			for (int i = 0; i < fldcount; i++)
			{
				this.appendNumber (upreq.getOffset (i));
				this.appendNumber (upreq.getSize (i));
			}
			this.appendNumber (upreq.getDataLen ());
			for (int i = 0; i < fldcount; i++)
			{
				this.write (upreq.getBytes (i));
			}
		}
		catch (Exception ex)
		{
			throw new IsamException ("Write Request Error", ex);
		}
	}
}
