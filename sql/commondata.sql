/*
 * Copyright (c) DuckDigit Technologies, Inc.  2009 ALL RIGHTS RESERVED
 * $Header: /home/cvsroot/ai/sql/commondata.sql,v 1.5 2010/10/20 05:48:23 secwind Exp $
 * Description:
 *		Common Data Format for Agency Insight.  All of the agency management systems convert to this format.
 */
drop table cd_customer;
create table cd_customer
(
	id				varchar (36)
	,office_id		int
	,searchname		varchar (36)
	,name1			varchar (80)
	,name2			varchar (80)
	,addr1			varchar (80)
	,addr2			varchar (80)
	,city			varchar (80)
	,statecode		varchar (2)
	,siccode		varchar (10)
	,sicmajor		varchar (2)	/* major catagory */
	,postalcode		varchar (80)
	,producer1_id	varchar (36) /* cd_staff */
	,producer2_id	varchar (36) /* cd_staff */
	,acctexec1_id	varchar (36) /* cd_staff */
	,acctexec2_id	varchar (36) /* cd_staff */
	,servicer_id	varchar (36) /* cd_staff */
	,level1org		varchar (36) 
	,level2org		varchar (36) 
	,billto			varchar (36) /* cd_customer */
);


drop table cd_coverage;
create table cd_coverage
(
	id				varchar (36)
	,office_id		int
	,code			varchar (10)
	,name			varchar (128)
	,covtype			varchar (10)
)
;

drop table cd_insuror;
create table cd_insuror
(
	id				varchar (36)
	,office_id		int
	,code			varchar (10)
	,name			varchar (128)
	,payee_id		varchar (36)
)
;

drop table cd_staff;
create table cd_staff
(
	id				varchar (36)
	,office_id		int
	,name			varchar (60)
	,code			varchar (10)
	,stafftype		char (1)

)
;

drop table cd_policy;
create table cd_policy
(
	id						varchar (36)
	,customer_id			varchar (36)
	,cust_searchname		varchar (36) /* Avoid the join */
	,cust_name1				varchar (80) /* Avoid the join */
	,office_id				int
	,level1org				varchar (36) 
	,level2org				varchar (36) 
	,insuror_id				varchar (36)
	,payee_id				varchar (36) 
	,coverage_id			varchar (36)
	,premium				float
	,revenue				float
	,producer_comm			float
	,written_premium		float
	,written_revenue		float
	,written_producer_comm	float
	,booked_premium			float
	,booked_revenue			float
	,booked_producer_comm	float
	,state					varchar (2)
	,siccode				varchar (10)
	,sicmajor				varchar (2)
	,producer_id			varchar (36) /* cd_staff */
	,acctexec_id			varchar (36) /* cd_staff */
	,servicer_id			varchar (36) /* cd_staff */
	,expdate				date
	,effdate				date
	,cancel_date			date
	,book_start				date /* The Starting date of the policies Book Range.  How long the premium applies to Book */
	,book_end				date /* The Ending of the Book Date.  How long the premium applies to Book */
								 /* Example bonds only last a year */
	,policy_no				varchar (50)
	,description			varchar
	,comments				text
	,named_insured			varchar(200)
	,term					varchar (40)
	,status					varchar (36) /* I)nactive, A)Active */
	,business				varchar (36) /* N)EW or R)ENEWAL */
	,bill_method			varchar (36)
	,kpi_group				int /* Key Performance Indicator Grouping ai_kpi_group */
)
;


drop table cd_transaction;
create table cd_transaction
(
	office_id		int
	,trandate		date
	,level1org		varchar (36) 
	,level2org		varchar (36) 
	,insuror_id		varchar (36)
	,customer_id	varchar (36)
	,policy_id		varchar (36)
	,coverage_id	varchar (36)
	,payee_id		varchar (36)
	,producer_id	varchar (36)
	,servicer_id	varchar (36)
	,sicmajor		varchar (36)
	,state			varchar (2)
	,trantype		char (1) /* N=New Business, R=Renewal, L=Lost Business */
	,premium		float
	,revenue		float
	,kpi_group		int
)
;


drop table cd_state;
create table cd_state
(
	id				varchar (2)
	,code			varchar (2)
	,name			varchar (60)
	,country		varchar (60)
)
;

drop table cd_level1org;
create table cd_level1org
(
	office_id	int
	, id			varchar (36)
	, name		varchar (128)
)
;

create unique index cd_level1orgi on cd_level1org (office_id, id);

drop table cd_level2org;
create table cd_level2org
(
	office_id	int
	, id		varchar (36)
	, level1org	varchar (36)
	, name		varchar (128)
)
;
create unique index cd_level2orgi on cd_level2org (office_id, level1org, id);


/*
 * KPI Month is a summary table used to stage the premium and revenue
 * for each kpi/tran type for each month.
 */
drop table cd_kpi_month;
create table cd_kpi_month
(
	office_id	int
	,tranmonth	date
	,kpi_group	int
	,trantype	char(1)
	,premium	float
	,revenue	float
)
;


grant select on 
	cd_coverage
	,cd_customer
	,cd_insuror
	,cd_kpi_month
	,cd_level1org
	,cd_level2org
	,cd_month
	,cd_policy
	,cd_siccode
	,cd_sicmajor
	,cd_staff
	,cd_state
	,cd_trangroup
	,cd_transaction
	,cd_trantype
to ai;



