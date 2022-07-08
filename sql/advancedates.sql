/* 
 * Copyright (c) DuckDigit Technologies, Inc.  2009 ALL RIGHTS RESERVED 
 * $Header: /home/cvsroot/ai/sql/advancedates.sql,v 1.3 2012/08/28 21:09:50 secwind Exp $
 */
/* KPI Month Buckets */
drop index cd_kpi_monthi;
update cd_kpi_month set tranmonth = tranmonth + interval '1 month';
create unique index cd_kpi_monthi on cd_kpi_month (office_id ,tranmonth ,kpi_group ,trantype);


update cd_policy set 
	expdate = expdate + interval '1 month'
	,effdate = effdate + interval '1 month'
	,cancel_date = cancel_date + interval '1 month'
	,book_start = book_start + interval '1 month'
	,book_end = book_end + interval '1 month';
update cd_transaction set trandate = trandate + interval '1 month';
