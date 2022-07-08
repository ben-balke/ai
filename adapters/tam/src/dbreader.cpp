/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/dbreader.cpp,v 1.1 2009/09/20 07:15:31 secwind Exp $
*****************************************************************/
#include <stdio.h>
#include <string.h>
#include <ctype.h>
#include "defines.h"
#include "dbreader.h"
#include "byteswap.h"

dbreader_::dbreader_ (int recordblock , dbobserver_ *observer, int verbose)
{
	m_state = DB3_READ_HEAD;
	m_recordblock = recordblock;
	m_block = NULL;
	m_recordfields = NULL;
	m_observer = observer;
	m_dbh.db_fields = NULL;
	m_verbose = verbose;
}

dbreader_::~dbreader_ ()
{
	if (m_dbh.db_fields)
	{
		delete m_dbh.db_fields;
	}
	if (m_block)
	{
		delete m_block;
	}
	if (m_recordfields)
	{
		for (int t = 0; t < m_dbh.db_nfields; t++)
		{
			field	   			*f;
			f = &m_recordfields [t];
			if (f->db_contents)
			{
				delete (f->db_contents);
			}
		}
		delete m_recordfields;
	}
}
int dbreader_::getBytesNeeded ()
{
	switch (m_state)
	{
	case DB3_READ_HEAD:
		return sizeof (m_head);
	case DB3_READ_FIELDS:
		return sizeof (m_fieldc);
	case DB3_READ_HEAD_PAD:
		return m_headremain;
	case DB3_READ_RECORDS:
		return m_dbh.db_rlen * m_recordblock;
	}
	return 0;
}

char * dbreader_::getBuffer ()
{

	switch (m_state)
	{
	case DB3_READ_HEAD:
		return (char *) &m_head;
	case DB3_READ_FIELDS:
		return (char *) &m_fieldc;
	case DB3_READ_HEAD_PAD:
		m_headpadbuf = new char [m_headremain];
		return m_headpadbuf;
	case DB3_READ_RECORDS:
		return m_block;
	}
	return NULL;
}

int dbreader_::process (int nbytes)
{

	switch (m_state)
	{
	case DB3_READ_HEAD:
		return processHeader (nbytes);
	case DB3_READ_FIELDS:
		return processField (nbytes);
	case DB3_READ_HEAD_PAD:
		delete m_headpadbuf;
		m_state = DB3_READ_RECORDS;
		return TRUE;
	case DB3_READ_RECORDS:
		return processRecords (nbytes);
	}
}
int dbreader_::processHeader (int nbytes)
{
	int		rslt = TRUE;
	if (!(m_head.dbh_dbt & DBH_NORMAL))
	{
		return FALSE;
	}

	if (m_head.dbh_dbt & DBH_MEMO)
	{
		m_dbh.db_memo = 1;
		if (m_verbose > 4)
			printf ("dbreader_::processHeader: MEMO Detected in header\n");
	}
	else
	{
		m_dbh.db_memo = 0;
	}
	m_dbh.db_year = m_head.dbh_year;
	m_dbh.db_month = m_head.dbh_month;
	m_dbh.db_day = m_head.dbh_day;
	m_dbh.db_hlen = byteswap_::get_short((u_char *) &m_head.dbh_hlen);
	m_dbh.db_records = byteswap_::get_long((u_char *) &m_head.dbh_records);
	m_dbh.db_currec = 0;
	m_dbh.db_rlen = byteswap_::get_short((u_char *) &m_head.dbh_rlen);
	m_dbh.db_nfields = (m_dbh.db_hlen - sizeof(dbf_header)) / sizeof(dbf_field);
	//printf ("Sizeof head: %d hlen:%d\n", sizeof (m_head), m_dbh.db_hlen);

	/*
	 * dbh->db_hlen - sizeof(dbf_header) isn't the correct size, cos
	 * dbh->hlen is in fact a little more cos of the 0x0D (and possibly
	 * another byte, 0x4E, I have seen this somewhere). Because of
	 * rounding everything turns out right :)
	 */

	m_dbh.db_fields = new f_descr [m_dbh.db_nfields];
	m_dbh.db_offset = m_dbh.db_hlen;

	m_dbh.db_buff = new u_char [m_dbh.db_rlen];
	m_block = new char [m_dbh.db_rlen * m_recordblock];

	m_curfield = 0;
	m_headremain = m_dbh.db_hlen - nbytes;
	m_state = DB3_READ_FIELDS;
	return rslt;
}

int dbreader_::processField (int nbytes)
{
	int			rslt = TRUE;
/*
	for (t = 0; t < dbh->db_nfields; t++)
	{
   Maybe I have calculated the number of fields incorrectly. This can happen
   when programs reserve lots of space at the end of the header for future
   expansion. This will catch this situation 
		if (fields[t].db_name[0] == 0x0D)
		{
			dbh->db_nfields = t;
			break;
		}
	}
  */
  	f_descr *f = &m_dbh.db_fields [m_curfield];
	strncpy((char *) f->db_name, 
		(char *) m_fieldc.dbf_name, DBF_NAMELEN);
	f->db_type = m_fieldc.dbf_type;
	f->db_flen = m_fieldc.dbf_flen;
	f->db_dec = m_fieldc.dbf_dec;
	if (m_verbose > 4)
		printf ("dbreader_::processField: field %d: %s header bytes remaining %d\n", m_curfield, f->db_name, m_headremain);
	m_headremain -= nbytes;
	if (++m_curfield >= m_dbh.db_nfields)
	{
		if (m_observer)
		{
			rslt = m_observer->lookHeader (&m_dbh);
		}
		if (rslt == TRUE)
		{
			rslt = buildRecord ();
		}
		m_state = DB3_READ_HEAD_PAD;
		m_records = 0;
	}
	return rslt;
}

int dbreader_::processRecords (int nbytes)
{
	int 		recs = nbytes / m_dbh.db_rlen;
	int			rslt = TRUE;

	for (int r = 0; r < recs; r++)
	{
		u_char *data = (u_char *)m_block + (r * m_dbh.db_rlen);
		if (populateRecord (data, m_records) == DBF_VALID)
		{
			if (m_observer)
			{
				rslt = m_observer->lookRecord (m_recordfields, (int) m_dbh.db_nfields, (int) m_records);
				if (!rslt)
				{
					break;
				}
			}
		}
		m_records++;
	}
	//printf ("records:%d\r", m_records);
	return rslt;
}


int dbreader_::populateRecord(u_char *data, u_long rec)
{
	int			t,
				i,
				offset;
	u_char	   *dbffield,
			   *end;

/* calculate at which offset we have to read. *DON'T* forget the
   0x0D which seperates field-descriptions from records!

	Note (april 5 1996): This turns out to be included in db_hlen
*/
	offset = m_dbh.db_hlen + (rec * m_dbh.db_rlen);
	m_dbh.db_offset = offset;
	m_dbh.db_currec = rec;

	if (data[0] == DBF_DELETED)
	{
		if (m_verbose > 8)
		{
			printf ("dbreader_::populateRecord: skip recno %d DBF_DELETED\n", 
				rec);
		}
		return DBF_DELETED;
	}

	dbffield = data + 1;
	for (t = 0; t < m_dbh.db_nfields; t++)
	{
		field				*f = &m_recordfields [t];
		f_descr             *df = &m_dbh.db_fields [t];

		if (f->db_type == 'C')
		{
			end = &dbffield[f->db_flen - 1];
			i = f->db_flen;
			while (i > 0 && !isprint(*end))
			{
				end--;
				i--;
			}
			strncpy((char *) f->db_contents, (char *) dbffield, i);
			f->db_contents[i] = '\0';
		}
		else
		{
			end = dbffield;
			i = f->db_flen;
			while (i > 0 && !isprint(*end))
			{
				end++;
				i--;
			}
			strncpy((char *) f->db_contents, (char *) end, i);
			f->db_contents[i] = '\0';
		}

		//if (t < 5)
			//printf ("%d:%s ", t, f->db_contents);
		dbffield += f->db_flen;
	}

	m_dbh.db_offset += m_dbh.db_rlen;
	//printf ("\n");

	return DBF_VALID;
}


int dbreader_::processEof ()
{
	int			rslt = TRUE;
	if (m_observer)
	{
		rslt = m_observer->lookEof ();
	}
	return rslt;
}

int dbreader_::buildRecord ()
{
	int					t;
	field	   			*f;
	f_descr 			*df;

	m_recordfields = new field [m_dbh.db_nfields];
	memset (m_recordfields, 0, sizeof (field) * m_dbh.db_nfields);

	for (t = 0; t < m_dbh.db_nfields; t++)
	{
		f = &m_recordfields [t];
		df = &m_dbh.db_fields [t];
		f->db_contents = (u_char *) new char [df->db_flen + 1];
		if (!f->db_contents)
		{
			fprintf (stderr, "Count not allocate memory for db_contents\n");
			FALSE;
		}
		strncpy((char *) f->db_name, (char *) df->db_name, DBF_NAMELEN);
		f->db_type = df->db_type;
		f->db_flen = df->db_flen;
		f->db_dec = df->db_dec;
	}
	return TRUE;
}

