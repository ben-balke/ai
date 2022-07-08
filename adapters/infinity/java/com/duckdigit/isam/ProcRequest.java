/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/ProcRequest.java,v 1.1 2010/10/19 00:44:26 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;
import java.util.*;

/**
 * ProcRequest creates a packet for transmission to the isam server
 * that contains a remote procedure call with arguments.  The packet 
 * contains the name of the procedure to call, the number of arguments
 * and the list of named parameters.
 */
public class ProcRequest extends IsamRequest
{
	public ProcRequest (byte buf [], String name, HashMap map)
		throws IsamException
	{
			/**
			 * Add one to the username and password for
			 * null termination.
			 */
		super (buf, IsamRequest.PRC);
		this.appendString (name);
		if (map != null)
		{
			this.appendNumber (map.size ());
			Set	s = map.keySet ();
			Iterator	itor = s.iterator ();
			while (itor.hasNext ())
			{
				String		key = itor.next ().toString ();
				this.appendString (key);
				this.appendString (map.get (key).toString ());
			}
		}
		else
		{
			this.appendNumber (0);
		}
	}
}
