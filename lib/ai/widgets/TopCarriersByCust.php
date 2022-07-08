<?php

require_once ("ai/properties.php");

/***************************************************************************
*** CLASS
*****************************************************************************/
/* Color Widget
 */
class TopCarriersByCust extends DynamicWidget 
{
	private			$m_cols;
	function __construct ($name, $cols = 3)
	{
		parent::Widget ($name, "topcarriers", "Top Customer Insurors", "600px"); //, "400px");
		$m_cols = $cols;

	}

	function addInsurors ($tagParser)
	{
		$widget_header=<<<CONTENT
<table class="querylist" cellpadding="0" cellspacing="0" width="100%">
<thead>
<tr>
	<th >#</th>
	<th >Top {dd:user.n_top_carriers_by_cust} Insurors</th>
	<th >Premium</th>
	<th >Revenue</th>
	<th >% Comm</th>
	<th >code</th>
</tr>
</thead>
CONTENT;
		$widget_carriers=<<<CONTENT
<tr class="{dd:tc.dd_oddeven}">
    <td align="right">{dd:tc.dd_recordno}.</td>
	<td><a href="{dd:dd.path}queryforins.php?id={dd:tc.id}">{dd:tc.description}</a>&nbsp;</td>
	<td align="right">{dd:tc.premium}&nbsp;</td>
	<td align="right">{dd:tc.revenue}&nbsp;</td>
	<td align="right">{dd:tc.commission}&nbsp;</td>
	<td align="right">{dd:tc.code}&nbsp;</td>
</tr>
CONTENT;
		$widget_footer=<<<CONTENT
<tfoot>
<tr class="">
    <td align="right">&nbsp;</td>
	<td>&nbsp;</td>
	<td align="right">{dd:tc.premium}&nbsp;</td>
	<td align="right">{dd:tc.revenue}&nbsp;</td>
	<td align="right">{dd:tc.commission}&nbsp;</td>
	<td align="right">&nbsp;</td>
</tr>
</tfoot>
</table>
CONTENT;
		$select = "select
   			 p.insuror_id as id
				, i.code as code
				, i.name as description
				, sum (p.premium) as premium
				, sum (p.revenue) as revenue
				, sum (p.revenue) / sum (p.premium) as commission
			from
				cd_policy p
				left outer join cd_insuror i on (i.office_id = p.office_id and i.id = p.insuror_id)
			where
				p.office_id = {user.active_office_id}
    			and p.book_end > date_trunc('day',now())
    			and p.book_start <= date_trunc('day',now())
				and p.customer_id = {http.customer_id} 
				and p.premium is not null and p.premium != 0
			group by p.insuror_id, i.code, i.name
			order by {SQL:user.sort_top_carriers_by_cust}
			limit {user.n_top_carriers_by_cust};";
		
		$rh = new DD_SqlRecordHandler (array (
			'name'=>'tc'
			,'connect'=>'sql.ai'
			,'select'=>$select
			,'autofields'=>true
		));

		$pricefld = array ( // Base price field extended below.
			'type'=>'display',
			'datatype'=>'float',
			'format'=>',$'
		);
		$premium = $rh->makeField ($pricefld, array ('name'=>'premium'));
		$revenue = $rh->makeField ($pricefld, array ('name'=>'revenue'));
 		$commission = $rh->makeField ($pricefld, array ('name'=>'commission', 'format'=>'2'));

		$premium_total = 0;
		$revenue_total = 0;

		$tagParser->addValueProvider ($rh);

		$text = $tagParser->parseContent ($widget_header);
		$rh->query ($tagParser);
		for ($i = 0; $rh->nextRecord (); $i++)
		{
			$pval = $premium->getValue ();
			$cval = $revenue->getValue ();
			$premium_total += $pval;
			$revenue_total += $cval;
			if ($pval != 0)
				$commission->set ($cval / $pval * 100);
			else
				$commission->set (null);
			$text .= $tagParser->parseContent ($widget_carriers);
		}
		$premium->set ($premium_total);
		$revenue->set ($revenue_total);
		$commission->set ($revenue_total / $premium_total * 100);
		$rh->freeQuery ();
		$text .= $tagParser->parseContent ($widget_footer);
		$tagParser->removeValueProviderByName ("tc");
		return $text;
	}

	function toString ($tagParser)
	{
		$text = "";
		$text .= $this->renderHeader ($tagParser);

		$text .= $this->addInsurors ($tagParser);

		$text .= $this->renderFooter ($tagParser);
		return $text;
	}
	function getEditControlTitle ($tagParser)
	{
		return "Preferences";
	}
	function getEditControlProperties ()
	{
		return array (
			'n_top_carriers_by_cust'
			,'sort_top_carriers_by_cust' 
		);
	}

}
?>
