<?php

require_once ("ai/properties.php");

/***************************************************************************
*** CLASS
*****************************************************************************/
/* Color Widget
 */
class CustomerInfo extends DynamicWidget 
{
	private			$m_cols;
	function __construct ($name, $cols = 3)
	{
		parent::Widget ($name, "customerinfo", "Customer Info", "600px"); //, "400px");
		$m_cols = $cols;

	}

	function addCustomerInfo ($tagParser)
	{
		$widget_content=<<<CONTENT
<table class="querylist" cellpadding="0" cellspacing="0" width="100%">
<thead>
<tr>
	<th colspan=4 align="left">{dd:cust.name1} ({dd:cust.searchname})</th>
</tr>
</thead>
	<tr>
		<td class="tag" valign="top">Name:</td><td>{dd:cust.name1} (<a href="{dd:dd.path}customer.php?customer_id={dd:%cust.id}">{dd:cust.searchname}</a>)</td>
		<td class="tag" valign="top">Bill To:</td><td>{dd:cust.billtoname} (<a href="{dd:dd.path}customer.php?customer_id={dd:%cust.billtoid}">{dd:cust.billtocode}</a>)</td>
	</tr>
	<tr>
		<td class="tag" valign="top">Level1Org:</td><td>{dd:cust.level1org} - {dd:@cust.level1name}</td>
		<td class="tag" valign="top">Producer:</td><td><a href="{dd:dd.path}producer.php?id={dd:cust.producer1_id}">{dd:cust.producername}</a></td>
	</tr>
	<tr>
		<td class="tag" valign="top">Address1:</td>
		<td>
			{dd:cust.addr1}
			<br/>{dd:-+cust.addr2}
			<br/>{dd:cust.city}, {dd:cust.statecode}, {dd:cust.postalcode}
			<br/>{dd:cust.postalcode}
		</td>
		<td class="tag" valign="top">Servicer:</td><td valign="top"><a href="{dd:dd.path}servicer.php?id={dd:cust.servicer_id}">{dd:cust.servicername}</a></td>
	</tr>
	<tr>
		<td class="tag" valign="top">SIC Code:</td><td>{dd:cust.siccode}</td>
	</tr>
</table>
CONTENT;
		$select = "select
   			 	c.*
				, b.name1 as billtoname
				, b.searchname as billtocode 
				, b.id as billtoid
				, s.name as servicername 
				, p.name as producername 
				, l1.name as level1name
			from cd_customer c 
			 	left outer join cd_customer b on (b.office_id = c.office_id and b.id = c.billto)
			 	left outer join cd_staff s on (s.office_id = c.office_id and s.id = c.servicer_id)
			 	left outer join cd_staff p on (p.office_id = c.office_id and p.id = c.producer1_id)
				left outer join cd_level1org l1 on (l1.office_id = c.office_id and l1.id = c.level1org)
			where 
				c.office_id = {user.active_office_id} and c.id = {http.customer_id}
			";
		
		$rh = new DD_SqlRecordHandler (array (
			'name'=>'cust'
			,'connect'=>'sql.ai'
			,'select'=>$select
			,'autofields'=>true
		));

		$pricefld = array ( // Base price field extended below.
			'type'=>'display',
			'datatype'=>'float',
			'format'=>',$'
		);
		$tagParser->addValueProvider ($rh);

		$rh->query ($tagParser);
		$text = "";
		if ($rh->nextRecord ())
		{
			$text .= $tagParser->parseContent ($widget_content);
		}
		$rh->freeQuery ();
		$tagParser->removeValueProviderByName ("cust");
		return $text;
	}

	function toString ($tagParser)
	{
		$text = "";
		$text .= $this->renderHeader ($tagParser);

		$text .= $this->addCustomerInfo ($tagParser);

		$text .= $this->renderFooter ($tagParser);
		return $text;
	}
	function getEditControlTitle ($tagParser)
	{
		return "Preferences";
	}
	function getEditControlProperties ()
	{
		return array ('n_top_carriers_by_cust');
	}

}
?>
