/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/RinsRequest.java,v 1.1 2010/10/19 00:44:26 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;
import java.util.*;

public class RinsRequest extends IsamRequest
{
	public RinsRequest (byte buf [], RandomFile randomFile, byte dataBuf [])
		throws IsamException
	{
		super (buf, IsamRequest.RIN);
		try
		{
			this.appendByte (randomFile.m_filechan);
			this.appendNumber (dataBuf.length);
			this.write (dataBuf);
		}
		catch (Exception ex)
		{
			throw new IsamException ("Insert Request Error", ex);
		}
	}
}
