/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/RdelRequest.java,v 1.1 2010/10/19 00:44:26 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;
import java.util.*;

public class RdelRequest extends IsamRequest
{
	public RdelRequest (byte buf [], RandomFile randomFile, int rcdno)
		throws IsamException
	{
		super (buf, IsamRequest.RDL);
		try
		{
			this.appendByte (randomFile.m_filechan);
			this.appendInt (rcdno);
		}
		catch (Exception ex)
		{
			throw new IsamException ("Delete Request Error", ex);
		}
	}
}
