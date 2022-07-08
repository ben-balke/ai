########################################################################
###
### Main script for processing the office data sets.  Walks the office
### records and processes each one.
###
### Created by: Ben Balke 9/1/2009
###
### Copyright (c) DuckDigit Technologies, Inc. 2009, ALL RIGHTS RESERVED
###
### $Header: /home/cvsroot/ai/bin/ai_office.sh,v 1.2 2012/08/28 22:02:07 secwind Exp $
###
########################################################################
. /etc/profile
. /home/ai/bin/ai_profile

set -x
function doOffice ()
{
	OFFICEID=$1

	OFFICE=(`echo "select '=' || hostname, '=' || office_type_id, '=' || noextract from ai_office where id = ${OFFICEID}" | ${AISQLCOMMAND}`)
	echo "${OFFICE[*]}"
	if [ "${OFFICE[0]}" = "" ]
	then
    	echo "No ID defined for office.  Aborting Conversion"
		exit 1
	fi
	
	HOSTNAME=${OFFICE[0]:1}
	OFFICETYPE=${OFFICE[1]:1}
	NOEXTRACT=${OFFICE[2]:1}

	CONFIGSTRING=`echo "select connectstring from ai_office where id = ${OFFICEID}" | ${AISQLCOMMAND}`
	NAME=`echo "select name from ai_office where id = ${OFFICEID}" | ${AISQLCOMMAND}`

	echo "=================================================================="
	echo "START Conversion for Office $OFFICEID"
	echo "=================================================================="
	echo "Started: " `date`
	echo "Name: ${NAME}"
	echo "Hostname: ${HOSTNAME}"
	echo "Office Type: ${OFFICETYPE}"
	echo "Configuration String: ${CONFIGSTRING}"
	echo "=================================================================="
	
	
	case ${OFFICETYPE} in
		'S')
			echo "sagitta"
			if [ "${NOEXTRACT}" = 'Y' ]
			then
				if [ "${CONFIGSTRING:0:6}" = "office" ]
				then
					echo "drop schema ${CONFIGSTRING} cascade;" | ${AISQLPIPE}
					time pg_dump ${CONFIGSTRING} | ${AISQLPIPE}
				fi
			else
				time ${AIADAPTERDIR}sagitta/bin/ai_convert ${HOSTNAME} ${OFFICEID}
			fi
			time ${AIBINDIR}ai_parse_office ${AIADAPTERDIR}sagitta/sql/import.sql ${OFFICEID} | ${AISQLPIPE}
			time ${AIBINDIR}ai_parse_office ${AISQLDIR}post_adapter.sql ${OFFICEID} | ${AISQLPIPE}
			;;
		's')
			echo "sagitta_online"
			;;
		'T') 
			echo "tam"
			if [ "${HOSTNAME}" = 'usedatabase' ]
			then
				if [ "${CONFIGSTRING:0:6}" = "office" ]
				then
					echo "drop schema ${CONFIGSTRING} cascade;" | ${AISQLPIPE}
					time pg_dump ${CONFIGSTRING} | ${AISQLPIPE}
				fi
			else
				time ${AIADAPTERDIR}tam/bin/ai_convert ${HOSTNAME} ${OFFICEID}
			fi
			time ${AIBINDIR}ai_parse_office ${AIADAPTERDIR}tam/sql/import.sql ${OFFICEID} | ${AISQLPIPE}
			time ${AIBINDIR}ai_parse_office ${AISQLDIR}post_adapter.sql ${OFFICEID} | ${AISQLPIPE}
			;;

		'I') 
			echo "infinity"
			# The connection string has a list of cono's and pcno's
			time ${AIADAPTERDIR}infinity/bin/ai_convert ${HOSTNAME} ${OFFICEID} ${CONFIGSTRING}
			time ${AIBINDIR}ai_parse_office ${AIADAPTERDIR}infinity/sql/import.sql ${OFFICEID} | ${AISQLPIPE}
			time ${AIBINDIR}ai_parse_office ${AISQLDIR}post_adapter.sql ${OFFICEID} | ${AISQLPIPE}
			;;
		'C')
			echo "Copy Ofice"
			time ${AIBINDIR}ai_parse_copy ${AISQLDIR}copy_office.sql ${OFFICEID} ${HOSTNAME} "${CONFIGSTRING}" | ${AISQLPIPE}
			time ${AIBINDIR}ai_parse_office ${AISQLDIR}post_adapter.sql ${OFFICEID} | ${AISQLPIPE}
			;;
		'3') 
			echo ams360
			;;
	esac
	echo "update ai_office set lastupdate = now() where id = ${OID};" | ${AISQLCOMMAND}
	echo "=================================================================="
	echo "END Conversion for Office $OFFICEID " `date`  >> ${OFFICELOG}
	echo "=================================================================="
	. ${AIBIN}/ai_notify 
}	

OID=$1
mkdir -p ${AILOGDIR}
export OFFICELOG="${AILOGDIR}office${OID}.log"
doOffice ${OID} > ${OFFICELOG} 2>&1

