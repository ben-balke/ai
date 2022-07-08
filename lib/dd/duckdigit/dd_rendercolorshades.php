<?php
function DD_createColorShades ($hex)
{
	$rx = substr ($hex,0,2);
	$gx = substr ($hex,2,2);
	$bx = substr ($hex,4,2);

	$rd = hexdec ($rx);
	$gd = hexdec ($gx);
	$bd = hexdec ($bx);


	$rti = $rd / 10;
	$gti = $gd / 10;
	$bti = $bd / 10;

	$rbi = (255 - $rd) / 10;
	$gbi = (255 - $gd) / 10;
	$bbi = (255 - $bd) / 10;
	
	$r = $rbi;
	$g = $gbi;
	$b = $bbi;

	$h = "10px";

	$text=<<<CONTENT
<style>
table.dd-color-shades {
		margin-right:5px; 
		font-family:courier new;
		font-size:$h;
}
table.dd-color-shades td {
		padding: 0;
		font-size:$h;
		font-family:courier new;
}
</style>
	<table class="dd-color-shades" cellpadding='0' cellspacing='0' border='0'>
	<tr>
		<td></td>
		<td valign='top' align='Center'>Shades</td>
		<td colspan='2' valign='top' align='center'>Hex</td>
	</tr>
	<tr>
		<td style='width:5px;'></td>
		<td onmouseout='dd_mom()' onclick='DD_clickColor("#000000",-1,-1)' onmouseover='dd_moc("#000000")' 
			style='width:40px;height:$h;color:#FFFFFF;background:rgb(0,0,0)'>&nbsp;</td>
		<td></td>
		<td>#000000</td>
	</tr>
CONTENT;
	$contenttop = '';
	for ($i = 1; $i < 10; $i++)
	{
		$r = round ($i * $rti);
		$g = round ($i * $gti);
		$b = round ($i * $bti);
		$hex = sprintf ("%02X%02X%02X", $r, $g, $b);
		$text.=<<<CONTENT
	<tr>
		<td></td>
		<td onmouseout='dd_mom()' onclick='DD_clickColor("#$hex",-1,-1)' onmouseover='dd_moc("#$hex")' 
			style='height:$h;color:#FFFFFF;background:rgb($r,$g,$b)'>&nbsp;
		</td>
		<td></td>
		<td>#$hex</td>
	</tr>
CONTENT;
	}

	$text.=<<<CONTENT
	<tr>
		<td onmouseout='dd_mom()' onclick='DD_clickColor("#$rx$gx$bx",-1,-1)' onmouseover='dd_moc("#$rx$gx$bx")' 
			colspan='3' style='height:$h;background:rgb($rd,$gd,$bd)'>&nbsp;
		</td>
		<td>#$rx$gx$bx</td>
	</tr>
CONTENT;

	$contentbottom = '';
	for ($i = 1; $i < 10; $i++)
	{
		$r = round ($rd + $i * $rbi);
		$g = round ($gd + $i * $gbi);
		$b = round ($bd + $i * $bbi);
		$hex = sprintf ("%02X%02X%02X", $r, $g, $b);
		$text.=<<<CONTENT
	<tr>
		<td></td>
		<td onmouseout='dd_mom()' onclick='DD_clickColor("#$hex",-1,-1)' onmouseover='dd_moc("#$hex")' 
			style='height:$h;color:#FFFFFF;background:rgb($r,$g,$b)'>&nbsp;
		</td>
		<td></td>
		<td>#$hex</td>
	</tr>
CONTENT;
	}

	$text.=<<<CONTENT
	<tr>
		<td></td>
		<td onmouseout='dd_mom()' onclick='DD_clickColor("#FFFFFF",-1,-1)' onmouseover='dd_moc("#FFFFFF")' 
			style='height:$h;background:rgb(255,255,255)'>
		</td>
		<td></td>
		<td>#FFFFFF</td>
	</tr>
</table>
CONTENT;
	return $text;
}

$hex = $_GET['colorhex'];
echo DD_createColorShades ($hex);
?>
