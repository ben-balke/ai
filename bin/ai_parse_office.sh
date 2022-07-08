########################################################################
###
### Talks a script and replaces the office_id and schema with strings
### based on the officeid provided.  Writes the result to standard out.
###
### Created by: Ben Balke 9/1/2009
###
### Copyright (c) DuckDigit Technologies, Inc. 2009, ALL RIGHTS RESERVED
###
########################################################################
if [ $# != 2 ]
then
	echo "ai_parse_office [usage]: <inputfile> <officeid>"
	exit 1;
fi
cat ${1} | sed "s/{office_id}/${2}/g" | sed "s/{schema}/office${2}/g"
