<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/htdocs/kpi/querybyprod.php,v 1.3 2012/08/28 21:14:33 secwind Exp $
*************************************************************************/
require_once 'ai/reportpage.php';

/************************************
** SQL
************************************/
$select = <<<SQL
select
    s.id as itemid
    , s.code as itemcode
    , s.name as itemname
    , sum (p.premium) as premium
    , sum (p.revenue) as revenue
	, sum (p.revenue) / sum (p.premium) * 100 as commission
    , count (*) as polcount
	, count (distinct customer_id) as custcount
from
    cd_transaction p
    left outer join cd_staff s on (s.office_id = {user.active_office_id} and s.id = p.producer_id)
where
    p.office_id = {user.active_office_id}
	and {SQL:kpi.kpi_clause}
	and p.trandate >= {kpi.begindate}
	and p.trandate <= {kpi.enddate}
    and p.premium is not null and p.premium != 0
	{SQL:page.user_filter}
group by itemid, itemcode, itemname
order by {SQL:user.sort_byprod};
SQL;

/************************************
** PAGE AND AUTHENTICATION
************************************/
$page = new REPORT_page ("page", "Index");
$page->makeField (array ('name'=>'caption', 'default'=>'{dd:kpi.kpi_desc} by Producer'));
$page->makeField (array ('name'=>'codetitle', 'default'=>'Code'));
$page->makeField (array ('name'=>'desctitle', 'default'=>'Producer'));
$page->makeField (array ('name'=>'link', 'default'=>'kpi/queryforprod.php?daterange={dd:http.daterange}&kpi_type={dd:http.kpi_type}'));
$page->makeField (array ('name'=>'sort_property_name', 'default'=>'sort_byprod'));
$page->makeField (array ('name'=>'xmllink', 'default'=>'kpi/querybyprod.php/{dd:http.kpi_type}_producers.xml?xml=true&daterange={dd:http.daterange}&kpi_type={dd:http.kpi_type}'));
$page->makeField (array ('name'=>'csvlink', 'default'=>'kpi/querybyprod.php/{dd:http.kpi_type}_producers.csv?csv=true&daterange={dd:http.daterange}&kpi_type={dd:http.kpi_type}'));
$page->makeField (array ('name'=>'list_filter', 'default'=>'daterange={dd:http.daterange}&kpi_type={dd:http.kpi_type}&producer_id'));
$page->makeField (array ('name'=>'total_filter', 'default'=>'daterange={dd:http.daterange}&kpi_type={dd:http.kpi_type}'));
if (isset ($_GET ['xml']))
	$page->renderXML (array ('select'=>$select, 'xmlrecord'=>'coverage'));
else if (isset ($_GET ['csv']))
	$page->renderCSV (array ('select'=>$select, 'csvrecord'=>'coverage'));
else
	$page->render (array ('select'=>$select));
?>
