########################################################################
###
### Environment Settings for the AgencyInsight Scripts
###
### Created by: Ben Balke 9/1/2009
###
### Copyright (c) DuckDigit Technologies, Inc. 2009, ALL RIGHTS RESERVED
###
########################################################################
export AIHOMEDIR=/home/ai/
export AIBINDIR=${AIHOMEDIR}bin/
export AILOGDIR=${AIHOMEDIR}logs/
export AISQLDIR=${AIHOMEDIR}sql/
export AIADAPTERDIR=${AIHOMEDIR}adapters/
export AICLASSES=/home/ai/classes
export AISQLCMD=psql
export AIDB=ai
export AIRUNSQL="${AISQLCMD} ${AIDB} -f "
export AISQLPIPE="${AISQLCMD} -e ${AIDB} "
export AISQLCOMMAND="${AISQLCMD} ${AIDB} -P border=off -t -q"
export AIQUEUEDIR=${AIHOMEDIR}queue/

