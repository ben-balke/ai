/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/BcdException.java,v 1.1 2010/10/19 00:44:26 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

public class BcdException extends Exception
{
	public static final int		INSUFFICIENT_BYTE_CNT = 0;
	public static final int		INVALID_BYTE_CNT = 1;
	public static final int		NIBBLE_GT_9 = 2;
	public static final int		INVALID_NUMBER_STR = 3;
	private int					m_iFailType;
	private int					m_iBadByteNo;

	public BcdException( int p_iFailType )
	{
		super();
		m_iFailType = p_iFailType;
	}

	public BcdException( int p_iFailType, int p_iBadByteNo )
	{
		super();
		m_iFailType = p_iFailType;
		m_iBadByteNo = p_iBadByteNo;
	}

	public String toString()
	{
		String strDisp;

		strDisp = new String();

		switch (m_iFailType)
		{
			case INSUFFICIENT_BYTE_CNT:
				strDisp = "Insufficient number of bytes to define instance";
				break;

			case INVALID_BYTE_CNT:
				strDisp = "Invaliud number of bytes to define instance";
				break;

			case NIBBLE_GT_9:
				strDisp = "Byte #" +
								m_iBadByteNo +
									" has a value greater than 9";
				break;

			case INVALID_NUMBER_STR:
				strDisp = "Could not interpret the numerical string";
				break;
		}

		return strDisp;
	}
}
