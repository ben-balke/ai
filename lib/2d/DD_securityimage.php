<?php
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/lib/2d/DD_securityimage.php,v 1.1 2016/02/16 21:18:00 secwind Exp $
*************************************************************************/

/**
 *	SecurityImage.php
 *
 *	Class to implement Captcha security Images
 *	to combat Spam, using PHP/GD
 * 
 *	@author			A.D.Surrey. (www.surneo.com)
 * 	@version		1.3
 */
class DD_SecurityImage
{
	var		$m_lib;	// Base directory where the fonts and images are stored.
	var	
		$m_image,
		$m_font,					// Located in fonts folder.
		$m_fontsize,
		$m_colour,					// Colour of text
		$m_strLength,					// Length of random Text
		$m_text = "",
		$m_num_dots,					// Num of noise dots to add
		$chars = array("A","B","C","D","E","F","G","H","I","J",
			   "K","L","M","N","P","Q","R","S","T",
			   "U","V","W","X","Y","Z","1","2","3","4","5","6","7","8","9");
	
		/**
		 * Constructor, setup initial values.
		 *
		 * @return SecurityImage
		 */
	function DD_SecurityImage($lib)
	{
		$this->m_lib = $lib;
		$this->m_num_dots = 300; // How much Noise to add.
		$this->m_strLength = 5;
		$this->m_fontsize = 18;
		
		$this->selectFont(); // Decide which Font to use.
		
		$bg = $this->m_lib . "images/" . mt_rand(1,7) . ".png"; // Set bg to use.
		$this->m_image = @imagecreatefrompng($bg);
		$this->m_colour = @ImageColorAllocate ($this->m_image, 0, 0, 0); // Black
	}
	function getCode ()
	{
		return $this->m_text;
	}
	/**
	 * Display our Captcha image
	 *
	 */
	function create()
	{
		Header ("Content-type: image/png"); 
		
		$this->genString();
		
		/**
		 * write each letter to image
		 */
		for($i = 0; $i < $this->m_strLength; $i++)
		{
			$this->writeLetter($this->m_text[$i],(20 + $i * 25));
		}
		
		$this->addNoise();
		
		@imagepng($this->m_image);
		@imagedestroy($this->m_image);
	}
	
	/**
	 * Generate a random string for our image
	 * using the caracters in our array.
	 *
	 */
	function genString()
	{
		for ($i = 0; $i < $this->m_strLength; $i++) 
		{
   			$this->m_text .= $this->chars[mt_rand(0, count($this->chars) - 1)];
		}
	}
	
	/**
	 * writes a single letter to the image, using random angles/colours
	 *
	 */
	function writeLetter($letter ,$xvalue)
	{
		$yvalue = 30 - mt_rand(0, 10); // Randomly adjust y position.
		$angle = @mt_rand(-30,30);// Give text a slight random angle.
		
		if (function_exists ("imagettftext"))
			imagettftext($this->m_image, $this->m_fontsize, $angle, $xvalue, $yvalue, $this->m_colour, $this->m_font, $letter);
		else
		{
			$font = imageloadfont($this->m_font);
			imagestring($this->m_image, $font, $xvalue, $yvalue - 15, $letter, $this->m_colour);
		}
	}
	
	/**
	 * Compares the given text to the one in the Security 
	 * Image.
	 * 
	 * @return true if the text matches.
	 */
	function isMatch($t)
	{
		if($t == $this->m_text) 
		{
			return true;
		}
		else return false;
	}
	
	/**
	 * function to add extra "noise"
	 * or random dots to the image
	 *
	 */
	function addNoise()
	{
		$width = imagesx($this->m_image);
		$height = imagesy($this->m_image);

		//random dots.
		for($i = 0; $i < $this->m_num_dots; $i++)
		{
			@imagefilledellipse($this->m_image, mt_rand(0,$width), mt_rand(0,$height), 1, 1, $this->m_colour);
		}
	}
	
	/**
	 * Chose a random TTF Font to use for out Captcha Text.
	 *
	 */
	function selectFont()
	{
		$this->m_font = $this->m_lib . "fonts/";
		if (function_exists ("imagettftext"))
		{
			switch (mt_rand(1,3))
			{
			case 1 : $this->m_font .= "Acidic.TTF"; break;
			case 2 : $this->m_font .= "arial.ttf"; break;
			case 3 : $this->m_font .= "frizzed.ttf"; break;
			}
		}
		else
		{
			//switch (mt_rand(1,3))
			//{
			//case 1: $this->m_font .= "duckdigit.gdf"; break;
			//case 2: $this->m_font .= "dimurph.gdf"; break;
			//case 3: $this->m_font .= "hootie.gdf"; break;
			//}
			$this->m_font .= "duckdigit.gdf";
		}
	}
} 
?>
