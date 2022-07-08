/*
 * Copyright (c) DuckDigit Technologies, Inc.  2009 ALL RIGHTS RESERVED
 * $Header: /home/cvsroot/ai/sql/alter_v10.3.1.sql,v 1.3 2010/10/19 07:12:28 secwind Exp $
 */

alter table ai_office add column kpi_type_id char (1) default 'O';


create table ai_kpi_map_coverage
(
	office_id   int
	,coverage_id	varchar (36)
	,kpi_group  int
)
;
create unique index ai_kpi_map_coveragei on ai_kpi_map_coverage (office_id, coverage_id);

create unique index ai_office_type_i1 on ai_office_type (id);
insert into ai_office_type values ('C', 'Copy from Office');

alter table ai_office alter column connectstring type varchar(256);

