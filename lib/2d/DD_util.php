<?php
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/lib/2d/DD_util.php,v 1.1 2016/02/16 21:18:00 secwind Exp $
*************************************************************************/

function DD_formatNumber ($num, $format)
{
	if (isset ($format))
	{
		$decimals = 0;
		$thousands_sep = "";
		$dec_point = ".";
		$dollar = "";
		$il = strlen($format);
		for ($i = 0; $i < $il; $i++)
		{
			$ch = substr($format, $i, 1);
			if ($ch == ".")
			{
				$dec_point = '.';
			}
			else if ($ch == ',')
			{
				$thousands_sep = ',';
			}
			else if ($ch == '$')
			{
				$dollar = '$';
			}
			else if(is_numeric($ch))
			{
				$decimals = $ch;
			}
		}
		return $dollar . number_format ($num, $decimals, $dec_point, $thousands_sep);
	}
	return $num;
}

function DD_stripNumber ($num)
{
	$il = strlen($num);
	$ch = "";
	$numstr = "";
	for($i = 0; $i < $il; $i++)
	{
		$ch = substr($num, $i, 1);
		if(is_numeric($ch) || $ch == ".")
			$numstr = $numstr.$ch;
	}
	return $numstr;
}

function DD_stripInteger ($num)
{
	$il = strlen($num);
	$ch = "";
	$intstr = "";
	for($i = 0; $i < $il; $i++)
	{
		$ch = substr($num, $i, 1);
		if(is_numeric($ch))
			$intstr = $intstr.$ch;
		else if ($ch == ".")
			break;
	}
	return $intstr;
}


function DD_formatPhone ($value)
{
	$phone = preg_replace ('/[^0-9]/s', '', $value); 
	$len = strlen ($phone);
	if ($len == 7)
	{
		return preg_replace("/([0-9]{3})([0-9]{4})/", "$1-$2", $phone);
	}
	else if ($len == 10)
	{
		return preg_replace("/([0-9]{3})([0-9]{3})([0-9]{4})/", "($1) $2-$3", $phone);
	}
	else if ($len == 11)
	{
		return preg_replace("/([0-9]{1})([0-9]{3})([0-9]{3})([0-9]{4})/", "+$1 ($2) $3-$4", $phone);
	}
	return $value;
}
?>
