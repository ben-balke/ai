package com.duckdigit.ai;

import java.io.RandomAccessFile;
import java.io.IOException;
import com.duckdigit.util.*;

public class AILogger
{
	static final int 		ITEMLENGTH = 30;
	static final int 		NUMLENGTH = 8;
	RandomAccessFile		m_raf;
	long					m_address;

		/**
		 * Increment the number of records inserted as needed.
		 * Written out on an Update call.
		 */
	public int				m_records = 0;
		/**
		 * Increment the number of warnings as needed.
		 * Written out on an Update call.
		 */
	public int				m_warnings = 0;
		/**
		 * Increment the number of errors as needed.
		 * Written out on an Update call.
		 */
	public int				m_errors = 0;

	public AILogger (String path)
		throws IOException
	{
		m_raf = new RandomAccessFile (path, "rw");
		m_address = m_raf.length ();
		m_raf.seek (m_address);
	}

	public AILogger ()
	{
		m_raf = null;
	}

	public void close ()
		throws IOException
	{
		if (null == m_raf) return;
		m_raf.close ();
	}
	public void abort (String reason)
		throws IOException
	{
		if (null == m_raf) return;
		m_raf.write (reason.getBytes ());
	}
	public void setItem (String header, String item)
		throws IOException
	{
		String		itemtext;

		m_errors = 0;
		m_warnings = 0;
		m_records = 0;

		if (null == m_raf) return;

		m_raf.write (header.getBytes ());
		m_raf.write (" ".getBytes ());
		m_raf.seek (m_raf.length ());
		itemtext = StringUtils.spacePadTextRight (item, AILogger.ITEMLENGTH);
		m_raf.write (itemtext.getBytes ());
		m_address = m_raf.length ();
	}
	public void update (int count, int warnings, int errors)
		throws IOException
	{
		if (null == m_raf) return;

		m_raf.seek (m_address);
		m_raf.write (StringUtils.spacePadDecLeft (count, NUMLENGTH).getBytes ());
		m_raf.write (StringUtils.spacePadDecLeft (warnings, NUMLENGTH).getBytes ());
		m_raf.write (StringUtils.spacePadDecLeft (errors, NUMLENGTH).getBytes ());
		m_raf.writeBytes ("\n");
	}
	public void update ()
		throws IOException
	{
		update (m_records, m_warnings, m_errors);
	}

	public static void main (String args [])
	{
		try
		{
			AILogger		dl = new AILogger ("/tmp/ai.log");
			dl.setItem ("   100      0", "Policy");
			dl.m_records = 100;
			dl.update ();
			dl.m_records = 110;
			dl.setItem ("   100      0", "Customer");
			dl.m_records = 110;
			dl.update ();
			dl.m_records = 300;
			dl.update ();
			dl.close ();
		}
		catch (Exception ex)
		{
			ex.printStackTrace ();
		}
	}
}

