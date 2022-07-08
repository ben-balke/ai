/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/IsamFailure.java,v 1.1 2010/10/19 00:44:26 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

/**
 * This class holds error information returned from the IsamServer
 * in failure packets.
 */
public class IsamFailure extends Object
{
	private int			m_errorNo;
	private String		m_errorText;
	/**
	 * Errorcode matching the Isam Server error codes.
	 */
    public static final int ER_BAD_CON_PACKET       = 1;
    public static final int ER_UNEXPECTED_CLOSE     = 2;
    public static final int ER_BAD_PACKET           = 3;
    public static final int ER_LOGIN_REQUIRED       = 10;
    public static final int ER_LOGIN_FAILED         = 11;
    public static final int ER_OPEN_LIMIT           = 12;
    public static final int ER_SYNTAX_ERROR         = 13;
    public static final int ER_NO_VARIABLE          = 14;
    public static final int ER_BAD_FILECHANNEL      = 15;
    public static final int ER_ISAM                 = 16;
    public static final int ER_VARLIST_WRONG_SIZE   = 17;
    public static final int ER_KEY_WRONG_SIZE       = 18;
    public static final int ER_INVALID_INDEX        = 19;
    public static final int ER_TIMEOUT              = 20;
    public static final int ER_SHELL_ERROR          = 21;
    public static final int ER_LICENSE_EXPIRE       = 22;
	public static final int ER_DUP_INDEX			= 23;

		/**
		 * Internal errors are < 0.  0-255 are errors generated
		 * by the server.
		 */
	public static final int		BADPACKET = -1;
	public static final int		UNEXPECTED_COMMAND = -2;

	public IsamFailure (IsamReply isamReply)
	{
		m_errorNo = isamReply.parseByte ();
		m_errorText = isamReply.parseString ();
	}
	public IsamFailure (int errno, String errorText)
	{
		m_errorNo = errno;
		m_errorText = errorText;
	}
	public String toString ()
	{
		return "IsamFailure: " + m_errorNo + ":" + m_errorText;
	}
	public int getErrorNo () 
	{
		return m_errorNo;
	}
}
