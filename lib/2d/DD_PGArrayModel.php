<?php
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/lib/2d/DD_PGArrayModel.php,v 1.1 2016/02/16 21:18:00 secwind Exp $
*************************************************************************/

require_once '2d/DD_tagparser.php';
require_once '2d/DD_model.php';

/*
 * Implements a model for walking parrallel postgres array columns.
 */
class DD_PGArrayModel extends DD_Model
{
	var			$m_fielddata = array ();
				/* In the form of name and array of pg balues.
				 */
	var			$m_maxrecord = 0;
	var			$m_memos;

	function __construct ($array, $extension = null)
	{
		parent::DD_Model ($array['name'], false);
		parent::setState ($array);
		if (isset ($array ['memos']))
		{
			$this->m_memos = $array ['memos'];
		}

		if (isset ($extension))
		{
			parent::setState ($extension);
		}
	}

	/**
	 * Sets the arrays from a value provider.  
	 */
	function setFromValueProvider ($src_vp)
	{
		global $G_HTML_NOTE_TR; // from DD_tagparser.php
		foreach($this->m_fields as $name=>$field)
		{
			$value = $src_vp->getValue ($name);
			if (isset ($value))
			{
				if (isset ($this->m_memos) && array_key_exists ($name, $this->m_memos))
				{
					$value = @strtr($value, $G_HTML_NOTE_TR);
				}
				$newarray = DD_PgSqlConnection::_pgSubArray ($value);
				$this->m_maxrecord = max($this->m_maxrecord, count($newarray));
				$this->m_fielddata [$name] = $newarray;
			}
		}
		return true;
	}

	function nextRecord ()
	{
		/*
		 * the record no gets incremented on the call to nextRecord.  We
		 * want zero based index so the parents method is called post field
		 * assignments.
		 */
		if ($this->m_recordNo >= $this->m_maxrecord)
		{
			return false;
		}

		foreach($this->m_fields as $name=>$field)
		{
			if (array_key_exists ($name, $this->m_fielddata))
			{
				$fielddata = $this->m_fielddata [$name];
				if (count ($fielddata) > $this->m_recordNo)
					$field->set ($fielddata [$this->m_recordNo]);
				else
					$field->set (null);
			}
		}
		parent::nextRecord ();
		return true;
	}
	function freeQuery ()
	{
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
	}
	/**
	 * Execute all the insert statements.  The keysource
	 * properties are processed for each field and all
	 * the inserts are executes.  See deleteRecord Comments for 
	 * multirecord.
	 */
	function insertRecord ($tagParser)
	{
	}
	/**
	 * Execute all the update statements. See deleteRecord Comments for 
	 * multirecord.
	 */
	function updateRecord ($tagParser)
	{
	}
	function prepareForRecord ($tagParser)
	{
		parent::prepareForRecord ($tagParser);
	}
}
?>
