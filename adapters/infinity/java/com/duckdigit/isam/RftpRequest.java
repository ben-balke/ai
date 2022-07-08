/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/RftpRequest.java,v 1.1 2010/10/19 00:44:26 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

import java.util.*;

public class RftpRequest extends IsamRequest
{
	public RftpRequest (byte buf [], String path, boolean download)
		throws IsamException
	{
		super (buf, IsamRequest.FTP);
		this.appendString (path);
		this.appendInt (download ? 1 : 0);
	}
}
