/*
 * Copyright (c) DuckDigit Technologies, Inc.  2009 ALL RIGHTS RESERVED
 * $Header: /home/cvsroot/ai/sql/copy_office.sql,v 1.3 2010/10/20 05:46:14 secwind Exp $
 * Description:
 *      Copy an office from one database to another.  Filter is parsed in for customer, policies, and transactions.
 */

delete from cd_customer where office_id = {office_id};
insert into cd_customer
(
	id
	,office_id
	,searchname
	,name1
	,name2
	,addr1
	,addr2
	,city
	,statecode
	,siccode
	,sicmajor
	,postalcode
	,producer1_id
	,producer2_id
	,acctexec1_id
	,acctexec2_id
	,servicer_id
	,level1org
	,level2org
	,billto
)
select
	id
	,{office_id}
	,searchname
	,name1
	,name2
	,addr1
	,addr2
	,city
	,statecode
	,siccode
	,sicmajor
	,postalcode
	,producer1_id
	,producer2_id
	,acctexec1_id
	,acctexec2_id
	,servicer_id
	,level1org
	,level2org
	,billto
from 
	cd_customer c 
where 
	office_id = {from_office_id} and 
	id in (select distinct customer_id from cd_policy p where p.office_id = {from_office_id} 
		and ({whereclause}));
	

delete from cd_coverage where office_id = {office_id};
insert into cd_coverage
(
	id
	,office_id
	,code
	,name
	,covtype
)
select
	id
	,{office_id}
	,code
	,name
	,covtype
from
	cd_coverage where office_id = {from_office_id};

delete from cd_insuror where office_id = {office_id};
insert into cd_insuror
(
	id
	,office_id
	,code
	,name
	,payee_id
)
select
	id
	,{office_id}
	,code
	,name
	,payee_id
from
	cd_insuror where office_id = {from_office_id};

delete from cd_staff where office_id = {office_id};
insert into cd_staff
(
	id
	,office_id
	,code
	,name
	,stafftype
)
select
	id
	,{office_id}
	,code
	,name
	,stafftype
from
	cd_staff where office_id = {from_office_id};

delete from cd_policy where office_id = {office_id};
insert into cd_policy
(
	id
    ,customer_id
    ,cust_searchname
    ,cust_name1
    ,office_id
    ,level1org
    ,level2org
    ,insuror_id
    ,payee_id
    ,coverage_id
    ,premium
    ,revenue
    ,producer_comm
    ,written_premium
    ,written_revenue
    ,written_producer_comm
    ,booked_premium
    ,booked_revenue
    ,booked_producer_comm
    ,state
    ,siccode
    ,sicmajor
    ,producer_id
    ,acctexec_id
    ,servicer_id
    ,expdate
    ,effdate
    ,cancel_date
    ,book_start
    ,book_end
    ,policy_no
    ,description
    ,comments
    ,named_insured
    ,term
    ,status
    ,business
    ,bill_method
)
select
	id
    ,customer_id
    ,cust_searchname
    ,cust_name1
	,{office_id}
    ,level1org
    ,level2org
    ,insuror_id
    ,payee_id
    ,coverage_id
    ,premium
    ,revenue
    ,producer_comm
    ,written_premium
    ,written_revenue
    ,written_producer_comm
    ,booked_premium
    ,booked_revenue
    ,booked_producer_comm
    ,state
    ,siccode
    ,sicmajor
    ,producer_id
    ,acctexec_id
    ,servicer_id
    ,expdate
    ,effdate
    ,cancel_date
    ,book_start
    ,book_end
    ,policy_no
    ,description
    ,comments
    ,named_insured
    ,term
    ,status
    ,business
    ,bill_method
from
	cd_policy where office_id = {from_office_id} and {whereclause};


delete from cd_transaction where office_id = {office_id};
insert into cd_transaction
(
	office_id
	,trandate
	,level1org
	,level2org
	,insuror_id
	,customer_id
	,policy_id
	,coverage_id
	,payee_id
	,producer_id
	,servicer_id
	,sicmajor
	,state
	,trantype
	,premium
	,revenue
)
select 
	{office_id}
	,trandate
	,level1org
	,level2org
	,insuror_id
	,customer_id
	,policy_id
	,coverage_id
	,payee_id
	,producer_id
	,servicer_id
	,sicmajor
	,state
	,trantype
	,premium
	,revenue
from
	cd_transaction where office_id = {from_office_id} and {whereclause};


delete from cd_level1org where office_id = {office_id};
insert into cd_level1org
(
    office_id
    , id
    , name
)
select 
    {office_id}
    , id
    , name
from
	cd_level1org where office_id = {from_office_id};

delete from cd_level2org where office_id = {office_id};
insert into cd_level2org
(
    office_id
    , id
    , level1org
    , name
)
select
    {office_id}
    , id
    , level1org
    , name
from
	cd_level2org where office_id = {from_office_id};
