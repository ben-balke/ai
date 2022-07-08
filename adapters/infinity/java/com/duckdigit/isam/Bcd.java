/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/Bcd.java,v 1.1 2010/10/19 00:44:25 secwind Exp $
==================================================================================================*/
package com.duckdigit.isam;

import com.duckdigit.util.StringUtils;

import java.text.DecimalFormat;
import java.io.StringReader;

public abstract class Bcd extends Object
{
	protected byte[]				m_byteValue;
	private static DecimalFormat	s_fmtDec;

	protected Bcd()
	{

	}
	byte [] getBytes ()
	{
		return m_byteValue;
	}

	protected abstract int getDefiningByteCnt();
	protected abstract int getPrecision();

	public Bcd( byte[] p_byteDef , int p_offset )
					throws BcdException
	{
		m_byteValue = new byte[ getDefiningByteCnt() ];

		setDefBytes( p_byteDef, p_offset );

		if (null == s_fmtDec)
		{
			s_fmtDec = new DecimalFormat();
		}
	}

	public Bcd( String p_strNum )
					throws BcdException
	{
		m_byteValue = new byte[ getDefiningByteCnt() ];

		fromString( p_strNum );

		if (null == s_fmtDec)
		{
			s_fmtDec = new DecimalFormat();
		}
	}

	public Bcd( double p_dVal )
					throws BcdException
	{
		m_byteValue = new byte[ getDefiningByteCnt() ];

		if (null == s_fmtDec)
		{
			s_fmtDec = new DecimalFormat();
		}

		fromDouble( p_dVal );
	}

	public Bcd( int p_iVal )
					throws BcdException
	{
		m_byteValue = new byte[ getDefiningByteCnt() ];

		if (null == s_fmtDec)
		{
			s_fmtDec = new DecimalFormat();
		}

		fromInteger( p_iVal );
	}

	public final void validateBytes()
						throws BcdException
	{
		int[]	iVal;
		int		iDefByteCnt;

		iDefByteCnt = getDefiningByteCnt();

		if (m_byteValue. length < iDefByteCnt)
		{
			throw new BcdException(
							BcdException. INSUFFICIENT_BYTE_CNT );
		}

		iVal = new int[ 2 ];

		for (int nByte = 1; nByte < iDefByteCnt; nByte++)
		{
			iVal[ 0 ] = m_byteValue[ nByte ] >> 4;

			if ( 0 != (m_byteValue[ nByte ] & 0x80) )
			{
				iVal[ 0 ] += 0x10;
			}

			iVal[ 1 ] = (m_byteValue[ nByte ] & 0xf);

			if ( (iVal[ 0 ] > 9) || (iVal[ 1 ] > 9) )
			{
				throw new BcdException(
								BcdException. NIBBLE_GT_9,
								nByte + 1 );
			}
		}
	}

	private boolean isNegative()
	{
		return ( 0 != (m_byteValue[ 0 ] & 0x80) );
	}

	private int getExponent()
	{
		return ( (m_byteValue[ 0 ] & 0x7f) - 0x40 );
	}

	private int[] getDigits()
	{
		int[]	iDigits;

		iDigits = new int[ getPrecision() ];

		for (int nByte = 1; nByte < getDefiningByteCnt(); nByte++)
		{
			iDigits[ 2 * (nByte - 1) ] = (m_byteValue[ nByte ] >> 4);

			if ( 0 != (m_byteValue[ nByte ] & 0x80) )
			{
				iDigits[ 2 * (nByte - 1) ] += 0x10;
			}

			iDigits[ ( 2 * (nByte - 1) ) + 1 ] =
									(m_byteValue[ nByte ] & 0xf);
		}

		return iDigits;
	}

	private String getCharDigits()
	{

		byte[]	bDigits;

		bDigits = new byte[ getPrecision() ];

		for (int nByte = 1; nByte < getDefiningByteCnt(); nByte++)
		{
			bDigits[ 2 * (nByte - 1) ] =
						(byte) ((m_byteValue[ nByte ] >> 4) + (int) '0');
			if ( 0 != (m_byteValue[ nByte ] & 0x80) )
			{
				bDigits[ 2 * (nByte - 1) ] += 0x10;
			}
			bDigits[ ( 2 * (nByte - 1) ) + 1 ] =
						(byte)	((m_byteValue[ nByte ] & 0xf) + (int)'0');
		}
		int			i;
		for (i = getPrecision (); i > 0; i--)
		{
			if (bDigits [i - 1] != '0')
			{
				break;
			}
		}
		return new String (bDigits, 0, i);
	}

	public final byte[] stringToBytes( String p_strNum )
											throws BcdException
	{
		int				nChar;
		int				nByte;
		int				nDig;
		char			c;
		StringBuffer	sbNum;
		String			strNum;
		boolean			bNeg;
		int				iExp;
		int				iDigitCnt;
		int[]			iDigits;
		long			lDigVal;
		long			lExpVal;

		if ( 0 == p_strNum. length() )
		{
			throw new BcdException(
							BcdException. INVALID_NUMBER_STR );
		}

		iExp = p_strNum. indexOf( 'E' );

		if (-1 == iExp)
		{
			sbNum = new StringBuffer( p_strNum );
		}
		else
		{
			sbNum = new StringBuffer(
							p_strNum. substring( 0, iExp - 1 ) );
			try
			{
				iExp = Integer. parseInt(
							p_strNum. substring(
										iExp,
										p_strNum. length()  ),
							10 );
			}
			catch (NumberFormatException nfEx)
			{
				throw new BcdException(
								BcdException. INVALID_NUMBER_STR );
			}
		}

		bNeg = ( '-' == sbNum. charAt( 0 ) );

		if ( -1 == p_strNum.indexOf( '.' ) )
		{
			iExp = sbNum. length();
		}

			//
			// Strip any characters other than digits or periods
			//
		for (nChar = 0; nChar < sbNum. length(); nChar++)
		{
			c = sbNum. charAt( nChar );

			if ( !Character. isDigit( c ) )
			{
				if ('.' == c)
				{
					iExp = nChar;
				}

				sbNum. deleteCharAt( nChar );
				nChar -= 1;
			}
		}

		iDigitCnt = getPrecision();

		if (sbNum. length() > iDigitCnt)
		{
			strNum = sbNum. substring( 0, iDigitCnt );
		}
		else
		{
			strNum = sbNum. toString();
		}

		try
		{
			lDigVal = parseLong( strNum );
		}
		catch (NumberFormatException nfEx)
		{
			throw new BcdException(
							BcdException. INVALID_NUMBER_STR );
		}

		if (strNum. length() > iDigitCnt)
		{
			if ( sbNum. charAt( iDigitCnt ) >= '5' )
			{
				lDigVal += 1;
			}
		}


		strNum = longToString( lDigVal );
		iDigits = new int[ iDigitCnt ];
		iExp += ( strNum. length() > iDigitCnt )  ?  1 : 0;
			// Can only be one character at the most

		for (nChar = 0; nChar < strNum. length(); nChar++)
		{
			iDigits[ nChar ] = Character. getNumericValue(
												strNum. charAt( nChar ) );
		}

		for (nChar = strNum. length(); nChar < iDigitCnt; nChar++)
		{
			iDigits[ nChar ] = 0;
		}

		return determineDefBytes( bNeg, iExp, iDigits );
	}

	private final byte[] determineDefBytes(
						boolean		p_bNeg,
						int			p_iExp,
						int[]		p_iDigits )
	{
		int			nByte;
		int			nDig;
		int			iDefByteCnt;
		byte[]		byteDef;
		boolean		nonzero = false;

		iDefByteCnt = getDefiningByteCnt();
		byteDef = new byte[ iDefByteCnt ];
		byteDef[ 0 ] = (byte) (p_iExp + 0x40);

		if (p_bNeg)
		{
			byteDef[ 0 ] -= (byte) 0x80;
		}

		for (nDig = 0, nByte = 1; nByte < iDefByteCnt; nByte++)
		{
			byteDef[ nByte ] =
					(byte) ( (p_iDigits[ nDig ] & 0x7) << 4 );

			if ( p_iDigits[ nDig ] >= 8 )
			{
				byteDef[ nByte ] -= 0x80;
			}

			byteDef[ nByte ] += p_iDigits[ nDig + 1 ];
			nDig += 2;
			if (byteDef [nByte] != 0)
			{
				nonzero = true;
			}
		}
		if (!nonzero)
		{
			byteDef [0] = 0;
		}

		return byteDef;
	}

	public final double getDoubleValue()
	{
		return Double.parseDouble (newString ());
	}

	public final int getIntegerValue()
	{
		int		nDecPl;
		int		iExp;
		int		iDigitCnt;
		int[]	iDigits;
		int		iExpVal;
		int		iVal;

		iExp = getExponent();
		iDigitCnt = getPrecision();
		iDigits = getDigits();
		iVal = 0;

		if (iExp >= iDigitCnt)
		{
			iExpVal = (int) pow( 10, iExp - iDigitCnt );
			nDecPl = iDigitCnt - 1;
		}
		else
		{
			iExpVal = 1;
			nDecPl = iExp - 1;

			if ( (iExp >= 0) &&
						(iDigits[ iExp ] > 5) )
			{
				iVal = 1;
			}
		}

		while (nDecPl >= 0)
		{
			iVal += iDigits[ nDecPl ] * iExpVal;
			iExpVal *= 10;
			nDecPl -= 1;
		}

		if ( isNegative() )
		{
			iVal = -iVal;
		}

		return iVal;
	}

	public final String toText( String p_strFormat )
	{
		if (null != p_strFormat)
		{
			s_fmtDec. applyPattern( p_strFormat );
		}

		return s_fmtDec. format(
						getDoubleValue() );
	}

	public final byte[] toBytes()
	{
		return m_byteValue;
	}

	private final void setDefBytes( byte[] p_byteDef, int p_offset )
										throws BcdException
	{
		if (p_byteDef.length - p_offset < getDefiningByteCnt() )
		{
			throw new BcdException(
							BcdException. INSUFFICIENT_BYTE_CNT );
		}

		System. arraycopy(
					p_byteDef,
					p_offset,
					m_byteValue,
					0,
					getDefiningByteCnt() );
	}

	private final void fromString( String p_strNum )
										throws BcdException
	{
		m_byteValue = stringToBytes( p_strNum );
	}

	private final void fromDouble( double p_dVal )
										throws BcdException
	{
		m_byteValue = stringToBytes(
							Double. toString( p_dVal ) );
	}

	private final void fromInteger( int p_iVal )
										throws BcdException
	{
		m_byteValue = stringToBytes(
							Integer. toString( p_iVal ) );
	}

	public static final String bcdToString(
			byte[]		p_byteStream,
			int			p_iOffset,
			int			p_iLen,
			String		p_strFormat )
					throws BcdException
	{
		int			nOff;
		int			nByte;
		byte[]		byteDef;
		Bcd			bcdNum;

		byteDef = new byte[ p_iLen ];
		System. arraycopy( p_byteStream, p_iOffset, byteDef, 0, p_iLen );

		switch (p_iLen)
		{
			case 4:
				bcdNum = new Bcd4( byteDef , 0);
				break;

			case 8:
				bcdNum = new Bcd8( byteDef, 0 );
				break;

			default:
				throw new BcdException( BcdException. INVALID_BYTE_CNT );
		}

		return bcdNum. toText( p_strFormat );
	}

	public static final void stringToBuffer(
					String		p_strNum,
					byte[]		p_byteBuff,
					int			p_iOffset,
					int			p_iLen )
							throws BcdException,
								   StringIndexOutOfBoundsException
	{
		int			nOff;
		int			nByte;
		byte[]		byteDummyDef;
		byte[]		byteBuff;
		Bcd			bcdNum;

		switch (p_iLen)
		{
			case 4:
				bcdNum = new Bcd4( p_strNum );
				break;

			case 8:
				bcdNum = new Bcd8( p_strNum );
				break;

			default:
				throw new BcdException( BcdException. INVALID_BYTE_CNT );
		}

		byteBuff = bcdNum. toBytes();
		System. arraycopy(
					byteBuff,
					0,
					p_byteBuff,
					p_iOffset,
					p_iLen );
	}

	public static long parseLong( String p_strNum )
								throws NumberFormatException
	{
		int				nChar;
		char			c;
		boolean			bNeg;
		int				iOffset;
		long			lExpVal;
		long			lVal;

		if ( 0 == p_strNum.length() )
		{
			throw new NumberFormatException( "Empty parse string" );
		}

		bNeg = ( '-' == p_strNum. charAt( 0 ) );
		iOffset = (bNeg) ? 1 : 0;

		lVal = 0;
		lExpVal = pow( 10, p_strNum. length() - iOffset - 1 );

		for (nChar = iOffset; nChar < p_strNum. length(); nChar++)
		{
			c = p_strNum. charAt( nChar );

			if ( !Character. isDigit( c ) )
			{
				throw new NumberFormatException(
										"Character #" +
											(nChar + 1) +
												" is not a digit" );
			}

			lVal += Character. getNumericValue( c ) * lExpVal;
			lExpVal /= 10;
		}

		return (bNeg) ? -lVal : lVal;
	}

	public static long pow( int a, int b )
	{
		int		i;
		long	lVal;

		lVal = 1;

		if (b < 0)
		{
			for (i = 0; i > b; i--)
			{
				lVal /= a;
			}
		}
		else
		{
			for (i = 0; i < b; i++)
			{
				lVal *= a;
			}
		}

		return lVal;
	}

	public static String longToString( long p_lVal )
	{
		boolean			bNeg;
		long			lMask;
		boolean			bNonzeroSeen;
		int				iDig;
		StringBuffer	sbNum;

		bNeg = (p_lVal < 0);
		p_lVal = Math. abs( p_lVal );

		lMask = pow( 10, 18 );
		bNonzeroSeen = false;
		sbNum = new StringBuffer( 19 );

		while (lMask > 0)
		{
			iDig = (int) (p_lVal / lMask);

			bNonzeroSeen |= (0 != iDig);

			if (bNonzeroSeen)
			{
				sbNum. append(
					Character. forDigit( iDig, 10 ) );
				p_lVal -= (iDig * lMask);
			}

			lMask /= 10;
		}

		if (!bNonzeroSeen)
		{
			return "0";
		}

		if (bNeg)
		{
			sbNum. insert( 0, '-' );
		}

		return sbNum. toString();
	}

	public static byte[] intsToBytes( int[] p_iValues )
	{
		byte[]	byteValues;

		byteValues = new byte[ p_iValues. length ];

		for (int n = 0; n < p_iValues. length; n++)
		{
			byteValues[ n ] = (byte) p_iValues[ n ];
		}

		return byteValues;
	}


	public String dump ()
	{
		return StringUtils.hexDumpToString (m_byteValue, getDefiningByteCnt());
	}

	public String newString ()
	{
		String				s = getCharDigits ();
		StringBuffer		sb = new StringBuffer ();
		int					len = s.length ();
		int					i;
		if (len == 0)
		{
			return "0";
		}
		int				exp = getExponent();
		if (exp > 20 || exp < -20)
		{
			System.out.println ("Invalid Exponent: " + exp);
			return "0";
		}
		if ( isNegative() )
		{
			sb.append ("-");
		}
		if (len == exp)
		{
			return sb + s;
		}
		if (exp > len)
		{
			sb.append (s);
			for (i = 0; i < exp - len; i++)
			{
				sb.append ('0');
			}
		}
		else if (exp > 0)
		{
			sb.append (s.substring (0, exp));
			sb.append ('.');
			sb.append (s.substring (exp));
		}
		else
		{
			sb.append ('.');
			if (exp < 0)
			{
				for (i = exp; i < 0; i++)
				{
					sb.append ('0');
				}
			}
			sb.append (s);
		}


		return sb.toString ();
	}
}
