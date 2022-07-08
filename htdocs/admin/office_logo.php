<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/htdocs/admin/office_logo.php,v 1.3 2009/10/28 19:01:57 secwind Exp $
*************************************************************************/
require_once 'dd/DD_page.php';
require_once 'ai/params.php';
require_once 'ai/page.php';

/************************************
** CONTENT
************************************/
$content = <<<CONTENT
<html {dd:dd.htmlattr}>
{dd:dd.htmlhead}
<head>
<title>Edit Office Properties | {dd:dd.productname}</title>
{dd:dd.head}
<style type="text/css">@import url("{dd:dd.styles}menu.css"); </style>
<script type="text/javascript" src="{dd:dd.scripts}prototype.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}duckdigit.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}ajax.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}json.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}menu.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}widget.js"></script>
<script>
var		deletePressed = false;
function validateForm (f)
{
	if (deletePressed)
	{
		if (!confirm ("Are you sure you want to delete this record."))
		{
			deletePressed = false;
			return false;
		}
	}
	DD_setAlertObject ('errorvalidate');
	if (
		!DD_ValidateRequired (f.file1, "Please provide a name.")
		)
	{
		return false;
	}
	alert (true);
	return true;
}
</script>
</head>
<body>
{dd:ai.bodyhead}
<form
	enctype="multipart/form-data"
	method=post onsubmit="return validateForm (this);"
	name="doc">
		<input type="hidden" name="MAX_FILE_SIZE" value="100000000" />
		{dd:page.office_id}
		<table align=center class="tabForm">
    		<tr><td></td><td class="dataLabel">Select a jpg, gif, png or other graphics format to upload as your logo for this office.</td></tr>
    		<tr><td class="dataLabel">Image File to Upload:{dd:dd.req}</td><td class='tabEditViewDF'>{dd:page.file1}</td></tr>
    		<tr><td colspan=2 align=center>{dd:page.DD_insert} {dd:page.cancel} </td></tr>
		</table>
</form>
{dd:ai.bodyfoot}
</body>
</html>
CONTENT;

/************************************
** SQL STATEMENTS
************************************/
/************************************
** PAGE AND AUTHENTICATION
************************************/
$page = new AI_page ("page", "Offices");
$DD_session = new DD_Session ($AI_authpolicy);
$DD_session->doAuth ($page->m_tp);
$page->prepare ();

$page->makeField (array (
    'type'=>'file',
    'name'=>"file1",
    'size'=>"60",
    ));

$idfield = $page->makeField (array (
    'type'=>'hidden',
    'name'=>'office_id',
    'default'=>'{dd:http.office_id}',
));

/************************************
** RECORD HANDLERS AND FIELDS
************************************/
$page->makeField (array (
	'type'=>'submit'
	,'name'=>'DD_insert'
	,'default'=>'Upload Logo'
	,'html'=>'class=button'
	));
$page->makeField (array (
	'type'=>'button'
	,'name'=>'cancel'
	,'default'=>'Cancel'
	,'html'=>'onclick="javascript: history.go (-1);" class=button'));


/************************************
* LOGIC, PAGE CONSTRUCTION AND POST PROCESSING
************************************/
$page->verifyRoles ("admin", "Administrative Privilges are Required");

if ($_SERVER['REQUEST_METHOD'] == "POST")
{
    if (isset ($_POST['DD_insert']))
    {
        $ext = @strrchr($_FILES['file1']['name'],'.');
		$page->prepareForRecord ();
        $page->processPost ();

		mkdir ($DD_officeImageRoot, 0777, true);

        $uploadfile = $_FILES['file1']['tmp_name'] . $ext;
        if (!move_uploaded_file($_FILES['file1']['tmp_name'], $uploadfile))
        {
            die ('upimage: ' . $_FILES['file1']['name'] . " failed to move to " . $uploadfile);
        }

        $logofile = $DD_officeImageRoot . 'office' . $idfield->getValue () . '.jpg';
        $out = array ();
        $cmd  = $page->m_tp->getValue ('dd.bin') . 'savelogo ' .
            $uploadfile . ' ' . $logofile;
        exec ($cmd, $out);
    }

	$newLoc = $page->m_tp->parseContent ('Location: {dd:dd.path}admin/office_list.php');
	header($newLoc);
}
else
{
	$page->prepareForRecord ();

	if (isset ($_GET['id']))
	{
		$isnew->set ('');
		$page->outputContent ($content);
	}
	else
	{
		$page->outputContent ($content);
	}
}
?>
