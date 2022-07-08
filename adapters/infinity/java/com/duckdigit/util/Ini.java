/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/util/Ini.java,v 1.1 2010/10/19 00:43:59 secwind Exp $
==================================================================================================*/
package com.duckdigit.util;

import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.Stack;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;


class IniReader
{
	private final String	INSERT_DIRECTIVE = "#include ";
	Stack			m_iniStack = null;
	BufferedReader	m_ini;
	PathResolver	m_pathResolver;

	IniReader (String path, PathResolver pathResolver)
		throws IOException, IniIncludeException
	{
		// Open the file
		m_ini = new BufferedReader( new FileReader (path) );
		m_pathResolver = pathResolver;
	}
	public String readLine ()
		throws IOException, IniIncludeException
	{

		String			line;
		while ((line = m_ini.readLine()) == null && m_iniStack != null && !m_iniStack.empty ())
		{
			m_ini.close ();
			m_ini = (BufferedReader) m_iniStack.pop ();
		}
		if (line == null)
		{
			return null;
		}
		line = line.trim();
		if (line.length () > 0)
		{
			if (line.charAt (0) == ';' || line.charAt (0) == '#')
			{
				if (line.startsWith (INSERT_DIRECTIVE))
				{
					String			newPath;
					String			fullPath;

					newPath = line.substring (INSERT_DIRECTIVE.length ());

					if (m_pathResolver != null)
					{
							// This handles the delete line case.
						fullPath = m_pathResolver. locateFile( newPath );
           				if (null == fullPath)
						{
							return "";
						}
					}
					else
					{
						fullPath = newPath;
					}
					try
					{
						BufferedReader newini = new BufferedReader( new FileReader (fullPath) );
						if (m_iniStack == null)
						{
							m_iniStack = new Stack ();
						}
						m_iniStack.push (m_ini);
						m_ini = newini;
						if (m_iniStack.size () > 10)
						{
							throw new IniIncludeException ("Too many #include(s).  Limit is 10.  Are you recursively including forms? Violating line is: " + line);
						}
					}
					catch (Exception ex)
					{
						throw new IniIncludeException ("Trouble opening [" + newPath + "] as [" + fullPath +"] from " + line, ex);
					}
				}
				line = "";
			}
		}
		return line;
	}
	public void close ()
		throws IOException
	{
		m_ini.close ();
		if (m_iniStack != null)
		{
			while (!m_iniStack.empty ())
			{
				m_ini = (BufferedReader) m_iniStack.pop ();
				m_ini.close ();
			}
		}
	}
}

/**
 *	<h3>Purpose:</h3>
 *	The Ini provides a means to coordinate application settings between
 *	Java programs and existing Windows programs that use an "ini" file for
 *	initialization.
 *
 *	<h3>Description</h3>
 *	Ini accesses an ini file from a local system directory. It parses
 *	locates a selected stanza, reading the definitions and storing the keys and
 *	values in a hash set. The definition hash sets are indexed themselves in a
 *	stanza hash set.
 */
public class Ini
{
	static private Ini s_Instance = null;

	private String	m_sFile = null;
	private HashMap	m_StanzaMap = null;
	private HashMap m_newStanzaMap = null;
	private String	m_sCurStanza = null;
	private PathResolver	m_pathResolver = null;




	/**
	 * Provide access to a singleton instance of the Ini.
	 */
	static public Ini getInstance()
	{
		if (s_Instance == null)
			s_Instance = new Ini();

		return s_Instance;
	}

	/**
	 * Creates and/or initializes internal variables.
	 */
	public Ini()
	{
		m_StanzaMap = new HashMap();
		m_newStanzaMap = new HashMap();
	}


	/**
	 */
	public void setPathResolver (PathResolver pathResolver)
	{
		m_pathResolver = pathResolver;
	}
	/**
	 * Read a configuration stanza from an 'ini' formatted input file. Any
	 * changes to the stanza prior to this call are discarded.  If the p_sStanza
	 * argument is null, all stanzas are loaded.  If the stanza is loaded successfully
	 * is is set as the active stanza.  see <code>setStanza()</code>.
	 *
	 * @return	true 		if stanza successfully loaded.
	 * @throws IOException	if file cannot be located, or a read error occurs.
	 * @throws MultipleINIFileException	if the file specified is different from
	 *									the file specified in a previous call to
	 *									this function or <code>createStanza</code>.
	 */
	public boolean loadStanza( String p_sFile, String p_sStanza )
			throws IOException, MultipleINIFileException, IniIncludeException
	{
		String			line;
		String			stanza = null;
		StateProperties	mapKeys = null;

		boolean			bStanzaFound = false;
		boolean			bIgnoreValues = false;

		int				iEqualSignPos;
		String			sKey;
		String			sValue;

		if ( m_sFile != null && m_sFile.compareTo (p_sFile) != 0 )
			throw new MultipleINIFileException();
		m_sFile = p_sFile;

		IniReader		ini = new IniReader (p_sFile, m_pathResolver);

		// Read the file one line at a time and store the stanzas and keys
		line = ini.readLine();
		while (line != null)
		{
			if (line.length () == 0)
			{
				line = ini.readLine();
				continue;
			}

			if (line.startsWith( "[" ) && line.endsWith( "]" ))
			{
				if (bStanzaFound)
				{
					// The stanza has been found and we are beginning
					//		a new stanza.  We ignore the rest of the file.
					break;
				}

				// extract stanza name
				stanza = line.substring ( 1, line.indexOf( "]" ) );

				if (p_sStanza == null || stanza.compareTo( p_sStanza ) == 0)
				{
					// Discard the current copy of the stanza in memory.  Any
					// changes done to the stanza will be discarded.
					m_StanzaMap.remove( stanza );

					// Create a new map of keys for the stanza.
					mapKeys = new StateProperties();
					m_StanzaMap.put( stanza, mapKeys );

					if (p_sStanza != null)
						bStanzaFound = true;
					bIgnoreValues = false;
				}
				else
					bIgnoreValues = true;
			}
			else if (stanza != null && !bIgnoreValues)
			{
				// extract key name and value
				iEqualSignPos = line.indexOf( "=" );
				if (iEqualSignPos != -1)
				{
					sKey = line.substring( 0, iEqualSignPos );
					sKey = sKey.trim();
					sValue = line.substring( iEqualSignPos + 1 );
					sValue = sValue.trim();
						//
						// See if we have a continuation character at the end of the line.  This
						// is our extension and is not compatible with other ini file definitions.
						// If the last character is a '\' character then concatonate the next line
						// onto the value.  If it is a '~' character, then add a new line and the 
						// next line.
						//
					while (sValue.length () > 0 && (sValue.charAt (sValue.length () - 1) == '\\' ||
						sValue.charAt (sValue.length () - 1) == '~'))
					{
						line = ini.readLine();
						if (line == null)
							break;
						if (sValue.charAt (sValue.length () - 1) == '~')
						{
							sValue = sValue.substring (0, sValue.length () - 1) + '\n' + line;
						}
						else
						{
							sValue = sValue.substring (0, sValue.length () - 1) + line;
						}
					}

					mapKeys. setProperty( sKey, sValue );
				}
			}

			line = ini.readLine ();
		}

		ini.close();
		m_sCurStanza = stanza;
		return (bStanzaFound || p_sStanza == null);
	}

	/**
	 * Releases a previously loaded stanza from memory.  If stanza is null,
	 * all stanzas are released.  Any changes to the stanza prior to this call
	 * are discarded.
	 *
	 * @true 		if stanza successfully released.
	 * @false		if stanza is not null and it was not previously loaded
	 */
	public boolean releaseStanza( String p_sStanza )
	{
		if (p_sStanza != null)
		{
			if (m_StanzaMap.containsKey( p_sStanza ))
			{
				m_StanzaMap.remove( p_sStanza );
				m_newStanzaMap.remove( p_sStanza );
			}
			else
				return false;
		}
		else
		{
			m_StanzaMap.clear();
			m_newStanzaMap.clear();
		}

		return true;
	}

	public static LinkedList queryStanzas (String p_sFile)
			throws IOException, IniIncludeException
	{
		return queryStanzas (p_sFile, null);
	}

	/**
	 * Queries an ini file for all its stanza entries.
	 * @param p_sFile	name of the ini file to query.
	 * @return a linked list of String objects containing the stanza names.
	 * @exception IOException when the file cannot be opened or a I/O error occurs.
	 */
	public static LinkedList queryStanzas (String p_sFile, PathResolver pathResolver)
			throws IOException, IniIncludeException
	{
		IniReader		ini = null;
		LinkedList		list = new LinkedList ();
		String			line;
		String 			stanza;

		// Open the file
		ini = new IniReader (p_sFile, pathResolver);

		// Read the file one line at a time and store the stanzas and keys
		line = ini.readLine();
		while (line != null)
		{
			if (line.length () > 0)
			{
				if (line.startsWith( "[" ) && line.endsWith( "]" ))
				{
					// extract stanza name
					stanza = line.substring ( 1, line.indexOf( "]" ) );
					list.add (stanza);
				}
			}
			line = ini.readLine();
		}

		ini.close();
		return list;
	}
	/**
	 * If the stanza selection is null, all stanzas will be searched
	 * in subsequent retrievals.
	 * @exception NoSuchValueException		if stanza is not known.
	 */
	public void setStanza( String p_sStanza )
			throws NoSuchValueException
	{
		if (p_sStanza == null)
			m_sCurStanza = null;
		else if (m_StanzaMap.get( p_sStanza ) != null)
			m_sCurStanza = p_sStanza;
		else
			throw new NoSuchValueException( p_sStanza );
	}

/**
	Purpose:  The following three methods interpret the string retrieved from
		the current stanza using the provided key.
		<p>
	Description:  The string is parsed and converted to the return type (obviously,
		no conversion is necessary for a string value).
		The default value is used if the key is not found in the
		current stanza.
		<p>

		If no stanza is currently selected, all definition sets will be
		searched. If multiple values are discovered, the default should
		be used.
		<p>
	@return	Value converted (as necessary) from the string retrieved from
		the definition set.	The provided default if key is not found in the stanza,
		or the key is found more than once in a multi-stanza lookup.
 */
	public int getInt( String p_sKey, int p_iDefault )
	{
		String	sValue;
		Integer	iValue;

		sValue = getString( p_sKey, null );

		if (sValue != null)
		{
			try
			{
				iValue = Integer.valueOf( sValue );
				return iValue.intValue();
			}
			catch (NumberFormatException e)
			{
				return p_iDefault;
			}
		}
		else
			return p_iDefault;
	}

	public float getFloat( String p_sKey, float p_fDefault )
	{
		String	sValue;
		Float	fValue;

		sValue = getString( p_sKey, null );

		if (sValue != null)
		{
			try
			{
				fValue = Float.valueOf( sValue );
				return fValue.floatValue();
			}
			catch (NumberFormatException e)
			{
				return p_fDefault;
			}
		}
		else
			return p_fDefault;
	}

	public String getString( String p_sKey, String p_sDefault )
	{
		java.util.Set		setStanza;
		java.util.Iterator	iterStanza;
		String				sStanza;
		StateProperties		stanza;
		String				sValue = null;
		String              curValue;

		if (m_sCurStanza != null)
		{
			stanza = (StateProperties) m_StanzaMap.get( m_sCurStanza );
			sValue = stanza.getProperty( p_sKey );
		}
		else
		{
			setStanza = m_StanzaMap.keySet();
			iterStanza = setStanza.iterator();

			while (iterStanza.hasNext())
			{
				sStanza = (String) iterStanza.next();
				stanza = (StateProperties) m_StanzaMap.get( sStanza );
				curValue = stanza.getProperty( p_sKey );

				if (null != curValue)
				{
					if (null == sValue)
					{
						sValue = curValue;
					}
					else
					{
						return p_sDefault;
							// We've already found the key in a previous stanza.
							// Return of the default value.
					}
				}
			}
		}

		if (sValue != null)
			return sValue;
		else
			return p_sDefault;
	}

	/**
	 * Creates a stanza in memory and associates it with the file p_sFile.
	 * If the stanza already exists in the file this function loads the stanza
	 * otherwise it acts as though a stanza with no keys was loaded.
	 *
	 * @param p_sFile	the name of the ini file
	 * @param p_sStanza	the name of the stanza to be created.
	 * @return true    if either the stanza was successfully created in memory
	 *                        or if the the stanza already exists.
	 * @return false  otherwise
	 * @exception IOException    if an I/O error occurs
	 * @exception MultipleINIFileException	if the file specified is different from
	 *									the file specified in a previous call to
	 *									this function or <code>loadStanza</code>.
	 */
	public boolean createStanza( String p_sFile, String p_sStanza )
		throws IOException, MultipleINIFileException, IniIncludeException
	{
		StateProperties	state = null;

		if ( m_sFile != null && m_sFile != p_sFile )
			throw new MultipleINIFileException();
		m_sFile = p_sFile;

		if ( !loadStanza( p_sFile, p_sStanza ) && p_sStanza != null )
		{
			state = new StateProperties();
			m_StanzaMap.put( p_sStanza, state );
			m_newStanzaMap.put( p_sStanza, state );
		}

		return true;
	}

	public Object [] enumStanzas ()
	{
		if (m_StanzaMap == null)
			return null;
		java.util.Set		set = m_StanzaMap.keySet();
		return set.toArray ();
	}
	/**
	 * Saves all changes to the stanza.  If stanza is null, all changes to all
	 * loaded stanzas are saved.  The stanzas remain "loaded" when this function
	 * returns.
	 *
	 * @exception IOException    if an I/O error occurs
	 */
	public void flush( String p_sStanza )
		throws IOException
	{
		File				    tempCopy;
		String				    tempName;
		File				    ini;
		BufferedReader		    reader;
		BufferedWriter		    writer;
		String				    line;
		String				    stanza = null;
		String				    sKey;
		String				    sValue;
		StateProperties		    state;
		java.util.Enumeration	enumKeys;
		boolean				    bStanzaFound = false;
		boolean				    bIgnoreValues = false;

		if ( m_sFile == null )
			return;

		// Make a copy of the ini file
		tempCopy = File.createTempFile( "TMP", "INI" );
		tempName = tempCopy.getAbsolutePath();
		writer = new BufferedWriter( new FileWriter( tempCopy ) );
		ini = new File( m_sFile );
		reader = new BufferedReader( new FileReader( ini ) );

		line = reader.readLine();
		while ( line != null )
		{
			writer.write( line, 0, line.length() );
			writer.newLine();

			line = reader.readLine();
		}
		reader.close();
		writer.flush();
		writer.close();

		// Now save the ini file
		reader = new BufferedReader( new FileReader( tempCopy ) );
		writer = new BufferedWriter( new FileWriter( ini ) );

		line = reader.readLine();
		while (line != null)
		{
			line = line.trim();

			if ( line.startsWith( ";" ) || line.compareTo( "" ) == 0 )
			{
				writer.write( line, 0, line.length() );
				writer.newLine();
				// ignore comment line or blank line
				line = reader.readLine();
				continue;
			}

			if ( line.startsWith( "[" ) && line.endsWith( "]" ) )
			{
				if (bStanzaFound)
				{
					writer.write( line, 0, line.length() );
					writer.newLine();
					// The stanza has been found and we are beginning
					//		a new stanza.  We simply copy the rest of the file.
					line = reader.readLine();
					while ( line != null )
					{
						writer.write( line, 0, line.length() );
						writer.newLine();
						line = reader.readLine();
					}
					break;
				}

				// extract stanza name
				stanza = line.substring ( 1, line.indexOf( "]" ) );

				if ( p_sStanza == null || stanza.compareTo( p_sStanza ) == 0)
				{
					state = (StateProperties) m_StanzaMap.get( stanza );
					if ( state != null )
					{
						writer.write( "[" + stanza + "]" );
						writer.newLine();

						enumKeys = state.propertyNames();

						while ( enumKeys.hasMoreElements() )
						{
							sKey = (String) enumKeys.nextElement();
							sValue = state.getProperty( sKey );

							writer.write( sKey + "=" + sValue );
							writer.newLine();
						}
					}

					if (p_sStanza != null)
					{
						bStanzaFound = true;
						m_newStanzaMap.remove( p_sStanza );
					}
					bIgnoreValues = true;
				}
				else
				{
					writer.write( "[" + stanza + "]" );
					writer.newLine();
					bIgnoreValues = false;
				}
			}
			else if (stanza != null && !bIgnoreValues)
			{
				writer.write( line, 0, line.length() );
				writer.newLine();
			}

			line = reader.readLine();
		}

		// Now iterate on the new stanza map and save if necessary
		java.util.Set		setMaps;
		java.util.Iterator	iterMaps;

		setMaps = m_newStanzaMap.keySet();
		iterMaps = setMaps.iterator();

		while ( iterMaps.hasNext() )
		{
			stanza = (String) iterMaps.next();

			if ( p_sStanza == null || stanza.compareTo( p_sStanza ) == 0 )
			{
				writer.write( "[" + stanza + "]" );
				writer.newLine();

				state = (StateProperties) m_StanzaMap.get( stanza );

				enumKeys = state.propertyNames();

				while ( enumKeys.hasMoreElements() )
				{
					sKey = (String) enumKeys.nextElement();
					sValue = state.getProperty( sKey );

					writer.write( sKey + "=" + sValue );
					writer.newLine();
				}

				try {
					iterMaps.remove();
				} catch (UnsupportedOperationException e) {
					System.err.println( "Error:" + e.getMessage() );
				} catch (IllegalStateException es) {
					System.err.println( "Error:" + es.getMessage() );
				}
			}
		}

		reader.close();
		writer.flush();
		writer.close();

		// Delete the temporary copy
		tempCopy.delete();
	}

	/**
	 * Sets a floating point value in the stanza.  This function creates
	 * the key if it does not already exist.
	 *
	 * @param p_sStanza		the stanza containing the key to be set
	 * @param p_sKey		the key to be set
	 * @param p_fValue		new float value for the key
	 * @exception NoSuchValueException	if the stanza has not been loaded or created.
	 */
	public void setFloat( String p_sStanza, String p_sKey, float p_fValue )
		throws NoSuchValueException
	{
		Float	fValue = new Float( p_fValue );
		setString( p_sStanza, p_sKey, fValue.toString() );
	}

	/**
	 * Sets an integer value in the stanza.  This function creates the
	 * key if it does not already exist.
	 *
	 * @param p_sStanza		the stanza containing the key to be set
	 * @param p_sKey		the key to be set
	 * @param p_iValue		new integer value for the key
	 * @exception NoSuchValueException	if the stanza has not been loaded or created.
	 */
	public void setInt( String p_sStanza, String p_sKey, int p_iValue )
		throws NoSuchValueException
	{
		Integer	iValue = new Integer( p_iValue );
		setString( p_sStanza, p_sKey, iValue.toString() );
	}

	/**
	 * Sets a string value in the stanza.  This function creates the key
	 * if it does not already exist.
	 *
	 * @param p_sStanza		the stanza containing the key to be set
	 * @param p_sKey		the key to be set
	 * @param p_sValue		new string value for the key
	 * @exception NoSuchValueException	if the stanza has not been loaded or created.
	 */
	public void setString( String p_sStanza, String p_sKey, String p_sValue )
		throws NoSuchValueException
	{
		StateProperties	mapKeys;

		if ( p_sStanza == null )
			return;

		mapKeys = (StateProperties) m_StanzaMap.get( p_sStanza );
		if ( mapKeys == null )
			throw new NoSuchValueException( p_sStanza );
		mapKeys.setProperty( p_sKey, p_sValue );
	}

	/**
	 * Provides a Map containing all the entires for the currently loaded stanza.
	 *
	 * @return a Map
	 */
	public StateProperties getStanzaEntries ()
	{
		StateProperties	mapKeys;

		if ( m_sCurStanza == null )
			return null;

		return (StateProperties) m_StanzaMap.get( m_sCurStanza );
	}
}
