/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/dataxlogger.cpp,v 1.2 2009/09/21 18:09:35 secwind Exp $
******************************************************************/
#include <stdio.h>
#include "dataxlogger.h"

DataXLogger      *DataXLogger::s_instance = NULL;


DataXLogger::DataXLogger (char * path)
{
	m_fd = fopen (path, "r+");
	if (m_fd)
	{
		fseek (m_fd, 0, SEEK_END);
		m_address = ftell (m_fd);
	}
	m_records = 0;
	m_errors = 0;
	m_warnings = 0;
}

void DataXLogger::close ()
{
	if (m_fd == NULL) 
		return;
	fclose (m_fd);
}

void DataXLogger::abort (char * reason)
{
	if (NULL == m_fd) 
		return;
	fputs (reason, m_fd);
}
void DataXLogger::setItem (char * header, char * item)
{
	char *		itemtext;

	m_errors = 0;
	m_warnings = 0;
	m_records = 0;

	if (NULL == m_fd) 
		return;
	fprintf (m_fd, "%s%-32s", header, item);
	fflush (m_fd);
	m_address = ftell (m_fd);
}
void DataXLogger::update (int count, int warnings, int errors)
{
	if (NULL == m_fd) 
		return;

	fseek (m_fd, m_address, SEEK_SET);
	fprintf (m_fd, "%8d  %8d  %8d\n", count, warnings, errors);
	fflush (m_fd);
}
void DataXLogger::update ()
{
	update (m_records, m_warnings, m_errors);
}

void DataXLogger::DataXLogger::test ()
{
	DataXLogger		dl ((char *) "/tmp/dx.log");
	dl.setItem ((char *) "   100      0", (char *) "Policy");
	dl.m_records = 100;
	dl.update ();
	dl.m_records = 110;
	dl.setItem ((char *) "   100      0", (char *) "Customer");
	dl.m_records = 110;
	dl.update ();
	dl.m_records = 300;
	dl.update ();
	dl.close ();
}
