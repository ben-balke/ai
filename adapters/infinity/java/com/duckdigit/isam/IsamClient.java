/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/IsamClient.java,v 1.1 2010/10/19 00:44:26 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

import java.net.*;
import java.io.*;
import java.util.*;

import com.duckdigit.util.Crypt;

/**
 * This class is the main client to the 2ndWind IsamServer.
 * It connects and manages all ISAM file requests to and from
 * 2ndWind Server.
 * BBB we may need to have a finalize method that disconnects
 * gracefully.
 */
public class IsamClient extends Object
{
		/**
		 * Number of milliseconds to wait for a reply from the server
		 * before giving up.
		 */
	static final int READ_TIMEOUT = 30000;
		/**
		 * Maximum size of the input and output buffers.
		 */
	static final int BUFMAX = 1024 * 3;
		/**
		 * Packet size that represents a keep alive situation
		 * where a long query is executing and records are being
		 * skipped.  Must match the server.
		 */
	static final int KEEPALIVE_PACKET = 0x0000ffff;
		/**
		 * Host that provides access to the ISAM database.
		 */
	String		m_host;
		/**
		 * TCP Port number where the ISAM server is listening.
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
		 * Buffer used to read data from the socket.
		 */
	byte			m_inbuf [] = new byte [BUFMAX];
		/**
		 * Buffer used to construct and write data to the
		 * socket.
		 */
	byte			m_outbuf [] = new byte [BUFMAX];

	String			m_username;
	String			m_password;
	String			m_ipaddress;
	/**
	 * Constructor.
	 * @param host	host URL to the 2ndWind server.
	 * @param port	TCP port number to 2ndWind.
	 */
	public IsamClient (String host, int port)
	{
		m_host = host;
		m_port = port;
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
	 * @exception IsamException Thrown when the server is not available,
	 *		of the connection request was denied for security reasons.
	 */
	public void connect (String username, String password, String ipaddress)
		throws IsamException
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

				ConnectRequest conreq = new ConnectRequest (getRequestBuf (), username,
						password, ipaddress);
				sendRequest (conreq);
				getReply (new ConnectReply (getReplyBuf ()), READ_TIMEOUT);
				m_username = username;
				m_password = password;
				m_ipaddress = ipaddress;
			}
			catch (IsamException iex)
			{
				throw iex;
			}
			catch (Exception ex)
			{
				throw new IsamException (ex.getMessage ());
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
	public synchronized boolean isalive () throws IsamException
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
		throws IsamException
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
				throw new IsamException (
					"Did not receive expected number of bytes: " +
						nbytes + " instead of " + readsize + ".");
			}
			readsize -= nbytes;
			offset += nbytes;
		} while (readsize > 0);
	}

	/**
	 * Waits for a reply from the server.
	 * @param reply		An IsamReply derived object that is to
	 *		receive the incoming data.
	 * @exception IsamException	when a error reading or parsing the packet
	 *		occurs.
	 */
	synchronized void getReply (IsamReply reply, int timeout)
		throws IsamException
	{
		if (m_socket == null)
		{
			throw new IsamException ("Connection is not open");
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
				readBytes (m_inbuf, 0, IsamRequest.PACKETHDR_SIZE);
				Crypt.decrypt (m_inbuf, 0, IsamRequest.PACKETHDR_SIZE);
				len =	reply.parsePacketSize ();
				//System.out.println ("packetsize = " + Integer.toHexString (len));
				// BBB
				if (len == KEEPALIVE_PACKET)
				{
					reply.reset ();
					//System.out.println ("Got Keep Alive\n");
				}
			} while (len == KEEPALIVE_PACKET);

			readBytes (m_inbuf, IsamRequest.PACKETHDR_SIZE, len);
			Crypt.decrypt (m_inbuf, IsamRequest.PACKETHDR_SIZE, len);
				//
				// Now let the derived IsamReply parse the packet.
				//
			reply.parseReply(len);
		}
		catch (IsamException iex)
		{
			throw iex;
		}
		catch (Exception ex)
		{
			ex.printStackTrace ();
			throw new IsamException (ex.getMessage ());
		}
	}
	/**
	 * Prepares and sends a packet to the server.
	 * @param packet	IsamRequest containing the data to send.
	 * @exception		IsamException when the transmission fails.
	 */
	public void sendRequest (IsamRequest packet)
		throws IsamException
	{
		if (m_out == null)
		{
			throw new IsamException ("Not connected.");
		}
		try
		{
			int count = packet.prepare ();
			Crypt.encrypt (m_outbuf, 0, count);
			m_out.write (m_outbuf, 0, count);
		}
		catch (IOException ex)
		{
			throw new IsamException (ex.getMessage ());
		}
	}
	/**
	 * Creates a random file.
	 * @param filename	The Basix Isam Filename.
	 * @param fields	The definition of the isam fields.  This defines
	 *		the fields of the record that correspond to the INfinity File Layout.
	 *		Each field segment of the layout is defined as a variable preceeded by
	 *		a type.  For example:  STRING J$(100), SHORT J(1), STD J1(9), LONG (5).
	 */
	public RandomFile getRandomFile (String filename, String fields,
		int recordSetSize) throws IsamException
	{
		return new RandomFile (this, filename, fields, recordSetSize);
	}

	synchronized public void remoteExecute (String command, OutputStream out)
		throws IsamException
	{
		boolean		m_eof;
		SOpnRequest sopn = new SOpnRequest (getRequestBuf (),
			command);

		sendRequest (sopn);

		RowReply	reply = new RowReply (getReplyBuf (), out);
		do
		{
			reply.reset ();
			getReply (reply, 10000);
		} while (!reply.isEof ());
	}
	/**
	 * Gets a remote file from the iserve.  A RftpRequest is sent and
	 * the server returns RowReplys until an end of file is reached where 
	 * a 0 byte packet is returned.
	 */
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

	public static void logInfo (String info)
	{
		try
		{
			FileOutputStream fo = new FileOutputStream ("/isam.log", true);
			fo.write (info.getBytes ());
			fo.write ((byte) '\n');
			fo.close ();
		}
		catch (Exception ex)
		{
			System.out.println (info);
		}
	}

	public static void main (String argv [])
	{

		String		host = "iserve";
		int			port = 3123;
		if (argv.length == 1)
		{
			port = Integer.parseInt (argv [0]);
		}
		IsamClient		isam = new IsamClient (host, port);
		try
		{
			isam.connect ("bbalke", "bbalke", "iserve");
			/****************
			isam.getFile ("/tmp/b", System.out);
			
			HashMap			map = new HashMap ();
			map.put ("file", "P0101236");
			map.put ("cono", "100");
			map.put ("pcno", "0");
			isam.execProc ("spooldesc", map);
			System.out.println ("desc = " + map.get ("desc"));
			isam.remoteExecute ("ls -l", System.out);
			//isam.remoteExecute ("tf_read 0 99301 138066688", System.out);
			*****************/

			RandomFile rf = isam.getRandomFile ("J+ARCMF",
				"J$(260),J(5),J1(13),J2(2)", 200);

			rf.addIndex ("J!ARCMF", "J1(0),J1(1),J$(1,8)");
			//rf.addIndex ("J!ARCMF1", "J1(0),J1(1),J$(9,18),J$(1,8)");

			rf.open ();

			BVar co = rf.makeVar ("J1(0)");
			BVar pc = rf.makeVar ("J1(1)");
			BVar custno = rf.makeVar ("J$(1,8)");
			BVar addr = rf.makeVar ("J$(99,128)");
			BVar city = rf.makeVar ("J$(129,143)");
			BVar state = rf.makeVar ("J$(144,148)");
			BVar zip = rf.makeVar ("J$(149,157)");
			BVar name1 = rf.makeVar ("J$(39,68)");
			BVar inqname = rf.makeVar ("J$(9,18)");
			BVar lastactive = rf.makeVar ("J1(4)");
			BVar custtype = rf.makeVar ("J$(19,20)");

			co.set (1);
			pc.set (0);
			custno.set ("A");

			//System.out.println (rf.dumpBuf ());
			//System.out.println (co.getInt ());
			//System.out.println (pc.getDouble ());
			//System.out.println (custno.getString ());
			//UpdateRequest	upr = new UpdateRequest ();
			//upr.addField (name1);
			//upr.addField (addr);
			//upr.addField (city);
			//upr.addField (state);
			//upr.addField (zip);

			rf.select ("J!ARCMF",
				"J1(0) = 1 AND J1(1) = 0 AND J$(1,1) = \"A\"", 
				"J1(0) = 1", 
				"J$(19,20),J$(9,18),J$(1,8)");
			int i = 1;
			while (rf.next ())
			{
				//if (co.getInt () != 100 || pc.getInt () != 0)
				//{
					//break;
				//}
				System.out.println (i + ". " + co.getInt () + " : " +
					pc.getInt () + " : " + custno.getString () + " Type: " + custtype.getString () + " InqName: " + inqname.getString ());
				//name1.set ("show: " + custno.getString (null));
				//addr.set ("" + i + " Yellow Brick Road", null);
				//city.set ("Emerald City", null);
				//state.set ("OZ", null);
				//zip.set ("91364", null);
				//upr.setRcdNo (rf.getRcdNo ());
				//rf.update (upr);
				i ++;
			}
			rf.close ();
			isam.disconnect ();
		}
		catch (Exception ex)
		{
			ex.printStackTrace ();
			try
			{
				isam.disconnect ();
			} catch (Exception ignore) { ex.printStackTrace (); }
		}
	}
}
