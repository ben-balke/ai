<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/htdocs/policylist.php,v 1.5 2012/08/28 21:13:55 secwind Exp $
*************************************************************************/
require_once 'dd/DD_page.php';
require_once 'ai/params.php';
require_once 'ai/page.php';
require_once ("ai/properties.php");
require_once 'ai/pagerfield.php';
require_once 'ai/CriteriaBuilder.class';
require_once 'dd/DD_sqltoxml.php';

/************************************
** CONTENT
************************************/
$header = <<<CONTENT
{dd:dd.htmlhead}
<html {dd:dd.htmlattr}>
<head>
<title>Welcome | {dd:dd.productname}</title>
{dd:dd.head}
<style type="text/css">@import url("{dd:dd.styles}menu.css"); </style>
<script type="text/javascript" src="{dd:dd.scripts}prototype.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}duckdigit.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}ajax.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}json.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}menu.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}widget.js"></script>
<script type="text/javascript">
function sortOption (opt)
{
	$('sort_policies').value = opt;
	$('sortform').submit ();
}
</script>
</head>
<body>
<form id="sortform" method="post" action="{dd:dd.path}widget/set_admin_props.php">
<input type=hidden name='sort_policies'  id='sort_policies'>
<input type=hidden name="DD_update">
</form>
{dd:ai.bodyhead}
<table class="querylist" cellspacing="0" width="800">
<thead>
	<caption>
	{dd:-user.export}<div style="float:right;"><a href="{dd:dd.path}policylist.php/policylist.xls?xml=true{dd:page.criteria}">Export</a>&nbsp;<a href="{dd:dd.path}policylist.php/policylist.xls?xml=true{dd:page.criteria}"><img border=0 src="{dd:dd.images}excel.gif" alt="Export to Microsoft Excel"></a></div>

		Policies
	</caption>
</thead>
<tr><td colspan="9">
	<table class="filter-settings" cellpadding="0" cellspacing="0">
CONTENT;


$filtercontent = <<<CONTENT
	<tr>
		<td class="tag">{dd:headrh.tag}:</td>
		<td>{dd:headrh.name}</td>
	</tr>
CONTENT;

$policyheader=<<<CONTENT
	</table>
</td></tr>
<tr>
	<td colspan="9">
		<div style="float: right; padding-bottom: 4px;">{dd:page.pager}</div>Search: {dd:page.searchstring}{dd:page.go}&nbsp;
	</td>
</tr>
<tr>
	<th >#</th>
	<th align=left><a href="#" onclick="sortOption ('policy_no')">Policy #</a>/<a href="#" onclick="sortOption ('producer, pol_revenue desc')">Producer</a>
	</th>
	<th align=left><a href="#" onclick="sortOption ('cust_name')">Customer</a>/<a href="#" onclick="sortOption ('cust_code')">Code</a></th>
	<th align=left><a href="#" onclick="sortOption ('coverage')">Coverage</a>/<a href="#" onclick="sortOption ('insuror')">Carrier</a></th>
	<th ><a href="#" onclick="sortOption ('pol_premium desc')">Premium</a></th>
	<th ><a href="#" onclick="sortOption ('pol_revenue desc')">Revenue</a></th>
	<th nowrap><a href="#" onclick="sortOption ('pol_percent desc')">% Comm</a></th>
	<th ><a href="#" onclick="sortOption ('effdate')">Effective</a></th>
	<th ><a href="#" onclick="sortOption ('expdate')">Expires</a></th>
</tr>
CONTENT;

$content=<<<CONTENT
<tr class="{dd:rh.dd_oddeven}">
	<td valign="top" align="right">{dd:rh.rank}.</td>
	<td valign="top" ><a href="{dd:dd.path}custpolicy.php?customer_id={dd:%rh.cust_id}&policy_id={dd:%rh.pol_id}">{dd:rh.policy_no}</a>&nbsp;<br/>{dd:rh.producer}</td>
	<td valign="top" align=left nowrap><a href="{dd:dd.path}customer.php?customer_id={dd:~%rh.cust_id}">{dd:rh.cust_name}</a><br/>{dd:rh.cust_code}</td>
	<td valign="top" align=left nowrap>{dd:rh.coverage}<br/>
	{dd:rh.insuror}&nbsp;</td>
	<td valign="top" align=right>{dd:rh.pol_premium}&nbsp;</td>
	<td valign="top" align=right>{dd:rh.pol_revenue}&nbsp;</td>
	<td valign="top" align=right>{dd:rh.pol_percent}&nbsp;</td>
	<td valign="top" align=left nowrap>{dd:rh.effdate}&nbsp;</td>
	<td valign="top" align=left nowrap>{dd:rh.expdate}&nbsp;</td>
</tr>
CONTENT;



$footer=<<<CONTENT
<tfoot>
<tr class="">
	<td colspan=3>&nbsp;</td>
	<td align="right">Totals:&nbsp;</td>
	<td align=right>{dd:rh.pol_premium}&nbsp;</td>
	<td align=right>{dd:rh.pol_revenue}&nbsp;</td>
	<td align=right>{dd:rh.pol_percent}&nbsp;</td>
	<td align=right colspan=2>&nbsp;</td>
</tr>
</tfoot>
</table>
{dd:ai.bodyfoot}
</body>
</html>
CONTENT;

/************************************
** PAGE AND AUTHENTICATION
************************************/
$page = new AI_page ("page", "PolicyList");
$DD_session = new DD_Session ($AI_authpolicy);
$DD_session->doAuth ($page->m_tp);
$page->prepare();

$criteriaBuilder = new CriteriaBuilder ($_GET, $page->m_tp);

/************************************
** SQL Construction
************************************/

if (isset ($_GET['kpi_type']))
{
	$select = "select
		p.policy_id as pol_id
		, pol.policy_no
		, p.customer_id as cust_id
		, pol.cust_searchname as cust_code
		, pol.cust_name1 as cust_name
		, c.name as coverage
		, i.name as insuror
		, p.premium as pol_premium
		, p.revenue as pol_revenue
		, p.revenue / (p.premium) as pol_percent
		, pol.effdate
		, pol.expdate
		, prod.name as producer
		, prod.id as producer_code
	from
		cd_transaction p
		left outer join cd_policy pol on (pol.office_id = {user.active_office_id} and pol.id = p.policy_id)
		left outer join cd_coverage c on (c.office_id = {user.active_office_id} and c.id = p.coverage_id)
		left outer join cd_insuror i on (i.office_id = {user.active_office_id} and i.id = p.insuror_id)
		left outer join cd_staff prod on (prod.office_id = {user.active_office_id} and prod.id = p.producer_id)
	where
		(p.office_id = {user.active_office_id}
		and {SQL:kpi.kpi_clause}
		and p.trandate >= {kpi.begindate}
		and p.trandate <= {kpi.enddate})
		and p.premium is not null  and p.premium != 0"
		. $criteriaBuilder->getWhere() . 
		" order by {SQL:user.sort_policies} {SQL:page.pagelimit}";

	$countsql = "select
		count(*)
	from
		cd_transaction p
	where
		(p.office_id = {user.active_office_id}
		and {SQL:kpi.kpi_clause}
		and p.trandate >= {kpi.begindate}
		and p.trandate <= {kpi.enddate})
		and p.premium is not null  and p.premium != 0"
		. $criteriaBuilder->getWhere();
}
else
{
	$select = "select
		p.id as pol_id
		, p.policy_no
		, p.customer_id as cust_id
		, p.cust_searchname as cust_code
		, p.cust_name1 as cust_name
		, c.name as coverage
		, i.name as insuror
		, p.premium as pol_premium
		, p.revenue as pol_revenue
		, p.revenue / (p.premium) as pol_percent
		, p.effdate
		, p.expdate
		, prod.name as producer
		, prod.id as producer_code
	from
		cd_policy p
		left outer join cd_coverage c on (c.office_id = {user.active_office_id} and c.id = p.coverage_id)
		left outer join cd_insuror i on (i.office_id = {user.active_office_id} and i.id = p.insuror_id)
		left outer join cd_staff prod on (prod.office_id = {user.active_office_id} and prod.id = p.producer_id)
	where
		(p.office_id = {user.active_office_id}
		and p.book_end > date_trunc('day',now())
		and p.book_start <= date_trunc('day',now())) 
		and p.premium is not null  and p.premium != 0"
		. $criteriaBuilder->getWhere() . 
		" order by {SQL:user.sort_policies} {SQL:page.pagelimit}";

	$countsql = "select
		count(*)
	from
		cd_policy p
	where
		(p.office_id = {user.active_office_id}
		and p.book_end > date_trunc('day',now())
		and p.book_start <= date_trunc('day',now()))
		and p.premium is not null  and p.premium != 0"
		. $criteriaBuilder->getWhere();
}

if (isset ($_GET ['xml']))
{
	$page->verifyRoles ("export", "You do not have sufficient privileges to export data from AgencyInsight");
	header("Content-Type: application/ms-excel");
	$page->prepareKpiForm ();
	$xmlFactory = new DD_SqlToXmlFactory ('sql.ai', $page->m_tp);
	$xmlFactory->setEcho (TRUE);
	$xmlFactory->generateRecords ("customer", $select);
	//$content = $xmlFactory->getResult ();
	//header("Content-Length: ".strlen($content));
	//echo $content;
	exit();
}

$pagelimit = $page->makeField (array ('name'=>'pagelimit','type'=>'work'));
$pager = new PagerField (array (
		'name'=>'pager',
		'pagesize'=>'{dd:-http.pagesize|user.n_policies|=25}',
		'curpage'=>'{dd:-http.page}',
		'url'=>'policylist.php?searchas={dd:%@http.searchas}'.$criteriaBuilder->getCriteria ().'&action={dd:@http.action}',
		'connect'=>'sql.ai',
		'countsql'=>$countsql
		));
$page->addField ($pager);
$page->makeField (array ('name'=>'searchstring','type'=>'text','maxlenth'=>100,'size'=>40));
$page->makeField (array ('name'=>'go','type'=>'submit','default'=>'GO'));
$page->makeField (array ('name'=>'criteria','default'=>$criteriaBuilder->getCriteria ()));

$headrh = new DD_SqlRecordHandler (array (
	'name'=>'headrh'
	,'connect'=>'sql.ai'
	,'select'=>$criteriaBuilder->getHeaderSql ()
	,'autofields'=>true
));

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
$premium = $rh->makeField ($pricefld, array ('name'=>'pol_premium'));
$revenue = $rh->makeField ($pricefld, array ('name'=>'pol_revenue'));
$percent = $rh->makeField ($pricefld, array ('name'=>'pol_percent', 'format'=>'2'));
$rank = $rh->makeField (array ('name'=>'rank'));
$rh->makeField (array ('name'=>'effdate', 'datatype'=>'date'));
$rh->makeField (array ('name'=>'expdate', 'datatype'=>'date'));

$page->addRecordHandler ($headrh);
$page->addRecordHandler ($rh);

/************************************
* LOGIC, PAGE CONSTRUCTION AND POST PROCESSING
************************************/


$premium_total = 0;
$revenue_total = 0;

$page->prepareKpiForm ();
$page->prepareForRecord ();
$page->outputContent ($header);

if ($criteriaBuilder->getCount () > 0)
{
	$headrh->prepareForRecord ($page->m_tp);
	$headrh->query ($page->m_tp);
	$headrh->query ($page->m_tp);
	while ($headrh->nextRecord ())
	{
		$page->outputContent ($filtercontent);
	}
}

$page->outputContent ($policyheader);

$rh->prepareForRecord ($page->m_tp);

$pagelimit->set ($pager->getLimitString ());
$rh->query ($page->m_tp);
for ($i = 0; $rh->nextRecord (); $i++)
{
	$pval = $premium->getValue ();
	$cval = $revenue->getValue ();
	$premium_total += $pval;
	$revenue_total += $cval;
	if ($pval != 0)
		$percent->set ($cval / $pval * 100);
	else
		$percent->set (null);
	$rank->set (1 + $pager->getOffset () + $i);

	$page->outputContent ($content);
}
$rh->freequery ();
$premium->set ($premium_total);
$revenue->set ($revenue_total);
$percent->set ($revenue_total / $premium_total * 100);
$page->outputContent ($footer);



?>
