<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/htdocs/admin/office_edit.php,v 1.6 2012/08/28 21:14:17 secwind Exp $
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
		!DD_ValidateRequiredSelect (f.office_type_id, "Please provide a last name.", true) ||
		!DD_ValidateRequired (f.name, "Please provide a name.") ||
		!DD_ValidateRequiredSelect (f.fiscal_month, "Please provide first month of your fiscal year.", true) ||
		!DD_ValidateRequired (f.hostname, "Please provide a hostname.")
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
<table align="center" class="tabForm">
	<tr><td colspan=2 class="dataLabel"><h4>Office Properties</h4></td></tr>
	<tr><td class="dataLabel">Type:{dd:dd.req}</td><td class='tabEditViewDF'>{dd:rh.office_type_id}</td></tr>
	<tr><td class="dataLabel">Name:{dd:dd.req}</td><td class='tabEditViewDF'>{dd:rh.name} Active: {dd:rh.active}</td></tr>
	<tr><td class="dataLabel">No Extract:</td><td class='tabEditViewDF'>{dd:rh.noextract}</td></tr>
	<tr><td class="dataLabel">Fiscal Year Begins:{dd:dd.req}</td><td class='tabEditViewDF'>{dd:rh.fiscal_month}</td></tr>
	<tr><td class="dataLabel">Hostname/Office:{dd:dd.req}</td><td class='tabEditViewDF'>{dd:rh.hostname}</td></tr>
	<tr><td class="dataLabel" valign=top>Connect/Filter Clause:</td><td class='tabEditViewDF'>{dd:rh.connectstring}
	<br>Copy Fitler Example:<br>
	level2org in ('500','5100','5200','5300','5400','5500')</td></tr>
	<tr><td class="dataLabel">KPI Type:</td><td class='tabEditViewDF'>{dd:rh.kpi_type_id}</td></tr>
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

/************************************
** SQL STATEMENTS
************************************/
$select = <<<SQL
select * from ai_office
	where id = {http.id}
SQL;


$update=<<<SQL
update ai_office set
	hostname = {rh.hostname}
	,name = {rh.name}
	,fiscal_month = {rh.fiscal_month}
	,office_type_id = {rh.office_type_id}
	,connectstring = {rh.connectstring}
	,kpi_type_id = {rh.kpi_type_id}
	,active = {rh.active}
	,noextract = {rh.noextract}
	,modifiedon = now()
	,modifiedby = {user.id}
	where id = {http.id}
SQL;

$insert=<<<SQL
insert into ai_office
	(
		hostname 
		,name
		,fiscal_month
		,office_type_id
		,connectstring
		,kpi_type_id
		,active
		,noextract
		,createdon
		,createdby
	) values (
		{rh.hostname}
		,{rh.name}
		,{rh.fiscal_month}
		,{rh.office_type_id}
		,{rh.connectstring}
		,{rh.kpi_type_id}
		,{rh.active}
		,{rh.noextract}
		,now()
		,{user.id}
	)
SQL;

$delete="delete from ai_office where id = {http.id}";

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
	,'delete'=>$delete
	,'update'=>$update
	,'insert'=>$insert
	));

$rh->makeField (array (
	'type'=>'hidden', 
	'name'=>'id', 
	));
$rh->makeField (array (
	'type'=>'select',
	'name'=>"office_type_id",
	'noselection'=>"",
	'listsource'=>new DD_SqlListSource ('sql.ai', "select id, name from ai_office_type order by name")
	));

$rh->makeField (array (
	'type'=>'select',
	'name'=>"fiscal_month",
	'noselection'=>"",
	'listsource'=>new DD_ArrayListSource (array (
		'1'=>'January'	
		,'2'=>'February'	
		,'3'=>'March'	
		,'4'=>'April'	
		,'5'=>'May'	
		,'6'=>'June'	
		,'7'=>'July'	
		,'8'=>'August'	
		,'9'=>'September'	
		,'10'=>'October'	
		,'11'=>'November'	
		,'12'=>'December'))
	));

$rh->makeField (array (
	'type'=>'text', 
	'name'=>'hostname', 
	'size'=>60, 
	'maxlength'=>100,
	));

$rh->makeField (array (
	'type'=>'text', 
	'name'=>'name', 
	'size'=>40, 
	'maxlength'=>100,
	));

$rh->makeField (array ( 'type'=>'text', 'name'=>'connectstring', 'size'=>60, 'maxlength'=>100,));

$rh->makeField (array (
	'type'=>'select',
	'name'=>"kpi_type_id",
	'noselection'=>"",
	'listsource'=>new DD_ArrayListSource (array ('O'=>'Organizational Levels', 'C'=>'Coverage Types'))));

$rh->makeField (array (
	'type'=>'checkbox'
	,'name'=>'active'
	,'default'=>'Y'
	,'checked'=>'Y'
	,'unchecked'=>'N'));

$rh->makeField (array (
	'type'=>'checkbox'
	,'name'=>'noextract'
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
	$page->processPost ();
	$newLoc = $page->m_tp->parseContent ('Location: {dd:dd.path}admin/office_list.php');
	header($newLoc);
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
