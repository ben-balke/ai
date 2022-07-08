/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/isam/BasixVar.java,v 1.1 2010/10/19 00:44:25 secwind Exp $
==================================================================================================*/
/**
 * Locates and describes a data variable in a Basix record.
 */
package com.duckdigit.isam;

public class BasixVar extends Object
{
		/**
		 * Constant enumeration for Basix data types. These must match the
		 * typedefs on the IsamServer.
		 */
	public static final int TYPE_SHORT = 2;
	public static final int TYPE_BCD4 = 4;
	public static final int TYPE_BCD8 = 8;
	public static final int TYPE_STRING = 1;
	public static final int TYPE_UNKNOWN = -1;
	public static final int	NOUPDATE = 0x01;
	public static final int NOINSERT = 0x02;


	protected String		m_name;			// Field type used in declaration. ie. "J$"
	protected int			m_type = 0;		// data type, from the enumeration above.
	protected int			m_start = 0;	// Starting index in variable.
	protected int			m_array = 0;	// number of elements from start.
	protected int			m_offset = 0;	// offset into record.
	protected int			m_masteroffset = 0;	// offset into master record.
	protected int			m_recflag = 0;	// UPDATE or INSERT
	/**
	 * Void constructor.
	 */
	public BasixVar() {}

	/**
	 * All-in-one constructor, ensuring initialization.
	 */
	public BasixVar (String name, int type, int start, int array,
					 int offset, int masteroffset)
		throws BasixDeclException
	{
		m_name = name;
		m_type = type;
		m_start = start;
		m_array = array;
		m_offset = offset;
		m_masteroffset = masteroffset;
	};

	public BasixVar (BasixVar mastervar, int idx1, int idx2)
		throws BasixDeclException
	{
		m_name = mastervar.m_name;
		m_type = mastervar.m_type;
		m_array = 1;
		switch (mastervar.m_type)
		{
		case TYPE_SHORT:
		case TYPE_BCD8:
		case TYPE_BCD4:
			if (idx2 != -1)
				throw new BasixDeclException ("Type for " + m_name + " cannot have a second index");
			if (idx1 == -1)
				m_start = 0;
			else
			{
				if (idx1 >= mastervar.m_array)
					throw new BasixDeclException ("Index exceeds record field size " +
						m_name + "(" + idx1 + "," + idx2 + ")");
				m_start = idx1;
			}
			break;
		case TYPE_STRING:
			if (idx1 != -1)
			{
				idx1--;
				m_start = idx1;
				if (idx2 == -1)
				{
					m_array = m_array - m_start;
				}
				else
				{
					m_array = idx2 - idx1;
				}
			}
			else
			{
				m_start = 0;
				m_array = mastervar.m_array;
			}
			break;
		}
		m_masteroffset = mastervar.m_offset + ((int)m_type) * m_start;
	}
	/**
	 * Causes the variable not to be used in an update command to the
	 * server.  The server will read the record and then overlay the fields that
	 * do not have this flag set.
	 */
	public void setNoUpdate ()
	{
		m_recflag |= NOUPDATE;
	}
	/**
	 * Causes the variable not to be used in an insert command to the
	 * record.
	 */
	public void setNoInsert ()
	{
		m_recflag |= NOINSERT;
	}
	/**
	 * Retrieves the update policy for this variable.
	 */
	public boolean getNoUpdate ()
	{
		return (m_recflag & NOUPDATE) == NOUPDATE;
	}
	/**
	 * Retrieves the insert policy for this variable.
	 */
	public boolean getNoInsert ()
	{
		return (m_recflag & NOINSERT) == NOINSERT;
	}
	public boolean nameEquals (String name)
	{
		return m_name.equals (name);
	}
	/**
	 * To report the data type.
	 */
	public int getType()
	{
		return m_type;
	}

	/**
	 * To calculate the size of the field.
	 */
	public int getSize()
	{
		return m_array * m_type;
	}

	/**
	 * To report the size of one field value.
	 */
	public int getValueSize()
	{
		return m_type;
	}

	/**
	 * To report the record offset for this field.
	 */
	public int getOffset( )
	{
		return m_offset;
	}

	/**
	 * Returns the byte offset into the master record.
	 */
	public int getMasterOffset ()
	{
		return m_masteroffset;
	}
	public String toString ()
	{
		return m_name + " type: " + m_type +
							" start:" + m_start + " array:" + m_array +
							" offset:" + m_offset +
							" mastoff:" + m_masteroffset;

	}
}
