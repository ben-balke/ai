####################################################################################################
# Copyright (c) DuckDigit Technologies, Inc.  ALL RIGHTS RESERVED
# $Header: /home/cvsroot/ai/adapters/tam/bin/ai_convert.sh,v 1.5 2009/12/01 22:27:54 secwind Exp $
####################################################################################################
if [ $# != 2 ]
then
	echo "usage: <hostname> <office_id>"
	exit -1
fi
HOST=$1
OFFICE=$2

echo "drop schema office${OFFICE} cascade;" | ${AISQLPIPE}
echo "create schema office${OFFICE};" | ${AISQLPIPE}


FILES="transact.def flds.def ins.def invtrans.def jourtran.def pinfo.def policy.def"

for f in ${FILES}
do
	echo "-- $f --"
	${AIADAPTERDIR}tam/bin/dbase2pg ${AIADAPTERDIR}/tam/def/$f verbose=0 office_id=${OFFICE} hostname=${HOST} database=ai log=/tmp/dbase2pg.log
done

echo "-- postex.sql --"
time ${AIBINDIR}ai_parse_office ${AIADAPTERDIR}tam/sql/postex.sql ${OFFICE} | ${AISQLPIPE}

