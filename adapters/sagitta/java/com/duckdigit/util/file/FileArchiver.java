/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/util/file/FileArchiver.java,v 1.2 2010/04/22 18:41:45 secwind Exp $
==================================================================================================*/
package com.duckdigit.util.file;
import com.duckdigit.util.lang.StringUtils;
import java.io.*;

public abstract class FileArchiver
{
	protected String			m_basePath;
	protected String			m_filename;
	public FileArchiver ()
	{
	}
	public String getPath ()
	{
		return m_basePath + File.separatorChar + m_filename;
	}
	public InputStream getInputStream ()
	{
		try
		{
			FileInputStream fin = new FileInputStream (new File (m_basePath + 
					File.separatorChar + m_filename));
			return fin;
		}
		catch (Exception ex)
		{
			return null;
		}
	}
	public boolean sendFile (OutputStream out)
		throws Exception
	{
		InputStream		in = getInputStream ();
		if (in == null)
		{
			return false;
		}
		byte		buf [] = new byte [1024];
		int			nbytes;
		while ((nbytes = in.read (buf, 0, buf.length)) > 0)
		{
			out.write (buf, 0, nbytes);
		}
		in.close ();
		return true;
	}
	public OutputStream getOutputStream ()
		throws Exception
	{
		File		dir = new File (m_basePath);
		dir.mkdirs ();
		FileOutputStream fout = new FileOutputStream (
			new File (m_basePath + File.separatorChar + m_filename));
		return fout;
	}
}
