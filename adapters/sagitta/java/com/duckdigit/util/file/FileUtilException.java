/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/util/file/FileUtilException.java,v 1.2 2010/04/22 18:41:45 secwind Exp $
==================================================================================================*/
package com.duckdigit.util.file;
import java.util.*;

/**
 * This class provides the exception used by the email monitor and email sender.
 */
public class FileUtilException extends Exception
{
	String		m_ErrorString;
	FileUtilException (String ErrorString)
	{
		super (ErrorString);
		m_ErrorString = ErrorString;
	}
	String getErrorString ()
	{
		return m_ErrorString;
	}
}

