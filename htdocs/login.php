<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/htdocs/login.php,v 1.3 2009/10/28 19:01:54 secwind Exp $
*************************************************************************/
require_once 'dd/DD_page.php';
require_once 'ai/params.php';
require_once 'ai/page.php';
require_once 'ai/loginfields.php';

/************************************
** CONTENT
************************************/
$content = <<<CONTENT
{dd:dd.htmlhead}
<html {dd:dd.htmlattr}>
<head>
<title>Welcome | {dd:dd.productname}</title>
{dd:dd.head}
<script type="text/javascript" src="{dd:dd.scripts}prototype.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}duckdigit.js"></script>
<script type="text/javascript">
var		imageno = 0;

function validateLoginForm (form)
{
	DD_setAlertObject ('errorlogin');
	if (!DD_ValidateRequired (form.DD_USERNAME, "Please provide your Username.") ||
		//!DD_ValidateEmail (form.DD_USERNAME, "Please provide a valid Email Address.") ||
		!DD_ValidateRequired (form.DD_PASSWORD, "Please provide a Password."))
	{
		return false;
	}
	return true;
}

</script>
</head>
<body class="homepage">
<noscript>
	<div class="nojavascriptdiv">Line of Business does not function without Javascript</div>
</noscript>



<table width="100%" height="100%" align="center">
<tr>
	<td class="login-main" valign="middle" align="center">
		<table align="center" width="300" cellspacing="0" cellpadding="0" border="0">
		<tr>
			<td nowrap="nowrap" valign="top">
				<form onsubmit="return validateLoginForm (this);" action="{dd:dd.httpspath}login.php" method="post" name="frmLogin" id="frmLogin">
				<table id="signin" class="logindialog" cellspacing="0" cellpadding="2">
					<tr ><td colspan="2" class="logintitle"><img alt="AgencyInsight" src="{dd:dd.logo}" /></td></tr>
					<tr ><td colspan="2" class="logintitle">LOG IN</td></tr>
					<tr><td class="label">{dd:page.DD_USERNAME.label}:</td><td>{dd:page.DD_USERNAME}</td></tr>
					<tr><td class="label">{dd:page.DD_PASSWORD.label}:</td><td>{dd:page.DD_PASSWORD}</td></tr>
					<tr><td></td><td>{dd:page.DD_REMEMBERME} {dd:page.DD_REMEMBERME.label}</td></tr>
					<tr><td colspan="2" align="center">{dd:page.DD_LOGIN}</td></tr>
					<tr><td colspan="2" ><div id="errorlogin" class="errordiv"></div></td> </tr>
					<tr><td colspan="2" >{dd:dd.copyright}</td> </tr>
				</table>
				</form>
			</td>
		</tr>
		</table>
	</td>
</tr>
</table>
<script type="text/javascript"> DD_ShowDiv ('errorlogin'); $('errorlogin').innerHTML = {dd:-#http.message}; 
$('DD_USERNAME').focus ();</script>
</body>
</html>
CONTENT;

/************************************
** SQL Statements
************************************/

/************************************
** PAGE AND AUTHENTICATION
************************************/
$page = new AI_page ("page", "Index");
$DD_session = new DD_Session ($AI_authpolicy, true);
$DD_session->doAuth ($page->m_tp);
$page->prepare();
$page->makeField ($dd_username);
$page->makeField ($dd_password);
$page->makeField ($dd_login, array (
"html"=>"onmouseout=\"javascript:DD_HideDiv ('errorlogin');\" class=\"button\""));

$page->makeField (array (
'name'=>'DD_insert'
,'type'=>'hidden'
));

$rh = new DD_SqlRecordHandler (array (
	'name'=>'rh'
	,'connect'=>'sql.ai'
	,'autofields'=>true
));

$page->addRecordHandler ($rh);

/************************************
* LOGIC, PAGE CONSTRUCTION AND POST PROCESSING
************************************/

$page->prepareForRecord ();

$rh->prepareForRecord ($page->m_tp);

$page->outputContent ($content);
?>
