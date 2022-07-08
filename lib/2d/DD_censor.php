<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/lib/2d/DD_censor.php,v 1.1 2016/02/16 21:18:00 secwind Exp $
*************************************************************************/
/** 
* IntelligentCensor v2.0 
* Copyright 2006-2008 sk89q 
* Written by sk89q 
* 
* This program is free software; you can redistribute it and/or 
* modify it under the terms of the GNU General Public License 
* as published by the Free Software Foundation; either version 2 
* of the License, or (at your option) any later version. 
* 
* This program is distributed in the hope that it will be useful, 
* but WITHOUT ANY WARRANTY; without even the implied warranty of 
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
* GNU General Public License for more details. 
* 
* You should have received a copy of the GNU General Public License 
* along with this program; if not, write to the Free Software 
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA. 
*/ 

/** 
* Censors text while taking into account wildcard substitutions and casing. 
* 
* @version 2.0 
* @author sk89q 
* @copyright Copyright (c) 2006-2008, sk89q 
*/ 
class DD_Censor 
{ 
	public $inclusion = array(); 
	public $exclusion = array(); 
	 
	public $censored_text = "[censored]"; 
	public $max_recursion = 15; 
	 
	private $c_level; 
	private $c_censor; 
	private $c_replacement; 
	 
	/** 
	 * Constructs the object. 
	 * @param array $inclusion Censor word list 
	 * @param array $exclusion Censor exclusion word list 
	 */ 
	public function __construct(array $inclusion, array $exclusion = array()) 
	{ 
		$this->inclusion = $inclusion; 
		$this->exclusion = $exclusion; 
	} 
	 
	/** 
	 * Attaches the ending of the censor (for the % wildcard) to the 
	 * beginning of the stem. It will also consider the grammar of the stem 
	 * and beginning so that it is conjugated together correctly. 
	 * @param string $stem The stem of the word 
	 * @param string $beginning The beginning to be prepended 
	 * @param string $replacement The censor replacement from the word list (for reference) 
	 * @return string Final attached version with stem and beginning 
	 */ 
	protected function attach_beginning($stem, $beginning, $replacement) 
	{ 
		return $beginning.$stem; 
	} 
	 
	/** 
	 * Attaches the ending of the censor (for the % wildcard) to the 
	 * end of the stem. It will also consider the grammar of the stem 
	 * and ending so that it is conjugated together correctly. 
	 * @param string $stem The stem of the word 
	 * @param string $ending The ending to be appended 
	 * @param string $replacement The censor replacement from the word list (for reference) 
	 * @return string Final attached version with stem and ending 
	 */ 
	protected function attach_ending($stem, $ending, $replacement) 
	{ 
		$last_letter = $replacement[strlen($replacement)-1]; 
		 
		// y => ies 
		if ($last_letter == "y" && $ending == "s") { 
			$stem = substr($stem, 0, -1); 
			$ending = "ies"; 
		} 
		// e => ing 
		else if ($last_letter == "e" && $ending == "ing") { 
			$stem = substr($stem, 0, -1); 
			$ending = "ing"; 
		} 
		// e => ed 
		else if ($last_letter == "e" && $ending == "ed") { 
			$stem = substr($stem, 0, -1); 
			$ending = "ed"; 
		} 
		// Repeated consants 
		else if (in_array($last_letter, array("p")) & $ending == "y") { 
			$stem .= $last_letter; 
			$ending = "y"; 
		} 
		 
		return $stem.$ending; 
	} 
	 
	/** 
	 * Attempts to copy the casing from the original uncensored version 
	 * to the final censored version. For example, if the original 
	 * uncensored word is in all caps, the final version will be 
	 * in all caps. This implementation supports all uppercase, 
	 * all lowercase, and a copying of the capitalizing of each 
	 * character starting from i=0, where i is the position in the 
	 * string (i.e. hAppY => sUpeRcrazy). 
	 * @param string $uncensored The original uncensored version, used for reference 
	 * @param string $final The string that needs to be transformed 
	 * @return string Final trasnformed string 
	 */ 
	protected function adapt_casing($final, $uncensored) 
	{ 
		// Uppercase? 
		if ($uncensored == strtoupper($uncensored)) { 
			$final = strtoupper($final); 
		} 
		// Lowercase? 
		else if ($uncensored == strtolower($uncensored)) { 
			// If the censor has casing, preserve that 
			if (strtolower($uncensored) != $uncensored) { 
				$final = strtolower($final); 
			} 
		} 
		// Now we just capitalize the letters in order... 
		else { 
			$new_final = ''; 
			 
			// Iterate through each letter 
			for ($i = 0; $i < strlen($final); $i++) { 
				if(strlen($uncensored) == $i) { // Ran out of letters, default... 
					$new_final .= substr($final, $i); 
					break; 
				} else if (strtoupper($uncensored[$i]) == $uncensored[$i]) { 
					$new_final .= strtoupper($final[$i]); 
				} else { 
					$new_final .= strtolower($final[$i]); 
				} 
			} 
			 
			$final = $new_final; 
		} 
		 
		return $final; 
	} 
	 
	/** 
	 * Used as the censor callback. 
	 * @param array $m Matches 
	 * @return string Censored version 
	 */ 
	private function censor_callback(array $m) 
	{ 
		$level = $this->c_level; 
		$censor = $this->c_censor; 
		$replacement = $this->c_replacement; 
		 
		// Exclusion! 
		foreach ($this->exclusion as $match) { 
			$pattern = preg_quote($match, '#'); // Make it regexp safe 
			$pattern = str_replace("\*", "\w*", $pattern); // For matches 
			$pattern = str_replace("\+", "+", $pattern); // For repeating characters 
		 
			if(preg_match("#^$pattern$#i", $m[0])) 
			{ 
				return $m[0]; 
			} 
		} 
		 
		// Get rid of characters 
		$replacement_clean = str_replace("%", "", $replacement); 
		$replacement_clean = str_replace("*", "", $replacement_clean); 
		$replacement_clean = str_replace("+", "", $replacement_clean); 
		 
		$final = ''; 
		 
		// Add beginning match 
		if ($censor[0] == "%" && strlen($m[1]) > 0) { 
			$beginning = $this->censor($m[1], $level+1, $level, $censor, $replacement); 
			 
			// Adapt grammar 
			$final = $this->attach_beginning($final, $beginning, $replacement_clean); 
		} 
		 
		// Add the replacement 
		$final .= $replacement_clean; 
		 
		// Add ending match 
		if ($censor[strlen($censor)-1] == "%" && strlen($m[count($m)-1]) > 0) { 
			$end = $this->censor($m[count($m)-1], $level+1, $level, $censor, $replacement); 
			 
			// Adapt grammar 
			$final = $this->attach_ending($final, $end, $replacement_clean); 
		} 
		 
		// Casing 
		$final = $this->adapt_casing($final, $m[0]); 
		 
		// Trim 
		$final = trim($final); 
		 
		return $final; 
	} 
	 
	/** 
	 * Performs censoring on a string of text 
	 * @param string $text The text 
	 * @param int $level Recursion level; internally incremented number 
	 * @param int $old_level Due to limitation of scope, we need to reset this for recursion to work 
	 * @param int $old_censor Due to limitation of scope, we need to reset this for recursion to work 
	 * @param int $old_replacement Due to limitation of scope, we need to reset this for recursion to work 
	 * @return string Final censored text string 
	 */ 
	public function censor($text, $level = 0, $old_level = 0, $old_censor = 0, $old_replacement = '') 
	{ 
		// Blank? 
		if(trim($text) == "") 
		{ 
			return $text; 
		} 
		 
		// Too much recursion 
		if ($level >= $this->max_recursion) { 
			return $this->censored_text; 
		} 
		 
		foreach ($this->inclusion as $censor => $replacement) { 
			// Need this for the callback 
			$this->c_level = $level; 
			$this->c_censor = $censor; 
			$this->c_replacement = $replacement; 
			 
			$pattern = preg_quote($censor, '#'); // Make it regexp safe 
			$pattern = "($pattern)"; // Add surrounding paranthesis so we can add )( later 
			$pattern = str_replace("%", ")(\w*)(", $pattern); // For beginning and end matches 
			$pattern = str_replace("\*", "\w*", $pattern); // For matches that don't matter 
			$pattern = str_replace("\+", "+", $pattern); // For repeating characters 
			$pattern = str_replace("()", "", $pattern); // Clean up useless match groups 
			 
			$text = preg_replace_callback("#\b$pattern\b#i", array($this, 'censor_callback'), $text); 
		} 
		 
		// Fake scope... 
		// The problem is... PHP does not have closures where variables 
		// can be bound. Setting properties of the current object 
		// work dandy if it's just a linear stack, but when recursion 
		// is involved, we need to "reset" the current "bounded variables." 
		$this->c_level = $old_level; 
		$this->c_censor = $old_censor; 
		$this->c_replacement = $old_replacement; 
		 
		return $text; 
	} 
}  
/* EXAMPLE....
require_once "IntelligentCensor.php"; 

// * are not expanded 
// % are expanded, and but they will only work when placed at either the 
// beginning or ending of the word 
$censors = array( 
	'*damn%' => 'darn', 
	'*fuck%' => 'frick', 
	'shit%' => 'poop', 
	'bastard*' => 'happyperson', 
	'*ho' => 'person', 
); 
// Be careful about creating censors with wildcards, as they match more 
// words than you may want. 

// Can use * wildcards 
$censors_exclusion = array( 
	'echo*', 
); 

$icensor = new IntelligentCensor($censors, $censors_exclusion); 

echo $icensor->censor("you're all DIETYDAMNING bastardly wHOres! you echo ho!"); 
*/
?>
