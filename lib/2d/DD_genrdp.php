<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/lib/2d/DD_genrdp.php,v 1.1 2016/02/16 21:18:00 secwind Exp $
*************************************************************************/

// Example:
//		DD_GenRDP $grdp = new DD_GenRDP ("192.168.0.1");
//		$grpd->generate ('test.rpd');
//
class DD_GenRDP
{
	var			$m_echo = FALSE;
	var			$m_ip;
	
	function __construct ($ip)
	{
		$this->m_ip = $ip;
	}
	function setHeaders ($filename)
	{
		header("Pragma: public");
		header("Cache-Control: max-age=0"); 
		header("Content-Type: application/rdp; charset=utf-8");
    	//header("Cache-Control: must-revalidate, post-check=0, pre-check=0");
    	header("Content-Disposition: attachment; filename=$filename");
	}
	function generate ($filename, $useheader = TRUE)
	{
		$this->setHeaders ($filename);
		echo ("
screen mode id:i:2
use multimon:i:0
desktopwidth:i:1280
desktopheight:i:1080
session bpp:i:32
winposstr:s:0,3,0,0,800,600
compression:i:1
keyboardhook:i:2
audiocapturemode:i:0
videoplaybackmode:i:1
connection type:i:7
networkautodetect:i:1
bandwidthautodetect:i:1
displayconnectionbar:i:1
enableworkspacereconnect:i:0
disable wallpaper:i:0
allow font smoothing:i:0
allow desktop composition:i:0
disable full window drag:i:1
disable menu anims:i:1
disable themes:i:0
disable cursor setting:i:0
bitmapcachepersistenable:i:1
full address:s:$this->m_ip
audiomode:i:0
redirectprinters:i:0
redirectcomports:i:0
redirectsmartcards:i:0
redirectclipboard:i:0
redirectposdevices:i:0
autoreconnection enabled:i:1
authentication level:i:2
prompt for credentials:i:0
negotiate security layer:i:1
remoteapplicationmode:i:0
alternate shell:s:
shell working directory:s:
gatewayhostname:s:
gatewayusagemethod:i:4
gatewaycredentialssource:i:4
gatewayprofileusagemethod:i:0
promptcredentialonce:i:0
use redirection server name:i:0
rdgiskdcproxy:i:0
kdcproxyname:s:
username:s:Administrator
");
	}
}
?>
