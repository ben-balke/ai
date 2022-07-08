<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/htdocs/admin/run.php,v 1.1 2012/08/28 21:55:18 secwind Exp $
*************************************************************************/
require_once 'dd/DD_page.php';
require_once 'ai/params.php';
require_once 'ai/page.php';

/************************************
** CONTENT
************************************/
$content = <<<CONTENT
{dd:dd.htmlhead}
<html {dd:dd.htmlattr}>
<head>
<title>Edit Office Properties | {dd:dd.productname}</title>
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
<form name="office" method="post">
{dd:rh.id}
<table align="center" class="tabForm">
	<tr><td colspan=2 class="dataLabel"><h4>Start Office: {dd:rh.name}</h4></td></tr>
	<tr><td class="dataLabel">STATUS:</td><td class='tabEditViewDF'><!--{dd:-rh.status}--> ALREADY QUEUED</td></tr>
	<tr><td class="dataLabel">ID:</td><td class='tabEditViewDF'>{dd:~rh.id}</td></tr>
	<tr><td class="dataLabel">Name:</td><td class='tabEditViewDF'>{dd:rh.name}</td></tr>
	<tr><td class="dataLabel">Type:</td><td class='tabEditViewDF'>{dd:rh.office_type_id}</td></tr>
	<tr><td class="dataLabel">Hostname:</td><td class='tabEditViewDF'>{dd:rh.hostname}</td></tr>
	<tr><td class="dataLabel">Skip Extract:</td><td class='tabEditViewDF'>{dd:rh.skipextract}</td></tr>
	<tr><td colspan=2 align=center>
		{dd:!-rh.status}{dd:page.DD_insert}
		{dd:page.cancel}
	</td></tr>
	 <tr><td colspan=2><div id=errorvalidate class=errordiv></div></div></td> </tr>
</table>
</form>
{dd:ai.bodyfoot}
</body>
</html>
CONTENT;

/************************************
** CONTENT
************************************/
$alreadyqueuedcontent = <<<CONTENT
{dd:dd.htmlhead}
<html {dd:dd.htmlattr}>
<head>
<title>Edit Office Properties | {dd:dd.productname}</title>
{dd:dd.head}
<style type="text/css">@import url("{dd:dd.styles}menu.css"); </style>
<script type="text/javascript" src="{dd:dd.scripts}prototype.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}duckdigit.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}menu.js"></script>
</head>
<body>
{dd:ai.bodyhead}
Already Queued
</form>
{dd:ai.bodyfoot}
</body>
</html>
CONTENT;

/************************************
** SQL STATEMENTS
************************************/
$select = <<<SQL
select *, (select status from ai_office_queue where id = {http.id}) as status  from ai_office
	where id = {http.id}
SQL;


$insert=<<<SQL
insert into ai_office_queue (id, userid, status, starttime) values ({http.id}, {user.id}, 'Q', now());
SQL;

/************************************
** PAGE AND AUTHENTICATION
************************************/
$page = new AI_page ("page", "Offices");
$DD_session = new DD_Session ($AI_authpolicy);
$DD_session->doAuth ($page->m_tp);
$page->prepare ();

/************************************
** RECORD HANDLERS AND FIELDS
************************************/
$rh = new DD_SqlRecordHandler (array (
	'name'=>"rh" 
	,'connect'=>"sql.ai"
	,'select'=>$select
	,'insert'=>$insert
	,'autofields'=>true
	));

$id = $rh->makeField (array (
	'type'=>'hidden',
	'name'=>'id',
));
$page->addRecordHandler ($rh);

$page->makeField (array (
	'type'=>'submit'
	,'name'=>'DD_insert'
	,'default'=>'Start'
	,'html'=>'class=button'
	));
$page->makeField (array (
	'type'=>'button'
	,'name'=>'cancel'
	,'default'=>'Cancel'
	,'html'=>'onclick="javascript: history.go (-1);" class=button'));

$isnew = $page->makeField (array (
	'type'=>'work',
	'name'=>'isnew',
));


/************************************
* LOGIC, PAGE CONSTRUCTION AND POST PROCESSING
************************************/
$page->verifyRoles ("run", "Run Privilges are Required");

if ($_SERVER['REQUEST_METHOD'] == "POST")
{
	try
	{
		$page->processPost ();
	}
	catch (DD_SqlDuplicationKeyException $ex)
	{
		$page->outputContent ($alreadyqueuedcontent);
		return;
	}
	$newLoc = $page->m_tp->parseContent ('Location: {dd:dd.path}admin/office_list.php');
	header($newLoc);
}
else
{
	$page->prepareForRecord ();
	$rh->prepareForRecord ($page->m_tp);

	$rh->query ($page->m_tp);
	$rh->nextRecord ();
	$page->outputContent ($content);
	$rh->freeQuery ();
}
?>
