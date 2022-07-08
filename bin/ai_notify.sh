########################################################################
###
### Script to notify someone that the office is finished.
###
### Created by: Ben Balke 9/1/2009
###
### Copyright (c) DuckDigit Technologies, Inc. 2009, ALL RIGHTS RESERVED
###
### $Header: /home/cvsroot/ai/bin/ai_notify.sh,v 1.1 2010/10/23 23:07:24 secwind Exp $
###
########################################################################
EMAILS=`echo "select email from ai_users where authroles like '%email%';" | ${AISQLCOMMAND}`
echo $EMAILS
if [ "${EMAILS}" != "" ]
then
	MESSAGE="Agency Insignt: ${OFFICEID}:${NAME} complete."
	echo wget --post-data "dataset=${OFFICEID}&subject=${OFFICEID}: ${NAME} is Finished&emails=${EMAILS}&server=Acordia&message=${MESSAGE}" http://www.duckdigit.com/datastore/notify.php
	rm notify.php
fi

