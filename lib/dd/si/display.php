<?php
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/lib/dd/si/display.php,v 1.2 2009/11/26 05:38:35 secwind Exp $
*************************************************************************/

/**
 * Test for SecurityImage class
 * 
 */
include("SecurityImage.php");

$secim = new SecurityImage();

$secim->show();

/**
 * To include the security image in your form
 * you can link to it as an image, e.g
 * 
 * <img src="display.php" />
 * 
 */

?>
