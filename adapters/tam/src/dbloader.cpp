/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/dbloader.cpp,v 1.3 2009/09/21 21:38:00 secwind Exp $
*****************************************************************/
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>
#include "dbclient.h"
#include "dbloader.h"
#include "defines.h"
#include "strutil.h"
#include "libpq-fe.h"

dbloader_::dbloader_ (tableconfig_ *conf, int office_id, int verbose) :
	m_charset ()
{
	m_conf = conf;
	m_verbose = verbose;
	m_office_id = office_id;
	m_conn = NULL;
}

dbloader_::~dbloader_ ()
{
	if (m_conn)
	{
		PQfinish(m_conn);
	}
}


void dbloader_::applyMaps (dbhead * dbh)
{
	/* NOTE: subarg is modified in this function */
	if (m_conf->m_namemap)
	{
		char		*map = new char [strlen (m_conf->m_namemap) + 1 + 2];
		/*
		 * Put a comma on either side so we can do a strstr for ,<name>= and
		 * always terminate on a ','.
		 */
		strcpy (map, ",");
		strcat (map, m_conf->m_namemap);
		strcat (map, ",");
		if (m_verbose > 1)
			printf("mapping new field names\n");
		/* use strstr instead of strtok because of possible empty tokens */
		char		search [128];
		char		*c;
		char 		*d;
		for (int i = 0; i < dbh->db_nfields; i++)
		{
			f_descr		*f = &dbh->db_fields [i];
			StrUtil_::strtolower ((char *)f->db_name);
			sprintf (search, ",%s=", f->db_name);
			if (c = strstr (map, search))
			{
				if (m_verbose > 0)
					printf ("mapping: %s to: ", f->db_name);
				char 		*d = (char *) f->db_name;
				c += strlen (search);
				for (int m = DBF_NAMELEN; m > 0 && *c != ',' && *c; m--)
				{
					*d++ = *c++;
				}
				*d = 0;
				if (m_verbose > 0)
					printf ("%s\n", (char *) (f->db_name));
			}
		}
		delete map;
	}
	if (m_conf->m_typemap)
	{
		char		*map = new char [strlen (m_conf->m_typemap) + 1 + 2];
		/*
		 * Put a comma on either side so we can do a strstr for ,<name>= and
		 * always terminate on a ','.
		 */
		strcpy (map, ",");
		strcat (map, m_conf->m_typemap);
		strcat (map, ",");
		if (m_verbose > 1)
			printf("mapping new types names\n");
		/* use strstr instead of strtok because of possible empty tokens */
		char		search [128];
		char		*c;
		char 		*d;
		for (int i = 0; i < dbh->db_nfields; i++)
		{
			f_descr		*f = &dbh->db_fields [i];
			sprintf (search, ",%s=", f->db_name);
			if (c = strstr (map, search))
			{
				if (m_verbose > 0)
					printf ("mapping type: %s from %c to: ", f->db_name, f->db_type);
				char 		*d = (char *) f->db_name;
				c += strlen (search);
				f->db_type = *c;
				if (m_verbose > 0)
					printf ("%c\n", f->db_type);
			}
		}
		delete map;
	}
}

int dbloader_::connect (char *host, char *database, char *username, char *password)
{
	m_conn = PQsetdbLogin(host, NULL, NULL, NULL, database, username, password);
	if (PQstatus(m_conn) != CONNECTION_OK)
	{
		fprintf(stderr, "Couldn't get a connection with the ");
		fprintf(stderr, "designated host!\n");
		fprintf(stderr, "Detailed report: %s\n", PQerrorMessage(m_conn));
		return FALSE;
	}

	PQexec(m_conn, "SET search_path = public");
	return TRUE;
}

int dbloader_::createTable (char *table, dbhead *dbh)
{
	char		t[20];
	int			i,
				length;
	PGresult   *res;
	int			rslt = FALSE;

	if (m_verbose > 1)
		printf("Dropping original table (if one exists)\n");

	sprintf(m_query, "DROP office%d.TABLE %s CASCADE", m_office_id, table);
	PQexec(m_conn, m_query);

	sprintf(m_query, "create schema office%d", m_office_id);
	PQexec(m_conn, m_query);


	if (m_verbose > 1)
		printf("Building CREATE-clause\n");

	sprintf(m_query, "CREATE TABLE office%d.%s (_rownum int", m_office_id, table);
	for (i = 0; i < dbh->db_nfields; i++)
	{
		if (!strlen((char *)dbh->db_fields[i].db_name))
		{
			continue;
			/* skip field if length of name == 0 */
		}
		strcat(m_query, ",\n");

		strcat(m_query, (char *)dbh->db_fields[i].db_name);
		switch (dbh->db_fields[i].db_type)
		{
			case 'D':
			case 'x':
			case 'X':
				strcat(m_query, " date");
				break;
			case 'C':
				if (dbh->db_fields[i].db_flen > 1)
				{
					strcat(m_query, " varchar");
					snprintf(t, 20, "(%d)",
							 dbh->db_fields[i].db_flen);
					strcat(m_query, t);
				}
				else
					strcat(m_query, " char");
				break;
			case 'N':
				if (dbh->db_fields[i].db_dec != 0)
					strcat(m_query, " real");
				else
					strcat(m_query, " int");
				break;
			case 'L':
				strcat(m_query, " char");
				break;
		}
	}

	strcat(m_query, ")");

	if (m_verbose > 1)
	{
		printf("Sending create-clause\n");
		printf("%s\n", m_query);
	}

	if ((res = PQexec(m_conn, m_query)) == NULL)
	{
		fprintf(stderr, "Error creating table!\n");
		fprintf(stderr, "Detailed report: %s\n", PQerrorMessage(m_conn));
	}
	else
	{
		PQclear(res);
		rslt = TRUE;
	}
	return rslt;
}

int dbloader_::startCopy ()
{
	int				rslt = FALSE;	
	PGresult   		*res;

	if (!m_charset.initconv ())
	{
		fprintf (stderr, "Failed to start character set conversion\n");
		return FALSE;
	}
	
	if (m_verbose > 0)
	{
		fprintf(stderr, "Transaction: START\n");
	}
	res = PQexec(m_conn, "BEGIN");
	if (res == NULL)
	{
		fprintf(stderr, "Error starting transaction!\n");
		fprintf(stderr, "Detailed report: %s\n", PQerrorMessage(m_conn));
	}
	else
	{
		PQclear(res);
		sprintf(m_query, "COPY office%d.%s FROM stdin", m_office_id, m_conf->m_tablename);
		res = PQexec(m_conn, m_query);
		if (res == NULL)
		{
			fprintf(stderr, "Error starting COPY!\n");
			fprintf(stderr, "Detailed report: %s\n", PQerrorMessage(m_conn));
		}
		else
		{
			PQclear(res);
			rslt = TRUE;
		}
	}
	return rslt;
}

int dbloader_::endCopy ()
{
	int				rslt = FALSE;
	PGresult   		*res;
	if (m_verbose > 0)
		fprintf(stderr, "Transaction: END\n");

	PQputline(m_conn, "\\.\n");

	if (PQendcopy(m_conn) != 0)
	{
		fprintf(stderr, "Something went wrong while copying. Check "
				"your tables!\n");
	}
	else
	{
		res = PQexec(m_conn, "END");
		if (res == NULL)
		{
			fprintf(stderr, "Error committing work!\n");
			fprintf(stderr, "Detailed report: %s\n", PQerrorMessage(m_conn));
		}
		else
		{
			PQclear(res);
			rslt = TRUE;
		}
	}
	m_charset.closeconv ();
	return rslt;
}

int validateDate (char *d)
{
	char		orig [20];
	static int daysinmonth [] = {31,29,31,30,31,30,31,31,30,31,30,31};
	int year = atoi (d);
	int mon = atoi (d + 5);
	int day = atoi (d + 8);
	strcpy (orig, d);

	if (mon <= 0)
	{
		d [5] = '0';
		d [6] = '1';
		mon = 1;
	}
	else if (mon >= 12)
	{
		d [5] = '1';
		d [6] = '2';
		mon = 12;
	}
	if (day <= 0)
	{
		d [8] = '0';
		d [9] = '1';
		day = 1;
	}
	else if (day > daysinmonth [mon - 1])
	{
		d [8] = '0' + (daysinmonth [mon - 1] / 10);
		d [9] = '0' + (daysinmonth [mon - 1] % 10);
		day = daysinmonth [mon - 1];
	}
	if (mon == 2 && day == 29)
	{
		if (year % 4 != 0)
		{
			day = 28;
			d [9] = '8';
		}
	}
	if (strcmp (orig, d))
	{
		printf ("Validate Date: %s date changed to %s\n", orig, d);
	}
	if (year < 1500 || year > 2300 || 
		mon < 1 || mon > 12 ||
		day < 1 || day > 31)
	{
		printf ("Validate Date: %s %d %d %d\n", d, year, mon, day);
		return FALSE;
	}
	return TRUE;
}
int charYear (char c1, char c2)
{
	int			year;

	if (isalpha (c1))
	{
		year = 2000 + ((c1 - 'A') * 10) + 
			(c2 - '0');
	}
	else
	{
		if (c1 == '0' && c2 == '0')
		{
			year = 2000;
		}
		else
		{
			year = 1900 + ((c1 - '0') * 10) + 
				(c2 - '0');
		}
	}
	return year;
}
int dbloader_::copyRecord (field *fields, int fcount, int recno)
{
	int 		j;
	int			h;
	char		pgdate [11];
	field      *f;
	char		*data;
	int			bad = 0;
	DataXLogger		*dl = DataXLogger::getInstance ();


	m_query[0] = '\0';
	j = 0;				/* counter for fields in the output */
	sprintf(m_query, "%d", recno);
	for (h = 0; h < fcount; h++)
	{
		f = &fields [h];
			/* When the new fieldname is empty, the field is
			 * skipped */
		if (!strlen((char *)f->db_name)) 
			continue;
		else
			j++;

			/*
			 * This is the field separator.
			 */
		strcat(m_query, "\t");

		data = (char *)f->db_contents;
		data = m_charset.convert (data);
		data = StrUtil_::Escape(data);

		switch (f->db_type)
		{
		case 'x':		// YYMMDD
			if (strlen (data) != 6 || strchr (data, ' ') ||
				!strcmp (data, "000000"))
			{
				strcat(m_query, "\\N");
			}
			else 
			{
				int year = charYear (data [0], data [1]);
				if (data [2] == '/' && data [5] == '/')
				{
					snprintf(pgdate, 11, "%d-%c%c-01",
						year, data[3], data[4]);
				}
				else
				{
					snprintf(pgdate, 11, "%d-%c%c-%c%c",
						year, data[2], data[3], data[4], data[5]);
				}
				if (!validateDate (pgdate))
				{
					fprintf (stderr, "BAD DATE: %s: %s\n", f->db_name, data);
					bad++;
					strcpy(pgdate, "\\N");
				}
				strcat(m_query, pgdate);
			}
			break;
		case 'X':
			if (strlen (data) != 8 || strchr (data, ' '))
			{
				strcat(m_query, "\\N");
			}
			else
			{
				int year = charYear (data [6], data [7]);
				snprintf(pgdate, 11, "%d-%c%c-%c%c",
					year, data[0], data[1], data[3], data[4]);
				if (!validateDate (pgdate))
				{
					fprintf (stderr, "BAD DATE: %s: %s\n", f->db_name, data);
					strcpy(pgdate, "\\N");
					bad++;
				}
				strcat(m_query, pgdate);
			}
			break;
		case 'D':
			if ((strlen(data) == 8) && StrUtil_::isinteger(data))
			{
				snprintf(pgdate, 11, "%c%c%c%c-%c%c-%c%c",
						 data[0], data[1], data[2], data[3],
						 data[4], data[5], data[6], data[7]);
				strcat(m_query, pgdate);
			}
			else
			{
				strcat(m_query, "\\N");
			}
			break;
		case 'N':
		case 'O':
			if ((f->db_dec == 0))
			{
				int 		isnumber = 0;
				for (char *c = data; *c; c++)
				{
					if (*c != ' ')
					{
						isnumber = 1;
						break;
					}
				}
				if (isnumber && StrUtil_::isinteger(data))
					strcat(m_query, data);
				else
				{
					strcat(m_query, "\\N");
					if (m_verbose)
					{
						bad++;
						fprintf(stderr, "BAD NUMBER: %s:  \"%s\"\n",
								f->db_name, data);
					}
				}
			}
			else 
			{
				int 		isnumber = 0;
				for (char *c = data; *c; c++)
				{
					if (*c != ' ')
					{
						isnumber = 1;
						break;
					}
				}
				if (isnumber)
					strcat(m_query, data); /* must be character */
				else
				{
					strcat(m_query, "\\N");
					if (m_verbose)
					{
						bad++;
						fprintf(stderr, "BAD NUMBER: %s:  \"%s\"\n",
								f->db_name, data);
					}
				}
			}
			break;
		default:
			strcat(m_query, data); /* must be character */
			break;
		}
	}
	strcat(m_query, "\n");

	if (bad)
	{
		dl->m_warnings++;
		printf (m_query);
	}
	PQputline(m_conn, m_query);
	return TRUE;
}

int dbloader_::lookHeader (dbhead *head)
{
	int			rslt = FALSE;
	applyMaps (head);
	if (createTable (m_conf->m_tablename, head))
	{
		if (startCopy ())
		{
			rslt = TRUE;
		}
	}
	return rslt;
}
int dbloader_::lookRecord (field *fields, int fcount, int recno)
{
	int rslt = copyRecord (fields, fcount, recno);
	DataXLogger		*dl = DataXLogger::getInstance ();

	dl->m_records++;
	if (dl->m_records % 200 == 0)
	{
		dl->update ();
	}
	return rslt;
}
int dbloader_::lookEof ()
{
	DataXLogger::getInstance ()->update ();
	return endCopy ();
}
