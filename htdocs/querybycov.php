<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/htdocs/querybycov.php,v 1.4 2012/08/28 21:13:55 secwind Exp $
*************************************************************************/
require_once 'ai/reportpage.php';

/************************************
** SQL
************************************/
$select=<<<SQL
select
	c.id as itemid
	, c.code as itemcode
	, c.name as itemname
	, sum (p.premium) as premium
	, sum (p.revenue) as revenue
	, sum (p.revenue) / sum (p.premium) * 100 as commission
	, count (*) as polcount
	, count (distinct customer_id) as custcount
from
	cd_policy p
	left outer join cd_coverage c on (c.office_id = {user.active_office_id} and c.id = p.coverage_id)
	left outer join cd_insuror i on (i.office_id = {user.active_office_id} and i.id = p.insuror_id)
where
	p.office_id = {user.active_office_id}
	and p.book_end > date_trunc('day',now())
	and p.book_start <= date_trunc('day',now())
	and p.premium is not null and p.premium != 0
	{SQL:page.user_filter}
group by itemid, itemcode, itemname
order by {SQL:user.sort_bycov};
SQL;

/************************************
** PAGE AND AUTHENTICATION
************************************/
$page = new REPORT_page ("page", "Index");
$page->makeField (array ('name'=>'caption', 'default'=>'Book by Coverage'));
$page->makeField (array ('name'=>'codetitle', 'default'=>'Code'));
$page->makeField (array ('name'=>'desctitle', 'default'=>'Coverage'));
$page->makeField (array ('name'=>'link', 'default'=>'queryforcov.php'));
$page->makeField (array ('name'=>'sort_property_name', 'default'=>'sort_bycov'));
$page->makeField (array ('name'=>'xmllink', 'default'=>'querybycov.php/coverages.xml?xml=true'));
$page->makeField (array ('name'=>'csvlink', 'default'=>'querybycov.php/coverages.csv?csv=true'));
$page->makeField (array ('name'=>'list_filter', 'type'=>'work','value'=>'coverage_id'));
//$page->makeField (array ('name'=>'total_filter', 'type'=>'work','value'=>'coverage_id'));

if (isset ($_GET ['xml']))
	$page->renderXML (array ('select'=>$select, 'xmlrecord'=>'coverage'));
else if (isset ($_GET ['csv']))
	$page->renderCSV (array ('select'=>$select, 'csvrecord'=>'coverage'));
else
	$page->render (array ('select'=>$select));
?>
