<?php
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/htdocs/piechart.php,v 1.3 2012/08/28 21:13:55 secwind Exp $
*************************************************************************/

error_reporting(0);

// Standard inclusions   
include("pChart/pData.class");
include("pChart/pChart.class");

session_start ();

$width = 325;
$height = 200;


// Dataset definition 
$graphid = $_GET['graphid'];
if (isset ($_GET['getmap']))
{
	$map = "/tmp/" . $graphid;
	echo file_get_contents ($map);
	exit ();
}

$DataSet = new pData;
$series1 = $_SESSION[$graphid]['series1'];
$series2 = $_SESSION[$graphid]['series2'];
$names = $_SESSION[$graphid]['names'];
$keys = $_SESSION[$graphid]['keys'];

$tot = 0;
foreach ($series1 as $amt)
{
	$tot += $amt;
}


if ($tot == 0)
{
	header ("Location: /images/nodata.png");
}
$final_series1 = array ();
$final_series2 = array ();
$final_names = array ();
$final_keys = array ();

$i = 0;
$other = 0;
foreach ($series1 as $amt)
{
	$i++;
	if ($series2 [$i] == 'other')
	{
		$other += $amt;
	}
	else
	{
		if ($amt / $tot >= 0.01)
		{
			$final_series1 [] = $amt;
			$final_series2 [] = $series2[$i];
			$final_names [] = $names[$i];
			$final_keys [] = $keys[$i];
		}
		else
		{
			$other += $amt;
		}
	}
}
if ($other > 0)
{
	$final_series1 [] = $other;
	$final_series2 [] = 'other';
	$final_names [] = 'Others';
	$final_keys [] = '';// Used by the javascript to determine no link.  Do not change.
}

$DataSet->AddPoint($final_series1,"Serie1", "Amount");
$DataSet->AddPoint($final_series2,"Serie2", "Code");
$DataSet->SetNames($final_names);
$DataSet->SetKeys($final_keys);
$DataSet->AddAllSeries();
$DataSet->SetAbsciseLabelSerie("Serie2");

 // Initialise the graph
$pChart = new pChart($width,$height);
$pChart->setImageMap (TRUE, $graphid);
$pChart->MapEscape = FALSE;

$pChart->setFontProperties("/home/secwind/www/php/lib/Fonts/tahoma.ttf",8);
$pChart->drawFilledRoundedRectangle(7,7,293,193,5,251,251,251);
$pChart->drawRoundedRectangle(5,5,295,195,5,0,0,220);

 // Draw the pie chart
$pChart->AntialiasQuality = 5;
$pChart->tmpFolder = "/tmp/";
//$pChart->setShadowProperties(2,3,90,90,150);
//$pChart->drawFlatPieGraphWithShadow($DataSet->GetData(),$DataSet->GetDataDescription(),120,100,60,PIE_PERCENTAGE,8);
$pChart->drawPieGraph($DataSet->GetData(),$DataSet->GetDataDescription(),133,90,90,PIE_PERCENTAGE,TRUE,70,20,6, 0); 
$pChart->clearShadow();

$pChart->drawPieLegend(255,20,$DataSet->GetData(),$DataSet->GetDataDescription(),235,235,235);

$pChart->Stroke();
unset ($_SESSION[$_GET['graphid']]);
?>
