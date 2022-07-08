<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/htdocs/sagitta/premiums.php,v 1.3 2009/10/28 19:02:05 secwind Exp $
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
<tr><td>rowid</td><td colspan=20>{dd:rh.rowid}</td></tr>
<tr><td>prem_seq_nr</td><td colspan=20>{dd:rh.prem_seq_nr}</td></tr>
<tr><td>pol_seq_nr</td><td colspan=20>{dd:rh.pol_seq_nr}</td></tr>
<tr><td>plan</td><td colspan=20>{dd:rh.plan}</td></tr>
<tr><td>day</td><td colspan=20>{dd:rh.day}</td></tr>
<tr><td>deposit_amnt</td><td colspan=20>{dd:rh.deposit_amnt}</td></tr>
<tr><td>down</td><td colspan=20>{dd:rh.down}</td></tr>
<tr><td>nr_of_p</td><td colspan=20>{dd:rh.nr_of_p}</td></tr>
<tr><td>nr_of_m</td><td colspan=20>{dd:rh.nr_of_m}</td></tr>
<tr><td>fee_amnt</td><td colspan=20>{dd:rh.fee_amnt}</td></tr>
<tr><td>fee_percent</td><td colspan=20>{dd:rh.fee_percent}</td></tr>
<tr><td>pmt_plan_remarks</td><td colspan=20>{dd:rh.pmt_plan_remarks}</td></tr>
<tr><td>anndate</td><td colspan=20>{dd:rh.anndate}</td></tr>
<tr><td>utm</td><td colspan=20>{dd:rh.utm}</td></tr>
<tr><td>ni</td><td colspan=20>{dd:rh.ni}</td></tr>
<tr><td>sales_id</td><td colspan=20>{dd:rh.sales_id}</td></tr>
<tr><td>nr_of_inv</td><td colspan=20>{dd:rh.nr_of_inv}</td></tr>
<tr><td>purge_date</td><td colspan=20>{dd:rh.purge_date}</td></tr>
<tr><td>trn</td><td align=right>{dd:rh.trn}</td></tr>
<tr><td>trandate</td><td align=right>{dd:rh.trandate}</td></tr>
<tr><td>entdate</td><td align=right>{dd:rh.entdate}</td></tr>
<tr><td>trans_amnt</td><td align=right>{dd:rh.trans_amnt}</td></tr>
<tr><td>agt_percent</td><td align=right>{dd:rh.agt_percent}</td></tr>
<tr><td>agcy_comm</td><td align=right>{dd:rh.agcy_comm}</td></tr>
<tr><td>producer</td><td align=right>{dd:rh.producer}</td></tr>
<tr><td>prod_percent</td><td align=right>{dd:rh.prod_percent}</td></tr>
<tr><td>prod_comm</td><td align=right>{dd:rh.prod_comm}</td></tr>
<tr><td>invoice_nr</td><td align=right>{dd:rh.invoice_nr}</td></tr>
<tr><td>ari</td><td align=right>{dd:rh.ari}</td></tr>
<tr><td>staff_code</td><td align=right>{dd:rh.staff_code}</td></tr>
<tr><td>department</td><td align=right>{dd:rh.department}</td></tr>
<tr><td>billto</td><td align=right>{dd:rh.billto}</td></tr>
<tr><td>payee</td><td align=right>{dd:rh.payee}</td></tr>
<tr><td>cov</td><td align=right>{dd:rh.cov}</td></tr>
<tr><td>ins</td><td align=right>{dd:rh.ins}</td></tr>
<tr><td>Tran Total</td><td align=right>{dd:rh.trantot}</td></tr>
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
	, ai_sag_get_invoiced_values(invoice_nr, trans_amnt) as trantot 
from office{SQL:user.active_office_id}.premiums 
where pol_seq_nr = {http.id}";


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

$arrays [] = $rh->makeField (array ('name'=>'trn'));
$arrays [] = $rh->makeField (array ('name'=>'trandate'));
$arrays [] = $rh->makeField (array ('name'=>'entdate'));
$arrays [] = $rh->makeField (array ('name'=>'trans_amnt'));
$arrays [] = $rh->makeField (array ('name'=>'agt_percent'));
$arrays [] = $rh->makeField (array ('name'=>'agcy_comm'));
$arrays [] = $rh->makeField (array ('name'=>'producer'));
$arrays [] = $rh->makeField (array ('name'=>'prod_comm'));
$arrays [] = $rh->makeField (array ('name'=>'invoice_nr'));
$arrays [] = $rh->makeField (array ('name'=>'staff_code'));
$arrays [] = $rh->makeField (array ('name'=>'department'));
$arrays [] = $rh->makeField (array ('name'=>'billto'));
$arrays [] = $rh->makeField (array ('name'=>'payee'));
$arrays [] = $rh->makeField (array ('name'=>'cov'));
$arrays [] = $rh->makeField (array ('name'=>'ins'));
$arrays [] = $rh->makeField (array ('name'=>'ari'));

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
