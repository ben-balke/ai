########################################################################
###
### Initializes the database
###
### Created by: Ben Balke 9/1/2009
###
### Copyright (c) DuckDigit Technologies, Inc. 2009, ALL RIGHTS RESERVED
###
########################################################################
. /home/ai/bin/ai_profile
if [ "$1" = "ALL" ]
then
		# if ALL is pased the ai tables are also dropped and recreated.  This will
		# get lose the kpi groups so be careful
	${AIRUNSQL} ${AISQLDIR}ai.sql
fi
	# These common tables are populated from the adapters and nothing should be 
	# important.
${AIRUNSQL} ${AISQLDIR}commondata.sql
${AIRUNSQL} ${AISQLDIR}commondata.idx
${AIRUNSQL} ${AISQLDIR}states.sql
