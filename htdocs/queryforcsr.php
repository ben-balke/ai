<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/htdocs/queryforcsr.php,v 1.4 2012/08/28 21:13:55 secwind Exp $
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
    cd_policy p "
	. $page->get ('join') .  "
where
    p.office_id = {user.active_office_id}
    and p.book_end > date_trunc('day',now())
    and p.book_start <= date_trunc('day',now())
    and p.servicer_id = {http.id}
    and p.premium is not null and p.premium != 0
	{SQL:page.user_filter}
group by itemid, itemcode, itemname
order by {SQL:user.sort_forcsr};
";


$selecthdr = <<<SQL
    select 'Servicer: ' || name as name, name as itemname from cd_staff where office_id = {user.active_office_id} and id = {http.id}
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
$page->makeField (array ('name'=>'link', 'default'=>$page->get ('link')));
$page->makeField (array ('name'=>'sort_property_name', 'default'=>'sort_forcsr'));


$page->makeField (array ('name'=>'xmllink', 'default'=>'queryforcsr.php/servicer_{dd:%hdr.itemname}_book_by_{dd:user.tab_forcsr}.xml?id={dd:http.id}&xml=true'));
$page->makeField (array ('name'=>'csvlink', 'default'=>'queryforcsr.php/servicer_{dd:%hdr.itemname}_book_by_{dd:user.tab_forcsr}.csv?id={dd:http.id}&csv=true'));

$page->makeField (array ('name'=>'list_filter','default'=>'servicer_id={dd:%http.id}&' . $page->get ('filter_arg')));
$page->makeField (array ('name'=>'total_filter','default'=>'servicer_id={dd:%http.id}'));

if (isset ($_GET ['xml']))
    $page->renderXML (array ('select'=>$select, 'xmlrecord'=>$page->m_tp->getValue ('user.tab_forcsr')));
else if (isset ($_GET ['csv']))
    $page->renderCSV (array ('select'=>$select, 'csvrecord'=>$page->m_tp->getValue ('user.tab_forcsr')));
else
    $page->render (array ('select'=>$select));
?>
