<?php
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/lib/2d/DD_valueproviderglyph.php,v 1.1 2016/02/16 21:18:00 secwind Exp $
*************************************************************************/
require_once "2d/DD_tagparser.php";

class DD_GlyphValueProvider extends DD_ValueProvider
{
    function __construct ()
    {
		parent::__construct ('glyph');
    }
	private function isFontAwesome ($glyph)
	{
		return @strncmp ($glyph, "fa-", 3) == 0;
	}
    function getValue ($fld_name, $mode, $tagParser = null)
    {
		if ($mode == MODE_VALUE)
		{
			return "glyphicon-" . $fld_name;
		}
		else
		{
			$pos = @strpos ($fld_name, '.');
			if ($pos !== FALSE)
			{
				$class = @substr ($fld_name, $pos + 1);			
				$glyph = @substr ($fld_name, 0, $pos);			
				if ($this->isFontAwesome ($glyph))
					return '<i class="fa ' . $glyph . ' ' . $class . '"></i>';
				return '<span class="glyphicon glyphicon-' . $glyph . ' ' . $class . '" aria-hidden="true"></span>';
			}
			if ($this->isFontAwesome ($fld_name))
				return '<i class="fa ' . $fld_name . ' ' . $class . '"></i>';
			return '<span class="glyphicon glyphicon-' . $fld_name . '" aria-hidden="true"></span>';
		}
        return null;
    }
}
?>
