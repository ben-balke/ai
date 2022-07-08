<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/lib/dd/fields/DD_autocomplete.php,v 1.1 2011/09/23 01:23:41 secwind Exp $
*************************************************************************/

/************************************
** CONTENT
************************************/
//
// How use:
//

class DD_AutoCompleteField extends DD_DomText
{
	var		 $m_listsource;
	var			$m_listrendered = false;

	function __construct ($array, $extension = null)
	{
		parent::DD_DomText ($array, $extension);
		$this->setState ($params);
		if (isset ($extension))
			$this->setState ($extension);
		if (!isset ($this->m_listsource))
			die ("Need a list source for field " . $this->m_name);

	}
	function setState (&$array)
	{
		if (isset ($array ['listsource']))
			$this->m_listsource = $array ['listsource'];
	}
	function prepareForRecord ($tagParser)
	{
		parent::prepareForRecord ($tagParser);
		$this->m_listsource->makeList ($tagParser);
	}

	function toString ($tagParser)
	{
		global $G_HTML_NOTE_TR;
		$arrayname = $this->m_name . "_ddac_array";
		if (!$this->m_listrendered)
		{
			if (!isset ($this->m_listsource))
			{
				die ("Select must have a list source");
			}
			$text .= '<script type="text/javascript">var ' . $arrayname . '= [';
			$lv = $this->m_listsource->getList ();
			$first = true;
			foreach($lv as $key=>$val)
			{
				if (!$first)
					$text .= ',';
				else
					$first = false;
				$text .= "'" . @strtr($val, $G_HTML_NOTE_TR) . "'";
			}
			$text .= "];</script>";
			$this->m_listrendered = true;
		}
			// Render the input strin from the base class.
		$input = parent::toString ($tagParser);
			// Replace the end of input field
		$text .= substr ($input, 0, strlen ($input) - 2);
		$text .= 'onkeydown="return DDAC_autocomplete(\'' .  $this->getFullName () . '\', event, ' . $arrayname . ',\'onkeydown\');" ';
		$text .= 'onkeypress="return DDAC_autocomplete(\'' .  $this->getFullName () . '\', event, ' . $arrayname . ',\'onkeypress\');" ';
		$text .= ' onblur="DDAC_takeDownList (\'' . $this->getFullName () . '\'); return false;" autocomplete="off" />
		<div class="ddac_autocomplete_dropdown" id="' . $this->getFullName () . '_DropDown"></div>';

		return $text;
	}
}
