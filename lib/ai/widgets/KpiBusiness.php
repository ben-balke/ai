<?php

require_once ("ai/properties.php");

/***************************************************************************
*** CLASS
*****************************************************************************/
/* Color Widget
 */
class KpiBusiness extends DynamicWidget 
{
	private			$m_type;
	private			$m_typecriteria;
	private			$m_typename;
	function __construct ($name, $type = 'N')
	{
		switch ($type)
		{
		case 'N': 
			$this->m_typename = 'New'; 
			$this->m_typecriteria = "trantype = 'N'"; 
			break;
		case 'R': 
			$this->m_typename = 'Renewal'; 
			$this->m_typecriteria = "trantype = 'R'"; 
			break;
		case 'W': 
			$this->m_typename = 'Rewrite'; 
			$this->m_typecriteria = "trantype = 'W'"; 
			break;
		case 'LC': 
		case 'L': 
			$this->m_typename = 'Lost'; 
			$this->m_typecriteria = "(trantype = 'L' or trantype = 'C')"; 
			break;
		default:
			throw new Execption ('KpiBusines Class needs a type of N,R, W or LC.');
		}
		parent::Widget ($name, "kpibusiness", $this->m_typename . " Business Summary", "600px"); //, "400px");
		$this->m_type = substr ($type, 0, 1);

	}

	function addOfficeInfo ($tagParser)
	{
		$header=<<<CONTENT
<table class="querylist" cellpadding="0" cellspacing="0" width="100%">
<thead>
<tr>
	<th align="center" class="top">{dd:group.typename} Business</th>
	<th align="center" colspan="2" class="top">This Month</th>
	<th align="center" colspan="2" class="top">Year to Date</th>
</tr>
<tr>
	<th align="center" class="bottom">{dd:group.thismonth}</th>
	<th align="center" class="bottom">Premium</th>
	<th align="center" class="bottom">Revenue</th>
	<th align="center" class="bottom">Premium</th>
	<th align="center" class="bottom">Revenue</th>
</tr>
</thead>
CONTENT;
$content=<<<CONTENT
	<tr class="{dd:group.class}">
		<td valign="top" align="left"><a href="{dd:dd.page}kpi/queryforlob.php?id={dd:group.id}&daterange=mtd&kpi_type={dd:group.type}">{dd:group.name}</a></td>
		<td align="right">{dd:-rhmtd.premium|=$0}</td>
		<td align="right">{dd:-rhmtd.revenue|=$0}</td>
		<td align="right">{dd:-rhytd.premium|=$0}</td>
		<td align="right">{dd:-rhytd.revenue|=$0}</td>
	</tr>
CONTENT;

$footer=<<<CONTENT
	<tfoot>
	<tr>
		<td valign="top" align="left">Totals</td>
		<td align="right">{dd:-rhmtd.premium|=$0}</td>
		<td align="right">{dd:-rhmtd.revenue|=$0}</td>
		<td align="right">{dd:-rhytd.premium|=$0}</td>
		<td align="right">{dd:-rhytd.revenue|=$0}</td>
	</tr>
	</tfoot>
</table>
CONTENT;
		$selectgroup = "select * from ai_kpi_group";
		$rhgroup = new DD_SqlRecordHandler (array (
			'name'=>'group'
			,'connect'=>'sql.ai'
			,'select'=>$selectgroup
			,'autofields'=>true
		));
		$rhgroup->makeField (array ('name'=>'thismonth', 'default'=>'now', 'datatype'=>'date','format'=>'M Y'));
		$class = $rhgroup->makeField (array ('name'=>'class', 'default'=>'odd'));
		$rhgroup->makeField (array ('name'=>'type', 'default'=>$this->m_type));
		$rhgroup->makeField (array ('name'=>'typecriteria', 'default'=>$this->m_typecriteria));
		$rhgroup->makeField (array ('name'=>'typename', 'default'=>$this->m_typename));

/* Fiscal Month
if ($current_month < $fiscal_month)
{
	begin_month = date_trunc ('year', now() - interval '1 year') + interfal $fiscal_month
}
*/


		$selectmtd="
			select kpi_group, premium, revenue
                    from cd_kpi_month y
                    where
                    y.office_id =  {user.active_office_id} and {SQL:group.typecriteria} and
                	y.kpi_group = {group.id} and
                    y.tranmonth = date_trunc ('month', now())";

		$selectytd="
			select distinct kpi_group, sum (premium) as premium, sum (revenue) as revenue
                    from cd_kpi_month y
                    where
                    y.office_id =  {user.active_office_id} and {SQL:group.typecriteria} and
                	y.kpi_group = {group.id} and
                    y.tranmonth >= ai_current_year_start ({user.active_office_id}, true) and 
                    y.tranmonth <= ai_current_year_end ({user.active_office_id}, true) group by kpi_group";

		$rhmtd = new DD_SqlRecordHandler (array (
			'name'=>'rhmtd'
			,'connect'=>'sql.ai'
			,'select'=>$selectmtd
			,'autofields'=>true
		));

		$rhytd = new DD_SqlRecordHandler (array (
			'name'=>'rhytd'
			,'connect'=>'sql.ai'
			,'select'=>$selectytd
			,'autofields'=>true
		));

		$pricefld = array ( // Base price field extended below.
			'type'=>'display',
			'datatype'=>'float',
			'format'=>',$'
		);
		$pmtd = $rhmtd->makeField ($pricefld, array ('name'=>'premium'));
		$rmtd = $rhmtd->makeField ($pricefld, array ('name'=>'revenue'));
		$pytd = $rhytd->makeField ($pricefld, array ('name'=>'premium'));
		$rytd = $rhytd->makeField ($pricefld, array ('name'=>'revenue'));

		$tagParser->addValueProvider ($rhgroup);
		$tagParser->addValueProvider ($rhmtd);
		$tagParser->addValueProvider ($rhytd);
		$rhgroup->prepareForRecord ($tagParser);
		$text = $tagParser->parseContent ($header);
		$rhgroup->query ($tagParser);
		$i = 0;
		$tot_pmtd = 0.0;
		$tot_rmtd = 0.0;
		$tot_pytd = 0.0;
		$tot_rytd = 0.0;
		while ($rhgroup->nextRecord ())
		{
			$rhmtd->prepareForRecord ($tagParser);
			$rhytd->prepareForRecord ($tagParser);
			$rhmtd->query ($tagParser);
			$rhytd->query ($tagParser);
			$rhmtd->nextRecord ();
			$rhytd->nextRecord ();
			if (!($pmtd->getValue () == '' &&
				$rmtd->getValue () == '' &&
				$pytd->getValue () == '' &&
				$rytd->getValue () == ''))
			{
				$tot_pmtd += $pmtd->getValue ();
				$tot_rmtd += $rmtd->getValue ();
				$tot_pytd += $pytd->getValue ();
				$tot_rytd += $rytd->getValue ();
				$i ++;
				$class->set ($i % 2 == 0 ? 'even' : 'odd');
				$text .= $tagParser->parseContent ($content);
			}
		}

		$pmtd->set ($tot_pmtd);
		$rmtd->set ($tot_rmtd);
		$pytd->set ($tot_pytd);
		$rytd->set ($tot_rytd);

		$text .= $tagParser->parseContent ($footer);
		$rhgroup->freeQuery ();
		$rhmtd->freeQuery ();
		$rhytd->freeQuery ();

		$tagParser->removeValueProviderByName ("rhgroup");
		$tagParser->removeValueProviderByName ("rhmtd");
		$tagParser->removeValueProviderByName ("rhytd");
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
