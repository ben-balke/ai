#!/bin/bash
########################################################################
###
### Main script for processing the office data sets.  Walks the office
### records and processes each one.
###
### Created by: Ben Balke 9/1/2009
###
### Copyright (c) DuckDigit Technologies, Inc. 2009, ALL RIGHTS RESERVED
###
### $Header: /home/cvsroot/ai/bin/ai_server.sh,v 1.3 2012/08/28 22:02:48 secwind Exp $
###
########################################################################
echo "########################################################################################"
echo "### Agency Insight Server"
echo "### Copyright (c) Duckdigit Technologies, Inc. 2009, ALL RIGHTS RESERVED"
echo "########################################################################################"
date
. /etc/profile
. /home/ai/bin/ai_profile

checkStop()
{
	if [ -f ${AIQUEUEDIR}stop ]
	then
		rm -f ${AIQUEUEDIR}stop
		echo "APERATOR ABORTED"
		exit 1
	fi
}

while :
do
	checkStop
	if [ ! -f ${AIQUEUEDIR}pause ]
	then
		OFFICE_LIST=`echo "select q.id from ai_office_queue q left outer join ai_office o on (o.id = q.id) left outer join ai_office_type t on (t.id = o.office_type_id) where status = 'Q' order by t.seq;" | ${AISQLCOMMAND} 2>/dev/null`
		for OID in ${OFFICE_LIST}
		do
			echo "update ai_office_queue set status = 'R' where id = ${OID};" | ${AISQLCOMMAND} 
			${AIBINDIR}ai_office ${OID} 
			echo "insert into ai_office_runlog select id, userid, 'C', starttime, now() from ai_office_queue where id = ${OID};" | ${AISQLCOMMAND}  
			echo "delete from ai_office_queue where id = ${OID};" | ${AISQLCOMMAND}  
			checkStop
		done 
	else
		echo "Paused"
	fi
	sleep 60
done

