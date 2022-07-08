<?php

require_once ("ai/properties.php");

/***************************************************************************
*** CLASS
*****************************************************************************/
/* Color Widget
 */
class TopCustomers extends DynamicWidget 
{
	private			$m_cols;
	function __construct ($name, $cols = 3)
	{
		parent::Widget ($name, "topcustomers", "Top Customers", "600px"); //, "400px");
		$m_cols = $cols;

	}

	function addCustomers ($tagParser)
	{
		$widget_header=<<<CONTENT
<table class="querylist" cellpadding="0" cellspacing="0" width="100%">
<thead>
<tr>
	<th >#</th>
	<th align="left">Top {dd:user.n_top_customers} Customers</th>
	<th >Premium</th>
	<th >Revenue</th>
	<th ># of Policies</th>
	<th ># of Claims</th>
</tr>
</thead>
CONTENT;
		$widget_customers=<<<CONTENT
<tr class="{dd:tc.dd_oddeven}">
    <td align="right">{dd:tc.dd_recordno}.</td>
	<td ><a href="{dd:dd.path}customer.php?customer_id={dd:tc.customer_id}">{dd:customer.customer_name}</a>&nbsp;</td>
	<td align="right">{dd:tc.premium}&nbsp;</td>
	<td align="right">{dd:tc.revenue}&nbsp;</td>
	<td align="right">{dd:tc.policies}&nbsp;</td>
	<td align="right">{dd:tc.claims}&nbsp;</td>
</tr>
CONTENT;
        $widget_footer=<<<CONTENT
<tfoot>
<tr class="">
    <td colspan="2"><a href="{dd:dd.path}customerlist.php">Search Customers</a></td>
    <td align="right">{dd:tc.premium}&nbsp;</td>
    <td align="right">{dd:tc.revenue}&nbsp;</td>
    <td align="right">{dd:tc.policies}&nbsp;</td>
    <td align="right">{dd:tc.claims}&nbsp;</td>
</tr>
</tfoot>
</table>
CONTENT;

		$filterclause = '';
        $filter = $tagParser->getValue ('user.filter');
        if (isset ($filter) && @strlen ($filter) != 0)
        {
            $filterclause = ' and ' . $filter;
        }


		$select = "select 
				p.customer_id, 
				sum (p.premium) as premium, 
				sum (p.revenue) as revenue, 
				count(p.*) as policies
			from 
				cd_policy p
			where 
				p.office_id = {user.active_office_id}
    			and p.book_end > date_trunc('day',now())
    			and p.book_start <= date_trunc('day',now())
				and p.premium is not null
				${filterclause}
			group by p.customer_id order by {SQL:user.sort_top_customers}
			limit {user.n_top_customers};";
		
		$selectcustomer = "select name1 as customer_name from cd_customer where office_id = {user.active_office_id} and id = {tc.customer_id}";
		$rh = new DD_SqlRecordHandler (array (
			'name'=>'tc'
			,'connect'=>'sql.ai'
			,'select'=>$select
			,'autofields'=>true
		));
		$rhcustomer = new DD_SqlRecordHandler (array (
			'name'=>'customer'
			,'connect'=>'sql.ai'
			,'select'=>$selectcustomer
			,'autofields'=>true
		));

		$pricefld = array ( // Base price field extended below.
			'type'=>'display',
			'datatype'=>'float',
			'format'=>',$'
		);
		$premium = $rh->makeField ($pricefld, array ('name'=>'premium'));
		$revenue = $rh->makeField ($pricefld, array ('name'=>'revenue'));
		$policies = $rh->makeField (array ('name'=>'policies'));

        $premium_total = 0;
        $revenue_total = 0;
        $policies_total = 0;

		$tagParser->addValueProvider ($rh);
		$tagParser->addValueProvider ($rhcustomer);

		$text = $tagParser->parseContent ($widget_header);
		$rh->query ($tagParser);
		for ($i = 0; $rh->nextRecord (); $i++)
		{
            $pval = $premium->getValue ();
            $cval = $revenue->getValue ();
            $pols = $policies->getValue ();
            $premium_total += $pval;
            $revenue_total += $cval;
            $policies_total += $pols;

			$rhcustomer->query ($tagParser);
			$rhcustomer->nextRecord ();
			$rhcustomer->freeQuery ();
			$text .= $tagParser->parseContent ($widget_customers);
		}
        $premium->set ($premium_total);
        $revenue->set ($revenue_total);
        $policies->set ($policies_total);
        $text .= $tagParser->parseContent ($widget_footer);
        $rh->freeQuery ();
        $tagParser->removeValueProviderByName ("tc");

		return $text;
	}

	function toString ($tagParser)
	{
		$text = "";
		$text .= $this->renderHeader ($tagParser);

		$text .= $this->addCustomers ($tagParser);

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
			'n_top_customers'
			,'sort_top_customers'
		);
	}

}
?>
