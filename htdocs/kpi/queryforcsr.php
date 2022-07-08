<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/htdocs/kpi/queryforcsr.php,v 1.3 2012/08/28 21:14:33 secwind Exp $
*************************************************************************/
require_once 'ai/tabpage.php';

/************************************
** PAGE
************************************/
$page = new TAB_page ("page", "Index", "tab_forcsr");

/************************************
** SQL
************************************/
$select = "
select " .
    $page->get('itemid_column') ." as itemid
    , " . $page->get ('code_column') . " as itemcode
    , " . $page->get ('name_column') . " as itemname
    , sum (p.premium) as premium
    , sum (p.revenue) as revenue
	, sum (p.revenue) / sum (p.premium) * 100 as commission
	,count(*) as polcount
	, count (distinct customer_id) as custcount
from
    cd_transaction p "
	. $page->get ('join') .  "
where
    p.office_id = {user.active_office_id}
    and {SQL:kpi.kpi_clause}
	and p.trandate >= {kpi.begindate}
	and p.trandate <= {kpi.enddate}
    and p.servicer_id = {http.id}
    and p.premium is not null and p.premium != 0
	{SQL:page.user_filter}
group by itemid, itemcode, itemname
order by {SQL:user.sort_forcsr};
";


$selecthdr = <<<SQL
    select 'Servicer: ' || name as name, name as itemname,
		(select shortname from ai_kpi_export_prefix_map where id = {http.kpi_type}) as kpi_prefix
	from cd_staff where office_id = {user.active_office_id} and id = {http.id}
SQL;

/************************************
** RECORDHANDLERS
************************************/
$rhhdr = new DD_SqlRecordHandler (array (
    'name'=>'hdr'
    ,'connect'=>'sql.ai'
    ,'select'=>$selecthdr
    ,'autofields'=>true
));
$description = $rhhdr->makeField (array ('name'=>'name'));
$page->addRecordHandler ($rhhdr);
$rhhdr->prepareForRecord ($page->m_tp);
$rhhdr->query ($page->m_tp);
$rhhdr->nextRecord ();
$rhhdr->freeQuery ();

$page->makeField (array ('name'=>'caption', 'default'=>$description->getValue ()));
$page->makeField (array ('name'=>'codetitle', 'default'=>'Code'));
$page->makeField (array ('name'=>'desctitle', 'default'=>$page->get ('tabname')));
$page->makeField (array ('name'=>'link', 'default'=>'kpi/'.$page->get ('link').'?daterange={dd:http.daterange}&kpi_type={dd:http.kpi_type}'));
$page->makeField (array ('name'=>'sort_property_name', 'default'=>'sort_forcsr'));

$page->makeField (array ('name'=>'xmllink', 'default'=>'kpi/queryforcsr.php/{dd:hdr.kpi_prefix}_{dd:http.daterange}_{dd:%hdr.itemname}_by_{dd:user.tab_forcsr}.xml?kpi_type={dd:http.kpi_type}&daterange={dd:http.daterange}&id={dd:http.id}&xml=true'));
$page->makeField (array ('name'=>'csvlink', 'default'=>'kpi/queryforcsr.php/{dd:hdr.kpi_prefix}_{dd:http.daterange}_{dd:%hdr.itemname}_by_{dd:user.tab_forcsr}.csv?kpi_type={dd:http.kpi_type}&daterange={dd:http.daterange}&id={dd:http.id}&csv=true'));

$page->makeField (array ('name'=>'list_filter','default'=>'servicer_id={dd:%http.id}&daterange={dd:http.daterange}&kpi_type={dd:http.kpi_type}&' . $page->get ('filter_arg')));
$page->makeField (array ('name'=>'total_filter','default'=>'servicer_id={dd:%http.id}&daterange={dd:http.daterange}&kpi_type={dd:http.kpi_type}'));

if (isset ($_GET ['xml']))
    $page->renderXML (array ('select'=>$select, 'xmlrecord'=>$page->m_tp->getValue ('user.tab_forcsr')));
else if (isset ($_GET ['csv']))
    $page->renderCSV (array ('select'=>$select, 'csvrecord'=>$page->m_tp->getValue ('user.tab_forcsr')));
else
    $page->render (array ('select'=>$select));

?>
