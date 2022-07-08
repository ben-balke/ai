delete from cd_customer where office_id = {office_id};
insert into cd_level1org (




delete from cd_customer where office_id = {office_id};
insert into cd_customer (
		id
		,office_id
		,searchname
		,name1
		,name2
		,addr1
		,addr2
		,city
		,statecode
		,postalcode
		,producer1_id
		,servicer_id
		,level1org
		,level2org
)
select
		rec as id
		,{office_id} as office_id
		,rec as searchname
		,name as name1
		,attn as name2
		,street as addr1
		,street2 as addr2
		,city
		,st as statecode
		,zip as postalcode
		,'P_' || prod as producer1_id
		,'S_' || csr as servicer_id
		,agcy as level1org
		,brch as level2org
from {schema}.customer;

delete from cd_policy where office_id = {office_id};
insert into cd_policy (
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
		,state
		,siccode
		,sicmajor
		,producer_id
		,servicer_id
		,acctexec_id
		,effdate
		,expdate
		,book_start
		,book_end
		,policy_no
		,description
		,comments
		,named_insured
		,term
		,status
	,bill_method
)
select
		p.pol_idx as id
		, p.key_ins as customer_id
		, p.key_ins as cust_searchname
		, c.name as cust_name
		, {office_id} as office_id
		, p.agcy as level1org
		, p.brch as level2org
		, p.ico as insuror_id
		, p.bco as payee_id
		, p.type as coverage_id
		, p.prem as premium
		, p.co_amt as revenue
		, p.com_p as producer_comm
		, p.prem as written_premium
		, p.co_amt as writtem_revenue
		, p.com_p as written_producer_comm
		, c.st as state
		, null as siccode
		, null as sicmajor
		, 'P_' || case
				when trim (p.pr) != '' then p.pr
				else p.pr2
		  end
				as producer_id
		, 'S_' || p.csr as servicer_id
		, null as acctexec_id
		, p.eff as effdate
		, p.exp as expdate
		, p.eff as book_start
		, p.exp as book_end
		, p.pol as policy_no
		, p.rcu as description
		, p.sections as comments
		, null as named_insured
		, null as term
		, p.status as status
		, p.bill as bill_method
from {schema}.policy p
		left outer join {schema}.customer c on (c.rec = p.key_ins)
where p.key_ins != '.PICTUR' and p.pol_idx != '			' and p.pol_idx not like 'Z %'
;

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
		keyvalue as id
		, {office_id} as office_id
		, keyvalue as code
		, descrip as name
		, null as covtype
from {schema}.cov_types;

delete from cd_staff where office_id = {office_id};
insert into cd_staff
(
		id
		,office_id
		,code
		,name
)
select
		'P_' || rec as id
		, {office_id} as office_id
		,rec as code
		,name as name
from {schema}.producer;

insert into cd_staff
(
		id
		,office_id
		,code
		,name
)
select
		'S_' || keyvalue as id
		, {office_id} as office_id
		,keyvalue as code
		,descrip as name
from {schema}.csr;

/*
 * Insert any CSR' that are not defined.  In TAM you don't have to have
 * a csr in the database.  Sort of dumb i now.
 */
insert into cd_staff (
		id
		, office_id
		, code
		, name
) select
		distinct 'S_' || csr as id
		, {office_id} as office_id
		, csr as code
		, csr as name
from {schema}.policy
where csr not in (select keyvalue from {schema}.csr);





/**********************************
*** INSURORS
**********************************/
delete from cd_insuror where office_id = {office_id};
insert into cd_insuror
(
		id
		, office_id
		, code
		, name
		, payee_id
)
select rec as id
		, {office_id} as office_id
		, rec as code
		, name as name, broker as payee_id
from {schema}.company;

/**********************************
*** LEVEL1ORG
**********************************/
\echo Deleting Level1org
delete from public.cd_level1org where office_id = {office_id};
\echo Inserting Level1org
insert into public.cd_level1org (
    office_id
    , id
    , name
) select
    {office_id} as office_id
    , rec as id
    , name as name
from {schema}.agency
;

/**********************************
*** LEVEL2ORG
**********************************/
\echo Deleting Level2org
delete from public.cd_level2org where office_id = {office_id};
\echo Inserting Level1org
insert into public.cd_level2org (
    office_id
    , id
    , name
	, level1org
) select
    {office_id} as office_id
    , substring (rec,2) as id
    , name as name
	, substring (rec, 1, 1) as level1org
from {schema}.branch
;


/**********************************
*** TRANSACTIONS
**********************************/
\echo Deleting transactions
delete from public.cd_transaction where office_id = {office_id};
\echo Inserting transactions for New and Renewal Business
insert into public.cd_transaction (
	office_id
	,trandate
	,level1org
	,level2org
	,insuror_id
	,customer_id
	,policy_id
	,coverage_id
	,payee_id
	,trantype
	,premium
	,revenue
	,sicmajor
	,producer_id
	,servicer_id
	,state
)
select 
	{office_id} as office_id
	, t.edate as trandate
	, t.agcy as level1org
	, t.brch as level2org
	, trim(t.ico) as insuror_id
	, t.key_ins as customer_id
	, p.pol_idx as policy_id
	, p.type as coverage_id
	, trim(p.bco) as payee_id
	, case when t.trans = 'NEW' then 'N' when t.trans = 'REN' then 'R' end as trantype
    , t.amt as premium
    , t.co_amt as revenue
    , null as sicmajor
    , trim(t.pr) as producer_id
    , trim(t.csr) as servicer_id
	, c.st as state
from {schema}.transact t
	left outer join {schema}.policy p on (p.pol_idx = t.pol_idx)
	left outer join {schema}.customer c on (c.rec = t.key_ins)
where t.trans = 'NEW' or t.trans = 'REN';

\echo Inserting transactions for Cancelled and Lapsed business
\echo ======= END import_sagitta.sql ============

