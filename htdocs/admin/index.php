<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/htdocs/admin/index.php,v 1.2 2009/10/28 19:01:57 secwind Exp $
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
/* Just redirect to the office list */
$newLoc = $page->m_tp->parseContent ('Location: {dd:dd.path}admin/office_list.php');
header($newLoc);
?>
