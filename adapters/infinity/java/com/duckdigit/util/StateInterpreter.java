/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/util/StateInterpreter.java,v 1.1 2010/10/19 00:43:59 secwind Exp $
==================================================================================================*/
package com.duckdigit.util;

/**
	Declares an object that accepts a key-value property map as the source of
	its state.
 */
public interface StateInterpreter
{
	void interpret( StateProperties      p_state )
		throws InvalidStateException;
}
