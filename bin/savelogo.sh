set -x
#which convert
CONVERT=/usr/bin/convert
ORIGINAL=${1}
NEWFILE=${2}
WSIZE=350
HSIZE=75
${CONVERT} ${ORIGINAL} -resize ${WSIZE}x${HSIZE} ${NEWFILE} > /tmp/convert.log 2>&1
#rm ${ORIGINAL}

