/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/Bcd8.java,v 1.1 2010/10/19 00:44:26 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

import java.text.DecimalFormat;

public class Bcd8 extends Bcd
{
	public Bcd8( byte[] p_byteDef, int offset )
						throws BcdException
	{
		super( p_byteDef , offset );
	}

	public Bcd8( String p_strNum )
						throws BcdException
	{
		super( p_strNum );
	}

	public Bcd8( double p_dVal )
					throws BcdException
	{
		super( p_dVal );
	}

	public Bcd8( int p_iVal )
					throws BcdException
	{
		super( p_iVal );
	}

	protected int getDefiningByteCnt()
	{
		return 8;
	}

	protected int getPrecision()
	{
		return 14;
	}
}
