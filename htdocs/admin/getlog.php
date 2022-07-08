<?php
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/htdocs/admin/getlog.php,v 1.1 2012/08/28 21:55:18 secwind Exp $
*************************************************************************/
require_once 'dd/DD_page.php';
require_once 'ai/params.php';
require_once 'ai/page.php';

/************************************
* LOGIC, PAGE CONSTRUCTION AND POST PROCESSING
************************************/
$filepath = $DD_stagePath . 'logs/office' . $_GET ['id'] . '.log';

error_log ($filepath);
$type = 'text/plain';
    // General download headers:
header("Pragma: public"); // required
//header("Expires: 0");
//header("Cache-Control: must-revalidate, post-check=0, pre-check=0");
//header("Cache-Control: private",false); // required by some browsers
header("Content-Transfer-Encoding: binary");
    // Filetype header
header("Content-Type: " . $type);
    // Filesize header
header("Content-Length: " . filesize($filepath));
    // Send file data
readfile($filepath);
?>
