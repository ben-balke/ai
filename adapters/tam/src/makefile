# $Header: /home/cvsroot/ai/adapters/tam/src/makefile,v 1.1 2009/09/20 07:15:31 secwind Exp $

PROGRAM=../bin/dbase2pg

# Uncomment this to provide charset translation
#PG_CPPFLAGS += -DHAVE_ICONV_H
# You might need to uncomment this too, if libiconv is a separate
# library on your platform
#PG_LIBS += -liconv

CPP=g++

INSTALLDIR=../bin

BIN=

CFLAGS=-O2 -I/usr/local/pgsql/include -I. 
#CFLAGS=-g -I/usr/local/pgsql/include -I. 

.SUFFIXES: .C 

.cpp.o:
	$(CPP) $(CFLAGS) -c $<

.c.o:
	$(CPP) $(CFLAGS) -c $<



LIBS=/usr/local/pgsql/lib/libpq.a

DBFILES=\
	dbreader.o \
	tcpclient.o\
	dbase2pg.o\
	dbrequest.o\
	dbreply.o\
	connectreply.o\
	rftprequest.o\
	rftpreply.o\
	Crypt.o\
	dbclient.o\
	byteswap.o\
	disconnectrequest.o \
	charsetconv.o \
	dbloader.o \
	tableconfig.o \
	strutil.o \
	connectrequest.o\
	dataxlogger.o\

$(PROGRAM):	$(DBFILES)
	@-mkdir ../bin
	g++ $(DBFILES) $(LIBS) -lcrypt -o $(PROGRAM)



clean:
	rm -rf $(PROGRAM) $(DBFILES)
