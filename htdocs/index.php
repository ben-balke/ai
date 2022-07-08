<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/htdocs/index.php,v 1.3 2009/10/28 19:01:54 secwind Exp $
*************************************************************************/
require_once 'dd/DD_page.php';
require_once 'ai/params.php';
require_once 'ai/page.php';
require_once 'ai/widgets/widget.php';
require_once 'ai/widgets/OfficeSummary.php';
require_once 'ai/widgets/TopCustomers.php';
require_once 'ai/widgets/TopCarriers.php';
require_once 'ai/widgets/TopCoverages.php';
require_once 'ai/widgets/KpiBusiness.php';

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
{dd:page.-_officesummary} <br/>
{dd:page.topcustomers}
<br/>
{dd:page.topcoverages}
<br/>
{dd:page.topcarriers}
<br/>
{dd:page.kpinewbusiness}
<br/>
{dd:page.kpirenewbusiness}
<br/>
{dd:page.kpilostbusiness}
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

$page->addField (new OfficeSummary ('officesummary'));
$page->addField (new KpiBusiness ('kpinewbusiness', 'N'));
$page->addField (new KpiBusiness ('kpirenewbusiness', 'R'));
$page->addField (new KpiBusiness ('kpilostbusiness', 'LC')); /* Lapse and Cancelled */
$page->addField (new TopCustomers ('topcustomers'));
$page->addField (new TopCarriers ('topcarriers'));
$page->addField (new TopCoverages ('topcoverages'));
$page->makeField (array ('name'=>'DD_update', 'type'=>'hidden'));

/************************************
* LOGIC, PAGE CONSTRUCTION AND POST PROCESSING
************************************/

$page->prepareForRecord ();
$page->outputContent ($content);
?>
