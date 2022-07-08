<?php
class DD_Modal extends DD_DomField
{
	var				$m_class;
	var				$m_title;
	var				$m_body;
	var				$m_footer;
	var				$m_button;

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
		if (isset ($array ['body']))
			$this->m_body = $array ['body'];
		if (isset ($array ['footer']))
			$this->m_footer = $array ['footer'];
		if (isset ($array ['button']))
			$this->m_button = $array ['button'];
	}

	function prepareForRecord ($tagParser)
	{
		parent::prepareForRecord ($tagParser);
	}

	function toString ($tagParser)
	{
		if (!isset ($this->m_class))
		{
			$this->m_class = "fade";
		}
		$text =	"";
		$val = $this->getFormattedValue ($tagParser);
		if (isset ($this->m_button))
		{
			$text .= '<button type="button" class="btn btn-primary btn-lg" data-toggle="modal" data-target="#' . $this->getFullName () . '">' . $tagParser->parseContent ($this->m_button) . '</button>';
		}


		$text .= '
		<div tabindex="-1" role="dialog"  class="modal ' 
			. $this->m_class 
			. '" id="' . $this->getFullName () . '"  aria-labelledby="'. $this->getFullName () . 'Label">'
			. '<div class="modal-dialog">
				<div class="modal-content">
					<div class="modal-header">	
						<button type="button" class="close" data-dismiss="modal" aria-label="Close">
							<span aria-hidden="true">&times;</span>
						</button>
						<h4 class="modal-title">' . $tagParser->parseContent ($this->m_title) . '</h4>
					</div>
					<div class="modal-body">
				        <p>' . $tagParser->parseContent ($this->m_body) . '</p>
			   		</div>
					<div class="modal-footer">'
				        . $tagParser->parseContent ($this->m_footer) . '
			   		</div>
			   	</div>
			</div>
		</div>';
							  
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
