<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/lib/2d/fields/DD_datetime.php,v 1.2 2016/03/16 18:48:52 secwind Exp $
*************************************************************************/
// SEE: https://eternicode.github.io/bootstrap-datepicker

/************************************
** CONTENT
************************************/
//
// How use:
// Add a DD_DatetimeHeader to your page and include a tag for it within your <head> section.  Declare DD_DatetimeField or DD_DateField
// within our record handler and include them.  Everything else is handled automatically.
//

class DD_DatetimeHeader extends DD_DomField
{
	function __construct ($name)
	{
		$params = array ('name'=>$name, 'type'=>'work');
		parent::__construct ($params);
	}
	function toString ($tagParser)
	{
		return ($tagParser->parseContent ('
<link href="/styles/bootstrap-datetimepicker.min.css" rel="stylesheet" type="text/css"/>
<script type="text/javascript" src="/scripts/bootstrap-datetimepicker.min.js"></script>'
			));
//<link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-datepicker/1.6.0/css/bootstrap-datepicker.min.css.map" rel="stylesheet" type="text/css"/>

	}
}

class DD_DatetimeField extends DD_DomField
{
	var			$m_stepping;
	function __construct ($name, $stepping = 5, $extension = null)
	{
		$params = array ('name'=>$name, 'format'=>'d-M-Y H:m', 'datatype'=>'datetime', 'interval'=>$interval);
		parent::__construct ($params, $extension);
		$this->setState ($params);
		if (isset ($extension))
			$this->setState ($extension);
		$this->m_stepping = $stepping;
	}
	function getDatePart ($part)
	{
		if (!isset ($this->m_value) || (($timestamp = strtotime ($this->m_value)) == false))
		{
			return null;
		}
		$dateparts = getdate ($timestamp);
		return $dateparts [$part];
	}

	public function getAttributeValue ($attribute, $tagParser)
	{
		switch ($attribute)
		{
		case 'seconds':
		case 'minutes':
		case 'hours':
		case 'mday':
		case 'wday':
		case 'mon':
		case 'year':
		case 'yday':
		case 'weekday':
		case 'month':
		case 'epoch':
			return $this->getDatePart ($attribute);
		case 'javascript':
			return $js;
		default:
			break;
		}
		return null;
	}
	/*
	 * Return an attribute's content from the field.  Override this to provide
	 * additional attributes from your derived class.
	 */
	public function getAttributeContent ($attribute, $tagParser)
	{
		return $this->getAttributeValue ($attribute, $tagParser);
	}

	function toString ($tagParser)
	{
		//$text = '<div class="form-group">';
		//$text .= $this->getLabelTag ($tagParser);
		//$text .= '<div class="input-group date form_datetime" id="div-' . $this->getFullName () . '">';
		//$text .= '<input type="text" size="16" class="form-control" value="' . $this->getFormattedValue ($tagParser) . '" id="' . $this->getFullName () . '" name="' . $this->getFullName () . '"';
		//$text .= ' aria-describedby="help' . $this->getFullName () .'"';
		//$text .= '><span class="input-group-addon"><i class="glyphicon glyphicon-th"></i></span></div></div>';
		//return $text;
		$text = '<div class="form-group">';
		$text .= $this->getLabelTag ($tagParser);
		$text .= '<div class="input-group date" id="div-' . $this->getFullName () . '">';
		$text .= '<input type="text" size="16" class="form-control" value="' . $this->getFormattedValue ($tagParser) . '" id="' . $this->getFullName () . '" name="' . $this->getFullName () . '"';
		$text .= ' aria-describedby="help' . $this->getFullName () .'"';
		if ($this->m_required)
			$text .= ' required';
		$text .= ' aria-describedby="help' . $this->getFullName () .'"';
		$text .= $tagParser->parseContent ('><span class="input-group-addon">{dd:glyph.calendar}</span></div></div>');
		$js = '<script>$("#div-' . $this->getFullName () . '").datetimepicker ({';
		$js .= 'format: "D-MMM-YYYY HH:mm"'; //, autoclose: true, todayBtn: true, pickerposition: "bottom-left"';
		$js .= ',defaultDate: "' . $this->getFormattedValue ($tagParser) . '", stepping: "' . $this->m_stepping . '"'; //, autoclose: true, todayBtn: true, pickerposition: "bottom-left"';
		$js .= '});</script>';
		return $text . $js;
	}
}

class DD_DateField extends DD_DomField
{
	function __construct ($name, $extension = null)
	{
		$params = array ('name'=>$name, 'format'=>'d-M-Y', 'datatype'=>'date', 'html'=>'class="dd_date"','maxlength'=>12,'size'=>12);
		parent::__construct ($params, $extension);
	}

	function toString ($tagParser)
	{
		$text = '<div class="form-group">';
		$text .= $this->getLabelTag ($tagParser);
		$text .= '<div class="input-group date form_datetime" id="div-' . $this->getFullName () . '">';
		$text .= '<input type="text" size="12" maxlength=12 class="form-control" value="' . $this->getFormattedValue ($tagParser) . '" id="' . $this->getFullName () . '" name="' . $this->getFullName () . '"';
		$text .= ' aria-describedby="help' . $this->getFullName () .'"';
		if ($this->m_required)
			$text .= ' required';
		$text .= $tagParser->parseContent ('><span class="input-group-addon">{dd:glyph.calendar}</span></div></div>');
		$js = '<script>$("#div-' . $this->getFullName () . '").datetimepicker ({';
		$js .= 'format: "D-MMM-YYYY"'; //, autoclose: true, todayBtn: true, pickerposition: "bottom-left"';
		$js .= ',defaultDate: "' . $this->getFormattedValue ($tagParser) . '"'; //, autoclose: true, todayBtn: true, pickerposition: "bottom-left"';
		$js .= '});</script>';
		return $text . $js;
	}
}

?>
