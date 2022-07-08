<?php
class DD_DomCheckList extends DD_DomField
{
	var				$m_listsource;
	var				$m_present;
	var				$m_layout;

	/**
		Creates an HTMLMultiItem
	 */
	function DD_DomCheckList ($array, $extension)
	{
		parent::DD_DomField ($array, $extension);
		$this->setState ($array);
		if (isset ($extension))
			$this->setState ($extension);
	}

	function setState (&$array)
	{
		if (isset ($array ['listsource']))
			$this->m_listsource = $array ['listsource'];
		else
			die ('Check List [' . $this->m_name . 'Must have a listsource]');
		if (isset ($array ['present']))
			$this->m_present = $array ['present'];
		if (isset ($array ['layout']))
			$this->m_layout = $array ['layout'];
		else
			$this->m_layout = "horizontal";
	}

	function prepareForRecord ($tagParser)
	{
		parent::prepareForRecord ($tagParser);
		$this->m_listsource->makeList ($tagParser);
	}

	function toString ($tagParser)
	{
		$bFirst = true;
		$text =	"";
		$val = $this->getFormattedValue ($tagParser);
		error_log ($val . ' value:' . $this->m_value);
		if (!isset ($val))
		{
			$ival = 0;
		}
		else
		{
			$ival = (int) $val;
		}
		$text .= '<input type="hidden" name="' . $this->getFullName () . '" value="' . $ival . '"/>';
		$lv = $this->m_listsource->getList ();
		foreach($lv as $iB=>$name)
		{
			if (strcmp ($this->m_layout, 'vertical'))
			{
				$text .= '<td>';
			}
			else
			{
				if (!$bFirst)
					$text .= '<br>';
			}
			$text .= '<input type="checkbox" onclick="javascript:DD_CheckToInt (this, this.form.' . $this->getFullName() . ');" name="cb_' .
				$this->getFullName () . '_' . $iB . '" value="' . $iB . '" ';

			if (($ival & (1 << $iB)) != 0)
			{
				$text .= " checked ";
			}

			if ($this->checkReadOnly())
			{
				$text .= " readonly ";
			}
			$text .= $this->getHtml ($tagParser) . " />";

			$text .= $this->escapeHtml ($name);
			if (strcmp ($this->m_layout, 'vertical'))
			{
				$text .= '</td>';
			}
			$bFirst = false;
		}
		return $text;
	}
}
?>
