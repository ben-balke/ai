/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/dataxlogger.h,v 1.1 2009/09/20 07:15:31 secwind Exp $
******************************************************************/
#ifndef DATAXLOGGER_H_
#define DATAXLOGGER_H_

#define		ITEMLENGTH 	30
#define		NUMLENGTH 	8
class DataXLogger
{
	FILE			*m_fd;
	long			m_address;
		/**
		 * Increment the number of records inserted as needed.
		 * Written out on an Update call.
		 */
	static DataXLogger		*s_instance;
public:
	int				m_records;
		/**
		 * Increment the number of warnings as needed.
		 * Written out on an Update call.
		 */
	int				m_warnings;
		/**
		 * Increment the number of errors as needed.
		 * Written out on an Update call.
		 */
	int				m_errors;


	DataXLogger (char * path);
	void close ();
	void abort (char * reason);
	void setItem (char * header, char * item);
	void update (int count, int warnings, int errors);
	void update ();
	void test ();

		/*
		 * Singleton operations.
		 */
	static DataXLogger *getInstance ()
	{
		return s_instance;
	}
	static DataXLogger *createInstance (char *path)
	{
		return s_instance = new DataXLogger (path);
	}
	static void releaseInstance ()
	{
		if (s_instance != NULL)
		{
			delete s_instance;
			s_instance = NULL;
		}
	}
};

#endif
