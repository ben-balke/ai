<?php
class DD_PopOver extends DD_DomField
{
	var				$m_class;
	var				$m_title;
	var				$m_content;
	var				$m_activator = "button"; // button/link/glyph
	var				$m_placement = "right";

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
		if (isset ($array ['title']))
			$this->m_title = $array ['title'];
		if (isset ($array ['content']))
			$this->m_content = $array ['content'];
		if (isset ($array ['placement']))
			$this->m_placement = $array ['placement'];
		if (isset ($array ['activator']))
			$this->m_activator = $array ['activator'];
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
		switch ($this->m_activator)
		{
			case 'link':
				$text .= '<a class="link ' . $this->m_class . '"  tabindex=0 data-trigger="focus" role="button"';
				break;
			case 'button':
			default:
				$text .= '<button type="button" class="btn btn-default ' . $this->m_class . '"';
				break;
		}
		$text .= ' data-container="body" data-toggle="popover" name="' 
				. $this->getFullName () . '" '; 
		if (isset($this->m_title))
			$text .= 'title="' . $this->escapeAttr ($tagParser->parseContent ($this->m_title)) . '" ';

		$text .= ' data-content="' . $this->escapeAttr ($tagParser->parseContent ($this->m_content)) . '" '
				. $this->getHtml ($tagParser) 
				. ' data-placement="' . $this->escapeAttr ($this->m_placement) 
				. '">' . $this->m_value;
		switch ($this->m_activator)
		{
			case 'link':
				$text .= '</a>';
				break;
			case 'button':
			default:
				$text .= '</button>';
				break;
		}
		return $text;
	}
    public function getAttributeContent ($attribute, $tagParser)
    {
$javascript=<<<JAVASCRIPT
$(function () {
  $('[data-toggle="popover"]').popover()
})
JAVASCRIPT;
        if ($attribute == 'javascript')
        {
			return $javascript;
        }
        return null;
    }
}
?>
