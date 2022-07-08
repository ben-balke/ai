<?php
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/lib/2d/DD_tagparser.php,v 1.1 2016/02/16 21:18:00 secwind Exp $
*************************************************************************/


function _L($text)
{
    return $text;
}
/**
 * DUCKDIGIT TAG Parser 
 */

/**
 * These are modes for the TagParser.  Used for getMode and setMode().
 */
define("MODE_NORMAL", 0);
define("MODE_VALUE", 1);
define("MODE_CONTENT", 2);
define("MODE_SQL_VALUE", 3);





/**
 * Tag Modifiers
 */
define("MOD_HTML", 			0x0001);	// ^
define("MOD_USEVALUE", 		0x0002);	// ~
define("MOD_JAVASCRIPT", 	0x0004);	// #
define("MOD_HTMLATTR", 		0x0008);	// &
define("MOD_UUENCODE", 		0x0010);	// %
define("MOD_HTMLNOTE", 		0x0020);	// ?
define("MOD_IGNORE", 		0x0040);	// @
define("MOD_DELLINE", 		0x0080);	// -
define("MOD_DELSECTION", 	0x0100);	// _
define("MOD_BLANKISNULL", 	0x0200);	// +
define("MOD_REVERSENULL", 	0x0400);	// !
define("MOD_URICOMPONENT", 	0x0800);	// `
define("MOD_JSON", 			0x1000);	// /

define("MOD_CHARS","^~#&%?@-_+!`/"); // Positions must match bit positions above.

$G_HTML_NOTE_TR = 
				array( "&" => "&amp;", "<" => "&lt;", ">" => "&gt;", "\n" => "<br>", "\r" => "");
$G_JAVASCRIPT_TR= 
				array("\r\n" => '\n', "\r" => '\n', "\n" => '\n', '"' => '\\042', "'" => "\\047");

/**
 * returned from parseLine when delete section is set.
 */
define("DELETE_SECTION_TOKEN", "<<::DELETE_SECTION::>>");


/**
 * Thrown when a delete line (MOD_DELLINE) modifier is found and a null value is
 * returned from the tag.
 */
class DD_DeleteLineException extends Exception
{
	public function errorMessage()
	{
		return "Delete Line Exception";
	}
}

/**
 * Thrown when a delete section (MOD_DELSECTION) modifier is found and a null value is
 * returned from the tag.
 */
class DD_DeleteSectionException extends Exception
{
	public function errorMessage()
	{
		return "Delete Section Exception";
	}
}


/**
 * This is the base abstract class for the 
 * value provider.  Do not instansiate directly.
 */
class DD_ValueProvider
{
	var			$m_name;

	function __construct ($name)
	{
		$this->m_name = $name;
	}
	function getValue ($fld_name, $mode, $tagParser = null)
	{
		return null;
	}
	function setValue ($name, $value)
	{
	}
	function appendArrayAsFields ($array)
	{
		foreach ($array as $id=>$name)
		{
	    	$this->setValue ($name, "");
		}
	}
	function appendDelimitedStringAsFields ($array, $delimiter = ',')
	{
		$fieldnames = @explode ($delimiter, $array);
		$this->appendArrayAsFields ($fieldnames);
	}

}

/**
 * This value provider provides access to named arrays.
 * If you don't have the array just use array() as
 * the second contstructor value.
 */
class DD_ArrayValueProvider extends DD_ValueProvider 
{
	var				$m_array;
	function __construct ($name, $array)
	{
		parent::__construct ($name);
		$this->m_array = $array;

	}
	function appendArray ($array)
	{
		$this->m_array = array_merge ($this->m_array, $array);
	}

	function getValue ($name, $mode = MODE_NORMAL, $tagParser = null)
	{
		if (@array_key_exists ($name, $this->m_array))
			return $this->m_array [$name];
		return null;
	}
	function setValue ($name, $value)
	{
		$this->m_array [$name] = $value;
	}

	function urldecodeValues ()
	{
		foreach ($this->m_array as $id=>$value)
		{
			if (isset ($value))
			{
				$this->m_array [$id] = @urldecode ($value);
			}
		}
	}
}


/**
 * This is the tag parser.  It is a collection of value providers and can
 * parse tags in SQL and content.
 */
class DD_TagParser 
{
	var			$m_vps;
	var			$m_modeStack;
	function __construct ()
	{
		$this->m_vps = array ();
		$this->m_modeStack = array ();
		$this->setMode (MODE_NORMAL);
	}
	function addValueProvider (&$vp)
	{
		$this->m_vps[$vp->m_name] = $vp;
	}
	function getValueProvider ($name)
	{
		return $this->m_vps [$name];
	}
	function removeValueProviderByName ($name)
	{
		$this->m_vps[$name] = null;
	}
	function parseSql ($text)
	{
		$this->setMode (MODE_SQL_VALUE);
		$start = 0;
		$r = "";
		while (($pos = @strpos ($text, '{', $start)) !== false)
		{
			$r .= @substr ($text, $start, $pos - $start);
			$end = @strpos ($text, '}', $pos);
			if ($end !== false)
			{
				$sqlsub = false;
				$start = $end + 1;
				$tag = @substr ($text, $pos + 1, $end - $pos - 1);
				if (!@strncmp($tag, "SQL:", 4))
				{
					$tag = @substr ($tag, 4);
					$sqlsub = true;
				}
				$val = $this->getTheValue ($tag);
				if (isset ($val))
				{
					if ($sqlsub)
						$r .= $val;
					else
					{
						// If magic quotes is on then remove this.
						$r .= "'" . @addslashes ($val) . "'";
					}
				}
				else 
				{
					if (!$sqlsub)
						$r .= "null";
				}
			}
			else
			{
				break;
			}
		}
		$r .= @substr ($text, $start);
		$this->restoreMode ();
		return $r;
	}

	function getTheValue ($tag)
	{
		while (1)
		{
			$slash = @strpos ($tag, "|");
			if ($slash === false)
			{
				return $this->getTagValue ($tag);
			}
			$curtag = @substr ($tag, 0, $slash);
			$value = $this->getTagValue ($curtag);
			if (isset ($value))
			{
				return $value;
			}
			$tag = @substr ($tag, $slash + 1);
		}
	}

	/**
	 * This retrieves a tag in the form <vpname>.<fieldname> no {} is expected.
	 */
	function getTagValue ($tag)
	{
		if (@substr ($tag, 0, 1) == "=")
		{
			return @substr ($tag, 1);
		}
		$dot = @strpos ($tag, ".");
		if ($dot !== false)
		{
			$vp_name = @substr ($tag, 0, $dot);
			$fld_name = @substr ($tag, $dot + 1);
			if (@array_key_exists ($vp_name, $this->m_vps))
			{
				$vp = $this->m_vps [$vp_name];
				if (isset ($vp))
				{
					return $vp->getValue ($fld_name, $this->getMode (), $this);
				}
			}
		}
		return null;
	}

	function setValue ($tag, $value)
	{
		$dot = @strpos ($tag, ".");
		if ($dot !== false)
		{
			$vp_name = @substr ($tag, 0, $dot);
			$fld_name = @substr ($tag, $dot + 1);
			$vp = $this->m_vps [$vp_name];
			if (isset ($vp))
			{
				$vp->setValue ($fld_name, $value);
			}
		}
	}

	function getValue ($tag)
	{
		$this->setMode (MODE_VALUE);
		$value = $this->getTheValue ($tag);
		$this->restoreMode ();
		return $value;
	}

	function getMode ()
	{
		return current ($this->m_modeStack);
	}
	function setMode ($mode)
	{
		$this->m_modeStack [] = $mode;
		end ($this->m_modeStack);
	}
	function restoreMode ()
	{
		array_pop($this->m_modeStack);
		end($this->m_modeStack);
	}


	function parseContent ($text)
	{
		$r = "";
		$start = 0;
		while (($pos = @strpos ($text, "\n", $start)) !== false)
		{
			$line = @substr ($text, $start, $pos + 1 - $start);
			try
			{
				$r .= $this->parseLine ($line);
			}
			catch (DD_DeleteLineException $edlx)
			{
			}
			catch (DD_DeleteSectionException $edlx)
			{
				return "";
			}
			$start = $pos + 1;
		}
		$line = @substr ($text, $start);
		try
		{
			$r .= $this->parseLine ($line);
		}
		catch (DD_DeleteLineException $edlx)
		{
		}
		catch (DD_DeleteSectionException $edlx)
		{
			return "";
		}
		return $r;
	}

	function parseStuff ($text)
	{
		return $this->parseLine ($text);
	}
	
	/**
	 * Parsed Content using tag modifiers.
	 */
	function parseLine ($text)
	{
		$tagopenlen = 4;
		$start = 0;
		$r = "";
		while (($pos = @strpos ($text, '{dd:', $start)) !== false)
		{
			$r .= @substr ($text, $start, $pos - $start);
			$end = @strpos ($text, '}', $pos);
			if ($end !== false)
			{
				$sqlsub = false;
				$start = $end + 1;
				$tag = @substr ($text, $pos + $tagopenlen, $end - $pos - 4);
				$done = null;
				$modifiers = 0x0000;

				for ($i = 0; $i < @strlen ($tag); $i++)
				{
					$ch = @substr ($tag, $i, 1);
					if ($ch == "=")	
					{
						$r .= @substr ($text, $pos, $i + $tagopenlen);
						$r .= @substr ($text, $pos + $i + $tagopenlen + 1, 
							$end - ($pos + $i + $tagopenlen));
						break;
					}
					else if (($mod_pos = @strpos (MOD_CHARS, $ch, 0)) !== false)
					{
						$modifiers |= 0x0001 << $mod_pos;
					}
					else
					{
						$done = $i;
						$tag = @substr ($tag, $i);
						if ($modifiers == 0)
						{
							$r .= $this->getTheValue ($tag);
							break;
						}
						else
						{
							if ($modifiers & MOD_USEVALUE)
							{
								$this->setMode (MODE_VALUE);
							}
							$val = $this->getTheValue ($tag);
							if ($modifiers & MOD_USEVALUE)
							{
								$this->restoreMode ();
							}
							if ($val == "" && ($modifiers & MOD_BLANKISNULL) == MOD_BLANKISNULL)
							{
								$val = null;
							}
								/*
								 * If reverse null is set then substitue a null for any
								 * non-null and space for a null value.
								 */
							if (($modifiers & MOD_REVERSENULL) == MOD_REVERSENULL)
							{
								if (isset ($val))
									$val = null;
								else
									$val = "";
							}
							//print "modifiers: $modifiers val: $val<br>";
							if (isset ($val))
							{
								if (($modifiers & MOD_HTML) == MOD_HTML)
									$val =  @htmlspecialchars ($val);
								if (($modifiers & MOD_UUENCODE) == MOD_UUENCODE)
									$val = @urlencode ($val);
								if (($modifiers & MOD_HTMLATTR) == MOD_HTMLATTR)
									$val = @strtr($val, array('"' => "&#034;"));
								if (($modifiers & MOD_HTMLNOTE) == MOD_HTMLNOTE)
								{
								 	global $G_HTML_NOTE_TR;
									$val = @strtr($val, $G_HTML_NOTE_TR);
								}
								if (($modifiers & MOD_URICOMPONENT) == MOD_URICOMPONENT)
									$val = rawurlencode ( $val );
								if (($modifiers & MOD_JAVASCRIPT) == MOD_JAVASCRIPT)
								{
								 	global $G_JAVASCRIPT_TR;
									$val = "'" . @strtr($val, $G_JAVASCRIPT_TR) . "'";
								}
								if (($modifiers & MOD_JSON) == MOD_JSON)
									$val = json_encode ($val);
								$r .= $val;
							}
							else 
							{
								if (($modifiers & MOD_DELLINE) == MOD_DELLINE)
								{
  									throw new DD_DeleteLineException("Delete Line");
								}
								if (($modifiers & MOD_JAVASCRIPT) == MOD_JAVASCRIPT)
								{
									$r .= "''";
								}
								else if (($modifiers & MOD_IGNORE) != MOD_IGNORE)
								{
									$r .= "null";
								}
								//BBB Do Null processing eventully.
							}
							break;
						}
					}
				}
			}
			else
			{
				break;
			}
		}
		$r .= @substr ($text, $start);
		return $r;
	}	
}

/**
Example.....
$vp1 = new DD_ArrayValueProvider ("ben", array ());
$vp1->setValue ("id", 43);
$vp1->setValue ("name", "Ben's Test Field");

$vp2 = new DD_ArrayValueProvider ("mod", array ());
$vp2->setValue ("id", 66);
$vp2->setValue ("name", "Ben's S'eco'nd Test");

$tp = new DD_TagParser();
$tp->addValueProvider ($vp1);
$tp->addValueProvider ($vp2);
$sql = <<<SQL
insert into ben values ({#%ben.id}, {SQL:mod.name})
SQL;

print $tp->parseContent($sql);
*/
?>
