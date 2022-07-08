<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/htdocs/admin/kpi_group_assignments_O.php,v 1.1 2010/09/27 23:25:12 secwind Exp $
*************************************************************************/
require_once 'dd/DD_page.php';
require_once 'ai/params.php';
require_once 'ai/page.php';

/************************************
** CONTENT
************************************/
$header = <<<HEADER
{dd:dd.htmlhead}
<html {dd:dd.htmlattr}>
<head>
<title>Key Performance Indicator Groups | {dd:dd.productname}</title>
{dd:dd.head}
<style type="text/css">@import url("{dd:dd.styles}menu.css"); </style>
<script type="text/javascript" src="{dd:dd.scripts}prototype.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}duckdigit.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}ajax.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}json.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}menu.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}widget.js"></script>
<script>
function makeAssignments ()
{
	var	group = $('kpi_group_select');
	if (group.selectedIndex <= 0)
	{
		alert ('Please select a group first.');
		return false;
	}
	var newval = group.options [group.selectedIndex].text
	newval = newval.substring (0, newval.indexOf ('-'));
	var rcds = $('DD_records').value;
	var val = group.options [group.selectedIndex].value;
	for (i = 1; i <= rcds; i++)
	{
		if ($('selected' + i).checked)
		{
			$('kpi_group' + i).value = val;
			$('grouptext' + i).innerHTML = '<font color="#dd0000">' + newval + '</font>';
			$('selected' + i).checked = false;
		}
	}
}

function selectWithText (str)
{
	var rcds = $('DD_records').value;
	var text = $('selecttext').value;
	var	selected = 0;
	for (i = 1; i <= rcds; i++)
	{
		if ($('level2org' + i).innerHTML.indexOf (text) != -1)
		{
			$('selected' + i).checked = true;
			selected++;
		}
		else
		{
			$('selected' + i).checked = false;
		}
	}
	alert (selected + ' items marked');
}
</script>
</head>
<body>
{dd:ai.bodyhead}
<form method="post">
<table class=adminlist align=center cellpadding=1 cellspacing=0>
<caption nowrap>KPI Group Assignments</caption>
<thead>
<tr><td colspan="6">
<div style="float:right;">
<table cellpadding="2" cellspacing="0">
<tr><td class="tag" align=right>Select By Level2Org:</td><td>{dd:page.selecttext} {dd:page.selecttextbut}</td></tr>
<tr><td class="tag" align=right>Assign Group:</td><td>{dd:page.kpi_group_select} {dd:page.assign}</td></tr>
<tr><td class="data" colspan="2" align="center">{dd:page.DD_insert} {dd:page.cancel}</td></tr>
</table>

</div>
Maintain KPI Groups
</td></tr>
<tr>
<th colspan="2" align="left">Level 1 Org</th>
<th colspan="2" align="left">Level 2 Org</th>
<th align="left">&nbsp;</th>
<th align="left">Group</th>
</tr>
</thead>
HEADER;

$content=<<<CONTENT
<tr class="{dd:rhmap.dd_oddeven}">
<td>{dd:~rhmap.level1org}</td>
<td nowrap id="level1org{dd:rhmap.dd_recordno}">{dd:rhmap.level1name}</td>
<td>{dd:~rhmap.level2org}</td>
<td nowrap id="level2org{dd:rhmap.dd_recordno}">{dd:rhmap.level2name}</td>
<td nowrap>
<input type="checkbox" name="selected{dd:rhmap.dd_recordno}" id="selected{dd:rhmap.dd_recordno}" value="{dd:rhmap.dd_recordno}" class="checkbox"/>
{dd:rhmap.kpi_group}
{dd:rhmap.level1org}
{dd:rhmap.level2org}
</td>
<td nowrap id="grouptext{dd:rhmap.dd_recordno}">{dd:rhmap.code}&nbsp;</td>
</tr>
CONTENT;

$footer=<<<FOOTER
<tfoot>
<tr class="{dd:rhmap.dd_oddeven}">
<td nowrap colspan=2>
{dd:rhmap.dd_recordno} KPI Assignments(s)
</td>
</tr>
</tfoot>
</table>
{dd:page.DD_records}<!--multirecord designator-->
{dd:page.office_id}
</form>
{dd:ai.bodyfoot}
</body>
</html>
FOOTER;

/************************************
** SQL Statements
************************************/
$select = <<<SQL
select 
	l2.office_id
	,l1.name as level1name 
	,l2.id as level2org 
	,l2.name as level2name 
	,l2.level1org
	,m.kpi_group 
	,g.code as code 
from cd_level2org l2 
	left outer join ai_kpi_map m on (m.office_id = {http.office_id} and m.level1org = l2.level1org and m.level2org = l2.id)
	left outer join ai_kpi_group g on (g.id = m.kpi_group)
	left outer join cd_level1org l1 on (l1.office_id = {http.office_id} and l1.id = l2.level1org)
where 
	l2.office_id = {http.office_id}
SQL;
$insert = <<<SQL
	insert into ai_kpi_map (
		office_id
		,level1org
		,level2org
		,kpi_group
	) values (
		1
		,{rhmap.level1org}
		,{rhmap.level2org}
		,{rhmap.kpi_group}
	);
SQL;

/************************************
** PAGE AND AUTHENTICATION
************************************/
$page = new AI_page ("page", "kpi_groups");
$DD_session = new DD_Session ($AI_authpolicy);
$DD_session->doAuth ($page->m_tp);
$page->prepare();
$page->makeField (array ('name'=>'kpi_group_select','type'=>'select','noselection'=>'','listsource'=>new DD_SqlListSource ('sql.ai', "select id, code || '-' || name from ai_kpi_group order by code, name")));
$page->makeField (array ('name'=>'assign','type'=>'button','html'=>'onclick="makeAssignments ();"','default'=>'Assign'));
$page->makeField (array ('name'=>'selecttextbut','type'=>'button','html'=>'onclick="selectWithText ();"','default'=>'Select'));
$page->makeField (array ('name'=>'selecttext','type'=>'text','size'=>30,'maxlength'=>30));
$page->makeField (array ('name'=>'office_id','type'=>'hidden','default'=>'{dd:http.office_id}'));
$page->makeField (array (
    'type'=>'submit'
    ,'name'=>'DD_insert'
    ,'default'=>'Save'
    ,'html'=>'class=button'
    ));
$page->makeField (array (
    'type'=>'button'
    ,'name'=>'cancel'
    ,'default'=>'Cancel'
    ,'html'=>'onclick="javascript: history.go (-1);" class=button'));

$dd_records = $page->makeField (array ('name'=>'DD_records','type'=>'hidden'));

$rhclear = new DD_SqlRecordHandler (array (
	'name'=>'rhclear'
	,'connect'=>'sql.ai'
	,'insert'=>"delete from ai_kpi_map where office_id = {http.office_id}"
));
$rhmap = new DD_SqlRecordHandler (array (
	'name'=>'rhmap'
	,'connect'=>'sql.ai'
	,'select'=>$select
	,'insert'=>$insert
	,'autofields'=>false
	,'multirecord'=>true
));
$skipfield = $rhmap->makeField (array ('name'=>'kpi_group','type'=>'hidden','multirecord'=>true));
$rhmap->makeField (array ('name'=>'level1org','type'=>'hidden','multirecord'=>true));
$rhmap->makeField (array ('name'=>'level2org','type'=>'hidden','multirecord'=>true));
$rhmap->makeField (array ('name'=>'level1name'));
$rhmap->makeField (array ('name'=>'level2name'));
$rhmap->makeField (array ('name'=>'code'));
$rhmap->addMultiRecordSkipField ($skipfield);

$page->addRecordHandler ($rhclear);
$page->addRecordHandler ($rhmap);

/************************************
* LOGIC, PAGE CONSTRUCTION AND POST PROCESSING
************************************/

if ($_SERVER['REQUEST_METHOD'] == "POST")
{
	$page->processPost ();
	$newLoc = $page->m_tp->parseContent ('Location: {dd:dd.path}admin/office_list.php');
	header($newLoc);
}
else 
{
	$page->prepareForRecord ();
	$page->outputContent ($header);

	$rhmap->prepareForRecord ($page->m_tp);

	$rhmap->query ($page->m_tp);
	while ($rhmap->nextRecord ())
	{
		$page->outputContent ($content);
	}
	$dd_records->set ($rhmap->getRecordNo ());
	$rhmap->freeQuery ();
	$page->outputContent ($footer);
}
?>
