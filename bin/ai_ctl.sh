#!/bin/bash
#======================================================================
# $Header: /home/cvsroot/ai/bin/ai_ctl.sh,v 1.2 2012/08/28 22:01:39 secwind Exp $
#
# Copyright (c) 2008 DuckDigit Technologies, Inc.
# All Rights Reserved.
#
#=====================================================================
. /home/ai/bin/ai_profile

cd ${AIBINDIR}
mkdir ${AIQUEUEDIR} > /dev/null 2>&1
PRODNAME="Agency Insight Server"

case  $1 in
	status)
		ps -fea | grep -v grep | grep "ai_server" > /dev/null
		if [ "$?" = "0" ]
		then
			STATUS="Running "
		else
			STATUS="Stopped "
		fi
		if [ -f ${AIQUEUEDIR}stop ]
		then
			STATUS="${STATUS} but Stopping"
		fi
		if [ -f ${AIQUEUEDIR}pause ]
		then
			STATUS="${STATUS} and Paused"
		fi
		echo ${STATUS}
		exit 0
		;;
	stop)
		ps -fea | grep -v grep | grep "ai_server" > /dev/null
		if [ "$?" = "0" ]
		then
			> ${AIQUEUEDIR}stop
			echo "${PRODNAME} is Stopping.  This may take a few minutes..."
			exit 0
		fi
		echo ${PRODNAME} is not running
		exit 1
		;;
	pause)
		if [ -f ${AIQUEUEDIR}pause ]
		then
			echo "${PRODNAME} is already paused."
			exit 0
		fi
		mkdir ${AIQUEUEDIR} > /dev/null 2>&1
		> ${AIQUEUEDIR}pause
		echo "${PRODNAME} is Pausing.  This may take a few minutes..."
		exit 0
		;;
	resume)
		if [ ! -f ${AIQUEUEDIR}pause ]
		then
			echo "${PRODNAME} is not paused."
			exit 0
		fi
		rm -f ${AIQUEUEDIR}pause
		echo "${PRODNAME} is Resuming.  This may take a few minutes..."
		exit 0
		;;
	start)
		ps -fea | grep -v grep | grep "ai_server" > /dev/null
		if [ "$?" = "0" ]
		then
			echo "${PRODNAME} is Already Running"
			exit 1
		fi
		rm -f ${AIQUEUEDIR}stop
		nohup ${AIHOMEDIR}/bin/ai_server > ${AILOGDIR}/server.log 2>&1 &
		echo "${PRODNAME} is Starting.  This may take a few minutes..."
		exit 0
		;;
	version)
		cat ${AIHOMEDIR}/bin/version.txt
		exit 0
		;;
esac

