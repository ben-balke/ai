<?php
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/lib/dd/DD_csvrecordhandler.php,v 1.2 2012/08/28 21:59:37 secwind Exp $
*************************************************************************/

require_once 'dd/DD_tagparser.php';
require_once 'dd/DD_recordhandlers.php';

class DD_CSVRecordHandler extends DD_RecordHandler
{
	var			$m_update = false;
	var			$m_csvfields;
	var			$m_handle = null;
	var			$m_filepath = null;

	function __construct ($array, $extension = null)
	{
		parent::DD_RecordHandler ($array['name'], false);
		parent::setState ($array);
		self::setState ($array);
		if (isset ($extension))
		{
			parent::setState ($extension);
			self::setState ($extension);
		}
	}

	function setState (&$array)
	{
		if (isset ($array ['fields']))
			$this->m_csvfields = $array ['fields'];
	}
	function setPath ($filepath)
	{
		$this->m_filepath = $filepath;
	}

	function query ($tagParser = null)
	{
		ini_set("auto_detect_line_endings", true);
		$this->m_row = 1;
		if (($this->m_handle = fopen($this->m_filepath, "r")) === FALSE) 
		{
			die ("DD_CVSRecordHandler::query(): cannot open {$this->m_filepath}.");
			return false;
		}
		return true;
	}

	function nextRecord ()
	{
		if (($data = fgetcsv($this->m_handle, 10000, ",")) === FALSE) {
			return false;
		}
		if (isset ($this->m_cvsfields))
		{
			foreach ($this->m_csvfields as $idx=>$name)
			{
				if (array_key_exists ($name, $this->m_fields))
					$field = $this->m_fields [$name];
				else
					$field = $this->makeField (array ('name'=>$name));
				$field->set ($data [$idx]);
			}
		}
		else
		{
			for ($idx = 0; $idx < count ($data); $idx++)
			{
				$fidx = "" + ($idx + 1);
				if (array_key_exists ($fidx, $this->m_fields))
					$field = $this->m_fields [$fidx];
				else
					$field = $this->makeField (array ('name'=>$fidx));
				$field->set ($data [$idx]);
			}
		}
		return true;
	}
	function freeQuery ()
	{
		fclose($this->m_handle);
	}
	/**
	 * Execute all the delete statements.  It is assumed that 
	 * all the fields have received there values already by
	 * a call to setFieldsFromArray().  Call setRecordNo to setup
	 * the recordExt for multirecord record handlers first.
	 * See DD_page.php for details.
	 */
	function deleteRecord ($tagParser)
	{
		$this->deleteRecord ($tagParser);
	}
	/**
	 * Execute all the insert statements.  The keysource
	 * properties are processed for each field and all
	 * the inserts are executes.  See deleteRecord Comments for 
	 * multirecord.
	 */
	function insertRecord ($tagParser)
	{
		$this->insertRecord ($tagParser);
	}
	/**
	 * Execute all the update statements. See deleteRecord Comments for 
	 * multirecord.
	 */
	function updateRecord ($tagParser)
	{
		$this->insertRecord ($tagParser);
	}
	function prepareForRecord ($tagParser)
	{
		parent::prepareForRecord ($tagParser);
	}
}
?>
