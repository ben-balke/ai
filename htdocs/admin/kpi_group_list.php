<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/htdocs/admin/kpi_group_list.php,v 1.3 2009/10/28 19:01:57 secwind Exp $
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
</head>
<body>
{dd:ai.bodyhead}
<table class=adminlist align=center cellpadding=1 cellspacing=0 width=500>
<caption nowrap>Key Performance Indicator (KPI) Groups</caption>
<thead>
<tr><td colspan="2">
Maintain KPI Groups
<p>
{dd:-user.admin}<a href="{dd:dd.path}admin/kpi_group_edit.php">Add a KPI Group...</a>
</td></tr>
<tr>
<th>Code</th>
<th>Name</th>
</tr>
</thead>
HEADER;

$content = <<<CONTENT
<tr class="{dd:rh.dd_oddeven}">
<td nowrap>
{dd:-user.admin}{dd:-rh.editable}<a href="{dd:dd.path}admin/kpi_group_edit.php?id={dd:~rh.id}">
{dd:rh.code}
{dd:-user.admin}{dd:-rh.editable}</a>
</td>
<td>{dd:rh.name}</td>
</tr>
CONTENT;

$footer=<<<FOOTER
<tfoot>
<tr class="{dd:rh.dd_oddeven}">
<td nowrap colspan=2>
{dd:rh.dd_recordno} KPI Groups(s)
</td>
</tr>
</tfoot>
</table>
{dd:ai.bodyfoot}
</body>
</html>
FOOTER;

/************************************
** SQL Statements
************************************/
$select = <<<SQL
select *, case when id < 0 then null else '' end as editable from ai_kpi_group order by code 
SQL;

/************************************
** PAGE AND AUTHENTICATION
************************************/
$page = new AI_page ("page", "kpi_groups");
$DD_session = new DD_Session ($AI_authpolicy);
$DD_session->doAuth ($page->m_tp);
$page->prepare();

$rh = new DD_SqlRecordHandler (array (
	'name'=>'rh'
	,'connect'=>'sql.ai'
	,'select'=>$select
	,'autofields'=>true
));

$page->addRecordHandler ($rh);

/************************************
* LOGIC, PAGE CONSTRUCTION AND POST PROCESSING
************************************/

$page->prepareForRecord ();
$page->outputContent ($header);

$rh->prepareForRecord ($page->m_tp);

$rh->query ($page->m_tp);
while ($rh->nextRecord ())
{
	$page->outputContent ($content);
}
$rh->freeQuery ();
$page->outputContent ($footer);
?>
