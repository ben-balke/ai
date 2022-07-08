/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/dbloader.h,v 1.2 2009/09/21 17:57:39 secwind Exp $
*****************************************************************/
#ifndef DB3LOADER_H_
#define DB3LOADER_H_
#include "libpq-fe.h"
#include "dbreader.h"
#include "charsetconv.h"
#include "tableconfig.h"

class dbloader_ : public dbobserver_
{
	int				m_verbose;
	int				m_office_id;
	PGconn			*m_conn;
	char			m_query [20480];
	CharSetConv_	m_charset;
	tableconfig_	*m_conf;

	int createTable (char *table, dbhead *dbh);
	void applyMaps(dbhead * dbh);
	int startCopy ();
	int endCopy ();
	int copyRecord (field *fields, int fcount, int recno);

public:
	dbloader_ (tableconfig_ *config, int office_id, int verbose);
	~dbloader_ ();
	int connect (char *host, char *database, char *username, char *password);
	int lookHeader (dbhead *head);
	int lookRecord (field *fields, int fcount, int recno);
	int lookEof ();
};
#endif 
