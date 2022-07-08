<?php
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/lib/2d/DD_fileutil.php,v 1.1 2016/02/16 21:18:00 secwind Exp $
*************************************************************************/

function DD_recusiveDelete ($dir)
{
	if (!file_exists ($dir))
		return $dir;
	$it = new RecursiveDirectoryIterator($dir, RecursiveDirectoryIterator::SKIP_DOTS);
	$files = new RecursiveIteratorIterator($it,
             	RecursiveIteratorIterator::CHILD_FIRST);
	foreach($files as $file) {
    	if ($file->getFilename() === '.' || $file->getFilename() === '..') {
        	continue;
    	}
    	if ($file->isDir()){
        	rmdir($file->getRealPath());
    	} else {
        	unlink($file->getRealPath());
    	}
	}
	rmdir($dir);
}
?>
