<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/htdocs/logout.php,v 1.2 2009/10/28 19:01:54 secwind Exp $
*************************************************************************/
require_once 'dd/DD_page.php';
require_once 'ai/params.php';
require_once 'ai/page.php';

/************************************
** PAGE AND AUTHENTICATION
************************************/
$DD_session = new DD_Session ($AI_authpolicy, true);
$DD_session->logout ();

/************************************
* LOGIC, PAGE CONSTRUCTION AND POST PROCESSING
************************************/

$DD_session->logout ();
header ("Location: " . $DD_params ['path'] . 'index.php');
?>
