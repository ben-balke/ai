<?php
/********************************************************************************
*** DuckDigit Infrastructure
*** Version: $Header: /home/cvsroot/ai/lib/2d/fields/DD_tokenfield.php,v 1.1 2016/02/16 21:20:45 secwind Exp $
*** Author: Ben Balke
*** Copyright (c) DuckDigit Technologies, Inc. 2008 ALL RIGHTS RESERVED
*** Description:
***		Combines multiple strings of same length into a single string and visa versa.  
***		Good for creating a unique key while obscuring the true key.
********************************************************************************/
class DD_TokenField extends DD_DomField
{
	var		$m_parts = null;
	var		$m_dynamic_parts;
	var		$m_len = 36;		// this is the default length for a uuid().

	function __construct ($array, $extension = null)
	{
		parent::DD_DomField ($array, $extension);
		$this->setState ($array);
		if (isset ($extension))
			$this->setState ($extension);
	}
	function setState (&$array)
	{
		if (isset ($array ['length']))
		{
			$this->m_len = $array ['length'];
		}
			//
			// Array of parts.
			//
		if (isset ($array ['parts']))
		{
			$this->m_parts = $array ['parts'];
			foreach ($this->m_parts as $value)
			{
				if (@strlen ($value) != $this->m_len)
				{
					throw new Exception ('TokenField::setDefault: length for each part must  ' . $this->m_len);
				}
			}
		}
		if (isset ($array ['dynamic_parts']))
		{
			$this->m_dynamic_parts = $array ['dynamic_parts'];
		}
	}
	function toString ($tagParser)
	{
		if (isset ($this->m_value))
			return $this->m_value;
		return null;
	}
	function makeTokenFromParts ()
	{
		if (isset ($this->m_parts))
		{
			$token = '';
			foreach ($this->m_parts as $value)
			{
				if (!isset ($value))
					return;
				if (strlen ($value) != $this->m_len)
					return;
			}
			for ($i = 0; $i < $this->m_len; $i++)
			{
				foreach ($this->m_parts as $value)
				{
					$token .= $value [$i];
				}
			}	
			$this->m_value = $token;
		}
		else
		{
			$this->m_value = null;
		}
	}
	function setDefault ($tagParser)
	{
		if (isset ($this->m_default))
		{
			$this->m_value = $tagParser->parseContent ($this->m_default);
			if (@isset ($this->m_value))
			{
				if (@strlen ($this->m_value) % $this->m_len != 0)
				{
					throw new Exception ('TokenField::setDefault: length for token must be a multiple of ' . $this->m_len);
				}
				$items = @strlen ($this->m_value) / $this->m_len;;
				if (@isset ($m_parts))
				{
					$this->m_parts = array ();
				}
				for ($i = 0; $i < $items; $i++)
				{
					$this->m_parts [] = "";
				}
				for ($c = 0; $c < $this->m_len; $c++)
				{
					for ($i = 0; $i < $items; $i++)
					{
						$this->m_parts [$i] .= $this->m_value [$i + $c * $items];
					}
				}
			}
		}
		else
		{
			$this->makeTokenFromParts ();
		}
	}
	public function setDynamicParts ($tagParser)
	{
		if (isset ($this->m_dynamic_parts))
		{
			unset ($this->m_parts);
			$this->m_parts = array ();
			foreach ($this->m_dynamic_parts as $value)
			{
				$this->m_parts [] = $tagParser->parseContent ($value);
			}
			$this->makeTokenFromParts ();
		}
	}

	public function getAttributeContent ($attribute, $tagParser)
	{
		$value = parent::getAttributeContent ($attribute, $tagParser);
		if (isset ($value))
		{
			return $value;
		}
		return $this->getAttributeValue ($attribute, $tagParser);
	}
	public function getAttributeValue ($attribute, $tagParser)
	{
		if (@!strncmp  ($attribute, 'part', 4))
		{
			
			$idx = @substr($attribute, 4);
			if (@count ($this->m_parts) < $idx)
			{
				throw new Exception ('TokenField::getAtrributeValue: requested part ' . $attribute . ' not available. ');
			}
			return $this->m_parts [$idx - 1];
		}
		return null;
	}
}
?>
