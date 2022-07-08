<?php

require_once ("ai/properties.php");

/***************************************************************************
*** CLASS
*****************************************************************************/
/* Color Widget
 */
class OfficeSummary extends DynamicWidget 
{
	private			$m_cols;
	function __construct ($name, $cols = 3)
	{
		parent::Widget ($name, "officesummary", "Office Summary", "600px"); //, "400px");
		$m_cols = $cols;

	}

	function addOfficeInfo ($tagParser)
	{
		$widget_content=<<<CONTENT
<table class="querylist" cellpadding="0" cellspacing="0" width="100%">
<thead>
<tr>
	<th align="left">{dd:office.name} ({dd:office.id})</th>
	<th align="right" class=tag>Written</th>
	<th align="right" class=tag>Annualized</th>
	<th align="right" class=tag>Booked</th>
</tr>
</thead>
	<tr class=odd>
		<td class="tag" valign="top" align="right">Premium:</td>
			<td align="right">{dd:summary.written_premium}</td>
			<td align="right">{dd:summary.premium}</td>
			<td align="right">{dd:summary.booked_premium}</td>
	</tr>
	<tr>
		<td class="tag" valign="top" align="right">Revenue:</td>
			<td align="right">{dd:summary.written_revenue}</td>
			<td align="right">{dd:summary.revenue}</td>
			<td align="right">{dd:summary.booked_revenue}</td>
	</tr>
</table>
CONTENT;
		$selecthdr = "select * from ai_office where id = {user.active_office_id}";
		$hdrrh = new DD_SqlRecordHandler (array (
			'name'=>'office'
			,'connect'=>'sql.ai'
			,'select'=>$selecthdr
			,'autofields'=>true
		));

		$select = "select
    		sum (p.premium) as premium
    		, sum (p.revenue) as revenue
    		, sum (p.revenue) / sum (p.premium) * 100 as commission
    		,sum (p.written_premium) as written_premium
    		, sum (p.written_revenue) as written_revenue
    		, sum (p.written_revenue) / sum (p.written_premium) * 100 as written_commission
    		,sum (p.booked_premium) as booked_premium
    		, sum (p.booked_revenue) as booked_revenue
    		, sum (p.booked_revenue) / sum (p.booked_premium) * 100 as booked_commission
    		, count (*) as polcount
    		, count (distinct customer_id) as custcount
		from
    		cd_policy p
		where
    		p.office_id = {user.active_office_id}
    		and p.book_end > date_trunc('day',now())
    		and p.book_start <= date_trunc('day',now())
    		and p.premium is not null and p.premium != 0";
	
		$rh = new DD_SqlRecordHandler (array (
			'name'=>'summary'
			,'connect'=>'sql.ai'
			,'select'=>$select
			,'autofields'=>true
		));

		$pricefld = array ( // Base price field extended below.
			'type'=>'display',
			'datatype'=>'float',
			'format'=>',$'
		);
		$rh->makeField ($pricefld, array ('name'=>'premium'));
		$rh->makeField ($pricefld, array ('name'=>'written_premium'));
		$rh->makeField ($pricefld, array ('name'=>'booked_premium'));
		$rh->makeField ($pricefld, array ('name'=>'revenue'));
		$rh->makeField ($pricefld, array ('name'=>'written_revenue'));
		$rh->makeField ($pricefld, array ('name'=>'booked_revenue'));
		$tagParser->addValueProvider ($rh);
		$tagParser->addValueProvider ($hdrrh);

		$hdrrh->query ($tagParser);
		$rh->query ($tagParser);
		if ($hdrrh->nextRecord () && $rh->nextRecord ())
		{
			$text .= $tagParser->parseContent ($widget_content);
		}
		$hdrrh->freeQuery ();
		$rh->freeQuery ();
		$tagParser->removeValueProviderByName ("office");
		$tagParser->removeValueProviderByName ("summar");
		return $text;
	}

	function toString ($tagParser)
	{
		$text = "";
		$text .= $this->renderHeader ($tagParser);

		$text .= $this->addOfficeInfo ($tagParser);

		$text .= $this->renderFooter ($tagParser);
		return $text;
	}
	function getEditControlTitle ($tagParser)
	{
		return "Preferences";
	}
	//function getEditControlProperties ()
	//{
		//return array ('n_top_carriers_by_cust');
	//}
}
?>
