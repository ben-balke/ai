<?php
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/lib/2d/DD_simplexml.php,v 1.1 2016/02/16 21:18:00 secwind Exp $
*************************************************************************/
function DD_xmlGetTag ($xml, $tag)
{
    $startindex = strpos($xml, "<${tag}>");
    if ($startindex !== FALSE)
    {
        $startindex += 2 + strlen ($tag);
        $endindex = strpos($xml, "</${tag}>", $startindex);
        if ($endindex !== FALSE)
        {
            return substr ($xml, $startindex, $endindex - $startindex);
        }
    }
    return "";
}
?>
