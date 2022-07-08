/*
 * Copyright (c) DuckDigit Technologies, Inc.  2009 ALL RIGHTS RESERVED
 * $Header: /home/cvsroot/ai/sql/post_adapter.sql,v 1.5 2010/11/11 17:00:22 secwind Exp $
 * Description:
 * 	This script is executed after the adapter is performed.  
 */

create or replace function ai_set_kpi_groups_{office_id} () returns int as $_AIFUNC_$
    DECLARE
        l_kpi_type_id   varchar;
        l_office_id integer;
    BEGIN
        select {office_id} into l_office_id;

        select kpi_type_id into l_kpi_type_id from ai_office where id = l_office_id;
        if l_kpi_type_id = 'C' then
            update cd_policy p
                set kpi_group =
                    (select m.kpi_group from public.ai_kpi_map_coverage m where m.office_id = l_office_id and
                        m.coverage_id = p.coverage_id) where p.office_id = l_office_id;
            update cd_transaction t
                set kpi_group =
                    (select m.kpi_group from public.ai_kpi_map_coverage m where m.office_id = l_office_id and
                        m.coverage_id = t.coverage_id) where t.office_id = l_office_id;
        else
            update cd_policy p
                set kpi_group =
                    (select m.kpi_group from public.ai_kpi_map m where m.office_id = l_office_id and
                        m.level1org = p.level1org and
                        m.level2org = p.level2org) where p.office_id = l_office_id;
            update cd_transaction t
                set kpi_group =
                    (select m.kpi_group from public.ai_kpi_map m where m.office_id = l_office_id and
                        m.level1org = t.level1org and
                        m.level2org = t.level2org) where t.office_id = l_office_id;
        end if;
        return 1;
    END;
$_AIFUNC_$ LANGUAGE plpgsql;

select ai_set_kpi_groups_{office_id}();

\echo ======= BEGIN post_adapter.sql ============
\timing 
\echo Remving KPI Month buckets
delete from cd_kpi_month where office_id = {office_id};
\echo Generating KPI Month buckets
insert into cd_kpi_month 
(
	office_id
	, tranmonth
	, kpi_group
	, trantype
	, premium
	, revenue
)
select distinct 
	office_id
	, date_trunc ('month', trandate) as tranmonth
	, kpi_group
	, trantype
	, sum (premium) as premium
	, sum(revenue) as revenue
from cd_transaction 
	where office_id = {office_id} and premium is not null  and premium != 0 and trandate <= date_trunc ('day', now())
group by office_id, tranmonth, kpi_group, trantype;
\echo ======= END post_adapter.sql ============
