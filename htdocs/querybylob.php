<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/htdocs/querybylob.php,v 1.4 2012/08/28 21:13:55 secwind Exp $
*************************************************************************/
require_once 'ai/reportpage.php';

/************************************
** SQL
************************************/
$select = <<<SQL
select
	i.id as itemid
	, i.code as itemcode
	, i.name as itemname
	, sum (p.premium) as premium
	, sum (p.revenue) as revenue
	, sum (p.revenue) / sum (p.premium) * 100 as commission
	, count (*) as polcount
	, count (distinct customer_id) as custcount
from
	cd_policy p
    left outer join ai_kpi_group i on (i.id = p.kpi_group)
where
    p.office_id = {user.active_office_id}
    and p.book_end > date_trunc('day',now())
    and p.book_start <= date_trunc('day',now())
    and p.premium is not null and p.premium != 0
	{SQL:page.user_filter}
group by itemid, itemcode, itemname
order by {SQL:user.sort_bylob};
SQL;

/************************************
** PAGE AND AUTHENTICATION
************************************/
$page = new REPORT_page ("page", "Index");
$page->makeField (array ('name'=>'caption', 'default'=>'Book by LOB'));
$page->makeField (array ('name'=>'codetitle', 'default'=>'Code'));
$page->makeField (array ('name'=>'desctitle', 'default'=>'Line of Business'));
$page->makeField (array ('name'=>'sort_property_name', 'default'=>'sort_bylob'));
$page->makeField (array ('name'=>'link', 'default'=>'queryforlob.php'));
$page->makeField (array ('name'=>'xmllink', 'default'=>'querybylob.php/lineofbusiness.xml?xml=true'));
$page->makeField (array ('name'=>'csvlink', 'default'=>'querybylob.php/lineofbusiness.csv?csv=true'));
$page->makeField (array ('name'=>'list_filter', 'type'=>'work','value'=>'kpi_group'));
//$page->makeField (array ('name'=>'total_filter', 'type'=>'work'));
if (isset ($_GET ['xml']))
    $page->renderXML (array ('select'=>$select, 'xmlrecord'=>'lineofbusiness'));
else if (isset ($_GET ['csv']))
    $page->renderCSV (array ('select'=>$select, 'csvrecord'=>'lineofbusiness'));
else
    $page->render (array ('select'=>$select));

?>
