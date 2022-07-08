/*
 * Copyright (c) DuckDigit Technologies, Inc.  2009 ALL RIGHTS RESERVED
 * $Header: /home/cvsroot/ai/sql/pt.sql,v 1.2 2010/10/18 19:04:36 secwind Exp $
 * Description:
 *      Common Data Format for Agency Insight.  All of the agency management systems convert to this format.
 */

insert into cd_policy (
    id
    , customer_id
    , office_id
    , policy_no
    , insuror_id
    , coverage_id
    , premium
    , revenue
    , producer_comm
	, written_premium
	, written_revenue
	, written_producer_comm
    , state
    , siccode
    , producer_id
    , servicer_id
	, level1org
	, level2org
    , expdate
    , effdate

	,description
	,comments
	,named_insured
	,term
	,status
	,bill_method
)
select 
	p.rowid as id
    , p.clientcode as customer_id
    , 1 as office_id
    , policyno as policy_no
	, p.insuror as insuror_id
    , p.coverages as coverage_id
    , p.ann_prem1 [1] as premium
    , p.ann_agencycomm [1] as revenue
    , p.ann_prodcomm as producer_comm
    , p.written_prem [1] as written_premium
    , p.wr_agencycomm [1] as written_revenue
    , p.wr_prodcomm as written_producer_comm
    , upper (p.state)
    , p.siccode
    , p.producer [1] as producer_id
    , p.servicer as servicer_id
    , c.division as level1org
    , p.department as level1org
    , p.expdate
    , p.effdate

	,description 
	,array_to_string(remarks,E'\n') as comments
	,named_insured
	,term
	,'Need' as status
	,billing_method as bill_method
from policies p left outer join clients c on (c.rowid = p.clientcode)
;

