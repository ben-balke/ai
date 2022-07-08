<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/lib/dd/fields/DD_emailjoin.php,v 1.2 2009/11/26 05:38:32 secwind Exp $
*************************************************************************/

/************************************
** CONTENT
************************************/
//
// Must include the following javascripts....
//
// <script src="{dd:dd.scripts}duckdigit.js"></script>
// <script src="{dd:dd.scripts}prototype.js"></script>
// <script src="{dd:dd.scripts}ajax.js"></script>
// <script src="{dd:dd.scripts}json.js"></script>
// You must have a form called addemail.php in your application's root that will process the post.
//
$emailJoinContent = <<<CONTENT
<script>
var emailSubmitPressed = false;
function sendEmail (f)
{
	DD_setAlertObject ('emailvalidate');
	if (emailSubmitPressed == true)
	{
		alert ('Your request is being processed.  Please be patient.  Thank You.');
		return false;
	}
	if (
		!DD_ValidateRequired (f.email, "Please provide us an email address we can contact you with.") ||
		!DD_ValidateEmail (f.email, "The email address provided is not valid.")
		)
	{
		return false;
	}
	emailSubmitPressed = true;
	var myrequest = new ajaxObject ("{dd:dd.path}addemail.php", processResponseFromServer);
	myrequest.update ("DD_insert=insert&email=" + escape (f.email.value), 'POST');
	return false;
}
function processResponseFromServer (responseText, responseStatus)
{
	emailSubmitPressed = false;
	if (responseStatus==200)
	{
		DD_setAlertObject ('emailvalidate');
		var insertResult = responseText.parseJSON ();
		if (insertResult.result == 'GOOD')
		{
			DD_alert (insertResult.content);
			$('joinemail').email.value = '';
		}
		else
			DD_alert (insertResult.content);
	}
	else
	{
		alert(responseStatus + ' -- Error Processing Request from Server');
	}
}
</script>
<form id="joinemail" name="joinemail">
Join Our Email List: {dd:emailjoin.email} {dd:emailjoin.join}
<div id="emailvalidate" class="errordiv" style="width:250;"></div>
</form>
CONTENT;

class DD_EmailJoinField extends DD_DomField
{
	function __construct ($name, $label)
	{
		$params = array ('name'=>$name,'label'=>label);
		parent::DD_DomField ($params);
	}
	function toString ($tagParser)
	{
		global 	$emailJoinContent;
		$vp = new DD_FieldValueProvider ('emailjoin');
		

		$vp->makeField (array (
			'type'=>'button'
			,'name'=>'join'
			,'value'=>'Join'
			,'html'=>'class=button onclick="sendEmail (this.form)" onmouseout="DD_HideDiv (\'emailvalidate\');"'
			));


		$vp->makeField (array (
			'name'=>'email'
			,'type'=>'text'
			,'size'=>20
			,'maxlength'=>128
			));
		$vp->makeField (array (
			'name'=>'email'
			,'type'=>'text'
			,'size'=>20
			,'maxlength'=>128
			));

		$tagParser->addValueProvider ($vp);
		$text = $tagParser->parseContent ($emailJoinContent);
		$tagParser->removeValueProviderByName ($vp->m_name);
		return $text;
	}
}
?>
