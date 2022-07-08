<?php

require_once ("ai/properties.php");

/***************************************************************************
*** CLASS
*****************************************************************************/
/* Color Widget
 */
class PolicyInfo extends DynamicWidget 
{
	private			$m_cols;
	function __construct ($name, $cols = 3)
	{
		parent::Widget ($name, "policyinfo", "Policy Info", "600px"); //, "400px");
		$m_cols = $cols;

	}

	function addPolicyInfo ($tagParser)
	{
		$widget_content=<<<CONTENT
<table class="querylist" cellpadding="0" cellspacing="0" width="100%">
<thead>
<tr>
	<th colspan=4 align="left">Policy Information : {dd:pol.policy_no}</th>
</tr>
</thead>
	<tr>
		<td nowrap="nowrap" valign=top class=tag>Policy Number:</td><td>{dd:pol.policy_no}</td>
		<td valign=top class=tag>Coverage:</td><td>{dd:pol.cov_code} - {dd:pol.cov_name}</td>
	</tr>
	<tr>
		<td nowrap="nowrap" valign=top class=tag>Effective Date:</td><td>{dd:pol.effdate}</td>
		<td valign=top class=tag>Term</td><td>{dd:pol.term}</td>
	</tr>
	<tr>
		<td nowrap="nowrap" valign=top class=tag>Expiration Date</td><td>{dd:pol.expdate}</td>
		<td valign=top class=tag>Status:</td><td>{dd:pol.status} / {dd:pol.business}</td>
	</tr>
	<tr>
		<td nowrap="nowrap" valign=top class=tag>Bill Method:</td><td>{dd:pol.bill_method}</td>
		<td nowrap="nowrap" valign=top class=tag>Level2org:</td><td>{dd:pol.level2org} - {dd:pol.level2name}</td>
	</tr>
	<tr>
		<td valign=top class=tag>Insuror:</td><td colspan=3>{dd:pol.ins_code} - {dd:pol.ins_name}</td>
	</tr>
	<tr>
		<td valign=top class=tag>Payee:</td><td colspan=3>{dd:pol.pay_code} - {dd:pol.pay_name}</td>
	</tr>
	<tr>
		<td nowrap="nowrap" valign=top class=tag>Named Insured:</td><td colspan=3>{dd:pol.named_insured}</td>
	</tr>
	<tr>
		<td nowrap="nowrap" valign=top class=tag>Description:</td><td colspan=3>{dd:pol.description}</td>
	</tr>
	<tr>
		<td nowrap="nowrap" valign=top class=tag>Comments:</td><td colspan=3>{dd:pol.comments}</td>
	</tr>
</table>
<table class="querylist" cellpadding="0" cellspacing="0" width="100%">
<thead>
<tr>
	<th align="left">Summary</th>
	<th align="right">Written</th>
	<th align="right">Annualized</th>
	<th align="right">Booked</th>
	<th>&nbsp;</th>
</tr>
</thead>
<tbody>
<tr>
	<td class=tag nowrap="nowrap">Premium:</td>
	<td align="right">{dd:pol.written_premium}</td>
	<td align="right">{dd:pol.premium}</td>
	<td align="right">{dd:pol.booked_premium}&nbsp;</td>
	<td align="right" width=130>&nbsp;</td>
</tr>
<tr class=odd>
	<td class=tag nowrap="nowrap">Agency Comm:</td>
	<td align="right">{dd:pol.written_revenue}</td>
	<td align="right">{dd:pol.revenue}</td>
	<td align="right">{dd:pol.booked_revenue}&nbsp;</td>
	<td align="right" >&nbsp;</td>
</tr>
<tr class=even>
	<td class=tag nowrap="nowrap">Producer Comm:</td>
	<td align="right">{dd:pol.written_producer_comm}</td>
	<td align="right">{dd:pol.producer_comm}</td>
	<td align="right">{dd:pol.booked_producer_comm}&nbsp;</td>
	<td align="right" >&nbsp;</td>
</tr>
<tr class=odd>
	<td class=tag nowrap="nowrapnowrap="nowrap"Net Comm:</td>
	<td align="right">{dd:pol.net_written_comm}</td>
	<td align="right">{dd:pol.net_revenue}</td>
	<td align="right">{dd:pol.net_booked_comm}&nbsp;</td>
	<td align="right" >&nbsp;</td>
</tr>
</tbody>
</table>
CONTENT;
		$select = "select
   			 	p.*
				,i.code as ins_code
				,i.name as ins_name
				,pay.code as pay_code
				,pay.name as pay_name
				,c.code as cov_code
				,c.name as cov_name
				,p.written_revenue - p.written_producer_comm as net_written_comm
				,p.booked_revenue - p.booked_producer_comm as net_booked_comm
				,p.revenue - p.producer_comm as net_revenue
				,l2.name as level2name
			from cd_policy p 
			 	left outer join cd_insuror i on (i.office_id = p.office_id and i.id = p.insuror_id)
			 	left outer join cd_insuror pay on (pay.office_id = p.office_id and pay.id = p.payee_id)
			 	left outer join cd_coverage c on (c.office_id = p.office_id and c.id = p.coverage_id)
			 	left outer join cd_level2org l2 on (l2.office_id = p.office_id and l2.id = p.level2org)
			where 
				p.office_id = {user.active_office_id} and p.id = {http.policy_id}
			";
		
		$rh = new DD_SqlRecordHandler (array (
			'name'=>'pol'
			,'connect'=>'sql.ai'
			,'select'=>$select
			,'autofields'=>true
		));

		$rh->makeField (array ('name'=>'effdate', 'datatype'=>'date'));
		$rh->makeField (array ('name'=>'expdate', 'datatype'=>'date'));
		$status = $rh->makeField (array ('name'=>'status'));
		$business = $rh->makeField (array ('name'=>'business'));

		$pricefld = array ( // Base price field extended below.
			'type'=>'display',
			'datatype'=>'float',
			'format'=>',$'
		);
		$rh->makeField ($pricefld, array ('name'=>'premium'));
		$rh->makeField ($pricefld, array ('name'=>'revenue'));
		$rh->makeField ($pricefld, array ('name'=>'producer_comm'));
		$rh->makeField ($pricefld, array ('name'=>'net_revenue'));

		$rh->makeField ($pricefld, array ('name'=>'written_premium'));
		$rh->makeField ($pricefld, array ('name'=>'written_revenue'));
		$rh->makeField ($pricefld, array ('name'=>'written_producer_comm'));
		$rh->makeField ($pricefld, array ('name'=>'net_written_comm'));

		$rh->makeField ($pricefld, array ('name'=>'booked_premium'));
		$rh->makeField ($pricefld, array ('name'=>'booked_revenue'));
		$rh->makeField ($pricefld, array ('name'=>'booked_producer_comm'));
		$rh->makeField ($pricefld, array ('name'=>'net_booked_comm'));

		$rh->makeField (array ('name'=>'expdate', 'datatype'=>'date'));
		$tagParser->addValueProvider ($rh);

		$text = "";
		$rh->query ($tagParser);
		if ($rh->nextRecord ())
		{
			global $AI_business;
			global $AI_status;
			$status->set ($AI_status [$status->getValue ()]);
			$business->set ($AI_business [$business->getValue ()]);
			$text .= $tagParser->parseContent ($widget_content);
		}
		$rh->freeQuery ();
		$tagParser->removeValueProviderByName ("pol");
		return $text;
	}

	function toString ($tagParser)
	{
		$text = "";
		$text .= $this->renderHeader ($tagParser);

		$text .= $this->addPolicyInfo ($tagParser);

		$text .= $this->renderFooter ($tagParser);
		return $text;
	}
	function getEditControlTitle ($tagParser)
	{
		return "Preferences";
	}
	//function getEditControlProperties ()
	//{
		//return array ('top_carriers_by_cust');
	//}

}
?>
