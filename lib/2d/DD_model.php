<?php
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/lib/2d/DD_model.php,v 1.2 2016/03/16 18:46:12 secwind Exp $
*************************************************************************/

require_once '2d/DD_tagparser.php';

class DD_FieldValueProvider extends DD_ValueProvider
{
	var			$m_fields;
	var			$m_autofields;	// Automatcially create fields from the source.
	function __construct ($name, $autofields = false)
	{
		parent::__construct ($name);
		$this->m_autofields = $autofields;
		$this->m_fields = array ();
	}
	function makeField ($array, $extension = null)
	{
		$f = DD_MakeField ($array, $extension);
		self::addField ($f);
		return $f;
	}
	function addField ($field /* DD_DomField */)
	{
		$this->m_fields [$field->m_name] = $field;
		return $field;
	}
	function getField ($name) /* return DD_DomField */
	{
		return $this->m_fields [$name];
	}
	/**
	 * getFields returns an array of DD_DomField objects
	 */
	function getFields ()
	{
		return $this->m_fields;
	}

	function getValue ($name, $mode = MODE_VALUE, $tagParser = null)
	{
		$subname = null;
		$len = strlen ($name);
		$dotidx = strpos ($name, '.');
		if (!($dotidx === false))
		{
			$subname = substr ($name, $dotidx + 1);
			$name = substr ($name, 0, $dotidx);
			//error_log ('Name: ' . $name . ' subname:' . $subname);
		}
		if (isset ($this->m_fields [$name]))
		{
			$f = $this->m_fields [$name];
		}
		if (isset ($f))
		{
			switch ($mode)
			{
			case MODE_NORMAL:
			case MODE_CONTENT:
				if (isset($subname))
					return $f->getAttributeContent ($subname, $tagParser);
				return $f->toString ($tagParser);
			case MODE_SQL_VALUE:
				if (isset($subname))
					return $f->getAttributeValue ($subname, $tagParser);
				return $f->getSqlValue ($tagParser);
			default:
				if (isset($subname))
					return $f->getAttributeValue ($subname, $tagParser);
				return $f->getValue ($tagParser);
			}
		}
		else if (!strncmp ($name,  'dd_fields', 9))
		{
			return $this->dumpFields ();
		}
		return null;
	}
	function dumpFields ()
	{
		$text = "<table><tr><td>Field</td><td>Value</td></tr>";
		foreach($this->m_fields as $name=>$field)
		{
			$text .= "<tr><td>" . $name . "</td><td>" . $field->getValue (). "</td></tr>";
		}
		$text .= "</table>";
		return $text;
	}
	function setValue ($name, $value)
	{
		$f = $this->m_fields [$name];
		if (isset ($f))
		{
			$f->set($value);
		}
	}
	/**
	 * Sets the fieds from the provided array.  This is useful for 
	 * bulk setting from $_GET and $_PUT.  If recordExt is provided
	 * then it is added to each of the field names. This is used
	 * when we have multirecord forms.
	 */
	function setFromArray ($array, $recordExt, $nullvalues)
	{
		//
		// Process all the predefined fields first.  Next create
		// new fields from the incoming array if autofields is set.
		// 
		foreach($this->m_fields as $name=>$field)
		{
			if (isset ($recordExt))
			{
				$name .= $recordExt;
			}
			if (@array_key_exists ($name, $array))
			{							 
				$value = $array [$name];
				if (isset ($value))
				{
					$field->set ($value);
				}
				else if (($nullvalues == true || $field->wantsNulls ()) && !$field->isWorkField() )
				{
					$field->set (null);
				}
			}
			else if (($nullvalues == true || $field->wantsNulls ()) && !$field->isWorkField() )
			{
				$field->set (null);
			}
		}
		//
		// do the auto fields next.
		//
		if ($this->m_autofields == true)
		{
			foreach($array as $name=>$value)
			{
				if (!@array_key_exists  ($name, $this->m_fields))
				{
					$field = $this->makeField (array ('name'=>$name));
					$field->set ($value);
				}
				
			}
		}
		return true;

	}
	function prepareForRecord ($tagParser)
	{
		foreach($this->m_fields as $name=>$field)
		{
			$field->prepareForRecord ($tagParser);
		}
	}
}

/**
 * This is an abstract class the should not be instansiated directly.
 * Creates special fields for dd_recordno and dd_oddeven to track the record number and
 * odd/even row for stylizing convenience.
 */
class DD_Model extends DD_FieldValueProvider
{
	var				$m_recordExt;	// Multirecord extension on the field names.
	var				$m_recordNo;	// Multirecord extension on the field names.
	var				$recordnoField;
	var				$oddevenField;
	var				$m_multirecord = false;
	var				$m_multirecordskipfields = null;
	var				$m_multirecordincludefields = null;
	function __construct ($name, $autofields)
	{
		parent::__construct ($name, $autofields);
		$this->recordnoField = $this->makeField (array ('name'=>'dd_recordno', 'type'=>'work'));
		$this->oddevenField = $this->makeField (array ('name'=>'dd_oddeven', 'type'=>'work'));
	}
    function setState (&$array)
    {
        if (isset ($array ['multirecord']))
		{
            $this->m_multirecord = $array ['multirecord'];
			if ($this->m_autofields == true && $this->m_multirecord)
			{
				throw new Exception ('Model "' .$this->m_name. '" is constructed with autofields and multirecord.  This is not compatible.');
			}
		}
	}
	function query ($tagParser)
	{
		$this->m_recordNo = 0;
		$this->recordnoField->set ($this->m_recordNo);
	}
	function nextRecord ()
	{
		$this->m_recordNo += 1;
		$this->recordnoField->set ($this->m_recordNo);
		if ($this->m_recordNo % 2 == 0)
			$this->oddevenField->set ('even');
		else
			$this->oddevenField->set ('odd');
		if ($this->m_multirecord == true)
		{
			foreach($this->m_fields as $name=>$field)
				$field->setRecordExtension ($this->m_recordNo);
		}
		return false;
	}
	/*
	function getValue ($name, $mode = MODE_VALUE, $tagParser = null)
	{
		if (!@strcmp ($field, "dd_json"))
		{
			return $this->toJson (false, $tagParser);
		}
		return DD_FieldValueProvider ($name, $mode, $tagParser);
	}
	function toJson ($asArray, $tagParser)
	{
		$this->query ($tagParser);
		$this->nextRecord ();
		$text = $this->m_name . ":" . ($asArray ? "[\n" : "{\n");
		foreach($this->m_fields as $name=>$field)
		{
			$text .= $name . ':\'' . $field->getValue () . '\''; 
		}
		$text .= $asArray ? "]\n" : "}\n";
		return $text;
	}
	 */

	function freeQuery ()
	{
	}
	function deleteRecord ($tagParser) /* virtual */
	{
	}
	function insertRecord ($tagParser) /* virtual */
	{
	}
	function updateRecord ($tagParser) /* virtual */
	{
	}
	/*
	 * Sets the fields from the provided array.  nullvalues is true then
	 * all of the fields not included in the array are set to null.  Call setRecordNo
	 * to get the multirecord extension setup.
	 */
	function setFieldsFromArray ($array, $nullvalues = false)
	{
		parent::setFromArray ($array, $this->m_recordExt, $nullvalues);
	}
	function prepareForRecord ($tagParser)
	{
		parent::prepareForRecord ($tagParser);
	}
	function doKeySources ($tagParser)
	{
		foreach($this->m_fields as $name=>$field)
		{
			$field->doKeySource ($tagParser);
		}
	}
	function getRecordNo ()
	{
		return $this->m_recordNo;
	}
	/**
	 * This is used to setup the multirecord extension.
	 */
	function setRecordNo ($recno)
	{
		$this->m_recordNo = $recno;
		if ($this->m_multirecord)
		{
			$this->m_recordExt = $recno;
		}
	}
	function addMultiRecordSkipField ($skipfield)
	{
		if (!isset ($this->m_multirecordskipfields))
		{
			$this->m_multirecordskipfields = array ();
		}
		$this->m_multirecordskipfields [] = $skipfield;
	}
	function addMultiRecordIncludeField ($includefield)
	{
		if (!isset ($this->m_multirecordincludefields))
		{
			$this->m_multirecordincludefields = array ();
		}
		$this->m_multirecordincludefields [] = $includefield;
	}
	function isMultiRecordSkipped ()
	{
		if (isset ($this->m_multirecordskipfields))
		{
			foreach($this->m_multirecordskipfields as $field)
			{
				$value = $field->getValue ();
				if (!isset($value) || strlen ($value) == 0)
				{
					return true;
				}
			}
		}
		if (isset ($this->m_multirecordincludefields))
		{
			foreach($this->m_multirecordincludefields as $field)
			{
				$value = $field->getValue ();
				if (isset($value) && ($value == 'Y' || $value == 'y'))
				{
					return false;
				}
			}
			return true;
		}
		return false;
	}
}

class DD_SqlModel extends DD_Model
{
	var			$m_connect;
	var			$m_conn;

	var			$m_select;
	var			$m_insert = array ();
	var			$m_update = array ();
	var			$m_delete = array ();
	var			$m_queryHandle;

	function __construct ($array, $extension = null)
	{
		$autofields = null;
		if (isset ($array ['autofields']))
			$autofields = $array ['autofields'];
		if (isset ($extention ['autofields']))
			$autofields = $extension ['autofields'];
		parent::__construct ($array['name'], $autofields);
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
        if (isset ($array ['connect']))
            $this->m_connect = $array ['connect'];
        if (isset ($array ['select']))
            $this->m_select = $array ['select'];

		foreach($array as $name=>$value)
		{
			if (strncmp ($name, "insert", 6) == 0)
			{
            	$this->m_insert [$name] = $value;
			}
			else if (strncmp ($name, "update", 6) == 0)
			{
            	$this->m_update [$name] = $value;
			}
			else if (strncmp ($name, "delete", 6) == 0)
			{
            	$this->m_delete [$name] = $value;
			}
		}
    }

	function quickSelect ($connect, $sql, $tagParser = null)
	{
		$rslt = false;
		$conn = DD_SqlPoolGetConn ($connect);
		if (!isset ($conn))
		{
			die ("DD_SqlModel::quickSelect(): Cannot find connection string" . $connect);
		}
		if (isset ($tagParser))
		{
			$queryHandle = $conn->query ($tagParser->parseSql ($sql));
		}
		else
		{
			$queryHandle = $conn->query ($sql);
		}
		$row = $conn->nextNamedRecord ($queryHandle);
		if (isset ($row))
		{
			//parent::setFromArray ($row, null, false);

			foreach($row as $name=>$value)
			{
				if (isset ($this->m_fields [$name]))
					$field = $this->m_fields [$name];
				else
					$field = $this->makeField (array ('name'=>$name));
				
				$field->set ($value);
			}
			$rslt = true;
		}
		$conn->freeQuery ($queryHandle);
		return $rslt;
	}
	function changeSelect ($select)
	{
		$this->m_select = $select;
	}

	function query ($tagParser = null)
	{
		$this->m_conn = DD_SqlPoolGetConn ($this->m_connect);
		if (!isset ($this->m_conn))
		{
			die ("DD_SqlModel::query(): Cannot find connection string" . $this->m_connect);
		}
		if (isset ($tagParser))
		{
			$this->m_queryHandle = $this->m_conn->query ($tagParser->parseSql ($this->m_select));
		}
		else
		{
			$this->m_queryHandle = $this->m_conn->query ($this->m_select);
		}
		DD_Model::query ($tagParser);
	}
	function nextRecord ()
	{

		$row = $this->m_conn->nextNamedRecord ($this->m_queryHandle);
		if (isset ($row))
		{
			DD_Model::nextRecord ();
			parent::setFromArray ($row, null, true);
			return true;
		}
		return false;
	}
	function freeQuery ()
	{
		$this->m_conn->freeQuery ($this->m_queryHandle);
	}
	function execute ($sql, $tagParser = null)
	{
		$this->m_conn = DD_SqlPoolGetConn ($this->m_connect);
		if (!isset ($this->m_conn))
		{
			die ("DD_SqlModel::execute(): Cannot find connection string" . $this->m_connect);
		}
		if (isset ($tagParser))
		{
			$this->m_conn->execute ($tagParser->parseSql ($sql));
		}
		else
		{
			$this->m_conn->execute ($sql);
		}
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
		foreach($this->m_delete as $name=>$statement)
			$this->execute ($statement, $tagParser);
	}
	/**
	 * Execute all the insert statements.  The keysource
	 * properties are processed for each field and all
	 * the inserts are executes.  See deleteRecord Comments for 
	 * multirecord.
	 */
	function insertRecord ($tagParser)
	{
		$this->doKeySources ($tagParser);
		foreach($this->m_insert as $name=>$statement)
			$this->execute ($statement, $tagParser);
	}
	/**
	 * Execute all the update statements. See deleteRecord Comments for 
	 * multirecord.
	 */
	function updateRecord ($tagParser)
	{
		foreach($this->m_update as $name=>$statement)
			$this->execute ($statement, $tagParser);
	}
	function prepareForRecord ($tagParser)
	{
		parent::prepareForRecord ($tagParser);
	}
}
?>
