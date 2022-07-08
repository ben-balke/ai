/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/util/lang/StateInterpreter.java,v 1.2 2010/04/22 18:41:48 secwind Exp $
==================================================================================================*/
package com.duckdigit.util.lang;

/**
	Declares an object that accepts a key-value property map as the source of
	its state.
 */
public interface StateInterpreter
{
	void interpret( StateProperties      p_state )
		throws InvalidStateException;
}
