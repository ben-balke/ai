<?php

/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/htdocs/charts/premium.php,v 1.2 2009/10/28 19:01:59 secwind Exp $
*************************************************************************/

require_once ("dd/DD_page.php");
require_once ("ai/params.php");
include("pChart/pData.class");
include("pChart/pChart.class");

//error_reporting(0);

/************************************
** PAGE SETUP
************************************/

$page = new DD_Page ('page');
$type = $page->makeField (array ('name'=>'type','type'=>'work'));
$startyear = $page->makeField (array ('name'=>'startyear', 'default'=>'{dd:-+http.year|=2009}'));
$startmonth = $page->makeField (array ('name'=>'startmonth', 'default'=>'{dd:-+http.month|=Dec}'));
$monthsago = $page->makeField (array ('name'=>'monthsago','type'=>'work'));

$select = "select
	(sum (p.{SQL:page.type}premium) / 1000000) as premium
			, (sum (p.{SQL:page.type}revenue) / 1000000)  as revenue
			, date_part ('month', date_trunc('month',TIMESTAMP '{SQL:page.startyear}/{SQL:page.startmonth}/1') - INTERVAL '{SQL:page.monthsago} month') as month
		from
			cd_policy p
		where
			p.office_id = {user.active_office_id}
			and p.expdate >= date_trunc('month',TIMESTAMP '{SQL:page.startyear}/{SQL:page.startmonth}/1') - INTERVAL '{SQL:page.monthsago} month' 
			and p.effdate <= date_trunc('month',TIMESTAMP '{SQL:page.startyear}/{SQL:page.startmonth}/1') - INTERVAL '{SQL:page.monthsago} month' 
			and p.premium is not null and p.premium != 0";

$rh = new DD_SqlRecordHandler (array (
	'name'=>'rh'
	,'connect'=>'sql.ai'
	,'select'=>$select
	,'autofields'=>true
	));
$premium = $rh->makeField (array ('name'=>'premium','datatype'=>int,'type'=>'work'));
$revenue = $rh->makeField (array ('name'=>'revenue','datatype'=>int,'type'=>'work'));
$month = $rh->makeField (array ('name'=>'month','type'=>'work'));

$page->addRecordHandler ($rh);

///$months = array("Jan",'Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec');
//$months = array("J",'F','M','A','M','J','J','A','S','O','N','D');
$serie1 = array ();
$serie2 = array ();
$serie3 = array ();
$page->prepareForRecord ();
$type->set ('');	
for ($m = 11; $m >= 0; $m--)
{
	$monthsago->set ($m);	
	$rh->query ($page->m_tp);
	$rh->nextRecord ();
	$serie1 [] = $premium->getValue () - $revenue->getValue ();
	$serie2 [] = $revenue->getValue ();
	$serie3 [] = $month->getValue ();
	$rh->freequery ($page->m_tp);
}

// Dataset definition 
$DataSet = new pData;


error_log (var_export ($serie1, TRUE));;
error_log (var_export ($serie2, TRUE));;

$DataSet->AddPoint($serie1,"Serie1");
$DataSet->AddPoint($serie2,"Serie2");
$DataSet->AddPoint($serie3,"Serie3");

$DataSet->AddAllSeries();
$DataSet->RemoveSerie("Serie3");
$DataSet->SetAbsciseLabelSerie("Serie3");
$DataSet->SetSerieName("Premium","Serie1");
$DataSet->SetSerieName("Revenue","Serie2");
$DataSet->SetYAxisName("Premium and Revenue");
$DataSet->SetYAxisUnit("");
$DataSet->SetXAxisUnit("");

// Initialise the graph
$Test = new pChart(700,230);
$Test->drawGraphAreaGradient(132,173,131,50,TARGET_BACKGROUND);
$Test->setFontProperties("/home/secwind/www/php/lib/Fonts/tahoma.ttf",8);
$Test->setGraphArea(120,20,675,190);
$Test->drawGraphArea(213,217,221,FALSE);
$Test->drawScale($DataSet->GetData(),$DataSet->GetDataDescription(),SCALE_ADDALLSTART0,213,217,221,TRUE,0,2,TRUE);
$Test->drawGraphAreaGradient(163,203,167,50);
$Test->drawGrid(4,TRUE,230,230,230,20);

  // Draw the bar chart
$Test->drawStackedBarGraph($DataSet->GetData(),$DataSet->GetDataDescription(),70);

  // Draw the title
$Title = "  Book of Business Premium and Revenue\r\n   for the last 12 months";
$Test->drawTextBox(0,0,50,230,$Title,90,255,255,255,ALIGN_BOTTOM_CENTER,TRUE,0,0,0,30);

  // Draw the legend
$Test->setFontProperties("/home/secwind/www/php/lib/Fonts/tahoma.ttf",8);
$Test->drawLegend(610,10,$DataSet->GetDataDescription(),236,238,240,52,58,82);

  // Render the picture
$Test->addBorder(2);
//$Test->Render("example23.png");
$Test->Stroke();
 ?>
 
