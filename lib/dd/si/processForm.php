<?php
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/lib/dd/si/processForm.php,v 1.2 2009/11/26 05:38:35 secwind Exp $
*************************************************************************/

session_start();
/**
 *	processForm.php
 * 
 * 	Porcess the data From our Form in here..
 */


/* If the form has been submitted check image */
if ($_POST['Submit'])
{
	// Check to see if security codes match.
	if ($_POST['txtSecurityCode'] == $_SESSION['SECURITY_CODE'])
	{
		if(!empty($_POST['txtMessage']) && !empty($_POST['txtEmail']))
		{
			$msg = "name: " . $_POST['txtName'] . "\n";
			$msg = $msg . "reply email: " . $_POST['txtEmail'] . "\n\n";
			$msg = $msg . $_POST['txtMessage'];
			
			mail("you@yoursite.com", "My Contact Form: " . $_POST['txtSubject'], $msg);
			header('location: form.php?msg=OK'); // Notify of email sent
		}
	}
	else 
	{
		header('location: form.php?msg=error'); // Display Error message
	}
}

?>
