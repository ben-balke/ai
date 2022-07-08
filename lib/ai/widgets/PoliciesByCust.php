<?php

require_once ("ai/properties.php");

/***************************************************************************
*** CLASS
*****************************************************************************/
/* Color Widget
 */
class PoliciesByCust extends DynamicWidget 
{
	private			$m_cols;
	function __construct ($name, $cols = 3)
	{
		parent::Widget ($name, "policiesbycust", "Active Customer Policies", "600px"); //, "400px");
		$m_cols = $cols;

	}

	function addPolicies ($tagparser)
	{
		$widget_header=<<<CONTENT
<table class="querylist" cellspacing="0" width="100%">
<tr>
	<th >#</th>
	<th >Policy #</th>
	<th >Coverage/Carrier</th>
	<th >Premium</th>
	<th >Revenue</th>
	<th >% Comm</th>
	<th >Effective</th>
	<th >Expires</th>
</tr>
CONTENT;
		$widget_content=<<<CONTENT
<tr class="{dd:tc.dd_oddeven}">
    <td valign="top" align="right">{dd:tc.dd_recordno}.</td>
	<td valign="top" ><a href="{dd:dd.path}custpolicy.php?customer_id={dd:@tc.customer_id}&policy_id={dd:~tc.id}">{dd:tc.policy_no}</a>&nbsp;</td>
	<td valign="top" align="left" nowrap="nowrap">{dd:tc.coverage}<br/>
	{dd:tc.insuror}&nbsp;</td>
	<td valign="top" align="right">{dd:tc.premium}&nbsp;</td>
	<td valign="top" align="right">{dd:tc.revenue}&nbsp;</td>
	<td valign="top" align="right">{dd:tc.commission}&nbsp;</td>
	<td valign="top" align="left" nowrap="nowrap">{dd:tc.effdate}&nbsp;</td>
	<td valign="top" align="left" nowrap="nowrap">{dd:tc.expdate}&nbsp;</td>
</tr>
CONTENT;

        $widget_footer=<<<CONTENT
<tfoot>
<tr class="">
    <td colspan="2">&nbsp;</td>
    <td align="right">Totals:&nbsp;</td>
    <td align="right">{dd:tc.premium}&nbsp;</td>
    <td align="right">{dd:tc.revenue}&nbsp;</td>
    <td align="right">{dd:tc.commission}&nbsp;</td>
    <td align="right" colspan="2">&nbsp;</td>
</tr>
</tfoot>
</table>
CONTENT;


		$select = "select
			p.id as id
			, p.policy_no 
			, p.policy_no as itemcode
			, c.name as coverage
			, p.customer_id
			, i.name as insuror
			, p.premium as premium
			, p.revenue as revenue
			, p.revenue / p.premium as commission
			, p.effdate
			, p.expdate
		from
			cd_policy p
			left outer join cd_coverage c on (c.office_id = p.office_id and c.id = p.coverage_id)
			left outer join cd_insuror i on (i.office_id = p.office_id and i.id = p.insuror_id)
		where
			p.office_id = {user.active_office_id}
			and p.customer_id = {http.customer_id} 
    		and p.book_end > date_trunc('day',now())
    		and p.book_start <= date_trunc('day',now())
			and p.premium is not null  and p.premium != 0
		order by {SQL:user.sort_policies_by_cust}";

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
 		$rh->makeField (array ('name'=>'effdate', 'datatype'=>'date'));
 		$rh->makeField (array ('name'=>'expdate', 'datatype'=>'date'));
		$premium_total = 0;
		$revenue_total = 0;


		$tagparser->addValueProvider ($rh);

		$text = $tagparser->parseContent ($widget_header);
		$rh->query ($tagparser);
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
			$text .= $tagparser->parseContent ($widget_content);
		}
		$rh->freequery ();
        $premium->set ($premium_total);
        $revenue->set ($revenue_total);
        $commission->set ($revenue_total / $premium_total * 100);
        $text .= $tagparser->parseContent ($widget_footer);
		$tagparser->removevalueproviderbyname ("tc");
		return $text;
	}

	function tostring ($tagparser)
	{
		$text = "";
		$text .= $this->renderheader ($tagparser);

		$text .= $this->addpolicies ($tagparser);

		$text .= $this->renderfooter ($tagparser);
		return $text;
	}
	function geteditcontroltitle ($tagparser)
	{
		return "preferences";
	}
	function geteditcontrolproperties ()
	{
		return array (
			'sort_policies_by_cust'
		);
	}

}
?>
