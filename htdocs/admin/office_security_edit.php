<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/htdocs/admin/office_security_edit.php,v 1.1 2012/08/28 21:55:18 secwind Exp $
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
</script>
</head>
<body>
{dd:ai.bodyhead}
<form method="post">
<table class=adminlist align=center cellpadding=1 cellspacing=0>
<caption nowrap>Office Security</caption>
<thead>
<tr><td colspan="6" align=center>
<b>Office: {dd:rhmap.office_name}</b>
</td></tr>
<tr>
<th>Username</th>
<th>Name</th>
<th align="left">Allowed</th>
<th align="left">Filter</th>
</tr>
</thead>
HEADER;

$content=<<<CONTENT
<tr class="{dd:rhmap.dd_oddeven}">
<td nowrap>{dd:rhmap.username}{dd:rhmap.user_id}</td>
<td nowrap>{dd:rhmap.first_name} {dd:rh.last_name}</td>
<td nowrap> {dd:rhmap.status} </td>
<td nowrap> {dd:rhmap.filter} </td>
</tr>
CONTENT;

$footer=<<<FOOTER
<tfoot>
<tr class="{dd:rhmap.dd_oddeven}">
<td nowrap colspan=4>
{dd:rhmap.dd_recordno} User(s)
{dd:page.DD_insert} {dd:page.cancel}{dd:page.office_id}
</td>
</tfoot>
</table>
{dd:page.DD_records}<!--multirecord designator-->
</form>
{dd:ai.bodyfoot}
</body>
</html>
FOOTER;

/************************************
** SQL Statements
************************************/
$select = <<<SQL
select u.id as user_id, u.username, u.first_name, u.last_name, s.status, s.filter from ai_users u left outer join ai_office_security s on (s.office_id = {http.office_id} and s.user_id = u.id) order by username;
SQL;
$insert = <<<SQL
	insert into ai_office_security (
		office_id
		,user_id
		,status
		,filter
	) values (
		{http.office_id}
		,{rhmap.user_id}
		,{rhmap.status}
		,{rhmap.filter}
	);
SQL;

/************************************
** PAGE AND AUTHENTICATION
************************************/
$page = new AI_page ("page", "ai_office_security");
$DD_session = new DD_Session ($AI_authpolicy);
$DD_session->doAuth ($page->m_tp);
$page->prepare();
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

$page->makeField (array ('name'=>'office_id','type'=>'hidden', 'default'=>'{dd:http.office_id}'));

$dd_records = $page->makeField (array ('name'=>'DD_records','type'=>'hidden'));

$rhclear = new DD_SqlRecordHandler (array (
	'name'=>'rhclear'
	,'connect'=>'sql.ai'
	,'insert'=>"delete from ai_office_security where office_id = {http.office_id}"
	,'autofields'=>true
));
$rhmap = new DD_SqlRecordHandler (array (
	'name'=>'rhmap'
	,'connect'=>'sql.ai'
	,'select'=>$select
	,'insert'=>$insert
	,'autofields'=>false
	,'multirecord'=>true
));
$skipfield = $rhmap->makeField (array ('name'=>'status','type'=>'checkbox','checked'=>'A','unchecked'=>'D','multirecord'=>true));
$rhmap->makeField (array ('name'=>'user_id','type'=>'hidden','multirecord'=>true));
$rhmap->makeField (array ('name'=>'filter','type'=>'text', 'size'=>60, 'maxlength'=>512, 'multirecord'=>true));
$rhmap->makeField (array ('name'=>'username'));
$rhmap->makeField (array ('name'=>'first_name'));
$rhmap->makeField (array ('name'=>'last_name'));
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
	$rhmap->quickSelect ('sql.ai', 'select name as office_name from ai_office where id = {http.office_id}', $page->m_tp);
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
