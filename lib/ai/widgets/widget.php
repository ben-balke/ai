<?php

/***************************************************************************
*** CLASS
*****************************************************************************/
class Widget extends DD_DomField
{
	var				$m_widgetwidth;
	var				$m_widgetheight;
	var				$m_widgettitle;
	var				$m_repopulate;
					// Repopulate the shell with new data.

	function Widget ($name, $type, $title, $width = null, $height = null)
	{
		$params = array ('name'=>$name,'type'=>$type);
		parent::DD_DomField ($params);
		$this->m_widgetwidth = $width;
		$this->m_widgetheight = $height;
		$this->m_widgettitle = $title;
	}

	function openContainingDiv ($tagParser)
	{
		$text = '<div class="widgetouter" id="id_' . $this->m_name . '">';
		return $text;
	}
	function closeContainingDiv ($tagParser)
	{
		return '</div>';
	}
	function renderHeader ($tagParser)
	{
		global $DD_imagePath;
		/* We use a table to get the mouseovers working on the entire title.
		 */
		$text = $this->openContainingDiv ($tagParser);
		$text .= '<table ' .  (isset ($this->m_widgetwidth) ?  'width="'.($this->m_widgetwidth).'" ' : ' width="100%"');
		$text .= 'cellspacing="0" cellpadding="0" border="0"><tr><td class="widgettitle" nowrap="nowrap" width="100%"
				onmouseover="javascript:$(\''.$this->m_name.'-edit-img\').src=\''.$DD_imagePath.'widgetedit.png\';"
				onmouseout="javascript:$(\''.$this->m_name.'-edit-img\').src=\''.$DD_imagePath.'widgetedit-grey.png\';">
			<div id="'.$this->m_name.'-control" class="widgetcontrols"> 
				<a href="#" class="widgetcontrol" onclick="' . $this->getOnClickEdit ($tagParser) . '"><img alt="" src="' . $DD_imagePath . 'widgetedit-grey.png" id="' . $this->m_name . '-edit-img" /></a> 
				</div>'
				.$this->m_widgettitle.'
		</td></tr></table>';
		/*
		 * Edit Div when the edit is checked.
		 */
		$text .= '<div class="widget-edit-control" id="'.$this->m_name.'-edit">';
		$text .= '<div class="widget-edit-control-title">';
		$text .= $this->getEditControlTitle ($tagParser);
		$text .= '</div>';
		$text .= '<div class="widget-edit-control-content">';
		$text .= $this->getEditControl ($tagParser);
		$text .= '</div>';
		$text .= '</div>';

		$text .= '<div class="widgetinner" ';
		if (isset ($this->m_widgetwidth) ||isset ($this->m_widgetheight))
		{
			$text .= "style=\"";
			if (isset ($this->m_widgetwidth))
				$text .= "width:".($this->m_widgetwidth).";";
			if (isset ($this->m_widgetheight))
				$text .= "height:".($this->m_widgetheight)."; overflow: auto;";
			$text .= "\""; 
		}
		/*
		 * This is where we name the inner object that can be refreshed
		 */
		$text .= '><table width="100%" class="widgettable" cellpadding="2" cellspacing="0" border="0"><tr><td id="' . 
			$this->getContentObjectName() . '">';
		return $text;
	}
	function renderFooter ($tagParser)
	{
		$text = '</td></tr>
			</table></div>';
		$text .= $this->closeContainingDiv ($tagParser);
		return $text;
	}
	function getOnClickEdit ($tagParser)
	{
		$text = 'WIDGET_showControlDiv (\''.$this->m_name.'\');';
		$text .= "return false;";
		return $text;
	}
	function getOnClickRemove ($tagParser)
	{
		return "alert ('Remove Picture'); return false;";
	}
	function getEditControl ($tagParser)
	{
		return "";
	}
	function getEditControlTitle ($tagParser)
	{
		return "TODO: Edit Control";
	}
	function getContentObjectName ()
	{
		return $this->m_name . '_content';
	}

}

class DynamicWidget extends Widget
{
	/* Overload this to return an array of items names from $AdminProperties 
	 * to get properties for.
	 */
	function getEditControlProperties ()
	{
		return null;
	}
	function getEditControl ($tagParser)
	{
		$items = $this->getEditControlProperties ();
		if (isset ($items))
		{
	   		global $AdminProperties;
	   		global $PropertyUpdate;
	   		$fv = new DD_FieldValueProvider ('edit');
			$fv->makeField ($PropertyUpdate);

			$content='<form action="{dd:dd.path}widget/set_admin_props.php" method="post"><table align="center" class="widget-control-edit-table">';
			foreach ($items as $name)
			{
				$content .= '<tr><td class="label">{dd:edit.' . $name . '.label}</td><td class="data">{dd:edit.' . $name .'}</td></tr>';
				$fv->makeField ($AdminProperties [$name]);
			}
			$content .= '<tr><td colspan="2" align="center">{dd:edit.DD_update}&nbsp;<input type="button" onclick="WIDGET_hideControlDiv ();" class="button" value="Cancel" /></td></tr></table></form>';
			$fv->prepareForRecord ($tagParser);
			$tagParser->addValueProvider ($fv);
			$text = $tagParser->parseContent ($content);
			$tagParser->removeValueProviderByName ('edit');
			return $text;
		}
		return "";
	}
}
?>
