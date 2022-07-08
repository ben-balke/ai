/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/dbase2pg.cpp,v 1.4 2009/09/21 18:16:31 secwind Exp $
******************************************************************/
#include "dbclient.h"
#include "dbreader.h"
#include "dbloader.h"
// USAGE:
//<hostname> <database> <config> <

#define TAMSERVE_PORT	6345

#define 	HOSTARG		"hostname="
#define 	DBARG		"database="
#define 	LOGARG		"log="
#define 	VERBOSEARG	"verbose="
#define 	OFFICEARG	"office_id="
main (int argc, char **argv)
{
	//CTcpClient::TcpInit ();
	char				buf [2048];
	char				*config;
	char				*hostname;
	char				*database;
	char				*log;
	int					verbose = 0;
	int					office_id = -1;
	const char				*usage = "%s <deffile> office_id=<office_id> hostname=<hostname> database=<local postgres database> log=<logfile>\n";
	if (argc < 6)
	{
		fprintf (stderr, usage, argv [0]);
		exit (-1);
	}
	config = argv [1];

	for (int i = 2; i < argc; i++)
	{
		if (!strncmp ((const char *) argv [i], HOSTARG, strlen (HOSTARG)))
		{
			hostname = argv [i] + strlen (HOSTARG);
		}
		else if (!strncmp (argv [i], DBARG, strlen (DBARG)))
		{
			database = argv [i] + strlen (DBARG);
		}
		else if (!strncmp (argv [i], LOGARG, strlen (LOGARG)))
		{
			log = argv [i] + strlen (LOGARG);
			DataXLogger::createInstance (log);
		}
		else if (!strncmp (argv [i], VERBOSEARG, strlen (VERBOSEARG)))
		{
			verbose = atoi (argv [i] + strlen (VERBOSEARG));
		}
		else if (!strncmp (argv [i], OFFICEARG, strlen (OFFICEARG)))
		{
			office_id = atoi (argv [i] + strlen (OFFICEARG));
		}
		else
		{
			fprintf (stderr, "Invalid argument [%d] %s\n", i, argv [i]);
			fprintf (stderr, usage, argv [0]);
			exit (-1);
		}
	}
		
	if (office_id <= 0)
	{
			fprintf (stderr, "You supply an office_id argument \n");
			fprintf (stderr, usage, argv [0]);
			exit (-1);
		
	}

	tableconfig_		conf;
	if (conf.load (config))
	{
		DbClient_			db;
		dbloader_			dbloader (&conf, office_id, verbose);
		dbreader_			dbreader (conf.m_recordset, &dbloader, verbose);

		if (dbloader.connect ((char *) "localhost", database, (char *) "secwind", NULL))
		{
			if (db.connect (hostname, TAMSERVE_PORT))
			{
				db.getFile (conf.m_filepath, &dbreader);
				db.disconnect ();
			}
		}
	}
	DataXLogger::releaseInstance ();
}
