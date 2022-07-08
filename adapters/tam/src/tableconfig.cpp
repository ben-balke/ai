/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/tableconfig.cpp,v 1.2 2009/09/21 18:09:35 secwind Exp $
*****************************************************************/
#include <stdio.h>
#include <string.h>
#include <fcntl.h>
#include <stdlib.h>
#include "dbclient.h"
#include "defines.h"
#include "tableconfig.h"

tableconfig_::tableconfig_ ()
{
	m_tablename = NULL;
	m_namemap = NULL;
	m_typemap = NULL;
	m_recordset = 100;
	m_filepath = NULL;
	m_desc = NULL;
}

int getItem (char *buf, char *item, char **dest)
{
	int 		rslt = FALSE;
	int			len = strlen (item);
	char		*c;
	if (!strncmp (buf, item, len))
	{
		c = buf + len;
		if (*dest)
		{
			delete *dest;
		}
		*dest = new char [strlen (c) + 1];
		strcpy (*dest, c);
		rslt = TRUE;
	}
	return rslt;
}

int tableconfig_::load (char *path)
{
	char		buf [4096];
	FILE 		*fd;
	char		*recset = NULL;
	fd = fopen (path, "r");
	if (!fd)
	{
		perror (path);
		return FALSE;
	}
	while (fgets (buf, sizeof (buf), fd))
	{
		char *c = strchr (buf, '\n');
		if (c)
		{
			*c = 0;
		}
		if (getItem (buf, (char *) "tablename=", &m_tablename) ||
			getItem (buf, (char *) "namemap=", &m_namemap) ||
			getItem (buf, (char *) "typemap=", &m_typemap) ||
			getItem (buf, (char *) "description=", &m_desc) ||
			getItem (buf, (char *) "filepath=", &m_filepath))
		{
		}
		else if (getItem (buf, (char *) "recordset=", &recset))
		{
			m_recordset = atoi (recset);
			if (m_recordset == 0)
			{
				m_recordset = 100;
			}
			delete recset;
		}
	}
	

	fclose (fd);
	DataXLogger::getInstance ()->setItem ((char *) "", m_desc == NULL ? m_tablename : m_desc);
	return TRUE;
}
