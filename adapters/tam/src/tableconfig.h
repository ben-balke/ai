/******************************************************************
* Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/tam/src/tableconfig.h,v 1.1 2009/09/20 07:15:31 secwind Exp $
*****************************************************************/
#ifndef TABLECONFIG_H_
#define TABLECONFIG_H_
class tableconfig_
{
public:
	char			*m_namemap;
					/* list of comma separated reassignments for 
					 * field names that are keywords.
					 */
	char			*m_typemap;  
					/* list of fields havimg x=YYMMDD, X=MM/DD/YY format 
					 * with hex number for year.
					 */
	char			*m_tablename;
					/* What to name the file.
					 */
	char			*m_filepath;
					/* Path of the dbase file.
					 */
	char			*m_desc;
					/* Description displayed in the logfile.
					 */
	int				m_recordset;
					/* Number of records to read in a single shot.
					 */

	tableconfig_ ();
	int load (char *path);
};
#endif
