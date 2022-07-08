if [ $# != 2 ]
then
	echo "usage: <hostname> <office_id>"
	exit -1
fi
HOST=$1
OFFICE=$2

FILES="clients policies premiums insurors coverages staff company_master"

${AIADAPTERDIR}sagitta/bin/maketables ${OFFICE} | ${AISQLPIPE}

for f in ${FILES}
do
	java -cp ${AICLASSES} com.duckdigit.ai.adapters.sagitta.PickExtractor ${f} log=${AIHOMEDIR}logs/office${OFFICE}ex.log hostname=${HOST} autocommit=N outbufsize=10240 inbufsize=400000 office=${OFFICE} | ${AISQLPIPE}
done

