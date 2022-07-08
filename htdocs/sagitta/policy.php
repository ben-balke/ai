<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/htdocs/sagitta/policy.php,v 1.3 2009/10/28 19:02:05 secwind Exp $
*************************************************************************/
require_once 'dd/DD_page.php';
require_once 'ai/params.php';
require_once 'ai/page.php';
require_once ("ai/properties.php");

/************************************
** CONTENT
************************************/
$content = <<<CONTENT
{dd:dd.htmlhead}
<html {dd:dd.htmlattr}>
<head>
<title>Welcome | {dd:dd.productname}</title>
{dd:dd.head}
<script type="text/javascript" src="{dd:dd.scripts}prototype.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}duckdigit.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}ajax.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}json.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}widget.js"></script>
<script type="text/javascript">
</script>
</head>
<body>
<form id="sortform" method="post" action="{dd:dd.path}widget/set_admin_props.php">
<input type=hidden name='sort_customers'  id='sort_customers'>
<input type=hidden name="DD_update">
</form>
{dd:ai.bodyhead}
<table class="querylist" cellspacing="0" width="800">
<thead>
<caption>
Premiums
</caption>
</thead>
{dd:rh.dd_dumpfields}
</table>
<div style="float: right; padding-bottom: 4px;">{dd:page.pager}</div>
{dd:ai.bodyfoot}
</body>
</html>
CONTENT;


/************************************
** SQL Construction
************************************/

$select = "
select *
	
from office{SQL:user.active_office_id}.policies 
where rowid = {http.id}";


/************************************
** PAGE AND AUTHENTICATION
************************************/
$page = new AI_page ("page", "Core");
$DD_session = new DD_Session ($AI_authpolicy);
$DD_session->doAuth ($page->m_tp);
$page->prepare();

$rh = new DD_SqlRecordHandler (array (
	'name'=>'rh'
	,'connect'=>'sql.ai'
	,'select'=>$select
	,'autofields'=>true
));

$pricefld = array ( // Base price field extended below.
	'type'=>'display',
	'datatype'=>'float',
	'format'=>',$'
);

$page->addRecordHandler ($rh);

/************************************
* LOGIC, PAGE CONSTRUCTION AND POST PROCESSING
************************************/

$page->prepareForRecord ();
$rh->prepareForRecord ($page->m_tp);
$rh->query ($page->m_tp);
if ($rh->nextRecord ())
{
	foreach ($arrays as $fld)
	{
		$val = $fld->getValue ();
		$val = strtr ($val, array ('{'=>'','}'=>'',','=>'</td><td align=right>'));
		$fld->set ($val);
	}
	$page->outputContent ($content);
}
$rh->freequery ();
?>
