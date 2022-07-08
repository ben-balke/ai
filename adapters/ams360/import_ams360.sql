/**********************************
*** CUSTOMERS
**********************************/
delete from cd_customer where office_id = 1;
insert into cd_customer (
	id
	, office_id
	, searchname
	, name1
	, name2
	, addr1
	, addr2
	, city
	, statecode
	, postalcode
	, siccode
	, sicmajor
	, producer1_id
	, producer2_id
	, acctexec1_id
	, acctexec2_id
	, servicer_id
	, level1org
	, billto

) select 
	rowid as id
	, 1 as office_id
	, clientcode as searchname
	, clientname as name1
	, null as name2
	, address1 as addr1
	, address2 as addr2
	, city
	, state as statecode
	, zipcode as postalcode
	, siccode[1] as siccode
	, substring(siccode[1],1,2) as sicmajor
	, producer [1]
	, producer[2]
	, null
	, null
	, servicer [1] as servicer_id 
	, division
	, billto

from clients;

create or replace function sum_array(anyarray)
returns anyelement as $$
select sum($1[i])
   from generate_series(array_lower($1,1),
   array_upper($1,1)) g(i);
$$ language sql immutable;

/**********************************
*** POLICIES
**********************************/
delete from cd_policy where office_id = 1;
insert into cd_policy (
    id
    , customer_id
    , cust_searchname
    , cust_name1
    , office_id
    , policy_no
    , insuror_id
    , payee_id
    , coverage_id
    , premium
    , revenue
    , producer_comm
	, written_premium
	, written_revenue
	, written_producer_comm
    , state
    , siccode
    , sicmajor
    , producer_id
    , servicer_id
	, level1org
	, level2org
    , expdate
    , effdate
	, description
	, comments
	, named_insured
	, term
	, status
	, bill_method
)
select 
	p.rowid as id
    , p.clientcode as customer_id
    , c.clientcode as cust_searchname
    , c.clientname as cust_name1
    , 1 as office_id
    , policyno as policy_no
	, p.insuror as insuror_id
	, COALESCE (i.rowid, p.insuror) as payee_id
    , p.coverages as coverage_id
    , COALESCE (sum_array(p.ann_prem1),0) as premium
    , COALESCE (sum_array(p.ann_agencycomm),0) as revenue
    , COALESCE (p.ann_prodcomm,0) as producer_comm
    , COALESCE (sum_array(p.written_prem),0) as written_premium
    , COALESCE (sum_array(p.wr_agencycomm),0) as written_revenue
    , COALESCE (p.wr_prodcomm,0) as written_producer_comm
    , upper (p.state)
    , p.siccode
    , substring(p.siccode,1,2) as sicmajor
    , p.producer [1] as producer_id
    , p.servicer as servicer_id
    , c.division as level1org
    , p.department as level1org
    , p.expdate
    , p.effdate
	, description 
	, array_to_string(remarks,E'\r\n') as comments
	, named_insured
	, term
	, 'Need' as status
	, billing_method as bill_method
from policies p 
	left outer join clients c on (c.rowid = p.clientcode)
	left outer join insurors i on (i.insurorcode = p.payee)
;

/**********************************
*** INSURORS
**********************************/
delete from cd_insuror where office_id = 1;
insert into cd_insuror (
	id
	, office_id
	, code
	, name
) select 
	rowid, 1
	, insurorcode
	, name 
from insurors;

delete from cd_coverage where office_id = 1;
insert into cd_coverage (
	id
	, office_id
	, code
	, name
	, covtype
) select 
	rowid
	, 1
	, covcode
	, description
	, covtype 
from coverages;

insert into cd_staff (
	id
	, office_id
	, name
	, code
	, stafftype
) select 
	rowid as id
	, 1 as office_id
	, name
	, rowid as code
	, null as stafftype 
from staff;

