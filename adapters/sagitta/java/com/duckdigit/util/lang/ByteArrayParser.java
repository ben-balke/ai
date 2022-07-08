
/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/util/lang/ByteArrayParser.java,v 1.2 2010/04/22 18:41:48 secwind Exp $
==================================================================================================*/
package com.duckdigit.util.lang;
public class ByteArrayParser
{
	private byte[]      m_bSrc;
	private String      m_sSeps;
	private boolean     m_bValid;
	private int         m_iBy;
	private int         m_iProg;

	private int         INIT_LOC = -2;

	/**
		Create a parser on the byte array source, with the indicated separators.
		@param byte[]       Source array.
		@param String       Token separators.
	 */
	public ByteArrayParser(
		byte[]      p_bSrc,
		String      p_sSeps )
	{
		m_bSrc = p_bSrc;
		m_sSeps = p_sSeps;

		m_bValid = (null != m_bSrc) &&
					(0 != m_bSrc.length) &&
					(null != m_sSeps) &&
					(0 != m_sSeps.length());

		m_iBy = INIT_LOC;
	}

	public void markProgress()
	{
		m_iProg = m_iBy;
	}

	public void restoreMark()
	{
		m_iBy = m_iProg;
	}

	public boolean findNext(
			String      p_sToken )
	{
		int         iBy;
		int         nBy;
		int         iCh;
		int         nCh;

		if (!m_bValid) return false;

		if ((null == p_sToken) ||
			(0 == p_sToken.length()))
		{
			return false;
		}

		if (INIT_LOC == m_iBy) m_iBy = 0;
		if (0 > m_iBy) m_iBy = 0;

		iCh = 0;
		nCh = p_sToken.length();
		iBy = m_iBy;
		nBy = m_bSrc.length;
		while (iBy < nBy)
		{
			// If trying to match first character, make sure preceeding
			// character is white space.
			if ((0 == iCh) &&
				   ((0 != iBy) &&
					(-1 == m_sSeps.indexOf( m_bSrc[iBy-1] ))))
			{
				iBy++;
				continue;
			}

			if (m_bSrc[iBy] == p_sToken.charAt( iCh ))
			{
				iCh++;
				iBy++;
				if (nCh == iCh)
				{
					if (iBy == nBy)
					{
						m_iBy = nBy;
						return true;
					}
					else if (-1 != m_sSeps.indexOf( m_bSrc[iBy] ))
					{
						m_iBy = iBy;// + 1;
						return true;
					}
					iCh = 0;
				}
			}
			else
			{
				iBy++;
			}
		}

		m_iBy = nBy;
		return false;
	}

	public boolean findPrior(
			String      p_sToken )
	{
		int         iBy;
		int         lBy;
		int         iCh;
		int         lCh;

		if (!m_bValid) return false;

		if ((null == p_sToken) ||
			(0 == p_sToken.length()))
		{
			return false;
		}

		lBy = m_bSrc.length - 1;

		if (INIT_LOC == m_iBy) m_iBy = lBy;
		if (m_iBy > lBy) m_iBy = lBy;

		lCh = p_sToken.length() - 1;
		iCh = lCh;
		iBy = m_iBy;
		while (0 <= iBy)
		{
			// If trying to match first character, make sure preceeding
			// character is white space.
			if ((lCh == iCh) &&
				   ((lBy != iBy) &&
					(-1 == m_sSeps.indexOf( m_bSrc[iBy+1] ))))
			{
				iBy--;
				continue;
			}

			if (m_bSrc[iBy] == p_sToken.charAt( iCh ))
			{
				iCh--;
				iBy--;
				if (-1 == iCh)
				{
					if (iBy == -1)
					{
						m_iBy = -1;
						return true;
					}
					else if (-1 != m_sSeps.indexOf( m_bSrc[iBy] ))
					{
						m_iBy = iBy;// - 1;
						return true;
					}
					iCh = lCh;
				}
			}
			else
			{
				iBy--;
			}
		}

		m_iBy = -1;
		return false;
	}

	private String getNextToken()
	{
		int     lBy;
		int     fCh;
		int     iCh;

		if (!m_bValid) return null;
		lBy = m_bSrc. length - 1;
		if (lBy < m_iBy) return null;

		if (INIT_LOC == m_iBy) m_iBy = 0;
		if (0 > m_iBy) m_iBy = 0;

		fCh = m_iBy;
		while (-1 != m_sSeps. indexOf( (char) m_bSrc[ fCh ] ))
		{
			fCh++;
			if (lBy < fCh)
			{
				m_iBy = lBy + 1;
				return null;
			}
		}

		iCh = fCh + 1;
		while (-1 == m_sSeps. indexOf((char) m_bSrc[ iCh ] ))
		{
			iCh++;
			if (lBy < iCh) break;
		}

		m_iBy = iCh;
		try {
			return new String(
						m_bSrc,
						fCh,
						iCh - fCh,
						"ISO-8859-1" );
		}
		catch (Exception e) { return null; }
	}

	public String getPrevToken()
	{
		int     lBy;
		int     fCh;
		int     lCh;

		if (!m_bValid) return null;

		lBy = m_bSrc.length - 1;
		if (INIT_LOC == m_iBy) m_iBy = lBy;
		if (m_iBy > lBy) m_iBy = lBy;

		if (0 > m_iBy) return null;
		lCh = m_iBy;
		while (-1 != m_sSeps. indexOf( (char) m_bSrc[ lCh ] ))
		{
			lCh--;
			if (0 > lCh)
			{
				m_iBy = -1;
				return null;
			}
		}

		fCh = lCh;
		if (0 != fCh) fCh--;
		while (-1 == m_sSeps. indexOf( (char) m_bSrc[ fCh ] ))
		{
			fCh--;
			if (0 > fCh) break;
		}

		m_iBy = fCh;
		fCh ++;
		try {
			return new String(
						m_bSrc,
						fCh,
						lCh - fCh + 1,
						"ISO-8859-1" );
		}
		catch (Exception e) { return null; }
	}

	public static void main (String args[])
	{
		//               012345678901234567890123456
		byte[] source = "this athis this' this  this".getBytes();
		String sTok;

		ByteArrayParser prs = new ByteArrayParser( source, " " );

/*		while (null != (sTok = prs.getPrevToken()))
		{
			System.out.println( sTok );
		}
		while (null != (sTok = prs.getNextToken()))
		{
			System.out.println( sTok );
		}
		while (null != (sTok = prs.getPrevToken()))
		{
			System.out.println( sTok );
		}
*/
		System.out.println( "Nexts" );
		while (prs.findNext( "this" ))
		{
			System.out.println( prs.m_iBy );
//			System.out.println( prs.getNextToken() );
		}
		System.out.println( "Priors" );
		while (prs.findPrior( "this" ))
		{
			System.out.println( prs.m_iBy );
//			System.out.println( prs.getPrevToken() );
		}
		System.out.println( "Nexts" );
		while (prs.findNext( "this" ))
		{
			System.out.println( prs.m_iBy );
			prs.markProgress();
			System.out.println( prs.getPrevToken() );
			prs.restoreMark();
		}
	}
}
