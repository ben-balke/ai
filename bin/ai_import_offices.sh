########################################################################
###
### Main script for processing the office data sets.  Walks the office
### records and processes each one.
###
### Created by: Ben Balke 9/1/2009
###
### Copyright (c) DuckDigit Technologies, Inc. 2009, ALL RIGHTS RESERVED
###
### $Header: /home/cvsroot/ai/bin/ai_import_offices.sh,v 1.11 2010/10/23 23:31:08 secwind Exp $
###
########################################################################
. /etc/profile
. /home/ai/bin/ai_profile

set -x
SERVERLOG="${AILOGDIR}/server.log"

echo "vacuum;" | ${AISQLPIPE}

echo "insert into ai_office_queue select o.id, 1, 'Q', now() from ai_office o where o.active = 'Y' and o.id not in (select q.id from ai_office_queue q where q.id = o.id);" | ${AISQLCOMMAND} 2>/dev/null


