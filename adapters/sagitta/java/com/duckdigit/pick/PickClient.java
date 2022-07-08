
/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/pick/PickClient.java,v 1.2 2010/04/22 18:41:42 secwind Exp $
==================================================================================================*/
package com.duckdigit.pick;

import java.net.*;
import java.io.*;
import java.util.*;

import com.duckdigit.util.http.Crypt;

/**
 * This class is the main client to the 2ndWind PickServer.
 * It connects and manages all PICK file requests to and from
 * 2ndWind Server.
 * BBB we may need to have a finalize method that disconnects
 * gracefully.
 */
public class PickClient extends Object
{
	public static final int PICK_PORT = 5234;
		/**
		 * Number of milliseconds to wait for a reply from the server
		 * before giving up.
		 */
	static final int READ_TIMEOUT = 30000;
		/**
		 * Maximum size of the input and output buffers.
		 */
	static final int BUFMAX = 1024 * 10;
		/**
		 * Packet size that represents a keep alive situation
		 * where a long query is executing and records are being
		 * skipped.  Must match the server.
		 */
	static final int KEEPALIVE_PACKET = 0x0000ffff;
		/**
		 * Host that provides access to the Pick database.
		 */
	String		m_host;
		/**
		 * Database to Connect to this opens a file called <database>.properties
		 * on the server.
		 */
	String		m_database;
		/**
		 * TCP Port number where the Pick server is listening. Default is 5234.
		 */
	int			m_port;
		/**
		 * Open socket connection to the server.
		 */
	Socket		m_socket = null;
		/**
		 * Input stream for the socket.
		 */
	InputStream		m_in = null;
		/**
		 * Output stream for the socket.
		 */
	OutputStream	m_out = null;

		/**
		 * Maximun size of the input buffer.
		 */
	private int			m_inbufsize = BUFMAX;
		/**
		 * Buffer used to read data from the socket.
		 */
	byte			m_inbuf [] = null;
		/**
		 * Maximun size of the output buffer.
		 */
	private int			m_outbufsize = BUFMAX;
		/**
		 * Buffer used to construct and write data to the
		 * socket.
		 */
	byte			m_outbuf [] = null;

	String			m_username;
	String			m_password;
	String			m_ipaddress;
	/**
	 * Constructor.
	 * @param host	host URL to the 2ndWind server.
	 * @param port	TCP port number to 2ndWind.
	 */
	public PickClient (String host, int port)
	{
		init (host, port);
	}

	public PickClient (String host, int port, int inbufsize, int outbufsize)
	{
		m_outbufsize = outbufsize;
		m_inbufsize = inbufsize;
		init (host, port);
	}

	public void init (String host, int port)
	{
		int		idx = host.indexOf (":");
		if (idx != -1)
		{
			m_host = host.substring (0, idx);
			m_database = host.substring (idx + 1);
		}
		else
		{
			m_host = host;
			m_database = "default";
		}
//System.out.println ("host: " + m_host);
//System.out.println ("database: " + m_database);
		m_port = port;
		m_outbuf = new byte [m_outbufsize];
		m_inbuf = new byte [m_inbufsize];
	}

	public String getHost ()
	{
		return m_host;
	}
	public int getPort ()
	{
		return m_port;
	}
	/**
	 * Provides direct access to the reply buffer.
	 * @return byte array that references the buffer used to
	 * read data from the socket.
	 */
	public byte [] getReplyBuf ()
	{
		return m_inbuf;
	}
	/**
	 * Provides direct access to the request buffer.
	 * @return byte array that references the buffer used to
	 * write data to the socket.
	 */
	public byte [] getRequestBuf ()
	{
		return m_outbuf;
	}
	/**
	 * Connects to the 2ndWind server.
	 * @param username	user name used to attach to 2ndWind.
	 * @param password	password for the above username.
	 * @param ipaddress of this machine.
	 * @exception PickException Thrown when the server is not available,
	 *		of the connection request was denied for security reasons.
	 */
	public void connect (String username, String password, String ipaddress)
		throws PickException
	{
		synchronized (this)
		{
			if (m_socket != null)
			{
				return;
			}
			try
			{
				int			nbytes;
				String		rslt = "";
				m_socket = new Socket (m_host, m_port);
				m_in = m_socket.getInputStream ();
				m_out = m_socket.getOutputStream ();

				m_socket.setSoLinger (true, 10);
				m_socket.setSoTimeout (READ_TIMEOUT);

				ConnectRequest conreq = new ConnectRequest (getRequestBuf (), 
					username, password, ipaddress, m_inbufsize, m_outbufsize, m_database);
				sendRequest (conreq);
				getReply (new ConnectReply (getReplyBuf ()), READ_TIMEOUT);
				m_username = username;
				m_password = password;
				m_ipaddress = ipaddress;
			}
			catch (PickException iex)
			{
				throw iex;
			}
			catch (Exception ex)
			{
				throw new PickException (ex.getMessage ());
			}
		}
	}
	/**
	 * Disconnects the active connection.
	 */
	public void disconnect ()
	{
		synchronized (this)
		{
			if (m_socket == null)
				return;
			try
			{
				DisconnectRequest disreq = new DisconnectRequest (getRequestBuf ());
				sendRequest (disreq);
			} catch (Exception ignore) { }

			try { m_in.close (); } catch (Exception ignore) {}
			try { m_out.close (); } catch (Exception ignore) {}
			try { m_socket.close (); } catch (Exception ignore) {}
			m_in = null;
			m_out = null;
			m_socket = null;
		}
	}

	/**
	 * Tests the line to see if the server is alive.
	 */
	public synchronized boolean isalive () throws PickException
	{
		synchronized (this)
		{
			if (m_socket == null)
				return false;
			try
			{
				AliveRequest alivereq = new AliveRequest (getRequestBuf ());
				sendRequest (alivereq);
				getReply (new AliveReply (getReplyBuf ()), 5000);
			}
			catch (Exception ex)
			{
				disconnect ();
				return false;
			}
			return true;
		}
	}

	public void reconnect ()
		throws PickException
	{
		connect (m_username, m_password, m_ipaddress);
	}

	private void readBytes (byte buf [], int offset, int len)
		throws Exception
	{
		int readsize = len;
		int nbytes;
			//
			// Read the actual data.
			//
		do
		{
			nbytes = m_in.read (buf, offset, readsize);
			if (nbytes < 0)
			{
				throw new PickException (
					"Did not receive expected number of bytes: " +
						nbytes + " instead of " + readsize + ".");
			}
			readsize -= nbytes;
			offset += nbytes;
		} while (readsize > 0);
	}

	/**
	 * Waits for a reply from the server.
	 * @param reply		An PickReply derived object that is to
	 *		receive the incoming data.
	 * @exception PickException	when a error reading or parsing the packet
	 *		occurs.
	 */
	synchronized void getReply (PickReply reply, int timeout)
		throws PickException
	{
		if (m_socket == null)
		{
			throw new PickException ("Connection is not open");
		}
		int		len;
		try
		{
			// BBB need to make the timer adjustable.  ie., When where clauses
			// take too long.
			m_socket.setSoTimeout (timeout);

			do
			{
				//
				// The read occurs in two stages.  The first reads
				// the packet size and the next reads the packet bytes.
				//
				readBytes (m_inbuf, 0, PickRequest.PACKETHDR_SIZE);
				Crypt.decrypt (m_inbuf, 0, PickRequest.PACKETHDR_SIZE);
				len =	reply.parsePacketSize ();
				if (len == KEEPALIVE_PACKET)
				{
					reply.reset ();
				}
			} while (len == KEEPALIVE_PACKET);

			readBytes (m_inbuf, PickRequest.PACKETHDR_SIZE, len);
			Crypt.decrypt (m_inbuf, PickRequest.PACKETHDR_SIZE, len);
				//
				// Now let the derived IsamReply parse the packet.
				//
			reply.parseReply(len);
		}
		catch (PickException iex)
		{
			iex.printStackTrace ();
			throw iex;
		}
		catch (Exception ex)
		{
			ex.printStackTrace ();
			throw new PickException (ex.getMessage ());
		}
	}
	/**
	 * Prepares and sends a packet to the server.
	 * @param packet	PickRequest containing the data to send.
	 * @exception		PickException when the transmission fails.
	 */
	public void sendRequest (PickRequest packet)
		throws PickException
	{
		if (m_out == null)
		{
			throw new PickException ("Not connected.");
		}
		try
		{
			int count = packet.prepare ();
			Crypt.encrypt (m_outbuf, 0, count);
			m_out.write (m_outbuf, 0, count);
		}
		catch (IOException ex)
		{
			throw new PickException (ex.getMessage ());
		}
	}

	/**
	 * Gets a remote file from the iserve.  A RftpRequest is sent and
	 * the server returns RowReplys until an end of file is reached where 
	 * a 0 byte packet is returned.
	 */
/*
	synchronized public void getFile (String path, OutputStream out)
		throws IsamException
	{
		boolean		m_eof;
		RftpRequest rftp = new RftpRequest (getRequestBuf (),
			path, true);

		sendRequest (rftp);

		RowReply	reply = new RowReply (getReplyBuf (), out);
		do
		{
			reply.reset ();
			getReply (reply, 10000);
		} while (!reply.isEof ());
	}
	synchronized public HashMap execProc (String procname, HashMap values)
		throws IsamException
	{
		boolean		m_eof;
		ProcRequest proc = new ProcRequest (getRequestBuf (),
			procname, values);

		sendRequest (proc);

		ProcReply	reply = new ProcReply (getReplyBuf (), values);
		getReply (reply, 10000);
		return reply.getResults ();
	}
*/
	public static void logInfo (String info)
	{
		try
		{
			FileOutputStream fo = new FileOutputStream ("/pick.log", true);
			fo.write (info.getBytes ());
			fo.write ((byte) '\n');
			fo.close ();
		}
		catch (Exception ex)
		{
			System.out.println (info);
		}
	}

	/**
	 * Creates a pick file.
 	 * @param filename	The Pick table name.
 	 * @param maxrecordlength	The default record length. Used to setup a buffer.
	 * @param recordSetSize	Number of records that should be returned at a time.
 	 */
 	public PickFile getPickFile (String filename, int maxrecordlength, 
		int recordSetSize) throws PickException
		{
			return new PickFile (this, filename, maxrecordlength, recordSetSize);
		}
															
	public static void main (String argv [])
	{

		String		host = "pickserve";
		int			port = PICK_PORT;
		if (argv.length == 1)
		{
			port = Integer.parseInt (argv [0]);
		}
		PickClient		pick = new PickClient (host, port);
		try
		{
			pick.connect ("bbalke", "bbalke", "pickserve");

			PickFile pClient = pick.getPickFile("CLIENTS",200,200);
			PVar rowid = pClient.makeVar(PVar.PICK_FIELD_ROWID);
			PVar clt1 = pClient.makeVar(1);
			PVar clt2 = pClient.makeVar(2);
			PVar clt3 = pClient.makeVar(3);
			PVar clt4 = pClient.makeVar(4);
			pClient.open();

			PickFile pPolicy = pick.getPickFile("POLICIES",200,200);
			PVar pol1 = pPolicy.makeVar(1);
			PVar pol2 = pPolicy.makeVar(2);
			PVar pol3 = pPolicy.makeVar(3);
			PVar pol4 = pPolicy.makeVar(4);
			pPolicy.open();

			for (int i = 0; i < 1; i++)
			{
				//if (pClient.read (100 + i))
				//{
					//System.out.println (clt1);
				//}
				//else
				//{
					//System.out.println ("-- no record -------");
				//}

				pClient.select (); //"SELECT CLIENTS BY CLIENT.CODE WITH CLIENT.CODE LIKE \"D]\"");
				while (pClient.next ())
				{
					System.out.println ("rec: " + rowid + " " + clt1 + " " + clt2 + " " + clt3 + " " + clt4);
					//pPolicy.select ("CLIENTS.POLICIES", pClient.getRcdNo ());
					//while (pPolicy.next ())
					//{
						//System.out.println ("    rec: " + pPolicy.getRcdNo () + " " + pol1 + " " + pol2 + " " + pol3 + " " + pol4);
					//}
				}
			}
			pClient.close();
			pPolicy.close();

			pick.disconnect ();
		}
		catch (Exception ex)
		{
			ex.printStackTrace ();
			try
			{
				pick.disconnect ();
			} catch (Exception ignore) { ex.printStackTrace (); }
		}
	}
}
