<?php
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/lib/dd/DD_valueproviderrecordhandler.php,v 1.1 2011/03/18 06:08:59 secwind Exp $
*************************************************************************/

require_once 'dd/DD_tagparser.php';
require_once 'dd/DD_recordhandlers.php';

class DD_ValueProviderRecordHandler extends DD_RecordHandler
{
	var			$m_update = false;
	var			$m_target;
	var			$m_vp = null;

	function __construct ($array, $extension = null)
	{
		parent::DD_RecordHandler ($array['name'], $autofields);
		parent::setState ($array);
        self::setState ($array);
        if (isset ($extension))
		{
			parent::setState ($extension);
            self::setState ($extension);
		}
	}

	/**
	 * Sets the fieds from the provided value provider.  This is useful for 
	 * bulk setting from other record handlers etc.  If recordExt is provided
	 * then it is added to each of the field names. This is used
	 * when we have multirecord forms.
	 */
	function setFromValueProvider ($src_vp, $recordExt, $nullvalues)
	{
		if ($this->m_autofields == false)
		{
			foreach($this->m_fields as $name=>$field)
			{
				if (isset ($recordExt))
				{
					$name .= $recordExt;
				}
				$value = $src_vp->getValue ($name);
				if (isset ($value))
				{
					$field->set ($value);
				}
				else if (($nullvalues == true || $field->wantsNulls ()) && !$field->isWorkField() )
				{
					$field->set (null);
				}
			}
			return true;
		}
		foreach($array as $name=>$value)
		{
			if (isset ($this->m_fields [$name]))
				$field = $this->m_fields [$name];
			else
				$field = $this->makeField (array ('name'=>$name));
				
			//if (!isset ($field))
			//{
			//}
			$field->set ($value);
		}
		return true;
	}

	/**
	 * Sets the fieds from the provided value provider.  This is useful for 
	 * bulk setting from other record handlers etc.  If recordExt is provided
	 * then it is added to each of the field names. This is used
	 * when we have multirecord forms.
	 */
	function pushToValueProvider ($dest_vp)
	{
		foreach($this->m_fields as $name=>$field)
		{
			$value = $field->getValue ($name);
			if (isset ($value))
			{
				$dest_vp->setValue ($name, $value);
			}
			else 
			{
				$dest_vp->setValue ($name, null);
			}
		}
		return true;
	}
    function setState (&$array)
    {
        if (isset ($array ['target']))
            $this->m_target = $array ['target'];
		else
			die ("ValueProviderRecordHandler must have a target value provider");
		if (!strcmp ($this->m_target, $this->m_name))
			die ("ValueProviderRecordHandler target cannot be the same as name");

        if (isset ($array ['update']))
            $this->m_update = $array ['update'];
    }

	function query ($tagParser = null)
	{
		if (!isset ($tagParser))
		{
			die ("DD_ValueProviderRecordHandler::query(): must have a tagParser provided");
		}
		$this->m_vp = $tagParser->getValueProvider ($this->m_target);
		if (!isset ($this->m_vp))
		{
			die ("DD_ValueProviderRecordHandler::query(): target value provider {$this->m_target} does not exist.");
		}
	}
	function nextRecord ()
	{

		$this->setFromValueProvider ($this->m_vp, null, false);
		return true;
	}
	function freeQuery ()
	{
		$this->m_conn->freeQuery ($this->m_queryHandle);
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
		$this->m_vp = $tagParser->getValueProvider ($this->m_target);
		if (!isset ($this->m_vp))
		{
			die ("DD_ValueProviderRecordHandler::insertRecord(): target value provider {$this->m_target} does not exist.");
		}
		$this->pushToValueProvider ($this->m_vp);
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
