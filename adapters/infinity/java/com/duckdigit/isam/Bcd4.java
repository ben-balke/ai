/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/Bcd4.java,v 1.1 2010/10/19 00:44:25 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

import java.text.DecimalFormat;

public class Bcd4 extends Bcd
{
	public Bcd4( byte[] p_byteDef, int p_offset )
						throws BcdException
	{
		super( p_byteDef, p_offset );
	}

	public Bcd4( String p_strNum )
						throws BcdException
	{
		super( p_strNum );
	}

	public Bcd4( double p_dVal )
					throws BcdException
	{
		super( p_dVal );
	}

	public Bcd4( int p_iVal )
					throws BcdException
	{
		super( p_iVal );
	}

	protected int getDefiningByteCnt()
	{
		return 4;
	}

	protected int getPrecision()
	{
		return 6;
	}
}
