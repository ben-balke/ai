<?php 
/********************************************************************************
** Copyright (c) DuckDigit Technologies, Inc. 2009 ALL RIGHTS RESERVED
** Author: Ben Balke
** Version: $Header: /home/cvsroot/ai/htdocs/widget/set_admin_props.php,v 1.3 2009/10/28 19:02:06 secwind Exp $
********************************************************************************/
require_once 'dd/DD_page.php';
require_once 'ai/params.php';
require_once 'ai/page.php';
require_once 'ai/properties.php';
require_once 'ai/widgets/widget.php';

/************************************
** SQL STATEMENTS
************************************/
/* We Dynamically build the update statement.
 */
//error_log (var_export ($_POST, TRUE));
//error_log (var_export ($_GET, TRUE));
$first = true;
$update = 'update ai_users set ';
foreach ($AdminProperties as $name=>$def)
{
	if (isset ($_POST [$name]))
	{
		if (!$first)
		{
			$update .= ',';
		}
		$update .= $name . '={http.' . $name . '}';
		$first = false;
	}
}
if ($first == true)
{
	die ('Nothing to do in set_admin_props');
}
$update .= ' where id = {user.id}';

/************************************
** PAGE AND AUTHENTICATION
************************************/
$page = new AI_page ("page", "Admin");
$DD_session = new DD_Session ($AI_authpolicy);
$DD_session->doAuth ($page->m_tp);


/************************************
** RECORD HANDLERS AND FIELDS
************************************/
$rh = new DD_SqlRecordHandler (array (
	'name'=>"rh" 
	,'connect'=>"sql.ai"
	,'update'=>$update
	));

/* Now add the fields...
 */
foreach ($AdminProperties as $name=>$def)
{
	if (isset ($_POST [$name]))
	{
		$rh->makeField ($def);
	}
}
$page->addRecordHandler ($rh);

/************************************
* LOGIC, PAGE CONSTRUCTION AND POST PROCESSING
************************************/

if ($_SERVER['REQUEST_METHOD'] == "POST")
{
	$page->processPost ();
		/* 
		 * Now update the session data.
		 */
	foreach ($AdminProperties as $name=>$def)
	{
		if (isset ($_POST [$name]))
			$page->m_tp->setValue ('user.' . $name, $page->m_tp->getValue ('rh.' . $name));
	}
	if (isset ($_POST ['widgetnextpage']))
		$newLoc = 'Location: ' . $_POST ['widgetnextpage'];
	else
		$newLoc = 'Location: ' . $_SERVER ['HTTP_REFERER'];
	header($newLoc);
}
?>
