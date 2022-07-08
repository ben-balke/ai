<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/htdocs/admin/user_list.php,v 1.3 2009/10/28 19:01:57 secwind Exp $
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
<title>User List | {dd:dd.productname}</title>
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
<caption>Users</caption>
<tr><td>
Add and Maintain Agency Insight Users
<p>
{dd:-user.admin}<a href="{dd:dd.path}admin/user_edit.php">Add A User...</a>
</td></tr>
<tr>
<th>Name</th>
<th>Username</th>
<th>Email</th>
<th>Roles</th>
<th>Password</th>
<th>Logins</th>
</tr>
HEADER;

$content = <<<CONTENT
<tr class="{dd:rh.dd_oddeven}">
<td nowrap>
{dd:-user.admin}<a href="{dd:dd.path}admin/user_edit.php?id={dd:~rh.id}">
{dd:rh.first_name} {dd:rh.last_name}
{dd:-user.admin}</a>
</td>
<td>{dd:rh.username}</td>
<td><a href="mailto:{dd:rh.email}">{dd:rh.email}</a>&nbsp;</td>
<td>{dd:rh.authroles}&nbsp;</td>
<td nowrap>
{dd:-user.admin}<a href="{dd:dd.path}admin/userpass.php?id={dd:%~rh.id}">password</a>
</td>
<td>{dd:rh.logins}</td>
</tr>
CONTENT;

$footer=<<<FOOTER
<tfoot>
<tr class="{dd:rh.dd_oddeven}">
<td nowrap colspan=6>
{dd:rh.dd_recordno} User(s)
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
select *, (select count (*) from ai_userlog where ai_userlog.userid = u.id) as logins from ai_users u order by last_name 
SQL;

/************************************
** PAGE AND AUTHENTICATION
************************************/
$page = new AI_page ("page", "users");
$DD_session = new DD_Session ($AI_authpolicy);
$DD_session->doAuth ($page->m_tp);
$page->prepare();
$style = $page->makeField (array ('name'=>'style','type'=>'work'));

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
for ($i = 1; $rh->nextRecord (); $i++)
{
	$style->set ($i % 2 ? ' class=odd ' : null);
	$page->outputContent ($content);
}
$rh->freeQuery ();
$page->outputContent ($footer);
?>
