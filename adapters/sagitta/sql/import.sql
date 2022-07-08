\echo ======= BEGIN post_adapter.sql ============
\echo ======= Copyright (c) 2009 DuckDigit Technologies, Inc. ALL RIGHTS RESERVED ===
\timing

/**********************************
*** CUSTOMERS
**********************************/
\echo Removing Customers
delete from public.cd_customer where office_id = {office_id};
\echo Inserting Customers
insert into public.cd_customer (
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
	, {office_id} as office_id
	, clientcode as searchname
	, clientname as name1
	, null as name2
	, address1 as addr1
	, address2 as addr2
	, city
	, upper (state) as statecode
	, zipcode as postalcode
	, siccode[1] as siccode
	, substring(siccode[1],1,2) as sicmajor
	, producer [1]
	, producer[2]
	, null
	, null
	, servicer [1] as servicer_id 
	, case when division = '' then '1' when division is null then '1' else division end as division
	, billto
from {schema}.clients;

/**********************************
*** POLICIES
**********************************/
/**
 * Sums an the premiums table transations for calculating book values.  This is used for premium, revenue,
 * and commission.  
 */
create or replace function ai_sag_get_invoiced_values(varchar [], varchar[], float[]) returns float as $_AIFUNC_$
    DECLARE
    trns    	alias for $1;
    invoices    alias for $2;
    vals    	alias for $3;
	i			integer;
	total		float;
    BEGIN
		if invoices is not null and vals is not null then
			total := 0.0;
			for i in array_lower(invoices,1)..array_upper(invoices,1) loop
				/**
				 * These are the transactions we are interested in...and only if there
				 * is an invoice number because then it is billed.
				 */
				if trns [i] in ('NEW','CAN','NRW','END','COM','COR','INS') then
					if invoices [i] != '' then
						total := total + vals [i]; 
					end if;
				end if;
			end loop;
		end if;
		return total;
    END;
$_AIFUNC_$ LANGUAGE plpgsql;

/**
 * Sums an array not sure if this is faster than a plpgsql function
 */
create or replace function sum_array(anyarray)
returns anyelement as $$
select sum($1[i])
   from generate_series(array_lower($1,1),
   array_upper($1,1)) g(i);
$$ language sql immutable;
/*
 * Clean up the premiums table before we start.  Create a unique index on the pol_seq_nr.
 */
\echo Clean up Sagitta premiums table
delete from {schema}.premiums where invoice_nr is null or trn is null or trans_amnt is null;
delete from {schema}.premiums where rowid = 'PRINT';
create unique index premiums_poli on {schema}.premiums (pol_seq_nr);

\echo Deleting policies
delete from public.cd_policy where office_id = {office_id};
/*
Playing with performance options.  Not much help.
set enable_sort=off;
set enable_seqscan=off;*/
\echo Inserting Policies
insert into public.cd_policy (
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
	, booked_premium
	, booked_revenue
	, booked_producer_comm
    , state
    , siccode
    , sicmajor
    , producer_id
    , servicer_id
	, level1org
	, level2org
    , expdate
    , effdate
    , cancel_date
    , book_start
    , book_end
	, description
	, comments
	, named_insured
	, term
	, status
	, business
	, bill_method
	, kpi_group
)
select 
	p.rowid as id
    , p.clientcode as customer_id
    , c.clientcode as cust_searchname
    , c.clientname as cust_name1
    , {office_id} as office_id
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
    , ai_sag_get_invoiced_values(t.trn, t.invoice_nr, t.trans_amnt) as booked_premium
    , ai_sag_get_invoiced_values(t.trn, t.invoice_nr, t.agcy_comm) as booked_revenue
    , ai_sag_get_invoiced_values(t.trn, t.invoice_nr, t.prod_comm) as booked_producer_comm
    , upper (p.state)
    , p.siccode
    , substring(p.siccode,1,2) as sicmajor
    , p.producer [1] as producer_id
    , p.servicer as servicer_id
    , case when c.division = '' then '1' when c.division is null then '1' else c.division end as level1org
    , p.department as level2org
    , p.expdate
    , p.effdate
    , p.cancel_date as cancel_date
    , p.effdate as book_start
    , case when p.cancel_date is not null and p.cancel_date < p.expdate then p.cancel_date else p.expdate end as book_end
	, description 
	, array_to_string(remarks,E'\r\n') as comments
	, named_insured
	, term
    , case when ( /* See Spport Ticket 3-29 */
                (p.c_n_r_w = '' or p.c_n_r_w is null) or
                (p.c_n_r_w = 'R'
                        and (p.effdate <= date_trunc('day',now()) 
                        and p.expdate >= date_trunc('day',now()))) or
                (p.c_n_r_w = 'C' and p.cancel_date >= date_trunc('day',now()))
                 )
        then 'A' else 'I' end as status
	, case when new_ren = 'REN' then 'R' when new_ren = 'NEW' then 'N' else null end as business
	, billing_method as bill_method
	, null as kpi_group
from {schema}.policies p 
	left outer join {schema}.premiums t on (t.pol_seq_nr = p.rowid)
	left outer join {schema}.clients c on (c.rowid = p.clientcode)
	left outer join {schema}.insurors i on (i.insurorcode = p.payee)
;
/*set enable_sort=on;
set enable_seqscan=on;*/
/**********************************
*** INSURORS
**********************************/
\echo Deleting Insurors
delete from public.cd_insuror where office_id = {office_id};
\echo Inserting Insurors
insert into public.cd_insuror (
	id
	, office_id
	, code
	, name
) select 
	rowid, {office_id}
	, insurorcode
	, name 
from {schema}.insurors;

/**********************************
*** COVERAGES
**********************************/
\echo Deleting Coverages
delete from public.cd_coverage where office_id = {office_id};
\echo Inserting Coverages
insert into public.cd_coverage (
	id
	, office_id
	, code
	, name
	, covtype
) select 
	rowid
	, {office_id}
	, covcode
	, description
	, covtype 
from {schema}.coverages;

/**********************************
*** STAFF
**********************************/
\echo Deleting Staff
delete from public.cd_staff where office_id = {office_id};
\echo Inserting Staff
insert into public.cd_staff (
	id
	, office_id
	, name
	, code
	, stafftype
) select 
	rowid as id
	, {office_id} as office_id
	, name
	, rowid as code
	, null as stafftype 
from {schema}.staff;


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
	, div_nr as id
	, div_name as name
from {schema}.company_master
;

/**********************************
*** LEVEL2ORG
**********************************/
/* We create a function to talk the department arrays and create a level2org record for each
 * element.
 */
\echo Deleting Level2org 
delete from public.cd_level2org where office_id = {office_id};
\echo Inserting Level2org 
create or replace function ai_sag_departments(integer) returns integer as $_AIFUNC_$
    DECLARE
    p_office_id  	alias for $1;
	i			integer;
	total		integer;
	rec			RECORD;
    BEGIN
		total := 0;
		FOR rec in select div_nr, department, department_name from {schema}.company_master LOOP
			if rec.department is not null and rec.department_name is not null then
				for i in array_lower(rec.department,1)..array_upper(rec.department,1) loop
					BEGIN
						insert into public.cd_level2org (office_id, level1org, id, name) values
							(p_office_id, rec.div_nr, rec.department [i], rec.department_name [i]);
						total := total + 1;
        			EXCEPTION WHEN unique_violation THEN
		            	-- do nothing
					END;
				end loop;
			end if;
		end loop;
		return total;
    END;
$_AIFUNC_$ LANGUAGE plpgsql;

select ai_sag_departments({office_id});


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
	,kpi_group
	,sicmajor
	,producer_id
	,servicer_id
	,state
)
select 
	{office_id} as office_id
	, effdate as trandate
    , case when c.division = '' then '1' when c.division is null then '1' else c.division end as level1org
    , p.department as level2org
	, p.insuror as insuror_id
	, p.clientcode as customer_id
	, p.rowid as policy_id
	, p.coverages as coverage_id
	, COALESCE (i.rowid, p.insuror) as payee_id
	, case when new_ren = 'NEW' then 'N' when new_ren = 'REN' then 'R' end as trantype
    , COALESCE (sum_array(p.ann_prem1),0) as premium
    , COALESCE (sum_array(p.ann_agencycomm),0) as revenue
	, null as kpi_group
    , substring(p.siccode,1,2) as sicmajor
    , p.producer [1] as producer_id
    , p.servicer as servicer_id
    , upper (p.state)
from {schema}.policies p
	left outer join {schema}.clients c on (c.rowid = p.clientcode)
	left outer join {schema}.insurors i on (i.insurorcode = p.payee)
where p.new_ren = 'NEW' or p.new_ren = 'REN';



\echo Inserting transactions for Cancelled and Lapsed business
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
	,kpi_group
	,sicmajor
	,producer_id
	,servicer_id
	,state
)
select 
	{office_id} as office_id
	, cancel_date as trandate
    , case when c.division = '' then '1' when c.division is null then '1' else c.division end as level1org
    , p.department as level2org
	, p.insuror as insuror_id
	, p.clientcode as customer_id
	, p.rowid as policy_id
	, p.coverages as coverage_id
	, COALESCE (i.rowid, p.insuror) as payee_id
	, case when transaction = 'CAN' then 'C' when transaction = 'NRW' then 'L' end as trantype
    , COALESCE (sum_array(p.ann_prem1),0) as premium
    , COALESCE (sum_array(p.ann_agencycomm),0) as revenue
	, null as kpi_group
    , substring(p.siccode,1,2) as sicmajor
    , p.producer [1] as producer_id
    , p.servicer as servicer_id
    , upper (p.state)
from {schema}.policies p
	left outer join {schema}.clients c on (c.rowid = p.clientcode)
	left outer join {schema}.insurors i on (i.insurorcode = p.payee)
where (p.transaction = 'CAN' or p.transaction = 'NRW') and cancel_reason != '' and cancel_reason is not null;

\echo ======= END import_sagitta.sql ============
