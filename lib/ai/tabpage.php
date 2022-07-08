<?php
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
*************************************************************************/
require_once 'dd/DD_page.php';
require_once 'ai/params.php';
require_once 'ai/page.php';
require_once 'ai/properties.php';
//require_once ('pChart/pData.class');
//require_once ('pChart/pChart.class');
require_once 'dd/DD_sqltoxml.php';
require_once 'dd/DD_sqltocsv.php';

class TAB_Page extends AI_Page
{

	var			$m_seriesMax = 7;
	var			$m_tab;
	var			$m_tab_property_name;

	function __construct ($name, $currentMenuName, $tab_property_name)
	{
		parent::AI_Page ($name, $currentMenuName);

		global $AI_authpolicy;
		$DD_session = new DD_Session ($AI_authpolicy);
		$DD_session->doAuth ($this->m_tp);
		$this->prepare();
		
		$this->m_tab_property_name = $tab_property_name;
		$currenttab = $this->m_tp->getValue ('user.' . $this->m_tab_property_name);
		if (isset ($currenttab))
			$this->m_tab = TabProperties::getTab ($currenttab);
		else
			$this->m_tab = TabProperties::getTab ('coverage');
	}

	
		//
		// Gets a property for the current tab.
		//
	function get ($property)
	{
		return $this->m_tab [$property];
	}

	function render ($properties)
	{
		/************************************
		** CONTENT
		************************************/
		$header = <<<CONTENT
<html>
<head>
<title>Book | {dd:dd.productname} </title>
{dd:dd.head}
<style type="text/css">@import url("{dd:dd.styles}menu.css"); </style>
<script type="text/javascript" src="{dd:dd.scripts}prototype.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}duckdigit.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}ajax.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}json.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}menu.js"></script>
<script type="text/javascript">
function onOver_pieSlice (itemid, name, serie1, serie2, key)
{
}
function onOut_pieSlice (itemid, name, serie1, serie2, key)
{
}
function onClick_pieSlice (itemid, name, serie1, serie2, key)
{
	//alert ('click: ' + itemid + ' name: ' + name + ' serie1:' + serie1 + ' serie2:' + serie2 + ' key: ' + key);
	if (key != '')
		window.location = '{dd:dd.path}{dd:page.link}{dd:page.linksep}id=' + escape (key);
}

function sortOption (opt)
{
	$({dd:#page.sort_property_name}).value = opt;
	$('sortform').submit ();
}
function tabOption (tab)
{
	//alert (tab);
	$({dd:#page.tab_property_name}).value = tab;
	$('tabform').submit ();
}
</script>
</head>
<body 
{dd:-user.graphs}onLoad=" DD_ClientSideInclude('premium-map', '{dd:dd.path}piechart.php/image.png?graphid={dd:page.premium_graphid}&getmap=1'); DD_ClientSideInclude('revenue-map', '{dd:dd.path}piechart.php/image.png?graphid={dd:page.revenue_graphid}&getmap=1');" 
>

{dd:ai.bodyhead}

<form id="sortform" method="post" action="{dd:dd.path}widget/set_admin_props.php">
<input type=hidden name='{dd:page.sort_property_name}'  id='{dd:page.sort_property_name}'>
<input type=hidden name="DD_update">
</form>
<form id="tabform" method="post" action="{dd:dd.path}widget/set_admin_props.php">
<input type=hidden name='{dd:page.tab_property_name}'  id='{dd:page.tab_property_name}'>
<input type=hidden name="DD_update">
</form>

<table align="center" class="charts" width=100%>
<caption>
    {dd:-user.export}<div class="export">Export as:&nbsp;<a href="{dd:dd.path}{dd:page.csvlink}"><img src="{dd:dd.images}csv.png" alt="Export to Microsoft Excel" /></a>&nbsp;<a href="{dd:dd.path}{dd:page.xmllink}"><img src="{dd:dd.images}xml.png" alt="Export To XML" border=0></a> </div>
	{dd:kpi.kpi_desc} {dd:page.caption} 
</caption>
{dd:-user.graphs}	<tr>
{dd:-user.graphs}		<td align="center">Premium Break Down<br><img width=325 height=200 src="{dd:dd.path}piechart.php/image.png?graphid={dd:page.premium_graphid}" usemap="#{dd:page.premium_graphid}"></td>
{dd:-user.graphs}		<td align="center">Revenue Break Down<br><img width=325 height=200 src="{dd:dd.path}piechart.php/image.png?graphid={dd:page.revenue_graphid}" usemap="#{dd:page.revenue_graphid}"></td>
{dd:-user.graphs}	</tr>
</table>
{$this->m_kpiformcontent}
<div class="tabbed-box">
<ul class="tabs">
{dd:page.tab_selector}
</ul>
<div class="content">
<table class="querylist" align="center" cellpadding="0" cellspacing="0">
<colgroup>
	<col width="280"></col>
	<col width="40"></col>
	<col width="80"></col>
	<col width="80"></col>
	<col width="50"></col>
	<col width="40"></col>
	<col width="40"></col>
</colgroup>

<thead>
<tr>
<th align="left"><a href="#" onclick="sortOption ('itemname')">{dd:page.desctitle}</a></th>
<th><a href="#" onclick="sortOption ('itemcode')">{dd:page.codetitle}</a></th>
<th><a href="#" onclick="sortOption ('premium desc')">Premium</a></th>
<th><a href="#" onclick="sortOption ('revenue desc')">Revenue</a></th>
<th><a href="#" onclick="sortOption ('commission desc')">Comm%</a></th>
<th><a href="#" onclick="sortOption ('polcount desc')">Pols</a></th>
<th><a href="#" onclick="sortOption ('custcount desc')">Custs</a></th>
</tr>
</thead>
<tbody>
CONTENT;

		$content = <<<CONTENT
<tr class="{dd:rh.dd_oddeven}">
	<td><a href="{dd:dd.path}{dd:page.link}{dd:page.linksep}id={dd:%rh.itemid}">{dd:^rh.itemname}</a></td>
	<td>{dd:rh.itemcode}</td>
	<td align=right>{dd:rh.premium}</td>
	<td align=right>{dd:rh.revenue}</td>
	<td align=right>{dd:rh.commission}%</td>
	<td align=right><a href="{dd:dd.path}policylist.php?{dd:page.list_filter}={dd:rh.itemid}">{dd:rh.polcount}</a></td>
	<td align=right><a href="{dd:dd.path}customerlist.php?{dd:page.list_filter}={dd:rh.itemid}">{dd:rh.custcount}</a></td>
</tr>
CONTENT;



		$footer = <<<CONTENT
</tbody>
<tfoot>
<tr class="total">
	<td>&nbsp;</td>
	<td align=right>Totals:</td>
	<td class=total align=right>{dd:page.ptotal}</td>
	<td class=total align=right>{dd:page.ctotal}</td>
	<td class=total align=right>{dd:page.totpercent}</td>
	<td class=total align=right><a href="{dd:dd.path}policylist.php?{dd:page.total_filter}">{dd:page.totpolcount}</a></td>
	<td class=total align=right><a href="{dd:dd.path}customerlist.php?{dd:page.total_filter}">{dd:page.totcustcount}</a></td>
</tr>
</tfoot>
</table>
</div>
</div>
{dd:ai.bodyfoot}
{dd:-user.graphs}<span id="premium-map"></span>
{dd:-user.graphs}<span id="revenue-map"></span>
</body>
</html>
CONTENT;


		/************************************
		** PAGE AND AUTHENTICATION
		************************************/
		global $AdminProperties;
		$pricefld = array ( // Base price field extended below.
			'type'=>'display',
			'datatype'=>'float',
			'format'=>',$'
		);

		$premium_graphid = md5(uniqid(rand(), 1));
		$revenue_graphid = md5(uniqid(rand(), 1));
		$this->makeField ($AdminProperties [$this->m_tab_property_name], array ('name'=>'tab_selector',
			'html'=>"onchange=\"tabOption (this.options [this.selectedIndex].value);\""));
		$this->makeField (array ('name'=>'tab_property_name', 'default'=>$this->m_tab_property_name));
		$this->makeField (array ('name'=>'premium_graphid','default'=>$premium_graphid));
		$this->makeField (array ('name'=>'revenue_graphid','default'=>$revenue_graphid));
		$ptotal = $this->makeField ($pricefld, array ('name'=>'ptotal'));
		$ctotal = $this->makeField ($pricefld, array ('name'=>'ctotal'));
		$totpercent = $this->makeField ($pricefld, array ('name'=>'totpercent', 'format'=>'2'));
		$polcounttotal = $this->makeField (array ('name'=>'totpolcount','type'=>'work'));
		$custcounttotal = $this->makeField (array ('name'=>'totcustcount','type'=>'work'));

		$filter = $this->m_tp->getValue ('user.filter');
		if (isset ($filter) && @strlen ($filter) != 0)
		{
			$filterfield = $this->makeField (array ('name'=>'user_filter','type'=>'work'));
			$filterfield->set (' and ' . $filter);
		}

	
		$rh = new DD_SqlRecordHandler (array (
			'name'=>'rh'
			,'connect'=>'sql.ai'
			,'select'=>$properties ['select']
			,'autofields'=>true
		));
	

		$polcount = $rh->makeField (array ('name'=>'polcount'));
		$custcount = $rh->makeField (array ('name'=>'custcount'));
		$itemid = $rh->makeField (array ('name'=>'itemid'));
		$itemname = $rh->makeField (array ('name'=>'itemname'));
		$itemcode = $rh->makeField (array ('name'=>'itemcode'));
		$premium = $rh->makeField ($pricefld, array ('name'=>'premium'));
		$revenue = $rh->makeField ($pricefld, array ('name'=>'revenue'));
		$percent = $rh->makeField ($pricefld, array ('name'=>'percent', 'format'=>'2'));
		$rh->makeField ($pricefld, array ('name'=>'commission', 'format'=>'1'));

		$this->addRecordHandler ($rh);

		/************************************
		* LOGIC, PAGE CONSTRUCTION AND POST PROCESSING
		************************************/

		$this->prepareForRecord ();
	
		$rh->prepareForRecord ($this->m_tp);


		$linksep = $this->makeField (array ('name'=>'linksep','type'=>'work'));
		$path = $this->m_tp->getValue ('page.link');
		$linksep->set (strpos ($path, '?') === false ? '?' : '&');

		$this->prepareKpiForm ();

error_log ($this->m_tp->parseSql ($properties ['select']));
		$rh->query ($this->m_tp);
		$this->outputContent ($header);
		$i = 0;
		$ptot = 0;
		$ctot = 0;
		$polcounttot = 0;
		$custcounttot = 0;

		$chart_premium_series1 = array ();
		$chart_revenue_series1 = array ();
		$chart_series2 = array ();
		$chart_names = array ();
		$chart_keys = array ();
		while ($rh->nextRecord ())
		{
			$i++;
			$polcountval = $polcount->getValue ();
			$custcountval = $custcount->getValue ();
			$pval = $premium->getValue ();
			$cval = $revenue->getValue ();
			if ($i <= $this->m_seriesMax)
			{
				$chart_premium_series1 [$i] = $pval;
				$chart_revenue_series1 [$i] = $cval;
				$chart_series2 [$i] = $itemcode->getValue ();
				$chart_names [$i] = $itemname->getValue ();
				$chart_keys [$i] = $itemid->getValue ();
			}
			else
			{
				$chart_premium_series1 [$this->m_seriesMax] += $pval;
				$chart_revenue_series1 [$this->m_seriesMax] += $cval;
			}
			if ($pval != 0)
				$percent->set ($cval / $pval * 100);
			else
				$percent->set (null);
			$ptot += $pval;
			$ctot += $cval;
			$polcounttot += $polcountval;
			$custcounttot += $custcountval;
		
			$this->outputContent ($content);
		}
		$rh->freeQuery ();
		$polcounttotal->set ($polcounttot);
		$custcounttotal->set ($custcounttot);
		$ptotal->set ($ptot);
		$ctotal->set ($ctot);
		if ($ptot != 0)
		{
			$totpercent->set ($ctot / $ptot * 100);
		}
			//
			// Finish up the chart numbers for the others.
			//
		if ($i > $this->m_seriesMax)
		{
			$chart_series2 [$this->m_seriesMax ] = 'other';
			$chart_names [$this->m_seriesMax] = "Others";
			$chart_keys [$this->m_seriesMax] = "";
		}
			//
			// Save the chart data into the Session for later retrieval.
			//
		$_SESSION [$premium_graphid] = array (
			'series1'=>$chart_premium_series1, 
			'series2'=>$chart_series2, 
			'names'=>$chart_names, 
			'keys'=>$chart_keys);
		$_SESSION [$revenue_graphid] = array (
			'series1'=>$chart_revenue_series1, 
			'series2'=>$chart_series2, 
			'names'=>$chart_names, 
			'keys'=>$chart_keys);

		$this->outputContent ($footer);
	}
	function renderXML ($properties)
	{
		global $AI_authpolicy;
		$DD_session = new DD_Session ($AI_authpolicy);
		$DD_session->doAuth ($this->m_tp);
		$this->verifyRoles ("export", "You do not have sufficient privileges to export data from AgencyInsight");
		$this->prepareKpiForm ();
		$xmlFactory = new DD_SqlToXmlFactory ('sql.ai', $this->m_tp);
		$xmlFactory->generateRecords ($properties ['xmlrecord'], $properties ['select']);
		$content = $xmlFactory->getResult ();
		header("Content-Type: text/xml");
		header("Content-Length: ".strlen($content));
		echo $content;
	}
    function renderCSV ($properties)
    {
        global $AI_authpolicy;
        $DD_session = new DD_Session ($AI_authpolicy);
        $DD_session->doAuth ($this->m_tp);
        $this->verifyRoles ("export", "You do not have sufficient privileges to export data from AgencyInsight");
        $this->prepareKpiForm ();
        $csvFactory = new DD_SqlToCsvFactory ('sql.ai', $this->m_tp);
        $csvFactory->generateRecords ($properties ['select']);
        $content = $csvFactory->getResult ();
        header("Content-Type: application/ms-excel");
        header("Content-Length: ".strlen($content));
        echo $content;
    }
}
?>
