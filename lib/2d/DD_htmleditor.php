<?php
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/lib/2d/DD_htmleditor.php,v 1.1 2016/02/16 21:18:00 secwind Exp $
*************************************************************************/

/*
 * FCKeditor - The text editor for Internet - http://www.fckeditor.net
 * Copyright (C) 2003-2008 Frederico Caldeira Knabben
 *
 * == BEGIN LICENSE ==
 *
 * Licensed under the terms of any of the following licenses at your
 * choice:
 *
 *  - GNU General Public License Version 2 or later (the "GPL")
 *    http://www.gnu.org/licenses/gpl.html
 *
 *  - GNU Lesser General Public License Version 2.1 or later (the "LGPL")
 *    http://www.gnu.org/licenses/lgpl.html
 *
 *  - Mozilla Public License Version 1.1 or later (the "MPL")
 *    http://www.mozilla.org/MPL/MPL-1.1.html
 *
 * == END LICENSE ==
 *
 * This is the integration file for PHP 5.
 *
 * It defines the FCKeditor class that can be used to create editor
 * instances in PHP pages on server side.
 */

/**
 * Check if browser is compatible with FCKeditor.
 * Return true if is compatible.
 *
 * @return boolean
 */
function FCKeditor_IsCompatibleBrowser()
{
	if ( isset( $_SERVER ) ) {
		$sAgent = $_SERVER['HTTP_USER_AGENT'] ;
	}
	else {
		global $HTTP_SERVER_VARS ;
		if ( isset( $HTTP_SERVER_VARS ) ) {
			$sAgent = $HTTP_SERVER_VARS['HTTP_USER_AGENT'] ;
		}
		else {
			global $HTTP_USER_AGENT ;
			$sAgent = $HTTP_USER_AGENT ;
		}
	}
	if ( strpos($sAgent, 'MSIE') !== false && strpos($sAgent, 'mac') === false && strpos($sAgent, 'Opera') === false )
	{
		$iVersion = (float)substr($sAgent, strpos($sAgent, 'MSIE') + 5, 3) ;
		return ($iVersion >= 5.5) ;
	}
	else if ( strpos($sAgent, 'like Gecko') !== false )
	{
		return true;
	}
	else if ( strpos($sAgent, 'Gecko/') !== false )
	{
		$iVersion = (int)substr($sAgent, strpos($sAgent, 'Gecko/') + 6, 8) ;
		return ($iVersion >= 20030210) ;
	}
	else if ( strpos($sAgent, 'Opera/') !== false )
	{
		$fVersion = (float)substr($sAgent, strpos($sAgent, 'Opera/') + 6, 4) ;
		return ($fVersion >= 9.5) ;
	}
	else if ( preg_match( "|AppleWebKit/(\d+)|i", $sAgent, $matches ) )
	{
		$iVersion = $matches[1] ;
		return ( $matches[1] >= 522 ) ;
	}
	else
		return false ;
}

class DD_HtmlEditor extends DD_DomField
{
	/**
	 * Width of the FCKeditor.
	 * Examples: 100%, 600
	 *
	 * @var mixed
	 */
	public $Width ;
	/**
	 * Height of the FCKeditor.
	 * Examples: 400, 50%
	 *
	 * @var mixed
	 */
	public $Height ;
	/**
	 * Name of the toolbar to load.
	 *
	 * @var string
	 */
	public $ToolbarSet ;
	/**
	 * This is where additional configuration can be passed.
	 * Example:
	 * $oFCKeditor->Config['EnterMode'] = 'br';
	 *
	 * @var array
	 */
	public $Config ;

	/**
	 * Main Constructor.
	 * Refer to the _samples/php directory for examples.
	 *
	 * @param string $name
	 */
	public function __construct( $name, $width = '100%', $height = '400', $toolbar = 'Default' )
 	{
		$param = array ('name'=>$name, 'type'=>'editor');
		parent::DD_DomField ($param);
		$this->Width		= $width;
		$this->Height		= $height;
		$this->ToolbarSet	= $toolbar;

		$this->Config		= array() ;
	}

	/**
	 * Display FCKeditor.
	 *
	 */

	/**
	 * Return the HTML code required to run FCKeditor.
	 *
	 * @return string
	 */
	public function toString ($tagParser)
	{
		global $DD_fckeditor;
		$Html = '' ;
		if ( $this->IsCompatible() )
		{
			if ( isset( $_GET['fcksource'] ) && $_GET['fcksource'] == "true" )
				$File = 'fckeditor.original.html' ;
			else
				$File = 'fckeditor.html' ;

			$Link = $DD_fckeditor . 'editor/'.$File.'?InstanceName=' . $this->getFullName ();

			if ( $this->ToolbarSet != '' )
				$Link .= "&Toolbar=" . $this->ToolbarSet;

			// Render the linked hidden field.
			$Html .= '<input type="hidden" id="'.$this->getFullName (). '" name="'.$this->getFullName ().'" value="' . $this->escapeAttr ($this->getFormattedValue($tagParser)) . '" style="display:none" />' ;

			// Render the configurations hidden field.
			$Html .= '<input type="hidden" id="' . $this->getFullName () . '___Config" value="' . $this->GetConfigFieldString() . '" style="display:none" />' ;

			// Render the editor IFRAME.
			$Html .= '<iframe id="' . $this->getFullName () . '___Frame" src="'.$Link.'" width="'.$this->Width.'" height="'.$this->Height.'" frameborder="0" scrolling="no"></iframe>';
		}
		else
		{
			if ( strpos( $this->Width, '%' ) === false )
				$WidthCSS = $this->Width . 'px' ;
			else
				$WidthCSS = $this->Width ;

			if ( strpos( $this->Height, '%' ) === false )
				$HeightCSS = $this->Height . 'px' ;
			else
				$HeightCSS = $this->Height ;

			$Html .= '<textarea name="'.$this->getFullName ().'" rows="4" cols="40" style="width:'.$WidthCSS.'; height:'.$HeightCSS.'">'.
				$this->escapeAttr ($this->getFormattedValue($tagParser)).'</textarea>';
		}

		return $Html ;
	}

	/**
	 * Returns true if browser is compatible with FCKeditor.
	 *
	 * @return boolean
	 */
	public function IsCompatible()
	{
		return FCKeditor_IsCompatibleBrowser() ;
	}

	/**
	 * Get settings from Config array as a single string.
	 *
	 * @access protected
	 * @return string
	 */
	public function GetConfigFieldString()
	{
		$sParams = '' ;
		$bFirst = true ;

		foreach ( $this->Config as $sKey => $sValue )
		{
			if ( $bFirst == false )
				$sParams .= '&amp;' ;
			else
				$bFirst = false ;

			if ( $sValue === true )
				$sParams .= $this->EncodeConfig( $sKey ) . '=true' ;
			else if ( $sValue === false )
				$sParams .= $this->EncodeConfig( $sKey ) . '=false' ;
			else
				$sParams .= $this->EncodeConfig( $sKey ) . '=' . $this->EncodeConfig( $sValue ) ;
		}

		return $sParams ;
	}

	/**
	 * Encode characters that may break the configuration string
	 * generated by GetConfigFieldString().
	 *
	 * @access protected
	 * @param string $valueToEncode
	 * @return string
	 */
	public function EncodeConfig( $valueToEncode )
	{
		$chars = array(
			'&' => '%26',
			'=' => '%3D',
			'"' => '%22' ) ;

		return strtr( $valueToEncode,  $chars ) ;
	}
}
