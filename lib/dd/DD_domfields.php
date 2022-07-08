<?php
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/lib/dd/DD_domfields.php,v 1.17 2016/03/08 23:22:22 secwind Exp $
*************************************************************************/

require_once "dd/DD_sql.php";
require_once "dd/DD_util.php";

$DD_DomProperties = array (
 'date'=>'m/d/Y'
,'datetime'=>'m/d/y H:i:s'
);

function DD_MakeField ($array, $extension = null)
{
	$field = null;
	if (isset ($array ['type']))
		$type = $array ['type'];
	if (isset ($extension) && isset ($extension ['type']))
	{
		$type = $extension ['type'];
	}
	if (!isset ($type))
	{
		$type = "display";
	}
	switch ($type)
	{
	case "select":
		$field = new DD_DomSelect ($array, $extension);
		break;
	case "checkbox":
		$field = new DD_DomCheckBox ($array, $extension);
		break;
	case "text":
		$field = new DD_DomText ($array, $extension);
		break;
	case "password":
		$field = new DD_DomPassword ($array, $extension);
		break;
	case "button":
		$field = new DD_DomButton ($array, $extension);
		break;
	case "file":
		$field = new DD_DomFile ($array, $extension);
		break;
	case "hidden":
		$field = new DD_DomHidden ($array, $extension);
		break;
	case "textarea":
		$field = new DD_DomTextArea ($array, $extension);
		break;
	case "submit":
		$field = new DD_DomSubmit ($array, $extension);
		break;
	case "multiitem":
		$field = new DD_DomMultiItem ($array, $extension);
		break;
	case "radio":
		$field = new DD_DomRadio ($array, $extension);
		break;
	case "tab":
		$field = new DD_DomTab ($array, $extension);
		break;
	case "links":
		$field = new DD_DomLinks ($array, $extension);
		break;
	case "work":
		$field = new DD_DomWork ($array, $extension);
		break;
	case "display":
		$field = new DD_DomDisplay ($array, $extension);
		break;
	case "image":
		$field = new DD_DomImage ($array, $extension);
		break;
	case "checklist":
		$field = new DD_DomCheckList ($array, $extension);
		break;
	case "list":
		$field = new DD_DomList ($array, $extension);
		break;
	}
	return $field;
}
/**
 * Key sources are used to create unique values before insert processing.  This 
 * is useful when you want to insert a key value into a table but need it for other
 * record handers or redirecting to a page that displays the new record etc.
 */
class DD_KeySource
{
	function DD_KeySource ()
	{
	}
	function makeKey ($tagParser)
	{
		return null;
	}
}

class DD_SqlKeySource extends DD_KeySource
{
	var			$m_query;
	var			$m_connect;
	function DD_SqlKeySource ($connect, $query)
	{
		parent::DD_KeySource ();
		$this->m_query = $query;
		$this->m_connect = $connect;
	}
	function makeKey ($tagParser)
	{
		$rslt = null;
		$conn = DD_SqlPoolGetConn ($this->m_connect);
		$result = $conn->query($tagParser->parseSql ($this->m_query));
		if ($row = $conn->nextRecord ($result))
		{
			if (isset ($row[0]))
			{
				$rslt = $row[0];
			}
   		}
		$conn->freeQuery ($result);
		return $rslt;
	}
}

/**
 * ListSources provide options for selects and radio buttons.
 * You can specialize this boy to provide any type of list from
 * any source including files, rpc, soap etc.  You just need
 * to populate an array by overloading makeList ().  You can 
 * specify that the values are parsed by the TagParser or they
 * are cached once.  A cache value of false causes the makeList()
 * to be called for each record in a record handler.  This is 
 * a very important performance issue.
 */
class DD_ListSource
{
	var			$m_array;
	var			$m_cache; // true means to only use makeList once.
	var			$m_parse; // Parse means to use the tag parser for each key and value pair.
	function DD_ListSource ($cache, $parse)
	{
		$this->m_cache = $cache;
		$this->m_parse = $parse;
	}
	function getList ()
	{
		return $this->m_array;
	}
	function makeList ($tagParser)
	{
	}
}

/* Uses the range method to create the list. */
class DD_RangeListSource extends DD_ListSource
{
	function DD_RangeListSource ($low, $high, $step = 1)
	{
		parent::DD_ListSource (true, false);
		$this->m_array = range ($low , $high, $step);
	}
}

class DD_ArrayListSource extends DD_ListSource
{
	var			$m_origArray;
	function DD_ArrayListSource ($array, $cache = true, $parse = false)
	{
		parent::DD_ListSource ($cache, $parse);
		$this->m_origArray = $array;
	}
	function makeList ($tagParser)
	{
		if (($this->m_cache || !$this->m_parse) && isset ($this->m_array))
		{
			return;
		}
		if ($this->m_parse)
		{
			
		}
		$this->m_array = $this->m_origArray;
	}
}
class DD_DirectoryListSource extends DD_ListSource
{
	var			$m_origArray;
	function DD_DirectoryListSource ($directory, $regex)
	{
		parent::DD_ListSource (true, false);
		$this->m_array = array();
		$handler = @opendir($directory);
		while ($file = @readdir($handler)) 
		{

			if ($file != "." && $file != "..") 
			{
				$this->m_array[] = $file;
			}
		}
		@sort ($this->m_array, SORT_STRING);
		@closedir($handler);
	}
}


/*
 * Creates a source from an SQL statement for DOM fields that allow selection.
 * If the result set returns 1 column, it is used for both the value and 
 * option.
 * If the result set returns 2 columns the first column is used as the 
 * value and the 2nd as the option (displayed).
 * If the result set returns 3 columns the first column is used as the
 * value, the 2nd as the option and the 3rd is used to group items.
 */
class DD_SqlListSource extends DD_ListSource
{
	var			$m_query;
	var			$m_connect;

	function DD_SqlListSource ($connect, $query, $cache = true, $parse = false)
	{
		parent::DD_ListSource ($cache, $parse);
		$this->m_query = $query;
		$this->m_connect = $connect;
	}
	function makeList ($tagParser)
	{
		if ($this->m_cache && isset ($this->m_array))
		{
			return;
		}
		$conn = DD_SqlPoolGetConn ($this->m_connect);
		$result = $conn->query($tagParser->parseSql ($this->m_query));
		$this->m_array = null;
		$this->m_array = array ();
		while($row = $conn->nextRecord ($result))
		{
			if (!isset ($row[1]))
			{
				if ($this->m_parse)
					$this->m_array [] = $tagParser->parseContent ($row [0]);
				else
					$this->m_array [] = $row [0];
			}
			else if (!isset ($row[2]))
			{
				if ($this->m_parse)
					$this->m_array [$tagParser->parseContent ($row [0])] = $tagParser->parseContent ($row [1]);
				else
					$this->m_array [$row [0]] = $row [1];
			}
			else
			{
				if ($this->m_parse)
					$this->m_array [$tagParser->parseContent ($row [0])] = 
						array ($tagParser->parseContent ($row [1], $tagParser->parseContent ($row [2])));
				else
					$this->m_array [$row [0]] = array ($row [1], $row [2]);
			}
   		}
		$conn->freeQuery ($result);
	}
}
/**
 * Base abstract class for a DOM field in html.  Note that a display type of field
 * can be used for anytype of generated content including xml, json, soap etc.
 */
class DD_DomField
{
	var			$m_name;
	var			$m_value;
	var			$m_html;
	var			$m_readonly;
	var			$m_default;
	var			$m_keysource;
	var			$m_datatype;
	var			$m_format;
	var			$m_label;  // For generating a label associated to the field. <vp>.<field>.label.
	var			$m_multirecord = false;  // TRUE or FALSE the field is a multirecord field and uses the recordExtension.
	var			$m_ellipsisat; // Max number of characters to allow before truncating value with m_ellipsis.
	var			$m_ellipsis;	// After ellipsisat characters are reached truncate with this string.  Default is "..."
		/*
		 * RecordExtension is used for multirecord forms.
		 */
	var			$m_recordExtension;
	
	function DD_DomField (&$array, $extension = null)
	{
		self::setState ($array);
		if (isset ($extension))
			self::setState ($extension);
	}
	function setState (&$array)
	{
		if (isset ($array ['name']))
			$this->m_name = $array ['name'];
		if (isset ($array ['default']))
			$this->m_default = $array ['default'];
		if (isset ($array ['readonly']))
			$this->m_readonly = $array ['readonly'];
		if (isset ($array ['html']))
			$this->m_html = $array ['html'];
		if (isset ($array ['value']))
			$this->m_value = $array ['value'];
		if (isset ($array ['keysource']))
			$this->m_keysource = $array ['keysource'];
		if (isset ($array ['datatype']))
			$this->m_datatype = $array ['datatype'];
		if (isset ($array ['format']))
			$this->m_format = $array ['format'];
		if (isset ($array ['label']))
			$this->m_label = $array ['label'];
		if (isset ($array ['multirecord']))
			$this->m_multirecord = $array ['multirecord'];
		if (isset ($array ['ellipsisat']))
			$this->m_ellipsisat = $array ['ellipsisat'];
		if (isset ($array ['ellipsis']))
			$this->m_ellipsis = $array ['ellipsis'];
	}
	function escapeAttr ($string) 
	{
		return strtr($string, array('"' => "&#034;"));
	} 
	function makeJavascriptString ($string)
	{
		return "'" . strtr ($string, array("\r\n" => '\n', "\r" => '\n', "\n" => '\n', '"' => '\\042', "'" => "\\047")) . "'";
	}

	function escapeHtml ($string)
	{
		return htmlspecialchars ($string);
	}
	function valueAsHtmlNote ()
	{
		return strtr($this->m_value, array(
			"&" => "&amp;",
			"<" => "&lt;",
			">" => "&gt;",
			"\n" => "<br>",
			"\r" => ""
			));
	}
	function valueAsHtml ()
	{
		return htmlspecialchars ($this->m_value);
	}
	function valueAsSql () 
	{
		return strtr($this->m_value, array("'" => "''"));
	} 
	function valueAsUrl ()
	{
		return urlencode ($this->m_value);
	} 
	function set ($value)
	{
		$this->m_value = $value;
	}
	function checkReadOnly ()
	{
		return isset ($this->readonly);
	}
		// Should support multi languages eventually.
	function getValue ($tagParser = null)
	{
		return $this->m_value;
	}
	function getFormattedValue ($tagParser)
	{
		global $DD_DomProperties;
		if (isset ($this->m_datatype))
		{
			switch ($this->m_datatype)
			{
			case 'string':
				if (isset ($this->m_format))
				{
					switch ($this->m_format)
					{
					case 'phone':
						return DD_formatPhone ($this->m_value);
					}
				}
				break;
			case 'int':
			case 'real':
			case 'float':
				if (!isset ($this->m_value) || $this->m_value == '')
				{
					return null;
				}
				return DD_formatNumber ($this->m_value, $this->m_format);
			case 'money':
				return DD_formatNumber ($this->m_value, "$2,");
			case 'date':
				if (!isset ($this->m_value) || $this->m_value == '')
				{
					return null;
				}
				$dateTime = new DateTime($this->m_value);
				if (!isset ($this->m_format))
				{
					$this->m_format = $DD_DomProperties ['date'];
				}
				$value = $dateTime->format($this->m_format);
				return $value;
			case 'datetime':
				if (!isset ($this->m_value) || $this->m_value == '')
				{
					return null;
				}
				$dateTime = new DateTime($this->m_value);
				if (!isset ($this->m_format))
				{
					$this->m_format = $DD_DomProperties ['datetime'];
				}
				$value = $dateTime->format($this->m_format);
				return $value;
			case 'time':
				if (!isset ($this->m_value) || $this->m_value == '')
				{
					return null;
				}
				try
				{
					$dateTime = new DateTime($this->m_value);
				}
				catch (Exception $ex)
				{
					error_log ($ex);
					return null;
				}
				if (!isset ($this->m_format))
				{
					$this->m_format = $DD_DomProperties ['time'];
				}
				$value = $dateTime->format($this->m_format);
				return $value;
			}
		}
		if (@isset ($this->m_ellipsisat) && @isset ($this->m_value))
		{
			$vallen = @strlen ($this->m_value);
			if ($vallen > $this->m_ellipsisat)
			{
				if (!isset ($this->m_ellipsis))
				{
					$this->m_ellipsis = "...";
				}
				$ellipsislen = @strlen ($this->m_ellipsis);
				if ($vallen > $ellipsislen + $this->m_ellipsisat)
					return @substr ($this->m_value, 0, $this->m_ellipsisat) . $this->m_ellipsis;
			}
		}
		return $this->m_value;
	}
	/**
	 * This determines tells a record handler that if the field is not provided in the 
	 * post or get parameters that it is set to null.  This is used for checkbox fields mainly. 
	 */
	function wantsNulls ()
	{
		return false;
	}
	/**
	 * This determines that the field is not to receive a null value from a
	 * record handler nextRecord if it is not provided in the record set.
	 */
	function isWorkField ()
	{
		return false;
	}
	function getSqlValue ($tagParser)
	{
		if (isset ($this->m_datatype))
		{
			switch ($this->m_datatype)
			{
			case 'string':
			case 'time':
				return $this->m_value;
			case 'int':
				if (!isset ($this->m_value) || $this->m_value == '')
				{
					return null;
				}
				return DD_stripInteger ($this->m_value);

			case 'float':
			case 'real':
			case 'money':
				if (!isset ($this->m_value) || $this->m_value == '')
				{
					return null;
				}
				return DD_stripNumber ($this->m_value);
			case 'date':
				if (!isset ($this->m_value) || $this->m_value == '')
				{
					return null;
				}
				$dateTime = new DateTime($this->m_value);
				return $dateTime->format('Y-m-d');
			case 'datetime':
				if (!isset ($this->m_value) || $this->m_value == '')
				{
					return null;
				}
				$dateTime = new DateTime($this->m_value);
				return $dateTime->format('Y-m-d H:i:s');
			}
		}
		return $this->m_value;
	}
	function getHtml ($tagParser)
	{
		if (isset ($this->m_html))
		{
			$tagParser->setMode (MODE_VALUE);
			$r = $tagParser->parseContent ($this->m_html);
			$tagParser->restoreMode ();
			return $r;
		}
		return "";
	}
	function setHtml ($html)
	{
		$this->m_html = $html;
	}
	function setRecordExtension ($ext)
	{
		if ($this->m_multirecord == true)
		{
			$this->m_recordExtension = $ext;
		}
	}
	function getFullName ()
	{
		if (isset ($this->m_recordExtension))
		{
			return $this->m_name . $this->m_recordExtension;
		}
		return $this->m_name;
	}
	function prepareForRecord ($tagParser)
	{
		$this->setDefault ($tagParser);	
	}
	function setDefault ($tagParser)
	{
		if (isset ($this->m_default))
		{
			$this->m_value = $tagParser->parseContent ($this->m_default);
		}
		else
		{
			$this->m_value = null;
		}
	}
	function doKeySource ($tagParser)
	{
		if (isset ($this->m_keysource))
		{
			$value = $this->m_keysource->makeKey ($tagParser);
			$this->set ($value);
		}
	}

	function setFromQuery ($connect, $query, $tagParser = null)
	{
		$rslt = null;
		$conn = DD_SqlPoolGetConn ($connect);
		if (isset ($tagParser))
		{
			$query = $tagParser->parseSql ($query);
		}
		$result = $conn->query($query);
		if ($row = $conn->nextRecord ($result))
		{
			if (isset ($row[0]))
			{
				$rslt = $row [0];
				$this->set ($rslt);
			}
   		}
		$conn->freeQuery ($result);
	}
	function getLabelTag ()
	{
		$text =	"";
		$text .= '<label for="' . $this->getFullName () . '"';
		$text .= ' id="label_' . $this->getFullName () . '">';
		if (isset ($this->m_label))
		{
			$text .= $this->escapeHtml ($this->m_label);
		}
		$text .= '</label>';
		return $text;
	}
	function getLabel()
	{
		return $this->m_label;
	}
	/*
	 * Return an attribute value from the field.  Override this to provide
	 * additional attributes from your derived class.  You should also 
	 * override getAttributeContent() to provide HTML content if appropriate.
	 */
	public function getAttributeValue ($attribute, $tagParser)
	{
		switch ($attribute)
		{
		case 'label':
			return $this->getLabel ();
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
		switch ($attribute)
		{
		case 'label':
			return $this->getLabelTag ();
		default:
			break;
		}
		return null;
	}
	public function isnull ()
	{
		return !@isset ($this->m_value);
	}
}


/**
 * Properties of a DomSelect are
 * 	noselection = option that represents a non-selected value.  This is useful when
 * 		a SQL statement is used as the DD_ListSource and it does not have a no select valeu
 *		in its table.
 *  multi = specifies that a multi item selection.
 *  size = 1 is a drop down, > 1 shows n items on the screen.
 *  listsource = A DD_ListSource derived class.  If the list source option
 *  array [1] is an array the second element is used as an <optgroup> tag.
 */
class DD_DomSelect extends DD_DomField
{
	var			$m_lastGroup;
	var			$m_size;
	var			$m_multi;
	var			$m_noselection;
	var			$m_listsource;
	var			$m_usekey;

	function DD_DomSelect ($array, $extension)
	{
		parent::DD_DomField ($array, $extension);
		$this->m_usekey = true;
		$this->setState ($array);
		if (isset ($extension))
			$this->setState ($extension);

		// Validate
		if (!isset ($this->m_listsource))
			die ("Need a list source for field " . $this->m_name);
	}
	function setState (&$array)
	{
		if (isset ($array ['noselection']))
			$this->m_noselection = $array ['noselection'];
		if (isset ($array ['size']))
			$this->m_size = $array ['size'];
		if (isset ($array ['multi']))
			$this->m_multi = $array ['multi'];
		if (isset ($array ['listsource']))
			$this->m_listsource = $array ['listsource'];
		if (isset ($array ['usekey']))
			$this->m_usekey = $array ['usekey'];
	}

	function prepareForRecord ($tagParser)
	{
		parent::prepareForRecord ($tagParser);
		$this->m_listsource->makeList ($tagParser);
	}
	function makeHeader ($tagParser)
	{
		if (isset ($this->m_multi))
		   $nameext = "[]";
		else
			$nameext = "";
		$text = "<select name='" . $this->getFullName () . $nameext . "' " . $this->getHtml ($tagParser) . " id='" . $this->getFullName () .  "'";
		if (isset ($this->m_multi))
		{
			$text .= " multiple ";
		}
		if (isset ($this->m_size))
		{
			$text .= " size=$this->m_size ";
		}
		$text .= ">\n";
		if (isset ($this->m_noselection))
		{
			$equal = strpos ($this->m_noselection, "=");
			if ($equal !== false)
			{
				$value = substr ($this->m_noselection, 0, $equal);
				$option = substr ($this->m_noselection, $equal + 1);
				$text .= $this->makeOption ($option, $value);
			}
			else
			{
				$text .= $this->makeOption ($this->m_noselection);
			}
		}
		return $text;
	}
	function makeOption ($option, $value = null)
	{
		$selected = '';
		$text = '';
		if (isset ($value))
		{
			if (!is_array ($option))
			{
				if (isset ($this->m_value) && $value == $this->m_value)
					$selected = ' selected="selected" ';
				$text = "<option value='" . $this->escapeAttr ($value) . "' $selected>" . 
					$this->escapeHtml ($option) . "</option>";
			}
			else
			{
				if (isset ($option [1]) && $option [1] != '')
				{
					if ($option [1] != $this->m_lastGroup)
					{
						if ($this->m_lastGroup != '')
						{
							$text .= '</optgroup>';
						}
						$text .= '<optgroup label="' . $this->escapeAttr ($option [1]) . '">';
						$this->m_lastGroup = $option [1];
					}
				}
				if (isset ($this->m_value) && $value == $this->m_value)
					$selected = ' selected="selected" ';
				$text .= "<option value='" . $this->escapeAttr ($value) . "' $selected>" . 
					$this->escapeHtml ($option [0]) . "</option>";
			}
		}
		else
		{
			if (isset ($this->m_value) && $option == $this->m_value)
				$selected = ' selected="selected" ';
			$text = "<option $selected>" . $this->escapeHtml ($option) . "</option>";
		}
		return $text;
	}
	function toString ($tagParser)
	{
		$this->m_lastGroup = '';
		$text = $this->makeHeader ($tagParser);
		if (!isset ($this->m_listsource))
		{
			die ("Select must have a list source");
		}
		$lv = $this->m_listsource->getList ();
		foreach($lv as $key=>$val)
		{
			if (isset ($this->m_usekey) && $this->m_usekey == true)
			{
				$text .= $this->makeOption ($val, $key);
			}
			else
			{
				$text .= $this->makeOption ($val);
			}
		} 
		$text .= "</select>";
		return $text;
	}
}
class DD_DomRadio extends DD_DomField
{
	var			$m_separator = '&nbsp;';/* between option. most likely <br> if changed */
	var			$m_prehtml = '&nbsp;'; 	/* Before we start outputing the radio. */
	var			$m_posthtml = "";		/* After we start outputing the radiooption text */
	var			$m_midhtml = "";		/* Mid html between the option and text visa versa*/
	var			$m_listsource;
	var			$m_usekey = true;
	var			$m_textleft = false;
	var			$m_usetablerows;
	var			$m_donotparse;

	function DD_DomRadio ($array, $extension)
	{
		parent::DD_DomField ($array, $extension);
		$this->setState ($array);
		if (isset ($extension))
			$this->setState ($extension);
		if (!isset ($this->m_listsource))
			die ("Need a list source for field " . $this->m_name);
	}
	function setState (&$array)
	{
		if (isset ($array ['separator'])) 
			$this->m_separator = $array ['separator'];
		if (isset ($array ['prehtml']))
			$this->m_prehtml = $array ['prehtml'];
		if (isset ($array ['posthtml']))
			$this->m_posthtml = $array ['posthtml'];
		if (isset ($array ['midhtml']))
			$this->m_midhtml = $array ['midhtml'];
		if (isset ($array ['listsource']))
			$this->m_listsource = $array ['listsource'];
		if (isset ($array ['usekey']))
			$this->m_usekey = $array ['usekey'];
		if (isset ($array ['textleft']))
			$this->m_textleft = $array ['textleft'];
		if (isset ($array ['textleft']))
			$this->m_textleft = $array ['textleft'];
		if (isset ($array ['usetablerows']))
			$this->m_textleft = $array ['usetablerows'];
		if (isset ($array ['donotparse']))
			$this->m_donotparse = $array ['donotparse'];
	}
	function makePreHtml (&$val, &$option, $tagParser)
	{
		return $this->m_prehtml;
	}
	function makeMidHtml (&$val, &$option, $tagParser)
	{
		return $this->m_midhtml;
	}
	function makePostHtml (&$val, &$option, $tagParser)
	{
		return $this->m_posthtml . $this->m_separator;
	}
	function prepareForRecord ($tagParser)
	{
		parent::prepareForRecord ($tagParser);
		$this->m_listsource->makeList ($tagParser);
	}
	function makeOption ($value, $option, $tagParser)
	{
		if ($this->m_donotparse == true)
			return $option;
		return htmlspecialchars ($option);
	}
	function makeRadio ($val, $option, $tagParser)
	{
		$text = '<input type="radio" name="' .  $this->getFullName () .'"';
		$text .= ' id="' .  $this->getFullName () .'"';
		$text .= ' value="' . $this->escapeAttr ($val) . '"';
		if ($val == $this->m_value)
		{
			$text .= " checked ";
		}
		if ($this->checkReadOnly())
		{
			$text .= " readonly ";
		}
		$text .= " " . $this->getHtml ($tagParser) . " />";
		return $text;
	}

	function toString ($tagParser)
	{
		if (!isset ($this->m_listsource))
		{
			die ("Radio must have a list source");
		}
		$lv = $this->m_listsource->getList ();
		$text = "";
		foreach($lv as $val=>$option)
		{
			if (!isset ($this->m_usekey) || !$this->m_usekey)
			{
				$val = $option;
			}
			if ($this->m_textleft)
			{
				$text .= $this->makePreHtml ($val, $option, $tagParser);
				$text .= $this->makeOption ($val, $option, $tagParser);
				$text .= $this->makeMidHtml ($val, $option, $tagParser);
				$text .= $this->makeRadio ($val, $option, $tagParser);
				$text .= $this->makePostHtml ($val, $option, $tagParser);
			}
			else
			{
				$text .= $this->makePreHtml ($val, $option, $tagParser);
				$text .= $this->makeRadio ($val, $option, $tagParser);
				$text .= $this->makeMidHtml ($val, $option, $tagParser);
				$text .= $this->makeOption ($val, $option, $tagParser);
				$text .= $this->makePostHtml ($val, $option, $tagParser);
			}
		} 
		return $text;
	}
} 

class DD_DomText extends DD_DomField
{
	var			$m_maxlength;
	var			$m_size;

	function DD_DomText ($array, $extension)
	{
		parent::DD_DomField ($array, $extension);
		$this->setState ($array);
		if (isset ($extension))
			$this->setState ($extension);
	}
	function setState (&$array)
	{
		if (isset ($array ['maxlength']))
			$this->m_maxlength = $array ['maxlength'];
		if (isset ($array ['size']))
			$this->m_size = $array ['size'];
	}
	function toString ($tagParser)
	{
		$text =  '<input type="text" name="' . $this->getFullName () . '" id="' . 
			$this->getFullName () . '"';
		$text .= ' value="' . $this->escapeAttr ($this->getFormattedValue($tagParser)) . '"'; 
		if (isset ($this->m_size))
		{
			$text .=  ' size="' . $this->m_size .  '"';
		}
		if (isset ($this->m_maxlength))
		{
			$text .=  ' maxlength="' . $this->m_maxlength .  '" ';
		}
		$text .= $this->getHtml ($tagParser) . ' />';
		return $text;
	}
}

class DD_DomPassword extends DD_DomField
{
	var			$m_maxlength;
	var			$m_size;

	function DD_DomPassword ($array, $extension)
	{
		parent::DD_DomField ($array, $extension);
		$this->setState ($array);
		if (isset ($extension))
			$this->setState ($extension);
	}
	function setState (&$array)
	{
		if (isset ($array ['maxlength']))
			$this->m_maxlength = $array ['maxlength'];
		if (isset ($array ['size']))
			$this->m_size = $array ['size'];
	}
	function toString ($tagParser)
	{
		$text =  '<input type="password" name="' . $this->getFullName () . '" id="' . 
			$this->getFullName () . '"';
		$text .= ' value="' . $this->escapeAttr ($this->getFormattedValue($tagParser)) . '"'; 
		if (isset ($this->m_size))
		{
			$text .=  ' size="' . $this->m_size .  '"';
		}
		if (isset ($this->m_maxlength))
		{
			$text .=  ' maxlength="' . $this->m_maxlength .  '" ';
		}
		$text .= $this->getHtml ($tagParser) . ' />';
		return $text;
	}
}

class DD_DomButton extends DD_DomField
{
	function DD_DomButton ($array, $extension)
	{
		parent::DD_DomField ($array, $extension);
	}
	function setState (&$array)
	{
	}
	function toString ($tagParser)
	{
		$text =  '<input type="button" name="' . $this->getFullName () . '" id="' . 
			$this->getFullName () . '"';
		$text .= ' value="' . $this->escapeAttr ($this->getFormattedValue($tagParser)) . '" '; 
		$text .= $this->getHtml ($tagParser) . ' />';
		return $text;
	}
}
class DD_DomImage extends DD_DomField
{
	var			$m_alt;

	function DD_DomImage ($array, $extension)
	{
		parent::DD_DomField ($array, $extension);
		$this->setState ($array);
		if (isset ($extension))
			$this->setState ($extension);
	}
	function setState (&$array)
	{
		if (isset ($array ['alt']))
			$this->m_alt = $array ['alt'];
	}
	function toString ($tagParser)
	{
		$text =  '<input type="image" name="' . $this->getFullName () . '" id="' . 
			$this->getFullName () . '" src="' . 
			$this->m_value
			. '" ';
		if (isset ($this->m_alt))
		{
			$text .= ' alt="' . $this->escapeAttr ($tagParser->parseContent ($this->m_alt)) . '" ';
		}
		$text .= $this->getHtml ($tagParser) . ' />';
		return $text;
	}
}
class DD_DomCheckBox extends DD_DomField
{
	var			$m_checked;
	var			$m_unchecked;

	function DD_DomCheckBox ($array, $extension)
	{
		parent::DD_DomField ($array, $extension);
		$this->setState ($array);
		if (isset ($extension))
			$this->setState ($extension);
	}
	function setState (&$array)
	{
		if (isset ($array ['checked']))
			$this->m_checked = $array ['checked'];
		if (isset ($array ['unchecked']))
			$this->m_unchecked = $array ['unchecked'];
	}
	function set ($value)
	{
		if (!isset ($value))
			$this->m_value = $this->m_unchecked;
		else
			$this->m_value = $value;
	}

	function wantsNulls ()
	{
		return true;
	}
	function toString ($tagParser)
	{
		$text =  '<input type="checkbox" name="' . $this->getFullName () . '" id="' . 
			$this->getFullName () . '"';
		$text .= ' value="' . $this->escapeAttr ($this->m_checked) . '" '; 
		if ($this->getFormattedValue($tagParser) == $this->m_checked)
		{
			$text .= ' checked ';
		}
		if ($this->checkReadOnly())
		{
			$text .= ' disabled ';
		}
		$text .= $this->getHtml ($tagParser) . ' />';
		return $text;
	}
}

class DD_DomFile extends DD_DomField
{
	var			$m_accept;
	var			$m_destination; // Directory to move file to using original name
	var			$m_target;		// Full path to move file to.
	var			$m_maxlength;
	var			$m_size;
	var			$m_finaltarget; // Final resting place for file as determined by m_destination of m_target.
	var			$m_file;		// Assigned from the $_FILES variable



	function DD_DomFile ($array, $extension)
	{
		parent::DD_DomField ($array, $extension);
		$this->setState ($array);
		if (isset ($extension))
			$this->setState ($extension);
	}
	function setState (&$array)
	{
		if (isset ($array ['destination']))
			$this->m_destination = $array ['destination'];
		if (isset ($array ['accept']))
			$this->m_accept = $array ['accept'];
		if (isset ($array ['target']))
			$this->m_target = $array ['target'];
		if (isset ($array ['maxlength']))
			$this->m_maxlength = $array ['maxlength'];
		if (isset ($array ['size']))
			$this->m_size = $array ['size'];
	}
	function toString ($tagParser)
	{
		$text =  '<input type="file" name="' . $this->getFullName () . '" id="' . 
			$this->getFullName () . '"';
		if (isset ($this->m_accept))
		{
			$text .= ' accept="' . $this->escapeAttr ($this->m_accept) . '"';
		}
		if (isset ($this->m_size))
		{
			$text .=  ' size="' . $this->m_size .  '"';
		}
		$text .= ' ' . $this->getHtml ($tagParser) . ' />';
		return $text;
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
		if (isset ($_FILES))
		{
			$file = $_FILES[$this->m_name];
			if (isset ($file))
			{
				switch ($attribute)
				{
				case 'file_ext':
					$extpos = @strpos($file['name'],'.');
					if ($extpos === false)
					{
						return null;
					}
					return @substr ($file['name'], $extpos + 1);
				case 'file_name':
					return $file['name'];
				case 'file_path':
					return $file['tmp_name'];
				case 'file_size':
					return @filesize ($file['tmp_name']);
				case 'file_mimetype':
					$finfo = @finfo_open (FILEINFO_MIME_TYPE);
					$mime = finfo_file ($finfo, $file['name']);
					@finfo_close ($finfo);
					return $mime;
				case 'file_target':
					return $m_finaltarget;
				}
			}
		}
		switch ($attribute)
		{
		case 'destination':
			return $this->m_destination;
		case 'target':
			return $this->m_target;
		}
		return null;
	}
	/**
	 * Prepares the downloaded file by determining the final target.  Make any directories that are needed.
	 */
	private function prepareFile ($tagparser = null)
	{
		if (!isset ($_FILES))
			return false;

		$this->m_file = $_FILES[$this->m_name];
		if (!isset ($this->m_file))
			return false;

		if (isset($tagparser))
		{
			if (isset ($this->m_destination))
			{
				$uploadfile = $tagparser->parseContent ($this->m_destination);
				if (!isset ($uploadfile))
					return false;
				$uploadfile .= '/' . $this->m_file['name'];
			}
			else if (isset ($this->m_target))
			{
				$uploadfile = $tagparser->parseContent ($this->m_target);
				if (!isset ($uploadfile))
					return false;
			}
			else
			{
				return false;
			}
		}
		else
		{
			if (isset ($this->m_destination))
			{
				$uploadfile = $this->m_destination . '/' . $this->m_file['name'];
			}
			else if (isset ($this->m_target))
				$uploadfile = $this->m_target;
		}
		$filedir = @dirname ($uploadfile);
		if (!@file_exists ($filedir))
		{
			@mkdir ($filedir, 0777, true);
		}
		$this->m_finalTarget = $uploadfile;
		return $uploadfile;
	}
	/**
	 * Moves the downloaded file to the destination or target property.
	 */
	public function moveFile ($tagparser = null)
	{
		$uploadfile = $this->prepareFile ($tagparser);
	   	if (!move_uploaded_file($this->m_file['tmp_name'], $uploadfile))
	   	{
		   	throw new exception ('DD_DomFile::moveFile: ' . $this->m_file['name'] . " failed to move to " . $uploadfile);
	   	}
		return true;
	}
	/**
	 * Use this to move an image and run convert on reduce its size to fit.  Maintains the aspect ratio.
	 */
	public function moveConstrainedImage ($width, $height, $tagparser = null)
	{
		$uploadfile = $this->prepareFile ($tagparser);
		$convert = "/usr/bin/convert " . $this->m_file['tmp_name'] . " -resize ${width}x${height} -gravity center $uploadfile";
		//error_log ($convert);
		exec ($convert);
		return true;
	}
	public function generateThumbnailImages ($sizes /* array */, $saveOrig = false, $tagparser = null)
	{
		$uploadfile = $this->prepareFile ($tagparser);
		foreach ($sizes as $width=>$height)
		{
			$extpos = @strpos($uploadfile,'.');
			if ($extpos === false)
			{
				return null;
			}
			$destfile = @substr ($uploadfile, 0, $extpos) . '_' . $width . 'x' . $height .  @substr ($uploadfile, $extpos);
			$convert = "/usr/bin/convert " . $this->m_file['tmp_name'] . " -resize ${width}x${height}^ -gravity center -extent ${width}x${height} $destfile";
			$convert = "/usr/bin/convert " . $this->m_file['tmp_name'] . " -resize ${width}x${height} -gravity center -extent ${width}x${height} $destfile";
			exec ($convert);
			//error_log ($convert);
		}
		if ($saveOrig == true)
		{
	   		if (!move_uploaded_file($this->m_file['tmp_name'], $uploadfile))
	   		{
		   		throw new exception ('DD_DomFile::generateImages: ' . $this->m_file['name'] . " failed to move to " . $uploadfile);
	   		}
		}
	}
}

class DD_DomHidden extends DD_DomField
{
	function DD_DomHidden ($array, $extension)
	{
		parent::DD_DomField ($array, $extension);
	}
	function toString ($tagParser)
	{
		$text =  '<input type="hidden" name="' . $this->getFullName () . '" id="' . 
			$this->getFullName () . '"';
		$text .= ' value="' . $this->escapeAttr ($this->getFormattedValue($tagParser)) . '"';
		$text .= ' ' . $this->getHtml ($tagParser) . ' />';
		return $text;
	}
}
class DD_DomWork extends DD_DomField
{
	function DD_DomWork ($array, $extension)
	{
		parent::DD_DomField ($array, $extension);
	}
	function toString ($tagParser)
	{
		return $this->getFormattedValue($tagParser);
	}
	function prepareForRecord ($tagParser)
	{
	}
	function setDefault ($tagParser)
	{
	}
	function isWorkField ()
	{
		return true;
	}
}

class DD_DomDisplay extends DD_DomField
{
	function DD_DomDisplay ($array, $extension)
	{
		parent::DD_DomField ($array, $extension);
	}
	function toString ($tagParser)
	{
		return $this->getFormattedValue($tagParser);
	}
}
class DD_DomTextArea extends DD_DomField
{
	var			$m_rows;
	var			$m_cols;

	function DD_DomTextArea ($array, $extension)
	{
		parent::DD_DomField ($array, $extension);
		$this->setState ($array);
		if (isset ($extension))
			$this->setState ($extension);
	}
	function setState (&$array)
	{
		if (isset ($array ['rows']))
			$this->m_rows = $array ['rows'];
		if (isset ($array ['cols']))
			$this->m_cols = $array ['cols'];
	}
	function toString ($tagParser)
	{
		$text =  '<textarea name="' . $this->getFullName() . 
			'" id="' . $this->getFullName () . '"';
		$text .= ' value="' . $this->escapeAttr ($this->getFormattedValue($tagParser)) . '"'; 
		if (isset ($this->m_rows))
		{
			$text .=  ' rows="' . $this->m_rows .  '"';
		}
		if (isset ($this->m_cols))
		{
			$text .=  ' cols="' . $this->m_cols .  '" ';
		}
		if ($this->checkReadOnly())
		{
			$text .= ' readonly';
		}
		$text .= " " . $this->getHtml ($tagParser) . ">";
		$text .= $this->valueAsHtml ();
		$text .= "</textarea>";
		return $text;
	}
}


class DD_DomSubmit extends DD_DomField
{
	var			$m_checked;

	function DD_DomSubmit ($array, $extension)
	{
		parent::DD_DomField ($array, $extension);
		$this->setState ($array);
		if (isset ($extension))
			$this->setState ($extension);
	}
	function setState (&$array)
	{
		if (isset ($array ['checked']))
			$this->m_checked = $array ['checked'];
	}
	function toString ($tagParser)
	{
		$text =  '<input type="submit" name="' . $this->getFullName () . '" id="' . 
			$this->getFullName () . '"';
		$text .= ' value="' . $this->escapeAttr ($this->getFormattedValue($tagParser)) . '" '; 
		$text .= $this->getHtml ($tagParser) . ' />';

		/**
		 * If a checked is specified then create a hidden field that sets this
		 * buttons name as the default.  This only workes for buttons names
		 * DDinsert, DDupdate, and DDdelete.
		 */
		if (isset ($this->m_checked))
		{
			$text .= '<input type="hidden" name="DD_default" value="' . $this->getFullName ()
				 . '" />';
		}
		return $text;
	}
}

class DD_DomMultiItem extends DD_DomField
{
	var				$m_listsource;
	var				$m_separator = "<br>";	// Between options

	/**
		Creates an HTMLMultiItem
	 */
	function DD_DomMultiItem ($array, $extension)
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
		if (isset ($array ['separator']))
			$this->m_separator = $array ['separator'];
	}

	/**
	 * This iterates the values to see if the option is contained within.
	 */
	function isInValue ($option, $values)
	{
		if(strstr (",".$values.",", ",".$option.","))
			return true;
		return false;
	}

	function prepareForRecord ($tagParser)
	{
		parent::prepareForRecord ($tagParser);
		$this->m_listsource->makeList ($tagParser);
	}

	function toString ($tagParser)
	{
		$text =	"";
		$val = $this->getFormattedValue ($tagParser);
		if (!isset ($val))
		{
			$val = "";
		}

		$text .= '<input type="hidden" name="';
 		$text .= $this->getFullName () . '" value="';
		$text .= $this->escapeAttr ($val) .  '" />';

		$lv = $this->m_listsource->getList ();
		$lastgroup = '';

		$i = 1;
   		foreach($lv as $option=>$display)
		{
			/*
			 * If the value is an array then we need to put up headings and use breaks.
			 */
			if (isset ($display) && is_array ($display))
			{

				$displayText = $display [0];
				$group = $display [1];
			}
			else
			{
				$displayText = $display;
				$group = '';
			}
			if ($i > 1)
			{
				$text .= $this->m_separator;
			}
			if (strcmp ($group, $lastgroup))
			{
				$text .= '<span class="dd_multigroup">' . $this->escapeHtml ($group) . "</span>" . $this->m_separator; 
				$lastgroup = $group;
			}
			$i++;
			$text .= '<span class="dd_multiitem"><input type="checkbox" name="cb_'.$i.'_' .
				$this->getFullName () . 
				'" onclick="DD_CheckToText (this, this.form.' .
				$this->getFullName () .
				');" value="' . 
				$this->escapeAttr ($option) . '" ';

			if ($this->isInValue ($option,$this->getFormattedValue($tagParser)))
			{
				$text .= " checked ";
			}
			if ($this->checkReadOnly())
			{
				$text .= " readonly ";
			}
			$text .= $this->getHtml ($tagParser) . " /></span>";

			if (isset ($displayText))
			{
				$text .= $this->escapeHtml ($displayText);
			}
			else
			{
				$text .= $this->escapeHtml ($option);
			}
		}
		return $text;
	}
}

/**
 * The Tab Field sets up a tab bar using <LI> elements.  The active one is classed as "active" and
 * the others are classed as "inactive".  The remainder is classed as "filler".  Each element is setup
 * as a link that activate the href or onclick property provided.  The href is appended by the key field and
 * the text field.  The onclick property provides javascript that can contain substitution 
 * values '{value}', '{option}', and '{name}' (without the single quotes of course).
 */
class DD_DomTab extends DD_DomField
{
	var			$m_width = "100px";/* between option. most likely <br> if changed */
	var			$m_listsource;
	var			$m_usekey = true;
	var			$m_donotparse;
	var			$m_href;
	var			$m_onclick;

	function __construct ($array, $extension)
	{
		parent::DD_DomField ($array, $extension);
		$this->setState ($array);
		if (isset ($extension))
			$this->setState ($extension);
		if (!isset ($this->m_listsource))
			die ("Need a list source for tab field " . $this->m_name);
		if (!isset ($this->m_onclick) && !isset($this->m_href))
			die ("Need an href or onclick property tab field " . $this->m_name);
	}
	function setState (&$array)
	{
		if (isset ($array ['width'])) 
			$this->m_width = $array ['width'];
		if (isset ($array ['listsource']))
			$this->m_listsource = $array ['listsource'];
		if (isset ($array ['usekey']))
			$this->m_usekey = $array ['usekey'];
		if (isset ($array ['donotparse']))
			$this->m_donotparse = $array ['donotparse'];
		if (isset ($array ['href']))
			$this->m_href = $array ['href'];
		if (isset ($array ['onclick']))
			$this->m_onclick = $array ['onclick'];
	}
	function prepareForRecord ($tagParser)
	{
		parent::prepareForRecord ($tagParser);
		$this->m_listsource->makeList ($tagParser);
	}
	function makeTab ($val, $option, $tagParser)
	{
		$text = '<li class="';
		if ($val == $this->m_value)
			$text .= 'active">';
		else 
			$text .= 'inactive">';

		$text .= '<a id="' . $this->escapeAttr ($val) . '" class="';

		if ($val == $this->m_value) 
			$text .= 'active" ';
		else 
			$text .= 'inactive" ';

		if (isset ($this->m_href))
		{
			$text .= 'href="' . $tagParser->parseContent ($this->m_href);
			if (@strpos($this->m_href, "?") === FALSE)
				$text .= '?';
			else
				$text .= '&';

			$text .= 'tabname=' . @urlencode ($this->getFullName ());
			$text .= '&tabval=' . @urlencode ($val);
			$text .= '&taboption=' . @urlencode ($option);
			$text .= '" ';
		}
		else
		{
			$text .= ' href="#" ';
		}
		if (isset ($this->m_onclick))
		{
			$text .= 'onclick="';
			$text .= strtr ($tagParser->parseContent ($this->m_onclick), 
				array(
					"{value}" => $this->makeJavascriptString ($val)
					,"{option}" => $this->makeJavascriptString ($option)
					,"{name}" => $this->makeJavascriptString ($this->getFullName ())
				));
			$text .= '" ';
		}
		$text .= '>' . $this->escapeHtml ($option) . '</a>';
		$text .= "</li>";
		return $text;
	}

	function toString ($tagParser)
	{
		if (!isset ($this->m_listsource))
		{
			die ("Tab must have a list source");
		}
		$lv = $this->m_listsource->getList ();
		$text = "";
		foreach($lv as $val=>$option)
		{
			if (!isset ($this->m_usekey) || !$this->m_usekey)
			{
				$val = $option;
			}
			$text .= $this->makeTab ($val, $option, $tagParser);
		} 
		return $text;
	}
} 

/**
 * The Tab Field sets up a tab bar using <LI> elements.  The active one is classed as "active" and
 * the others are classed as "inactive".  The remainder is classed as "filler".  Each element is setup
 * as a link that activate the href or onclick property provided.  The href is appended by the key field and
 * the text field.  The onclick property provides javascript that can contain substitution 
 * values '{value}', '{option}', and '{name}' (without the single quotes of course).
 */
class DD_DomLinks extends DD_DomField
{
	var			$m_width = "100px";/* between option. most likely <br> if changed */
	var			$m_listsource;
	var			$m_usekey = true;
	var			$m_donotparse;
	var			$m_href;
	var			$m_onclick;

	function __construct ($array, $extension)
	{
		parent::DD_DomField ($array, $extension);
		$this->setState ($array);
		if (isset ($extension))
			$this->setState ($extension);
		if (!isset ($this->m_listsource))
			die ("Need a list source for tab field " . $this->m_name);
		if (!isset ($this->m_onclick) && !isset($this->m_href))
			die ("Need an href or onclick property tab field " . $this->m_name);
	}
	function setState (&$array)
	{
		if (isset ($array ['width'])) 
			$this->m_width = $array ['width'];
		if (isset ($array ['listsource']))
			$this->m_listsource = $array ['listsource'];
		if (isset ($array ['usekey']))
			$this->m_usekey = $array ['usekey'];
		if (isset ($array ['donotparse']))
			$this->m_donotparse = $array ['donotparse'];
		if (isset ($array ['href']))
			$this->m_href = $array ['href'];
		if (isset ($array ['onclick']))
			$this->m_onclick = $array ['onclick'];
	}
	function prepareForRecord ($tagParser)
	{
		parent::prepareForRecord ($tagParser);
		$this->m_listsource->makeList ($tagParser);
	}
	function makeLink ($val, $option, $tagParser)
	{
		$text = '<a id="' . $this->escapeAttr ($val) . '" class="';

		if ($val == $this->m_value) 
			$text .= 'active" ';
		else 
			$text .= 'inactive" ';

		if (isset ($this->m_href))
		{
			$text .= 'href="' . $this->m_href;
			if (@strpos($this->m_href, "?") === FALSE)
				$text .= '?';
			else
				$text .= '&';

			$text .= 'name=' . @urlencode ($this->getFullName ());
			$text .= '&val=' . @urlencode ($val);
			$text .= '&option=' . @urlencode ($option);
			$text .= '" ';
		}
		else
		{
			$text .= ' href="#" ';
		}
		if (isset ($this->m_onclick))
		{
			$text .= 'onclick="';
			$text .= strtr ($tagParser->parseContent ($this->m_onclick), 
				array(
					"{value}" => $this->makeJavascriptString ($val)
					,"{option}" => $this->makeJavascriptString ($option)
					,"{name}" => $this->makeJavascriptString ($this->getFullName ())
				));
			$text .= '" ';
		}
		$text .= '>' . $this->escapeHtml ($option) . '</a>';
		return $text;
	}

	function toString ($tagParser)
	{
		if (!isset ($this->m_listsource))
		{
			die ("Liks must have a list source");
		}
		$lv = $this->m_listsource->getList ();
		$text = "";
		foreach($lv as $val=>$option)
		{
			if (!isset ($this->m_usekey) || !$this->m_usekey)
			{
				$val = $option;
			}
			$text .= $this->makeLink ($val, $option, $tagParser);
		} 
		return $text;
	}
} 

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


class DD_DomList extends DD_DomField
{
	var			$m_separator = '&nbsp;';/* between option. most likely <br> if changed */
	var			$m_prehtml = '&nbsp;'; 	/* Before we start outputing the radio. */
	var			$m_posthtml = "";		/* After we start outputing the radiooption text */
	var			$m_midhtml = "";		/* Mid html between the option and text visa versa*/
	var			$m_listsource;
	var			$m_usetablerows = false;
	var			$m_donotparse = false;

	function __construct ($array, $extension)
	{
		parent::DD_DomField ($array, $extension);
		$this->setState ($array);
		if (isset ($extension))
			$this->setState ($extension);
		if (!isset ($this->m_listsource))
			die ("Need a list source for field " . $this->m_name);
	}
	function setState (&$array)
	{
		if (isset ($array ['separator'])) 
			$this->m_separator = $array ['separator'];
		if (isset ($array ['prehtml']))
			$this->m_prehtml = $array ['prehtml'];
		if (isset ($array ['posthtml']))
			$this->m_posthtml = $array ['posthtml'];
		if (isset ($array ['midhtml']))
			$this->m_midhtml = $array ['midhtml'];
		if (isset ($array ['listsource']))
			$this->m_listsource = $array ['listsource'];
		if (isset ($array ['textleft']))
			$this->m_textleft = $array ['textleft'];
		if (isset ($array ['usetablerows']))
			$this->m_usetablerows = $array ['usetablerows'];
		if (isset ($array ['donotparse']))
			$this->m_donotparse = $array ['donotparse'];
	}
	function makePreHtml (&$val, &$option, $tagParser)
	{
		if ($this->m_usetablerows == true)
			return '<tr><td>';
		return $this->m_prehtml;
	}
	function makeMidHtml (&$val, &$option, $tagParser)
	{
		if ($this->m_usetablerows == true)
			return '</td><td>';
		return $this->m_midhtml;
	}
	function makePostHtml (&$val, &$option, $tagParser)
	{
		if ($this->m_usetablerows == true)
			return '</td></tr>';
		return $this->m_posthtml . $this->m_separator;
	}
	function prepareForRecord ($tagParser)
	{
		parent::prepareForRecord ($tagParser);
	}
	function makeText ($value, $tagParser)
	{
		if ($this->m_donotparse == true)
			return $option;
		return htmlspecialchars ($value);
	}

	function toString ($tagParser)
	{
		if (!isset ($this->m_listsource))
		{
			die ("Radio must have a list source");
		}
		$this->m_listsource->makeList ($tagParser);
		$lv = $this->m_listsource->getList ();
		$text = "";
		foreach($lv as $left=>$right)
		{
			$text .= $this->makePreHtml ($val, $option, $tagParser);
			$text .= $this->makeText ($left, $tagParser);
			$text .= $this->makeMidHtml ($val, $option, $tagParser);
			$text .= $this->makeText ($right, $tagParser);
			$text .= $this->makePostHtml ($val, $option, $tagParser);
		} 
		return $text;
	}
} 
?>
