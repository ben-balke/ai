/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/util/file/FileHasher.java,v 1.2 2010/04/22 18:41:45 secwind Exp $
==================================================================================================*/
package com.duckdigit.util.file;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author bkbalke
 */
public class FileHasher
{
	/** Hash algorithm to use. */
	private static String HASH_METHOD = "SHA-1";
	
	/** Digest implementation. */
	private MessageDigest		m_Digester;
	/** Actual digest. */
	private byte				m_Digest[];
	
	/** Creates a new instance of FileHasher */
	public FileHasher()
	{
	}
	
	public void prepareDigest()
		throws NoSuchAlgorithmException
	{
		m_Digester = MessageDigest.getInstance( HASH_METHOD );
	}
	
	public boolean digestFile(
			String		p_sPath )
	{
		FileInputStream			is;
		byte					buff[];
		int						iR;
		
		try {
			is = new FileInputStream( p_sPath );
		}
		catch (FileNotFoundException fnfe)
		{
			return false;
		}
		
		m_Digester.reset();
		buff = new byte[ 4096 ];
		try {
			while (0 != is.available())
			{
				iR = is. read( buff );
				m_Digester. update( buff, 0, iR );
			}
		}
		catch (IOException ioe) {
			return false;
		}
		
		m_Digest = m_Digester. digest();
		
		return true;
	}
	
	public String hexEncode()
	{
		StringBuffer	hex;
		int				iD;
		
			
		hex = new StringBuffer();
		for (iD=0; iD<m_Digest.length; iD++)
		{
			hex. append(
					Integer. toString( 
								(int) (m_Digest[ iD ] + 256) % 256, 16 ) );
		}
		
		return hex. toString();
	}
	
	public static void main(
			String		args[] )
	{
		FileHasher		hasher;
		
		hasher = new FileHasher();
		try {
			hasher. prepareDigest();
			if ( hasher. digestFile( args[ 0 ] ))
			{
				System.out.println( hasher. hexEncode() );
			}
			else
			{
				System.exit( 1 );
			}
		}
		catch (Exception ex) {
			System.out.println( ex.getMessage() );
		}
	}
}
