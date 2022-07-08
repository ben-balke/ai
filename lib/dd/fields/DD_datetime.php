<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/lib/dd/fields/DD_datetime.php,v 1.2 2012/01/03 07:39:13 secwind Exp $
*************************************************************************/

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
		parent::DD_DomField ($params);
	}
	function toString ($tagParser)
	{
		return ($tagParser->parseContent ('
<link href="{dd:dd.styles}duckdigit/epoch_styles.css" rel="stylesheet" type="text/css"/>
<script type="text/javascript" src="{dd:dd.scripts}duckdigit/epoch_classes.js"></script>
<script type="text/javascript" src="{dd:dd.scripts}duckdigit/dd_datetime.js"></script>'
			));

	}
}

class DD_DatetimeField extends DD_DomHidden
{
	var			$m_interval;
	function __construct ($name, $interval = 5, $extension = null)
	{
		$params = array ('name'=>$name, 'datatype'=>'datetime', 'html'=>'class="dd_datetime"','interval'=>$interval);
		parent::DD_DomHidden ($params, $extension);
		$this->setState ($params);
		if (isset ($extension))
			$this->setState ($extension);
	}
	function setState (&$array)
	{
		if (isset ($array ['interval']))
			$this->m_interval = $array ['interval'];
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
		$name = $this->getFullName ();
		$content = "{dd:datetime.". $name . "day}&nbsp;<img src=\"{dd:dd.path}images/cal.gif\" onclick=\"DD_doDateClick ('" . $name . "');\">&nbsp;";
		$content .= "{dd:datetime.". $name . "hour}:";
		$content .= "{dd:datetime.". $name . "minute} ";
		$content .= "{dd:datetime.". $name . "ampm}";

		$text = parent::toString ($tagParser);
		$vp = new DD_FieldValueProvider ('datetime');

			/**
			 * Create a list source for the minutes selector.  We use the interval provided
			 * to create an array.
			 */
		$minutes = array ();
		for ($i = 0, $m = 0; $m < 60; $m += $this->m_interval, $i++)
		{
			$minutes [$i] = sprintf ("%02d", $m);
		}

			/**
			 * Set up the default values for the time and date fields based on the current value
			 * or just default them.
			 */
		$default_hour = '';
		$default_minute = '';
		$default_ampm = '';
		$default_day = '';
		if (isset ($this->m_value))
		{
			if (($timestamp = strtotime ($this->m_value)) == false)
			{
				die ('bad time in DD_datetime field ' . $name . ': ' . $this->m_value);
			}
			$dateparts = getdate ($timestamp);
			$iminute = $dateparts ['minutes'];
			$iminute = ((int)($iminute / $this->m_interval)) * $this->m_interval;
			$default_minute = sprintf ("%02d", $iminute);
			$default_hour = $dateparts ['hours'];
			if ($default_hour >= 12)
			{
				$default_hour -= 12;
				$default_ampm = 'PM';
			}
			else
			{
				$default_ampm = 'AM';
			}
			if ($default_hour == 0)
			{
				$default_hour = '12';
			}
			$default_day = $this->m_value;
		}
		$vp->makeField (array ( 
			'type'=>'text'
			,'name'=>$name.'day'
			,'size'=>15
			,'maxlength'=>15
			,'datatype'=>'date'
			,'default'=>$default_day
			));

		$vp->makeField (array ( 
			'type'=>'select'
			,'name'=>$name . 'hour'
			,'noselection'=>''
			,'usekey'=>false
			,'listsource'=>new DD_RangeListSource (1,12)
			,'default'=>$default_hour
			));

		$vp->makeField (array ( 
			'type'=>'select'
			,'name'=>$name . 'minute'
			,'noselection'=>''
			,'usekey'=>false
			,'listsource'=>new DD_ArrayListSource ($minutes)
			,'default'=>$default_minute
			));

		$vp->makeField (array ( 
			'type'=>'select'
			,'name'=>$name.'ampm'
			,'usekey'=>false
			,'listsource'=>new DD_ArrayListSource (array ('AM','PM'))
			,'default'=>$default_ampm
			));
		
		$tagParser->addValueProvider ($vp);
		$vp->prepareForRecord ($tagParser);
		$text .= $tagParser->parseContent ($content);
		$tagParser->removeValueProviderByName ($vp->m_name);
		return $text;
	}
}

class DD_DateField extends DD_DomText
{
	function __construct ($name, $extension = null)
	{
		$params = array ('name'=>$name, 'datatype'=>'date', 'html'=>'class="dd_date"','maxlength'=>12,'size'=>12);
		parent::DD_DomText ($params, $extension);
	}
	function toString ($tagParser)
	{
		$text = DD_DomText::toString ($tagParser);
		$text .= $tagParser->parseContent ("&nbsp;<img src=\"{dd:dd.path}images/cal.gif\" onclick=\"" . "DD_doDateClick ('" . $this->m_name . "');\">&nbsp;");
		return $text;
	}
}

?>
