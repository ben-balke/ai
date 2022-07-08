<?php
/*
 * This class puts up a dialog in the middle of the page as an iframe surrounded by a div that is dynamically shown and modal in nature. 
 */
class DD_IframePopup extends DD_DomField
{
	var				$m_width;
	var				$m_height;
	var				$m_url;
	var				$m_args;

	function DD_IframePopup ($name, $url, $args /*array*/, $width = null, $height = null, $html = null)
	{
		$params = array ('name'=>$name, 'type'=>'as_popup');
		parent::DD_DomField ($params);
		$this->m_url = $url;
		$this->m_args = $args;
		$this->m_width = $width;
		$this->m_height = $height;
		$this->m_html = $html;
	}
	function toString ()
	{
		$text = '<script language="javascript">';
		$text .= "\nfunction hide_" . $this->m_name . " (blankwindow) \n{\n";
		$text .= "	DD_HideDiv ('" . $this->m_name . "-popup');\n";
		$text .= "	if (blankwindow == true) $('" . $this->m_name . "-iframe').contentWindow.location = 'about:blank';";
		$text .= "}\n";
		$text .= 'function show_' . $this->m_name . ' (' . "\n";
		$args = $this->getArgs ();
		if (isset ($args))
		{
			$i = 0;
			foreach ($args as $name)
			{
				if ($i != 0)
					$text .= ',';
				$text .= $name;
				$i++;
			}
		}
		$text .= "){";
		$text .= "var url = '" . $this->getBaseUrl () . "';\n";
		if (isset ($args))
		{
			$text .= "if (url.indexOf ('?') == -1)";
			$text .= "sep = '?';";
			$text .= "else sep = '&';";
			foreach ($args as $name)
			{
				$text .= "\n" . 'url += sep + "' . $name . '="';
				$text .= '+escape (' . $name . ');';
				$text .= 'sep = "&";';
			}
		}
		$text .= "\n" . 'url += sep + "popupname=" + escape(\'' . $this->m_name . '\');';
		$text .= "\n" . '$(\'' . $this->m_name . '-iframe\').src = url; ';
		$text .= "\n" . '$(\'' . $this->m_name . '-iframe\').contentWindow.location = url;';
		$text .= "\n" . 'DD_ShowDivAndCenter (\'' . $this->m_name . '-popup\');';
		
		$text .= '}</script>';
		$text .= ' <div name="' . $this->m_name . '-popup"';
		$text .= ' id="' . $this->m_name . '-popup" class="iframe-popup"><iframe scrolling=no frameborder=0 id="' . $this->m_name . '-iframe" name="' . $this->m_name . '-iframe" class="iframe-popup"';
		if (isset ($this->m_html))
		{
			$text .= ' ' . $this->m_html . ' ';
		}
		if (isset ($this->m_width) && $this->m_width != 0)
		{
			$text .= ' width='. $this->m_width;
		}
		if (isset ($this->m_height) && $this->m_height != 0)
		{
			$text .= ' height='. $this->m_height;
		}
		if (isset ($this->m_height) && $this->m_height == 0)
		{
			$text .= " scrolling=no ";
		}
		
		$text .= '>';
		$text .= '</iframe></div>';
		return $text;
	}
	function getArgs ()
	{
		return $this->m_args;
	}
	function getBaseUrl ()
	{
		return $this->m_url;
	}
}
?>
