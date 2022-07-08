/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/BasixData.java,v 1.1 2010/10/19 00:44:25 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

public interface BasixData
{
	public void set (int i, int offset, int size)
		throws BasixDataException;
	public void set (double d, int offset, int size)
		throws BasixDataException;
	public void set (String str, int offset, int size)
		throws BasixDataException;
	public int getInt (int offset, int size)
		throws BasixDataException;
	public double getDouble (int offset, int size)
		throws BasixDataException;
	public String getString (int offset, int size)
		throws BasixDataException;
	public byte [] getBytes (int offset, int size)
		throws BasixDataException;
	public void getBytes (byte dest [], int destoffset, int offset, int size)
		throws BasixDataException;
}
