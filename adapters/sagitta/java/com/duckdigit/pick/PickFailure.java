
/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/pick/PickFailure.java,v 1.2 2010/04/22 18:41:42 secwind Exp $
==================================================================================================*/
package com.duckdigit.pick;

/**
 * This class holds error information returned from the PickServer
 * in failure packets.
 */
public class PickFailure extends Object
{
	private int		m_errorNo;
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
    public static final int ER_PICK                  = 16;
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

	public PickFailure (PickReply pickReply)
	{
		m_errorNo = pickReply.parseByte ();
		m_errorText = pickReply.parseString ();
	}
	public PickFailure (int errno, String errorText)
	{
		m_errorNo = errno;
		m_errorText = errorText;
	}
	public String toString ()
	{
		return "PickFailure: " + m_errorNo + ":" + m_errorText;
	}
	public int getErrorNo () 
	{
		return m_errorNo;
	}
}
