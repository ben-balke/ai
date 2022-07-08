/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/dbreader.h,v 1.2 2009/09/21 18:09:35 secwind Exp $
*****************************************************************/
#ifndef DB3TOPG_H_
#define DB3TOPG_H_

#include "fileprocessor.h"
#include "dbf.h"

enum eDbState_
{
	DB3_READ_HEAD,
	DB3_READ_FIELDS,
	DB3_READ_HEAD_PAD,
	DB3_READ_RECORDS
};

class dbobserver_
{
public:
	virtual int lookHeader (dbhead *head) = 0;
	virtual int lookRecord (field *fields, int fcount, int recno) = 0;
	virtual int lookEof () = 0;
};

class dbreader_ : public FileProcessor_
{
	dbhead		m_dbh;
				// This is the header containing our information.
				//
	dbf_header	m_head;
				// This is the actual dbf header and is target
				// for the header block read.
				//
	dbf_field	m_fieldc;
				// This is a working field that i9s the target
				// of the read for the fields.
				//
	int			m_curfield;
				// As fields are being read this determines the 
				// current one.
				//
	eDbState_	m_state;	
				// State of the extract.  This determines
				// what is currently being read from the file
				// processor.
				//
	int			m_records;
				// Number of records currently processed.
				//
	char		*m_block;
				// Block of records to read.
				//
	int			m_recordblock;
				// Number of records to read in a single block.
				//
	field		*m_recordfields;
				// Prepared place to save data for each field type.
				//
	dbobserver_	*m_observer;
				// This is the object that is looking at the file 
				// contents.
				//
 	char		*m_headpadbuf;
	int			m_headremain;

	int			m_verbose;

	int populateRecord(u_char *data, u_long rec);
public:
	dbreader_ (int recordblock, dbobserver_ *observer, int verbose);
	~dbreader_ ();
	virtual int getBytesNeeded ();
	virtual char *getBuffer ();
	virtual int process (int nbytes);
	virtual int processEof ();
	int processHeader (int nbytes);
	int processField (int nbytes);
	int processRecords (int nbytes);
	int buildRecord ();
};
#endif
