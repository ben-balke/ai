<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/htdocs/admin/userpass.php,v 1.3 2009/10/28 19:01:57 secwind Exp $
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
<title>Reset Password | {dd:dd.productname}</title>
{dd:dd.head}
<style type="text/css">@import url("{dd:dd.styles}menu.css"); </style>
<script type="text/javascript" src="{dd:dd.scripts}prototype.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}duckdigit.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}ajax.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}json.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}menu.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}widget.js"></script>
<script>
function validateForm (f)
{
	DD_setAlertObject ('errorvalidate');
	if (
		!DD_ValidateRequired (f.password, "Password is Required")
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
	<tr><td colspan=2 class="dataLabel"><h4>Reset Password</h4></td></tr>
	<tr><td class="dataLabel">Position Name:</td><td class='tabEditViewDF'>{dd:rh.username}</td></tr>
	<tr><td class="dataLabel">Director Name:</td><td class='tabEditViewDF'>{dd:rh.first_name} {dd:rh.last_name}</td></tr>
	<tr><td class="dataLabel">Email:</td><td class='tabEditViewDF'>{dd:rh.email}</td></tr>
	<tr><td class="dataLabel">Password:{dd:dd.req}</td><td class='tabEditViewDF'>{dd:rh.password}</td></tr>
	<tr><td colspan=2 align=center>
		{dd:page.DD_update}
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
select id, first_name, last_name, email from ai_users 
	where id = {http.id}
SQL;


$update=<<<SQL
update ai_users set
	password = md5({rh.password})
	where id = {http.id}
SQL;

/************************************
** PAGE AND AUTHENTICATION
************************************/
$page = new AI_page ("page", "board");
$DD_session = new DD_Session ($AI_authpolicy);
$DD_session->doAuth ($page->m_tp);
$page->prepare ();

/************************************
** RECORD HANDLERS AND FIELDS
************************************/
$rh = new DD_SqlRecordHandler (array (
	'name'=>"rh" 
	,'connect'=>"sql.ai"
	,'update'=>$update
	,'select'=>$select
	));

$rh->makeField (array (
	'type'=>'hidden', 
	'name'=>'id', 
	));

$rh->makeField (array (
	'name'=>'first_name', 
	));

$rh->makeField (array (
	'name'=>'last_name', 
	));

$rh->makeField (array (
	'type'=>'password', 
	'name'=>'password', 
	'size'=>14, 
	'maxlength'=>20,
	));

$rh->makeField (array (
	'name'=>'email', 
	));

$page->addRecordHandler ($rh);

$page->makeField (array (
	'type'=>'submit'
	,'name'=>'DD_update'
	,'default'=>'Change Password'
	,'html'=>'class=button'
	));
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
	$rh->query ($page->m_tp);
	$rh->nextRecord ();
	$page->outputContent ($content);
	$rh->freeQuery ();
}
?>
