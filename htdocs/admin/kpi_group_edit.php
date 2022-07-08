<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/htdocs/admin/kpi_group_edit.php,v 1.4 2012/09/04 18:19:14 secwind Exp $
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
<title>Edit KPI Group Properties | {dd:dd.productname}</title>
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
		!DD_ValidateRequired (f.code, "Please provide a code.", true) ||
		!DD_ValidateRequired (f.name, "Please provide a name.")
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
<form name="office" method="post" onsubmit="return validateForm (this);">
{dd:rh.id}
<table align="center" class="edit-form">
	<tr><td colspan=2 class="tag"><h4>KPI Group Properties</h4></td></tr>
	<tr><td class="tag">Code:{dd:dd.req}</td><td class='data'>{dd:rh.code}</td></tr>
	<tr><td class="tag">Name:{dd:dd.req}</td><td class='data'>{dd:rh.name}</td></tr>
	<tr><td colspan=2 align=center>
		{dd:!-page.isnew}{dd:page.DD_insert}
		{dd:-page.isnew}{dd:page.DD_update}&nbsp;{dd:page.DD_delete}
		{dd:page.cancel}
	</td></tr>
	 <tr><td colspan=2><div id=errorvalidate class=errordiv></div></div></td> </tr>
</table>
</form>
{dd:ai.bodyfoot}
</body>
</html>
CONTENT;

$dupcontent =<<<CONTENT
<html>
<head>
<title>Edit KPI Group Properties | {dd:dd.productname}</title>
{dd:dd.head}
<script src="{dd:dd.scripts}duckdigit.js"></script>
</head>
<body>
{dd:ai.bodyhead}
KPI Group code or name already exists.  <a href="#" onclick="history.go(-1);">Press here to try again.</a>
{dd:ai.bodyfoot}
</body>
</html>
CONTENT;
/************************************
** SQL STATEMENTS
************************************/
$select = <<<SQL
select * from ai_kpi_group
	where id = {http.id}
SQL;


$update=<<<SQL
update ai_kpi_group set
	code = {rh.code}
	,name = {rh.name}
	where id = {http.id}
SQL;

$insert=<<<SQL
insert into ai_kpi_group
	(
		code 
		,name
	) values (
		{rh.code}
		,{rh.name}
	)
SQL;

$delete="delete from ai_kpi_group where id = {http.id}";

/************************************
** PAGE AND AUTHENTICATION
************************************/
$page = new AI_page ("page", "KPI Groups");
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
	'type'=>'text', 
	'name'=>'code', 
	'size'=>6, 
	'maxlength'=>6,
	));

$rh->makeField (array (
	'type'=>'text', 
	'name'=>'name', 
	'size'=>30, 
	'maxlength'=>30,
	));

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

$isnew = $page->makeField (array (
	'type'=>'work',
	'name'=>'isnew',
));


/************************************
* LOGIC, PAGE CONSTRUCTION AND POST PROCESSING
************************************/
$page->verifyRoles ("admin", "Administrative Privilges are Required");

if ($_SERVER['REQUEST_METHOD'] == "POST")
{
	try
	{
		$page->processPost ();
		$newLoc = $page->m_tp->parseContent ('Location: {dd:dd.path}admin/kpi_group_list.php');
		header($newLoc);
	}
	catch (DD_SqlDuplicateKeyException $ex)
	{
		$page->outputContent ($dupcontent);
	}
}
else
{
	$page->prepareForRecord ();
	$rh->prepareForRecord ($page->m_tp);

	if (isset ($_GET['id']))
	{
		$isnew->set ('');
		$rh->query ($page->m_tp);
		$rh->nextRecord ();
		$page->outputContent ($content);
		$rh->freeQuery ();
	}
	else
	{
		$rh->setFieldsFromArray ($_GET, false);
		$page->outputContent ($content);
	}
}
?>
