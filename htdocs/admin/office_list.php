<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/htdocs/admin/office_list.php,v 1.8 2012/08/28 21:14:17 secwind Exp $
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
<title>Offices | {dd:dd.productname}</title>
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
<table class=adminlist align=center cellpadding=1 cellspacing=0>
<caption>Offices</caption>
<thead>
<tr><td colspan="12">
Maintain Agency Insight Offices
<p>
{dd:-user.admin}<a href="{dd:dd.path}admin/office_edit.php">Add an Office...</a>
</td></tr>
<tr>
<th>ID</th>
<th align=left>Name</th>
<th align=left>Hostname</th>
<th>Type</th>
<th>Config String</th>
<th>Act</th>
<th>NoEx</th>
<th>Sts</th>
<th>Last Update</th>
<th>KPI Groups</th>
<th>Secure</th>
<th>Logo</th>
<th>Run</th>
<th>Log</th>
</tr>
</thead>
HEADER;

$content = <<<CONTENT
<tr class="{dd:rh.dd_oddeven}">
<td>{dd:rh.id}</td>
<td nowrap>
{dd:-user.admin}<a href="{dd:dd.path}admin/office_edit.php?id={dd:~rh.id}">
{dd:rh.name}
{dd:-user.admin}</a>
</td>
<td>{dd:rh.hostname}</td>
<td>{dd:rh.office_type}</td>
<td>{dd:rh.connectstring}&nbsp;</td>
<td>{dd:rh.active}</td>
<td>{dd:rh.noextract}</td>
<td>{dd:rh.status}&nbsp;</td>
<td>{dd:rh.lastupdate}&nbsp;</td>
<td><a href="{dd:dd.path}admin/kpi_group_assignments_{dd:rh.kpi_type_id}.php?office_id={dd:~rh.id}">KPI Groups</a></td>
<td><a href="{dd:dd.path}admin/office_security_edit.php?office_id={dd:~rh.id}">Secure</a></td>
<td><a href="{dd:dd.path}admin/office_logo.php?office_id={dd:~rh.id}">Logo</a></td>
<td><a href="{dd:dd.path}admin/run.php?id={dd:rh.id}">Run</a></td>
<td>
{dd:-user.admin}<a href="{dd:dd.path}admin/getlog.php?id={dd:rh.id}">Log</a>
</td>

</tr>
CONTENT;

$footer=<<<FOOTER
<tfoot>
<tr class="{dd:rh.dd_oddeven}">
<td nowrap colspan=14>
{dd:rh.dd_recordno} Office(s)
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
select o.*, t.name as office_type, q.status from ai_office o 
	left outer join ai_office_type t on (t.id = o.office_type_id) 
	left outer join ai_office_queue q on (q.id = o.id) 
	order by id 
SQL;

/************************************
** PAGE AND AUTHENTICATION
************************************/
$page = new AI_page ("page", "offices");
$DD_session = new DD_Session ($AI_authpolicy);
$DD_session->doAuth ($page->m_tp);
$page->prepare();

$rh = new DD_SqlRecordHandler (array (
	'name'=>'rh'
	,'connect'=>'sql.ai'
	,'select'=>$select
	,'autofields'=>true
));

$rh->makeField (array ('name'=>'lastupdate','datatype'=>'datetime'));
$page->addRecordHandler ($rh);

/************************************
* LOGIC, PAGE CONSTRUCTION AND POST PROCESSING
************************************/

$page->prepareForRecord ();
$page->outputContent ($header);

$rh->prepareForRecord ($page->m_tp);

$rh->query ($page->m_tp);
for ($i = 1; $rh->nextRecord (); $i++)
{
	$page->outputContent ($content);
}
$rh->freeQuery ();
$page->outputContent ($footer);
?>
