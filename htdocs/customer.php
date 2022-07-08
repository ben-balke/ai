<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/htdocs/customer.php,v 1.4 2009/10/28 19:01:54 secwind Exp $
*************************************************************************/
require_once 'dd/DD_page.php';
require_once 'ai/params.php';
require_once 'ai/page.php';
require_once 'ai/widgets/widget.php';
require_once 'ai/widgets/TopCarriersByCust.php';
require_once 'ai/widgets/TopCoveragesByCust.php';
require_once 'ai/widgets/CustomerInfo.php';
require_once 'ai/widgets/PoliciesByCust.php';

/************************************
** CONTENT
************************************/
$content = <<<CONTENT
{dd:dd.htmlhead}
<html {dd:dd.htmlattr}>
<head>
<title>Welcome | {dd:dd.productname}</title>
{dd:dd.head}
<style type="text/css">@import url("{dd:dd.styles}menu.css"); </style>
<script type="text/javascript" src="{dd:dd.scripts}prototype.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}duckdigit.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}ajax.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}json.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}menu.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}widget.js"></script>
</head>
<body>
{dd:ai.bodyhead}
{dd:page.customerinfo}
<br/>
{dd:page.coverages}
<br/>
{dd:page.carriers}
<br/>
{dd:page.policies}
<br/>
{dd:ai.bodyfoot}
</body>
</html>
CONTENT;

/************************************
** PAGE AND AUTHENTICATION
************************************/
$page = new AI_page ("page", "Index");
$DD_session = new DD_Session ($AI_authpolicy);
$DD_session->doAuth ($page->m_tp);
$page->prepare();

$page->addField (new CustomerInfo ('customerinfo'));
$page->addField (new TopCarriersByCust ('carriers'));
$page->addField (new TopCoveragesByCust ('coverages'));
$page->addField (new PoliciesByCust ('policies'));

/************************************
* LOGIC, PAGE CONSTRUCTION AND POST PROCESSING
************************************/

$page->prepareForRecord ();
$page->outputContent ($content);
?>
