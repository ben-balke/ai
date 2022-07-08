<?php
require_once ("2d/bs/DD_popover.php");
class DD_Input extends DD_DomField
{
	var				$m_class;
	var				$m_addonleft;
	var				$m_addonright;
	var				$m_placeholder;
	var				$m_maxlength;
	var				$m_size;
	var				$m_inputtype; // text, password, datetime, datetime-local, date, month, time, week, number, email, url, search, tel, and color
	var				$m_help; // help
	var				$m_help_popover; // this is a popover left/right/top/bottom
	var				$m_addon = false;

	/**
		Creates an HTMLMultiItem
	 */
	function __construct ($array, $extension = null)
	{
		parent::__construct ($array, $extension);
		$this->setState ($array);
		if (isset ($extension))
			$this->setState ($extension);
	}

	function setState (&$array)
	{
		if (isset ($array ['class']))
			$this->m_class = $array ['class'];
		if (isset ($array ['addonleft']))
		{
			$this->m_addonleft = $array ['addonleft'];
			$this->m_addon = true;
		}
		if (isset ($array ['addonright']))
		{
			$this->m_addonright = $array ['addonright'];
			$this->m_addon = true;
		}
		if (isset ($array ['maxlength']))
		{
			$this->m_maxlength = $array ['maxlength'];
		}
		if (isset ($array ['size']))
		{
			$this->m_size = $array ['size'];
		}
		if (isset ($array ['help']))
		{
			$this->m_help = $array ['help'];
			$this->m_addon = true;
		}
		if (isset ($array ['help']))
		{
			$this->m_help = $array ['help'];
		}
		if (isset ($array ['help_popover']))
		{
			$this->m_help_popover = $array ['help_popover'];
			$this->m_addon = true;
		}
		if (isset ($array ['placeholder']))
			$this->m_placeholder = $array ['placeholder'];
		if (isset ($array ['inputtype']))
			$this->m_inputtype = $array ['inputtype'];
		else
			die ("DD_input must have input type specified");
	}

	function prepareForRecord ($tagParser)
	{
		parent::prepareForRecord ($tagParser);
	}

	function toString ($tagParser)
	{
		if (!isset ($this->m_class))
		{
			$this->m_class = "";
		}
		$text =	"";
		$val = $this->getFormattedValue ($tagParser);
		if (isset ($this->m_label))
		{
			$text .= '<label for="' .  $this->getFullName () . '">' . $this->m_label . '</label>';
		}
		$text .= '<div class="input-group">';
		if ($this->m_addon == true)
		{
 			$text .= '<div class="input-group">';
			if (isset ($this->m_addonleft))
			{
 				$text .= '<div class="input-group-addon">' . $tagParser->parseContent ($this->m_addonleft) . '</div>';
			}
		}
		$text .= '<input type="' . $this->m_inputtype . '" class="form-control" id="' . $this->getFullName() . '"';
		if (isset ($this->m_placeholder))
			$text .= ' placeholder="' . $tagParser->parseContent ($this->m_placeholder) . '" ';
		if (isset ($this->m_value))
			$text .= ' value="' . $this->escapeAttr ($this->getFormattedValue($tagParser)) . '"';
		if ($this->checkReadonly())
			$text .= ' readonly';
		if (isset ($this->m_size))
			$text .= ' size="' . $this->m_size . '"';;
		if (isset ($this->m_maxlength))
			$text .= ' maxlength="' . $this->m_maxlength . '"';;


		if (isset($this->m_help) && !isset($this->m_help_popover))
		{
			$text .= ' aria-describedby="help' . $this->getFullName () .'" ';
		}


		$text .= '>';

		if ($this->m_addon == true)
		{
			if (isset ($this->m_addonright))
			{
 				$text .= '<div class="input-group-addon">' . $tagParser->parseContent ($this->m_addonright) . '</div>';
			}
			if (isset ($this->m_help) && isset ($this->m_help_popover))
			{
				$popover = new DD_PopOver (array (
				    'name'=>'help_' . $this->getFullName
					,'activator'=>'link'
				    ,'content'=>$this->m_help
				    ,'placement'=>$this->m_help_popover
					,'html'=>'data-html=true'
					));
				$popover->set ($tagParser->parseContent ('{dd:glyph.fa-question}'));

 				$text .= '<div class="input-group-addon">' . $popover->toString ($tagParser) . '</div>';
			}
		}
		$text .= '</div>';
		if (isset ($this->m_help) && !isset ($this->m_help_popover))
			$text .= '<span id="help' . $this->getFullName () . '" class="help-block">' . $tagParser->parseContent ($this->m_help) . '</span>';
		return $text;
	}
}
?>
