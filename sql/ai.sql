/*
 * Copyright (c) DuckDigit Technologies, Inc.  2009 ALL RIGHTS RESERVED
 * $Header: /home/cvsroot/ai/sql/ai.sql,v 1.5 2012/08/28 21:09:20 secwind Exp $
 */

drop table ai_users;
create table ai_users
(
	id			serial
	,first_name	varchar (128)
	,last_name	varchar (128)
	,username	varchar (128)
	,email	varchar (128)
	,password	varchar (36)
	,active		char (1)
	,authroles	varchar (256)
		/* PREFERENCES HERE */
	,active_office_id	int default 1
	,n_customers	int default 20
	,sort_customers	varchar(64) default 'cust_revenue desc'

	,n_policies	int default 20
	,sort_policies	varchar(64) default 'pol_revenue desc'

	,n_top_customers	int default 5
	,sort_top_customers	varchar(64) default 'revenue desc'

	,n_top_carriers	int default 5
	,sort_top_carriers	varchar(64) default 'revenue desc'

	,n_top_coverages	int default 5
	,sort_top_coverages	varchar(64) default 'revenue desc'

	,n_top_carriers_by_cust	int default 5
	,sort_top_carriers_by_cust	varchar(64) default 'revenue desc'

	,n_top_coverages_by_cust int default 5
	,sort_top_coverages_by_cust varchar(64) default 'revenue desc'
	,sort_policies_by_cust varchar(64) default 'revenue desc'

	,sort_bycov			varchar(64) default 'revenue desc'
	,sort_byins			varchar(64) default 'revenue desc'
	,sort_bypayee		varchar(64) default 'revenue desc'
	,sort_byprod		varchar(64) default 'revenue desc'
	,sort_bystate		varchar(64) default 'revenue desc'
	,sort_bycsr			varchar(64) default 'revenue desc'
	,sort_bysiccode		varchar(64) default 'revenue desc'
	,sort_bysicmajor	varchar(64) default 'revenue desc'
	,sort_bylob			varchar(64) default 'revenue desc'
	,last_office		varchar(64) default 1

	,sort_forprod		varchar(64) default 'revenue desc'
	,tab_forprod		varchar(20) default 'coverage'
	,sort_forcsr		varchar(64) default 'revenue desc'
	,tab_forcsr			varchar(20) default 'coverage'
	,sort_forpayee		varchar(64) default 'revenue desc'
	,tab_forpayee		varchar(20) default 'coverage'
	,sort_forins		varchar(64) default 'revenue desc'
	,tab_forins			varchar(20) default 'coverage'
	,sort_forsiccode	varchar(64) default 'revenue desc'
	,tab_forsiccode		varchar(20) default 'coverage'
	,sort_forsicmajor	varchar(64) default 'revenue desc'
	,tab_forsicmajor	varchar(20) default 'coverage'
	,sort_forcov		varchar(64) default 'revenue desc'
	,tab_forcov			varchar(20) default 'insuror'
	,sort_forstate		varchar(64) default 'revenue desc'
	,tab_forstate		varchar(20) default 'coverage'
	,sort_forlob		varchar(64) default 'revenue desc'
	,tab_forlob			varchar(20) default 'coverage'

	,createdon			datetime
	,createdby			int
	,modifiedon			datetime
	,modifiedby			int
)
;

create unique index ai_users_i1 on ai_users (username)
;

insert into ai_users (id, first_name, last_name, username, password, active, authroles, createdon, createdby) values
	(0, 'Joe', 'Administrator', 'admin', md5('admin'), 'Y', 'admin,graphs', now(), 0);
insert into ai_users (id, first_name, last_name, username, password, active, authroles, createdon, createdby) values
	(1, 'Steve', 'Sentz', 'steve', md5('steve'), 'Y', 'admin,graphs', now(), 0);

drop table ai_authrole;
CREATE TABLE ai_authrole 
(
	name varchar(10) NOT NULL,
	description varchar(256) NOT NULL,
	grouping varchar(40) DEFAULT NULL,
	PRIMARY KEY (name)
);
insert into ai_authrole (name, description, grouping) values ('admin', 'Administrator Super User', 'Administration');
insert into ai_authrole (name, description, grouping) values ('graphs', 'Show Graphs', 'Display');
insert into ai_authrole (name, description, grouping) values ('export', 'Export Data', 'Data');



drop table ai_office_type;
create table ai_office_type
(
	id					char(1)
	,name				varchar(20)
);

insert into ai_office_type values ('X', 'Test');
insert into ai_office_type values ('3', 'AMS360');
insert into ai_office_type values ('S', 'Sagitta');
insert into ai_office_type values ('s', 'Sagitta ONline');
insert into ai_office_type values ('I', 'Infinity');
insert into ai_office_type values ('T', 'TAM');
insert into ai_office_type values ('C', 'Copy from Office');
create unique index ai_office_type_i1 on ai_office_type (id);

drop table ai_kpi_type;
create table ai_kpi_type
(
	id					char(1)
	,name				varchar(20)
);

insert into ai_kpi_type values ('O', 'Organizations');
insert into ai_kpi_type values ('C', 'Coverage Types');

drop table ai_office;
create table ai_office
(
	id					serial
	,name				varchar (100)
	,hostname			varchar (100)
	,connectstring		varchar (256)
	,office_type_id		char(1) /* ai_office_type */
	,kpi_type_id		char(1) /* ai_kpi_type */
	,level1org			char (40)
	,level2org			char (40)
	,active				char (1) /* Y=Yes, N=No */
	,update_command		char (100)
	,lastupdate			datetime
	,fiscal_month		int /* Starting month of the fiscal period 1 to 12 */
	,createdby	int
	,createdon	datetime
	,modifiedby	int
	,modifiedon	datetime
)
;
insert into ai_office (name, hostname, connectstring, office_type_id, active) values
	('Agency Insight Demo', 'demo', 'noconnect', 'X', 'Y');

create unique index ai_office_namei on ai_office (name);

drop table ai_office_group;
create table ai_office_group
(
	id				serial
	,name			varchar (40)
)
;

drop table ai_office_item;
create table ai_office_item
(
	office_group_id		int
	,office_id			int
	,createon			datetime
	,primary key (office_group_id, office_id)
)
;

drop table ai_userlog;
create table ai_userlog
(
	eventdate		datetime
	,userid			int
	,ipaddress		varchar (30)
	,code			varchar (20)
	,description		varchar (128)
)
;
create index ai_userlogi on ai_userlog (userid, eventdate);


drop table ai_insuror_group;
create table ai_insuror_group
(
	id			serial
	,name		varchar (60)
	,createdby	int
	,createdon	datetime
);

drop table ai_insuror_group_link;
create table ai_insuror_group_link
(
	id			serial
	,groupid		int
	,rowid		varchar (60) /* Rowid from insuror table */
	, primary key (groupid, rowid)
);

drop table ai_kpi_group;
create table ai_kpi_group
(
	id			serial
	,code		varchar (6)
	,name		varchar (30)
);
alter table ai_kpi_group alter column id drop not null;
create unique index ai_kpi_groupi1 on ai_kpi_group (name);
create unique index ai_kpi_groupi2 on ai_kpi_group (code);

insert into ai_kpi_group (id, code, name) values (null, 'NONE', 'Unassigned');
insert into ai_kpi_group (id, code, name) values (-1, 'COM', 'Commercial');
insert into ai_kpi_group (id, code, name) values (-2, 'PER', 'Personal Lines');
insert into ai_kpi_group (id, code, name) values (-3, 'BOND', 'Bonds');
insert into ai_kpi_group (id, code, name) values (-4, 'BEN', 'Benefits');


create table ai_kpi_map
(
	office_id	int
	,level1org	varchar (36)
	,level2org	varchar (36)
	,kpi_group	int
)
;
create unique index ai_kpi_mapi on ai_kpi_map (office_id, level1org, level2org);


create table ai_kpi_map_coverage
(
	office_id	int
	,covcode	varchar (36)
	,kpi_group	int
)
;
create unique index ai_kpi_map_coveragei on ai_kpi_map_coverage (office_id, covcode);

create or replace function ai_current_year_start(int, boolean) returns date as $_AIFUNC_$
	DECLARE
	p_office_id		alias for $1;
	p_use_fiscal	alias for $2;
	l_result		date;
	l_fiscal_month	int;
	l_current_month	int;
	BEGIN
		if p_use_fiscal = false then
			return date_trunc ('year', now());
		end if;
		select fiscal_month into l_fiscal_month from ai_office where id = p_office_id;
		select date_part ('month', now()) into l_current_month;
		if l_current_month < l_fiscal_month then
			select date_trunc ('year', now()) - interval '1 year' + (interval '1 month' * (l_fiscal_month - 1)) into l_result;
		else
			select date_trunc ('year', now()) + (interval '1 month' * (l_fiscal_month - 1)) into l_result;
		end if;
		return l_result;
	END;
$_AIFUNC_$ LANGUAGE plpgsql;


create or replace function ai_current_year_end(int, boolean) returns date as $_AIFUNC_$
	DECLARE
	p_office_id		alias for $1;
	p_use_fiscal	alias for $2;
	l_result		date;
	l_fiscal_month	int;
	l_current_month	int;
	BEGIN
		if p_use_fiscal = false then
			return date_trunc ('year', now() + interval '1 year') - interval '1 day';
		end if;
		select fiscal_month into l_fiscal_month from ai_office where id = p_office_id;
		select date_part ('month', now()) into l_current_month;
		if l_current_month < l_fiscal_month then
			select date_trunc ('year', now()) - interval '1 year' + (interval '1 month' * (l_fiscal_month - 1)) into l_result;
		else
			select date_trunc ('year', now()) + (interval '1 month' * (l_fiscal_month - 1)) into l_result;
		end if;
		return (l_result + interval '1 year') - interval '1 day';
	END;
$_AIFUNC_$ LANGUAGE plpgsql;



/*
 * Add the following entry into pg_hba.conf for BIRT reporting.

####
#Duckdigit Local Network
host    ai         ai                   0/0        md5
####

 */
 
create user ai;
alter user ai with password '!*ai$';
grant select on
	ai_insuror_group
	,ai_insuror_group_link
	,ai_kpi_group
	,ai_kpi_map
	,ai_office
	,ai_office_group
	,ai_office_item
	,ai_office_type
to ai;


create table ai_office_security
(
	office_id			int,
	user_id				int,
	status				char(1), /* A=Access, D=Denied */
	filter				varchar (512)
)
;

create unique index ai_office_securityi1 on ai_office_security (office_id, user_id);
