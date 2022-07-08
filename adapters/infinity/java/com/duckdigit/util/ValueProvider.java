/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/util/ValueProvider.java,v 1.1 2010/10/19 00:43:59 secwind Exp $
==================================================================================================*/
package com.duckdigit.util;

import java.io.PrintStream;
import java.util.Iterator;

public interface ValueProvider
{
	/**
		Returns all keys defined on the provider.
		@return Iterator            To access keys.
	 */
	Iterator getKeys();

	/**
		Gets the value associated with the key.
		@param String       Key.
		@return Object      Representation of the value.
	 */
	Object getValue (String key);

	/**
		Gets the context-sensitive representation of a value.
		<p>
		Depending upon the context of the parsing operation, different
		representations of the value may be desired. The mode parameter
		is used to select the representation.
		@param String       Key.
		@param mode         Mode.
		@return Object      Representation of the value.
	 */
	Object getValue (String key, int mode);

	/**
		Returns the provider's name.
		@return String
	 */
	String getName ();

	/**
		Sets the provider name. Used for aliasing operations.
		@param name
	 */
	void setName (String name);

	/**
		Changes a value stored in the provider.
		@param key
		@param value
		@throws ReadOnlyValueProviderException
	 */
	void setValue (String key, Object value)
		throws ReadOnlyValueProviderException;

	/**
	 * Returns a nested value provider from this value provider.  If this
	 * ValueProvider does not contain a ValueProvider named as key then
	 * null is to be returned.
	 * @param key the key that identifies the nested value provider.
	 */
	ValueProvider getNestedProvider (String key);
}
