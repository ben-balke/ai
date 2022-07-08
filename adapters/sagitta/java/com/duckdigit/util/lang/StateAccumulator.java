/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/util/lang/StateAccumulator.java,v 1.2 2010/04/22 18:41:48 secwind Exp $
==================================================================================================*/
package com.duckdigit.util.lang;

/**
	Declares an object that supports state definition through multiple calls to
	{@loc com.duckdigit.util.lang.StateInterpreter.interpret}.
	<p>
	Objects implementing this interface should call {@loc establishDefaults} in
	their constructors, and pass their attributes as the default values when
	retrieving state from the {@loc com.duckdigit.util.lang.StateProperties}:
	<p>
	<code>myAttr = p_state( "MyField", myAttr );</code>.
 */
public interface StateAccumulator
	extends StateInterpreter
{
	/**
		Set up default values for attributes.
	 */
	void establishDefaults();
}
