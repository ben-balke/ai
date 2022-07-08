<?php

require_once ("ai/properties.php");

/***************************************************************************
*** CLASS
*****************************************************************************/
/* Color Widget
 */
class PolicyList extends DynamicWidget 
{
	private			$m_cols;
	function __construct ($name, $cols = 3)
	{
		parent::Widget ($name, "policylist", "Policies", "600px"); //, "400px");
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
	<td valign="top" ><a href="{dd:dd.path}custpolicy.php?policy_id={dd:~tc.id}">{dd:tc.policy_no}</a>&nbsp;</td>
	<td valign="top" align="left" nowrap="nowrap">{dd:tc.coverage}<br/>
	{dd:tc.insuror}&nbsp;</td>
	<td valign="top" align="right">{dd:tc.premium}&nbsp;</td>
	<td valign="top" align="right">{dd:tc.revenue}&nbsp;</td>
	<td valign="top" align="right">{dd:tc.percent}&nbsp;</td>
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
    <td align="right">{dd:tc.percent}&nbsp;</td>
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
			p.id as id
			, p.policy_no 
			, cust.searchname
			, c.name as coverage
			, i.name as insuror
			, p.premium as premium
			, p.revenue as revenue
			, p.revenue / (p.premium) as percent
			, p.effdate
			, p.expdate
		from
			cd_policy p
			left outer join cd_customer cust on (cust.office_id = p.office_id and cust.id = p.customer_id)
			left outer join cd_coverage c on (c.office_id = p.office_id and c.id = p.coverage_id)
			left outer join cd_insuror i on (i.office_id = p.office_id and i.id = p.insuror_id)
		where
			(p.office_id = {user.active_office_id}
    		and p.book_end > date_trunc('day',now())
    		and p.book_start <= date_trunc('day',now())) " .
			" and p.premium is not null  and p.premium != 0"
			. $where . " order by p.premium desc";

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
		$premium = $rh->makeField ($pricefld, array ('name'=>'premium'));
		$revenue = $rh->makeField ($pricefld, array ('name'=>'revenue'));
 		$percent = $rh->makeField ($pricefld, array ('name'=>'percent', 'format'=>'2'));
 		$rh->makeField (array ('name'=>'effdate', 'datatype'=>'date'));
 		$rh->makeField (array ('name'=>'expdate', 'datatype'=>'date'));
		$premium_total = 0;
		$revenue_total = 0;


		$tagparser->addValueProvider ($rh);

		$text .= $tagparser->parseContent ($widget_header);
		$rh->query ($tagparser);
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
			$text .= $tagparser->parseContent ($widget_content);
		}
		$rh->freequery ();
        $premium->set ($premium_total);
        $revenue->set ($revenue_total);
        $percent->set ($revenue_total / $premium_total * 100);
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
