<?php

require_once ("ai/properties.php");

/***************************************************************************
*** CLASS
*****************************************************************************/
/* Color Widget
 */
class TopCoveragesByCust extends DynamicWidget 
{
	private			$m_cols;
	function __construct ($name, $cols = 3)
	{
		parent::Widget ($name, "topcoveragesbycust", "Top Customer Coverages", "600px"); //, "400px");
		$m_cols = $cols;

	}

	function addCoverages ($tagParser)
	{
		$widget_header=<<<CONTENT
<table class="querylist" cellspacing="0" width="100%">
<tr>
	<th >#</th>
	<th >Top {dd:user.n_top_coverages_by_cust} Coverages</th>
	<th >Premium</th>
	<th >Revenue</th>
	<th >% Comm</th>
	<th >code</th>
</tr>
CONTENT;
		$widget_coverages=<<<CONTENT
<tr class="{dd:tc.dd_oddeven}">
    <td align="right">{dd:tc.dd_recordno}.</td>
	<td ><a href="{dd:dd.path}queryforcov.php?id={dd:~tc.itemid}">{dd:tc.description}</a>&nbsp;</td>
	<td align="right">{dd:tc.premium}&nbsp;</td>
	<td align="right">{dd:tc.revenue}&nbsp;</td>
	<td align="right">{dd:tc.commission}&nbsp;</td>
	<td align="right">{dd:tc.itemcode}&nbsp;</td>
</tr>
CONTENT;
        $widget_footer=<<<CONTENT
<tfoot>
<tr class="">
    <td colspan="2"><a href="{dd:dd.path}querybyins.php">See All Coverages</a></td>
    <td align="right">{dd:tc.premium}&nbsp;</td>
    <td align="right">{dd:tc.revenue}&nbsp;</td>
    <td align="right">{dd:tc.commission}&nbsp;</td>
    <td align="right">&nbsp;</td>
</tr>
</tfoot>
</table>
CONTENT;

		$select = "select
			p.coverage_id as itemid
			, c.code as itemcode
			, c.name as description
			, sum (p.premium) as premium
			, sum (p.revenue) as revenue
			, sum (p.revenue) / sum (p.premium) as commission
		from
			cd_policy p
			left outer join cd_coverage c on (c.office_id = p.office_id and c.id = p.coverage_id)
		where
			p.office_id = {user.active_office_id}
			and p.customer_id = {http.customer_id} 
    		and p.book_end > date_trunc('day',now())
    		and p.book_start <= date_trunc('day',now())
			and p.premium is not null and p.premium != 0
		group by itemid, itemcode, c.name
		order by {SQL:user.sort_top_coverages_by_cust} limit {user.n_top_coverages_by_cust};";

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
			$text .= $tagParser->parseContent ($widget_coverages);
		}
        $premium->set ($premium_total);
        $revenue->set ($revenue_total);
        $commission->set ($revenue_total / $premium_total * 100);
        $text .= $tagParser->parseContent ($widget_footer);
        $rh->freeQuery ();
        $tagParser->removeValueProviderByName ("tc");

		return $text;
	}

	function toString ($tagParser)
	{
		$text = "";
		$text .= $this->renderHeader ($tagParser);

		$text .= $this->addCoverages ($tagParser);

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
			'n_top_coverages_by_cust'
			,'sort_top_coverages_by_cust'
		);
	}

}
?>
