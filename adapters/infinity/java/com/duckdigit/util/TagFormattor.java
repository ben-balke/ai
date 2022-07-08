/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/util/TagFormattor.java,v 1.1 2010/10/19 00:43:59 secwind Exp $
==================================================================================================*/
package com.duckdigit.util;

import java.util.*;
import java.io.*;
import java.net.URLEncoder;

/**
 * Interface for providing inline formating of ZML tags.  Group operations allow
 * the formattor to be notified when repeating content is established and for subsequent 
 * parsing (iteration).
 */
public interface TagFormattor
{
	/**
	 * formatValue transforms value based on the rules provided in
	 * the freeform format string.
	 * @param value	Value to transform.  Can be null.
	 * @param format Formatting rules.
	 */
	String formatValue (String field, Object value, String format) throws SkipLineException;
	/**
	 * establishGroup is called when a content grouping is identified
	 * by the TagParser.  It is only called when the group is first identified.
	 */
	void establishGroup ();
	/**
	 * startGroupIteration is called when the group is about to be parsed.
	 */
	void startGroupIteration ();
	/**
	 * endGroupIteration is called when the group content has finished being 
	 * parsed for the current field values.
	 */
	void endGroupIteration ();
	
	/**
	 * finishGroup is called when all group iterations are complete.
	 */
	void finishGroup ();
}
