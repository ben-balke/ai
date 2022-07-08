########################################################################
###
### Takes a script and replaces the office_id and from_office_id strings
### based on the officeid and from_office_id provided.  Writes the result 
### to standard out.
###
### Created by: Ben Balke 9/1/2009
###
### Copyright (c) DuckDigit Technologies, Inc. 2009, ALL RIGHTS RESERVED
###
########################################################################
if [ $# != 4 ]
then
	echo "ai_parse_copy [usage]: <inputfile> <officeid> <fromofficeid> <whereclause>"
	exit 1;
fi
cat ${1} | sed "s/{office_id}/${2}/g" | sed "s/{from_office_id}/${3}/g" | sed "s/{whereclause}/$4/g"
