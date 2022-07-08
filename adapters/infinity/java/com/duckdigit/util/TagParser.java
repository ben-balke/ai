/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/util/TagParser.java,v 1.1 2010/10/19 00:43:59 secwind Exp $
==================================================================================================*/
package com.duckdigit.util;

import java.util.*;
import java.io.*;
import java.net.URLEncoder;

/**
 * Provides a simple mechanism for parsing tags from a string or stream.
 * To use:
 * <UL>
 * <LI>Establish a Hashtable of key/value pairs.
 * <LI>Instansiate a TagPraser with either the default tag identifiers '{ZML:' and '}'
 * or your own tag identifiers like '${' and '}'.
 * <LI>Call substituteLine to substitute a single text string, or parseStream to substitute
 * an entire input stream.
 * </UL>
 */
public class TagParser extends Object implements LineFilter
{
	static final String 	CONTROLS = "@%-#$^+*!=_~";
	static final String     PROV_SEP = ",";
	static final String     SELECT_SEP = "|";
	static final String     FIELD_SEP = ".";
	LinkedList				m_valueProviders = new LinkedList ();
	String                  m_defPrefix;
	String                  m_defSuffix;
	public String			m_prefix;
	public String			m_suffix;
	int						m_prefixLen;
	int						m_suffixLen;
	/* Expression compiler. */
	Stack                   m_modes = new Stack();
	Stack                   m_frmtStack = new Stack();
	TagFormattor			m_tagformattor = null;

	// Value representation modes.
	public static final int NORMAL       = 0;
	public static final int VALUE        = 1;
	public static final int CONTENT      = 2;

	/**
	 * Creates a tag parser with custom tag parsing.
	 * @param prefix	identifier prefix.
	 * @param suffix	identifier suffix.
	 */
	public TagParser (String prefix, String suffix)
	{
		m_defPrefix = prefix;
		m_defSuffix = suffix;
		setSearchTags (prefix, suffix);
		setMode( NORMAL );
	}

	/**
		Release resources assigned to the parser.
	 */
	public void clear ()
	{
		m_valueProviders.clear ();
		m_valueProviders = null;
	}

	/*==========================================================================
	===== Search tag management.
	==========================================================================*/

	/**
	 * Sets the prefix and suffix strings that idenfify a tag to
	 * substitute.
	 * @param prefix	identifier prefix.
	 * @param suffix	identifier suffix.
	 */
	public void setSearchTags (String prefix, String suffix)
	{
		m_prefix = prefix;
		m_prefixLen = prefix.length ();
		m_suffix = suffix;
		m_suffixLen = suffix.length ();
	}

	/**
	 * Restores default substitution tag delimiters.
	 */
	public void setDefaultSearchTags ()
	{
		setSearchTags (m_defPrefix, m_defSuffix);
	}

	/*==========================================================================
	===== Value association and recovery.
	==========================================================================*/

	/**
	 * Appends a ValueProvider to the Tag Parser.  ValueProviders are used
	 * in the order in which they are added.
	 */
	public void addValueProvider (ValueProvider values)
	{
		m_valueProviders.add (values);
	}

	/**
	 * Retrieve the described value provider.
	 * @param   String  Provider name.
	 * @returns ValueProvider
	 */
	public ValueProvider getValueProvider(
			String      m_sProv )
	{
		Iterator            provs;
		ValueProvider       prov;

		provs = m_valueProviders.iterator();
		while (provs. hasNext())
		{
			prov = (ValueProvider) provs. next();
			if (0 == prov.getName().compareToIgnoreCase( m_sProv ))
			{
				return prov;
			}
		}

		return null;
	}

	/**
	 * Remove a ValueProvider.
	 * @param	values	ValueProvider added using addValueProvider().
	 */
	public void removeValueProvider (ValueProvider values)
	{
		m_valueProviders.remove (values);
	}

	/**
		Sets the mode for value interpretation.
		@param int          Interpretation mode.
	 */
	public void setMode(
		    int         p_iMode )
	{
		m_modes.push( new Integer( p_iMode ) );
	}

	public int getMode()
	{
		return ((Integer) m_modes. peek()). intValue();
	}

	public void restoreMode()
	{
		m_modes. pop();
	}

	/**
		Retrieves an object from the value collection.
		@param String       Value identifier, in the format
							<provider name>.<value name> <format>
		@returns Object     Value. <code>null</code> if name cannot be resolved.
	 */
	 public Object getObject(
			String      p_sIdent )
	{
		int             iProv;
		int             iSp;

		StringTokenizer refs;
		String          sRef;
		Object          value;

		iProv = 0;
		while (-1 != CONTROLS.indexOf( p_sIdent.charAt( iProv)))
		{
			iProv++;
		}

		iSp = p_sIdent.indexOf( ' ', iProv );
		if (-1 == iSp)
		{
			sRef = p_sIdent.substring( iProv );
		}
		else
		{
			sRef = p_sIdent.substring( iProv, iSp );
		}

		if (-1 != sRef. indexOf( PROV_SEP ))
		{
			sRef = expandProvLists( sRef );
		}

		if (-1 == sRef. indexOf( SELECT_SEP ))
		{
			return lookupObject( sRef );
		}

		value = null;
		refs = new StringTokenizer( sRef, SELECT_SEP );
		while ((null == value) &&
				(refs. hasMoreTokens()))
		{
			String		tok = refs. nextToken();
				/*
				 * If the next value is proceeded by an '=' sign
				 * then use it is a constant value and no addition
				 * parsing is done. Simply assign as a string.
				 */
			if (tok.charAt (0) == '=')
			{
				value = tok.substring (1);
			}
			else
			{
					/**
					 * Here we prepend the first fields modifiers and get the actual value
					 * so that the format process is correct for the next provider.
					 */
				if (iProv > 0)
				{
					sRef = p_sIdent.substring (0, iProv);
				}
				else
				{
					sRef = "-";
				}
				sRef += tok;
				value = getValue ( sRef );
			}
		}

		return value;
	}

	/*
		Find object registered under given name.
		<p>
		If the provider name is not provided, we search all providers.
		@param String           Value reference, of format
								{<Provider Name>.]<Value Key>
		@returns Object         <code>null</code> if key is unknown.
	*/
	private Object lookupObject(
			String      p_sRef )
	{
		int             iSep;
		int             iName;
		String          sProv;
		String          sName;
		String          sSub;
		StringTokenizer namePrs;

		Iterator        itor;
		ValueProvider   prov;
		Object          value;

		iSep = p_sRef.indexOf( '.' );
		if (-1 == iSep)
		{
			sProv = null;
			sName = p_sRef;
		}
		else
		{
			sProv = p_sRef.substring( 0, iSep );
			sName = p_sRef. substring( iSep + 1 );
		}

		itor = m_valueProviders.iterator();
		while (itor. hasNext())
		{
			prov = (ValueProvider) itor. next();
			if (null != sProv)
			{
				if (prov. getName(). equals( sProv ))
				{
					value = null;
					namePrs = new StringTokenizer( sName, "." );
					sName = "";
					while (namePrs.hasMoreTokens())
					{
						if (0 != sName.length()) sName += ".";
							// Above implies we did not find value with
							// first token. Append in case dot-separators are
							// used in key itself 
						sName += namePrs.nextToken();
						if (namePrs.hasMoreTokens ())
						{
							ValueProvider newProv = prov.getNestedProvider (sName);
							if (newProv != null)
							{
								prov = newProv;
								sName = "";
							}
						}
						else
						{
							value = prov. getValue(
												sName,
												getMode() );
						}
					}
					return value;
				}
			}
			else
			{
				value = prov. getValue(
							    sName,
								getMode() );
				if (null != value)
				{
					return value;
				}
			}
		}

		return null;
	}

	private String expandProvLists(
			String      p_sRef )
	{
		StringBuffer        sExp;
		StringTokenizer     terms;
		String              sTerm;
		StringTokenizer     fields;
		String              sField;
		StringTokenizer     provs;
		String              sProv;

		sExp = new StringBuffer( 256 );
		terms = new StringTokenizer( p_sRef, SELECT_SEP, false );
		while (terms.hasMoreTokens())
		{
			sTerm = terms. nextToken();

			fields = new StringTokenizer( sTerm, FIELD_SEP, false );
			if (1 == fields.countTokens())
			{
				sExp. append( fields. nextToken() );
				sExp. append( SELECT_SEP );
			}
			else if (2 == fields.countTokens())
			{
				provs = new StringTokenizer(
										fields. nextToken(),
										PROV_SEP,
										false );
				sField = fields. nextToken();

				while (provs. hasMoreTokens())
				{
					sExp.append( provs. nextToken() );
					sExp. append( FIELD_SEP );
					sExp. append( sField );
					sExp. append( SELECT_SEP );
				}
			}
		}

		sExp. setLength( sExp. length() - 1 );
		return new String( sExp );
	}


	/*==========================================================================
	===== Substitution value recovery.
	==========================================================================*/

	/**
	 * Returns a value for a ZML variable that matches name.  If
	 * name has an @ sign as the first character and the field cannot
	 * be found a blank string is returned.  If the name has a % before
	 * it is does URL encoding of the string value.
	 */
	public Object getValue (String name)
	{
		try
		{
			return getFormattedValue (name);
		}
		catch (SkipLineException ex)
		{
			return null;
		}
		catch (SkipSectionException ex)
		{
			return null;
		}
	}
	/**
	 * Returns a value for a ZML variable that matches name.  If
	 * name has an @ sign as the first character and the field cannot
	 * be found a blank string is returned.  If the name has a % before
	 * it is does URL encoding of the string value.  If the name is preceeded
	 * by a '-' character, the entire and the value is null, the SkipLineException
	 * is thrown.
	 * @param name	The name of the ZML variable to extract from the
	 *		value providers.  Its variable identifier is <zmlgroup>.<field>
	 *      followed by an optional formatting string which is passed to the
	 *      current tag formatter.  The format is comprised of all characters
	 *      after the first space.
	 * @exception SkipLineException	tells the calling routine that the
	 *		entire line is to be ignored if no value is associated with the tag.
	 * @exception SkipSectionException	tells the calling routine it should skip
	 *      an entire section (as specified by its own syntax) if no value is
	 *      associated with the tag.
	 */
	public Object getFormattedValue (String name)
		throws
			SkipLineException,
			SkipSectionException
	{
		Object			value;
		int             iCtl;
		int             iSp;
		String			formatting = null;
			/**
			 * BBB need to add interoperability for both @ and %
			 */
		boolean			ignore = false;
			/**
			 * Do URL character conversions.  IE., escapes
			 * spaces, etc.
			 */
		boolean			url = false;

			/**
			 * BBB need to add interoperability for both @ and %
			 */
			/**
			 * Skip line if no text.
			 */
		boolean			skipline = false;
			/**
			 * Skip section if no text.
			 */
		boolean			skipsection = false;
			/**
			 * Require text.
			 */
		boolean         needtext = false;
			/**
			 * If multipass is set then the results of the substitution
			 * are parsed again.
			 */
		boolean			multipass = false;

			/**
			 * Generate a Java script concatonated string with
			 * quotes and newline characters.  This facilitates
			 * generating documents from Javascript.
			 */
		boolean 		javascript = false;
			/**
			 * Generate a HTML attribute quoted string.  This replaces all "'s with &#034;
			 */
		boolean 		attrescape = false;
			/**
			 * Generate a HTML quoted string.  This replaces <,>, and & with &lt;, &gt; and &amp;
			 * respectively.
			 */
		boolean 		htmlescape = false;

		boolean			boolnot = false;

			/**
			 * Forces the substitution engine into "VALUE" mode.
			 */
		boolean         modenorm = false;

		int				noparse = -1;

		String          sCtrlCand;

		if ((null == name) || (0 == name.length()))  return null;

		iCtl = 0;
		while (iCtl < name.length())
		{
			switch (name. charAt( iCtl ))
			{
			case '@': ignore        = true;   break;
			case '%': url           = true;   break;
			case '-': skipline      = true;   break;
			case '_': skipsection   = true;   break;
			case '+': needtext      = true;   break;
			case '#': javascript    = true;   break;
			case '^': htmlescape    = true;   break;
			case '$': attrescape    = true;   break;
			case '*': multipass     = true;   break;
			case '!': boolnot       = true;   break;
			case '~': modenorm      = true;   break;
			case '=': noparse       = iCtl;   break;
			// After adding new options, be sure the key is added to the
			// CONTROLS string.
			default:
				iCtl = name.length();
				break;
			}
			iCtl ++;
		}

		if (noparse != -1)
		{
			if (noparse != 0)
			{
				return m_prefix + name.substring (0, noparse) +
					   name.substring (noparse + 1) + m_suffix;
			}
			return m_prefix + name.substring (1) + m_suffix;
		}

		if (modenorm)
		{
			setMode( NORMAL );
		}
		try {
			value = getObject( name );
		}
		finally {
			if (modenorm)
			{
			    restoreMode();
			}
		}

		/**
		 * First let the tag formattor work on the value.
		 * Let the tag formattor have a chance to work on null
		 * values.
		 */
		if (null != m_tagformattor)
		{
			iSp = name. indexOf( ' ' );
			if (-1 != iSp)
			{
				formatting = name. substring( iSp + 1 );
				formatting = formatting.trim();
				value = m_tagformattor.formatValue (name, value, formatting);
			}
		}
		/**
		 * Determine the null status of the field and apply the
		 * boolnot modifier to ascertain the final decision.
		 */
		boolean nullvalue = false;
		String		valueString = null;
		nullvalue = true;
		if (value != null)
		{
			valueString = value.toString ();
			if (valueString != null)
			{
				if (!(needtext && valueString.trim().length() == 0))
				{
					nullvalue = false;
				}
			}
		}
		if ((nullvalue == true && boolnot == false) ||
			(nullvalue == false && boolnot == true))
		{
			if (ignore)
			{
				if (javascript)
				{
					return "''";
				}
				return "";
			}
			else if (skipline)
			{
				throw new SkipLineException ();
			}
			else if (skipsection)
			{
				throw new SkipSectionException();
			}
			return null;
		}
		else if (boolnot == true)
		{
			return "";
		}
			/**
			 * If the tag was actually found but the string
			 * value is null, then replace the tag unless skip line
			 * is set.
			 */
		if (valueString == null)
		{
			if (skipline)
			{
				throw new SkipLineException ();
			}
			else if (skipsection)
			{
				throw new SkipSectionException();
			}
			return "";
		}
		if (url)
		{
			try {
				value = URLEncoder.encode (valueString, "UTF8");
			} catch (Exception ex) {}
		}
		else if (htmlescape)
		{
			value = StringUtils.generateHtmlEscapedString (valueString);
			if (javascript)
			{
				value = StringUtils.generateJavascriptString ((String) value);
			}	
		}
		else if (attrescape)
		{
			value = StringUtils.generateHtmlAttributeString (valueString);
		}
		else if (javascript)
		{
			value = StringUtils.generateJavascriptString (valueString);
		}
		if (multipass)
		{
			value = substituteLine (valueString);
		}
		return value;
	}

	/**
	 * Parses a line using the '{' and '}' characters as the
	 * prefix and suffix respectively.  The default search tags
	 * are restored after calling this service.
	 * @param line	the string to parse.
	 * @return the substituted line.
	 */
	public String parseSimple (String line)
	{
		String		sInPre;
		String		sInSuf;
		String		rslt;
		
		sInPre = m_prefix;
		sInSuf = m_suffix;
		setSearchTags ("{", "}");
		setMode( NORMAL );
		rslt = substituteLine (line);
		restoreMode();
//		setDefaultSearchTags ();
		setSearchTags( sInPre, sInSuf );
		return rslt;
	}

	public String processTags(
		    String      line )
		throws
			SkipLineException,
			SkipSectionException
	{
		int				index = 0;
		int				startindex;
		int				endindex;
		String			name;
		StringBuffer	strbuf = null;
		Object			value;

		if (line == null)
			return null;
		while ((startindex = line.indexOf (m_prefix, index)) != -1)
		{
			if (strbuf == null)
			{
				strbuf = new StringBuffer ();
			}
			endindex = line.indexOf (m_suffix, startindex + m_prefixLen);
			if (endindex == -1)
			{
				strbuf.append (line.substring (index,
					startindex + m_prefixLen));
				index = startindex + m_prefixLen;
			}
			else
			{
				strbuf.append (line.substring (index, startindex));
				name = line.substring (startindex + m_prefixLen, endindex);
				value = getFormattedValue (name);
					// Point of origination for SkipLine and SkipSection
					// exceptions.
				if (value == null)
				{
					index = startindex + m_prefixLen;
					strbuf.append (m_prefix);
					//index = endindex + m_suffixLen;
					//strbuf.append (line.substring (startindex, endindex + m_suffixLen));
				}
				else
				{
					String		valueString = value.toString ();
					strbuf.append (valueString);
					index = endindex + m_suffixLen;
				}
			}
		}
			//
			// This is the case where no tags where found.  Just return
			// the incoming line to avoid string thrashing.
			//
		if (strbuf == null)
		{
			return line;
		}
		strbuf.append (line.substring (index));
		return strbuf.toString ();
	}

	/**
	 * Substitutes the tags in line with the key/value pairs.
	 * @param line	string to search and replace.
	 * @return String containing the substituted data.  Returns null if the
	 *			entire line is ignored.
	 */
	public String substituteLine (String line)
	{
		try {
			return processTags( line );
		}
		catch (SkipLineException sle) {
			return null;
		}
		catch (SkipSectionException sse) {
			return null;
		}
	}


	/**
	 * Substitutes the tags in a multiline (designated by newline characters)
	 * string with the key/value pairs.  Delete line operations remove entire lines
	 * of text.
	 * @param line	string to search and replace.
	 * @return String containing the substituted data.  Returns null if the
	 *			entire line is ignored.
	 */
	public String substituteMultiLine (String multiLine)
	{
		boolean                 bOutLast = true;
		String					line = null;
		String					parsedLine = null;
		StringBuffer			strbuf;
		
		if (multiLine.indexOf ('\n') == -1)
		{
			return substituteLine (multiLine);
		}
		strbuf = new StringBuffer ();

		StringTokenizer			stk = new StringTokenizer (multiLine, "\n");

		while (stk. hasMoreTokens())
		{
			line = stk. nextToken ();
			try 
			{
				parsedLine = processTags (line);
				if (parsedLine != null)
				{
					strbuf.append (parsedLine)
						  .append ('\n');
				}
			} 
			catch (SkipLineException ignore) 
			{
			}
			catch (SkipSectionException ignore)
			{
			}
		}
		return strbuf.toString ();
	}

	/**
	 * Preparses a line by building an array list of objects that
	 * represent the fields within the line.  The tags are substituted
	 * with the fill string if one is provided.  This is useful for
	 * replacing bind variables in an SQL statement.
	 * @param line	string to search and replace.
	 * @param fill	A string to replace the found fields.  If null the
	 *				tagged fieds are unchanged.  For example this is used
	 *				to replace a JDBC parameter with a '?' character so
	 *				a prepared statement can be bound to ZML variables.
	 * @param objects	Appends a list of fields that are located in the
	 *				line.  Each field is returns as {[<provider>.]<field name>).
	 * @return String containing the substituted data.
	 */
	public static String preparseLine (String line, String fill, ArrayList objects,
				String prefix, String suffix)
	{
		int				index = 0;
		int				startindex;
		int				endindex;
		String			name;
		StringBuffer	strbuf = null;

		int prefixLen = prefix.length ();
		int suffixLen = suffix.length ();

		if (line == null)
			return null;
		while ((startindex = line.indexOf (prefix, index)) != -1)
		{
			if (strbuf == null)
			{
				strbuf = new StringBuffer ();
			}
			endindex = line.indexOf (suffix, startindex + prefixLen);
			if (endindex == -1)
			{
				strbuf.append (line.substring (index,
					startindex + prefixLen));
				index = startindex + prefixLen;
			}
			else
			{
				strbuf.append (line.substring (index, startindex));
				name = line.substring (startindex + prefixLen, endindex);
				objects.add (name);
				if (fill != null)
				{
					strbuf.append (fill);
				}
				index = endindex + suffixLen;
			}
		}
			//
			// This is the case where no tags where found.  Just return
			// the incoming line to avoid string thrashing.
			//
		if (strbuf == null || fill == null)
		{
			return line;
		}
		strbuf.append (line.substring (index));
		return strbuf.toString ();
	}

	/*==========================================================================
	===== Post-processing resources.
	==========================================================================*/

	/**
		Select a tag formattor for subsequent parsing operations.
		<p>
		The parser recognizes any tokens following the tag name as possible
		formatting tags. They are provided to the formatter, if one id defined,
		following retrieval of the tag value. The formatter interprets the tags
		to produce the final tag value.
		@param TagFormattor
	 */
	public void setTagFormattor (TagFormattor tagformattor)
	{
		if (tagformattor == m_tagformattor) return;
		m_frmtStack.push( m_tagformattor );
		m_tagformattor = tagformattor;
	}


	/**
		Restore prior tag formattor for subsequent parsing operations.
		<p>
		This operation must be called when the formattor goes out of scope.
		@param TagFormattor
	 */
	public void resetTagFormattor(TagFormattor tagformattor)
	{
		if (m_tagformattor == tagformattor)
		{
			if (m_frmtStack.isEmpty())
			{
				m_tagformattor = null;
			}
			else
			{
				m_tagformattor = (TagFormattor) m_frmtStack.pop();
			}
		}
	}

	/*==========================================================================
	===== Test facility.
	==========================================================================*/

	public static void main (String args [])
	{
		Hashtable	ht = new Hashtable ();

		ht.put ("NAME", "BoneHead");
		ht.put ("A", "Stupid");

		Hashtable	ht2 = new Hashtable ();
		ht2.put ("NAME", "httpname");
		ht2.put ("A", "httpA");
		ht2.put ("B", "Abstract");

///*
		TagParser		jt = new TagParser ("${", "}");
		jt.addValueProvider (new MapValueProvider ("session", ht));
		jt.addValueProvider (new MapValueProvider ("http", ht2));
		System.out.println (jt.substituteLine ("The name of the insured is: {2ND:NAME} his statement: {2ND:A} was something he said"));
		System.out.println (jt.substituteLine ("{2ND:NAME} his statement: {2ND:A}"));
		System.out.println (jt.substituteLine ("{2ND:NAME}{2ND:A}{2ND:123123}"));
		System.out.println (jt.substituteLine ("The name of the insured is: ${NAME} his statement: ${A} was something he said"));
		System.out.println (jt.substituteLine ("${session.NAME} his statement: ${A}"));
		System.out.println (jt.substituteLine ("${http.NAME}${A}${123123}"));
		System.out.println (jt.substituteLine ("${@http.NAME|session.NAME}") );
		System.out.println (jt.substituteLine ("${session.NAME|http.NAME}") );
		System.out.println (jt.substituteLine ("${@session.B|http.B}") );
		System.out.println (jt.substituteLine ("${@C|http,session.D|http.B}") );
		System.out.println (jt.substituteLine ("${-session.C|http.C}") );
//*/
		/*
		TagParser		jt = new TagParser ();
		jt.addValueProvider (new MapValueProvider ("session", ht));
		try
		{
			//jt.parseStream (new InputStreamReader (System.in), System.out);
			jt.parseFile (" ./ben , ./ben1 ", System.out);
		}
		catch (Exception ex)
		{
			ex.printStackTrace (System.err);
			return;
		}
		TagParser		tagParser = new TagParser ("{", "}");
		ArrayList		params = new ArrayList ();
		String newline = tagParser.preparseLine ("insert into sw_users values ({firstname}, {lastname},"+
												 "{address}, null, {email})","?", params, "{", "}");

		System.out.println (newline);
		for (int i = 0; i < params.size (); i++)
		{
			System.out.println (i + "= " + params.get (i).toString ());
		}*/
	}
}

