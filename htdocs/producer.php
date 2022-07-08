<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/htdocs/producer.php,v 1.2 2009/10/28 19:01:54 secwind Exp $
*************************************************************************/
require_once 'dd/DD_page.php';
require_once 'ai/params.php';
require_once 'ai/page.php';

/************************************
** PAGE AND AUTHENTICATION
************************************/
$page = new AI_page ("page", "Index");
$DD_session = new DD_Session ($AI_authpolicy);
$DD_session->doAuth ($page->m_tp);
$page->prepare();
$newLoc = $page->m_tp->parseContent ('Location: {dd:dd.path}queryforprod.php?id={dd:%http.id}');
header($newLoc);
?>
