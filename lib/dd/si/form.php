<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/lib/dd/si/form.php,v 1.2 2009/11/26 05:38:35 secwind Exp $
*************************************************************************/
session_start(); 
?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>Test Form</title>
	<style type="text/css">
		.err {color: #FF0000;}
	</style>
</head>

<body>

	<strong>Contact Form </strong>
	<em class="err"><?php showMessage();?></em>
	<hr />
	<br />
	<form method="post" action="processForm.php">
		<table width="100%" border="0" cellspacing="2" cellpadding="2">
             <tr>
               <td><label for="txtName">Name:</label></td>
               <td colspan="2"><input type="text" name="txtName" size="30" /></td>
             </tr>
             <tr>
               <td><label for="txtEmail">Reply Email:</label></td>
               <td colspan="2"><input type="text" name="txtEmail" size="30" /></td>
             </tr>
             <tr>
               <td><label for="txtSubject">Subject:</label></td>
               <td colspan="2"><input type="text" name="txtSubject" size="30" /></td>
             </tr>
             <tr>
             	<td><label for="txtSecurityCode">Security Code</label>: </td>
              	<td colspan="2"><input type="text" name="txtSecurityCode" size="30" /></td>
            </tr>
             <tr>
               <td><label for="txtMessage">Message:</label></td>
               <td colspan="2">&nbsp;</td>
             </tr>
             <tr>
               <td><textarea name="txtMessage" cols="30" rows="6"></textarea></td>
               <td><img name="captchaimg" alt="Security Code" src="SecurityImage.php" /></td>
               <td><a href="javascript:location.reload();"><img src="images/arrow_refresh.png" alt="Refresh Code" border="0" /></a></td>
             </tr>
             <tr>
               <td><input type="submit" name="Submit" value="Submit" /></td>
               <td colspan="2">&nbsp;</td>
             </tr>
         </table>
	</form>
</body>
</html>


<?php
function showMessage()
{
	if(isset($_GET['msg']))
	{
		switch ($_GET['msg'])
		{
			case 'error':	echo "Please Complete all Fields!"; break;
			case 'OK':		echo "Email Seccessfully Sent!"; break;
		}
	}
}
?>
