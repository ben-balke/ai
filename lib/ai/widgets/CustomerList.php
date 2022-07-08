<?php

require_once ("ai/properties.php");

/***************************************************************************
*** CLASS
*****************************************************************************/
/* Color Widget
 */
class CustomerList extends DynamicWidget 
{
	private			$m_cols;
	function __construct ($name, $cols = 3)
	{
		parent::Widget ($name, "Customerlist", "Customers", "600px"); //, "400px");
		$m_cols = $cols;

	}

	function addPolicies ($tagparser)
	{
		$widget_header=<<<CONTENT
<table class="querylist" cellspacing="0" width="100%">
<tr>
	<th >#</th>
	<th >Name</th>
	<th >Premium</th>
	<th >Revenue</th>
	<th >% Comm</th>
	<th >Policies</th>
	<th >Claims</th>
	<th >Code</th>
</tr>
CONTENT;
		$widget_content=<<<CONTENT
<tr class="{dd:tc.dd_oddeven}">
    <td valign="top" align="right">{dd:tc.dd_recordno}.</td>
	<td valign="top" ><a href="{dd:dd.path}customer.php?customer_id={dd:~tc.cust_id}">{dd:tc.cust_name}</a>&nbsp;</td>
	<td valign="top" align="right">{dd:tc.cust_premium}&nbsp;</td>
	<td valign="top" align="right">{dd:tc.cust_revenue}&nbsp;</td>
	<td valign="top" align="right">{dd:tc.cust_percent}&nbsp;</td>
	<td valign="top" align="right" nowrap="nowrap">{dd:tc.policy_count}&nbsp;</td>
	<td valign="top" align="right" nowrap="nowrap">{dd:tc.claim_count}&nbsp;</td>
	<td valign="top" align="left" nowrap="nowrap">{dd:tc.cust_code}&nbsp;</td>
</tr>
CONTENT;

        $widget_footer=<<<CONTENT
<tfoot>
<tr class="">
    <td colspan=1>&nbsp;</td>
    <td align="right">Totals:&nbsp;</td>
    <td align="right">{dd:tc.cust_premium}&nbsp;</td>
    <td align="right">{dd:tc.cust_revenue}&nbsp;</td>
    <td align="right">{dd:tc.cust_percent}&nbsp;</td>
    <td align="right">{dd:tc.policy_count}&nbsp;</td>
    <td align="right">{dd:tc.claim_count}&nbsp;</td>
    <td align="right" colspan="2">&nbsp;</td>
</tr>
</tfoot>
</table>
CONTENT;

		$where = "";
		foreach ($_GET as $column=>$value)
		{
			switch ($column)
			{
			case 'coverage_id':
				$where .= " and p.coverage_id='" . @addslashes ($value) . "' ";
				break;
			case 'siccode':
				$where .= " and p.siccode='" . @addslashes ($value) . "' ";
				break;
			case 'sicmajor':
				$where .= " and p.sicmajor='" . @addslashes ($value) . "' ";
				break;
			case 'producer_id':
				$where .= " and p.producer_id='" . @addslashes ($value) . "' ";
				break;
			case 'servicer_id':
				$where .= " and p.servicer_id='" . @addslashes ($value) . "' ";
				break;
			case 'insuror_id':
				$where .= " and p.insuror_id='" . @addslashes ($value) . "' ";
				break;
			case 'state':
				$where .= " and p.state='" . @addslashes ($value) . "' ";
				break;
			}
		}




		$select = "select
			distinct cust.id as cust_id
			, cust.name1 as cust_name
			, cust.searchname as cust_code
			, sum(p.premium) as cust_premium
			, sum(p.revenue) as cust_revenue
			, sum(p.revenue) / sum(p.premium) as cust_percent
			, count(*) as policy_count
			, null as claim_count
		from
			cd_policy p
			left outer join cd_customer cust on (cust.office_id = p.office_id and cust.id = p.customer_id)
		where
			(p.office_id = {user.active_office_id}
    		and p.expdate > date_trunc('day',now())
    		and p.effdate <= date_trunc('day',now())) " .
			" and p.premium is not null  and p.premium != 0"
			. $where . " group by cust_id, cust_name, cust_code order by cust_premium desc limit 40";

			//" order by {SQL:user.sort_policylist}";

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
		$premium = $rh->makeField ($pricefld, array ('name'=>'cust_premium'));
		$revenue = $rh->makeField ($pricefld, array ('name'=>'cust_revenue'));
 		$percent = $rh->makeField ($pricefld, array ('name'=>'cust_percent', 'format'=>'2'));
		$policy_count = $rh->makeField (array ('name'=>'policy_count'));
		$premium_total = 0;
		$revenue_total = 0;
		$policy_count_total = 0;


		$tagparser->addValueProvider ($rh);

		$text .= $tagparser->parseContent ($widget_header);
		$rh->query ($tagparser);
		for ($i = 0; $rh->nextRecord (); $i++)
		{
			$pval = $premium->getValue ();
			$cval = $revenue->getValue ();
			$premium_total += $pval;
			$revenue_total += $cval;
			$policy_count_total += $policy_count->getValue ();
			if ($pval != 0)
				$percent->set ($cval / $pval * 100);
			else
				$percent->set (null);
			$text .= $tagparser->parseContent ($widget_content);
		}
		$rh->freequery ();
        $premium->set ($premium_total);
        $revenue->set ($revenue_total);
        $percent->set ($revenue_total / $premium_total * 100);
        $policy_count->set ($policy_count_total);
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
