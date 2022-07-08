# you must run this:
# . /home/secwind/www/ai/bin/as_profile
DEFSDIR=${AIADAPTERDIR}sagitta/defs
if [ $# != 1 ]
then
	echo "maketables <office_id>"
	exit 1
fi


function generateSql
{
	FIELDSEP=""
	FILENAME=$1
	OFFICE=$2
	echo "drop table office${OFFICE}.${FILENAME};"
	echo create table office${OFFICE}.${FILENAME} 
	echo "("
	cat $g | grep -v "^SET " | sed 's/#.*//g' | awk '{ print "\t"$1"\t"$2"\t"$3"\t"$4 }' | while read i
	do
		PARAMS=($i)
		FIELDNAME=${PARAMS[0]}
		FIELDNUM=${PARAMS[1]}
		FIELDTYPE=${PARAMS[2]}
		CONVERT=${PARAMS[3]}
		if [ "${FIELDNAME}" != "" ]
		then
			if [ "${FIELDNAME}" = "BLANK" ]
			then
				FIELDNAME=BLANK${FIELDNUM}
			fi
			echo ${CONVERT} | grep MV > /dev/null
			if [ $? = 0 ]
			then
				ARRAY="[]"
			else
				ARRAY=
			fi
			echo "${FIELDSEP}"
			FIELDSEP=","
			echo -n "	${FIELDNAME}		${FIELDTYPE}${ARRAY}"
		fi
	done
	echo 
	echo ");"
}



cd ${DEFSDIR}

echo "create schema office${1};"
for g in *.def
do
	FILENAME=`echo $g | sed 's/\.def$//'`
	generateSql ${FILENAME} ${1}
done

