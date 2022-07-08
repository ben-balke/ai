<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/htdocs/admin/user_edit.php,v 1.4 2012/08/28 21:14:17 secwind Exp $
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
<title>Edit User Properties | {dd:dd.productname}</title>
{dd:dd.head}
<style type="text/css">@import url("{dd:dd.styles}menu.css"); </style>
<script type="text/javascript" src="{dd:dd.scripts}prototype.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}duckdigit.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}ajax.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}json.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}menu.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}widget.js"></script>
<script>
var		deletePressed = false;
function validateForm (f)
{
	if (deletePressed)
	{
		if (!confirm ("Are you sure you want to delete this record."))
		{
			deletePressed = false;
			return false;
		}
	}
	DD_setAlertObject ('errorvalidate');
	if (
		!DD_ValidateRequired (f.username, "Please provide a username.") ||
		{dd:-page.isnew}!DD_ValidateRequired (f.password, "Please enter a password") ||
		!DD_ValidateRequired (f.first_name, "Please provide a first name.") ||
		!DD_ValidateRequired (f.last_name, "Please provide a last name.") ||
		!DD_ValidateRequired (f.email, "Email is Required") ||
		!DD_ValidateEmail (f.email, "Email must be a valid email address")
		)
	{
		return false;
	}
	return true;
}
</script>
</head>
<body>
{dd:ai.bodyhead}
<form name=user method=post onsubmit="return validateForm (this);">
{dd:rh.id}
<table align=center class="tabForm">
	<tr><td colspan=2 class="dataLabel"><h4>User Properties</h4></td></tr>
	<tr><td class="dataLabel">Username:{dd:dd.req}</td><td class='tabEditViewDF'>{dd:rh.username} Active: {dd:rh.active}</td></tr>
	{dd:-page.isnew}<tr><td class="dataLabel">Password:{dd:dd.req}</td><td class='tabEditViewDF'>{dd:rh.password}</td></tr>
	<tr><td class="dataLabel">First Name:{dd:dd.req}</td><td class='tabEditViewDF'>{dd:rh.first_name}</td></tr>
	<tr><td class="dataLabel">Last Name:{dd:dd.req}</td><td class='tabEditViewDF'>{dd:rh.last_name}</td></tr>
	<tr><td class="dataLabel">Email:{dd:dd.req}</td><td class='tabEditViewDF'>{dd:rh.email}</td></tr>
	<tr><td class="dataLabel">Filter:</td><td class='tabEditViewDF'>{dd:rh.filter} *sql where clause!  Careful</td></tr>
	<tr><td class="dataLabel" valign=top>Roles:</td><td class='tabEditViewDF'>{dd:rh.authroles}</td></tr>
	<tr><td colspan=2 align=center>
		{dd:-page.isnew}{dd:page.DD_insert}
		{dd:!-page.isnew}{dd:page.DD_update}&nbsp;{dd:page.DD_delete}
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
** SQL STATEMENTS
************************************/
$select = <<<SQL
select * from ai_users
	where id = {http.id}
SQL;


$update=<<<SQL
update ai_users set
	email = {rh.email}
	,first_name = {rh.first_name}
	,last_name = {rh.last_name}
	,username = {rh.username}
	,authroles = {rh.authroles}
	,filter = {rh.filter}
	,active = {rh.active}
	,modifiedon = now()
	,modifiedby = {user.id}
	where id = {http.id}
SQL;

$insert=<<<SQL
insert into ai_users
	(
		email 
		,first_name
		,last_name
		,username
		,active
		,authroles 
		,filter 
		,password 
		,createdon
		,createdby
	) values (
		{rh.email}
		,{rh.first_name}
		,{rh.last_name}
		,{rh.username}
		,{rh.active}
		,{rh.authroles}
		,{rh.filter}
		,md5({rh.password})
		,now()
		,{user.id}
	)
SQL;

$delete="delete from ai_users where id = {http.id}";

/************************************
** PAGE AND AUTHENTICATION
************************************/
$page = new AI_page ("page", "Users");
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
	,'delete'=>$delete
	,'update'=>$update
	,'insert'=>$insert
	));

$rh->makeField (array (
	'type'=>'hidden', 
	'name'=>'id', 
	));
$rh->makeField (array (
	'type'=>'multiitem',
	'name'=>"authroles",
	'html'=>'class="checkbox"',
	'listsource'=>new DD_SqlListSource ('sql.ai', "select name, description from ai_authrole order by grouping")
	));

$rh->makeField (array (
	'type'=>'text', 
	'name'=>'first_name', 
	'size'=>40, 
	'maxlength'=>80,
	));

$rh->makeField (array (
	'type'=>'text', 
	'name'=>'last_name', 
	'size'=>40, 
	'maxlength'=>80,
	));

$rh->makeField (array (
	'type'=>'password', 
	'name'=>'password', 
	'size'=>14, 
	'maxlength'=>20,
	));

$rh->makeField (array (
	'type'=>'text', 
	'name'=>'email', 
	'size'=>50, 
	'maxlength'=>255,
	));

$rh->makeField (array (
	'type'=>'text', 
	'name'=>'filter', 
	'size'=>60, 
	'maxlength'=>255,
	));

$rh->makeField (array ( 'type'=>'text', 'name'=>'username', 'size'=>20, 'maxlength'=>20,));

$rh->makeField (array (
	'type'=>'checkbox'
	,'name'=>'active'
	,'default'=>'Y'
	,'checked'=>'Y'
	,'unchecked'=>'N'));

$rh->makeField (array (
	'type'=>'checkbox'
	,'name'=>'executive'
	,'default'=>'N'
	,'checked'=>'Y'
	,'unchecked'=>'N'));

$page->addRecordHandler ($rh);

$page->makeField (array (
	'type'=>'submit'
	,'name'=>'DD_insert'
	,'default'=>'Save'
	,'html'=>'class=button'
	));
$page->makeField (array (
	'type'=>'submit'
	,'name'=>'DD_update'
	,'default'=>'Save'
	,'html'=>'class=button'
	));
$page->makeField (array (
	'type'=>'submit'
	,'name'=>'DD_delete' 
	,'html'=>'onclick="deletePressed = true; return true;" class=button'
	,'default'=>'Delete'));
$page->makeField (array (
	'type'=>'button'
	,'name'=>'cancel'
	,'default'=>'Cancel'
	,'html'=>'onclick="javascript: history.go (-1);" class=button'));



/************************************
* LOGIC, PAGE CONSTRUCTION AND POST PROCESSING
************************************/
$page->verifyRoles ("admin", "Administrative Privilges are Required");

if ($_SERVER['REQUEST_METHOD'] == "POST")
{
	$page->processPost ();
	$newLoc = $page->m_tp->parseContent ('Location: {dd:dd.path}admin/user_list.php');
	header($newLoc);
}
else
{
	$page->prepareForRecord ();
	$rh->prepareForRecord ($page->m_tp);

	if (isset ($_GET['id']))
	{
		$rh->query ($page->m_tp);
		$rh->nextRecord ();
		$page->outputContent ($content);
		$rh->freeQuery ();
	}
	else
	{
		$isnew = $page->makeField (array (
			'type'=>'work',
			'name'=>'isnew',
		));
		$isnew->set ('');
		$rh->setFieldsFromArray ($_GET, false);
		$page->outputContent ($content);
	}
}
?>
