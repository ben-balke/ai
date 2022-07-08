<?php 
/********************************************************************************
*** DuckDigit Infrastructure
*** Version: $Header: /home/cvsroot/ai/lib/dd/fields/DD_calendar.php,v 1.3 2012/01/09 08:23:12 secwind Exp $
*** Author: Ben Balke
*** Copyright (c) DuckDigit Technologies, Inc. 2008 ALL RIGHTS RESERVED
********************************************************************************/

/* Create a javascript function called DD_gotomonth (month, year) to navigate your calendar.
 * Override getDateControls of DD_eventloader to put in a day action buttonts.
 */
/*
 * Implement this class to feed the DD_calendar events to display.
 */
class DD_eventloader
{
    function load ($cal_mindate, $cal_maxdate, $tagParser) /* virtual */
	{
	}
	function getDaysEvents ($timestamp, $tagParser) /* virtual */
	{
	}
	function getDateControls ($timestamp, $tagParser) /* virtual */
	{
	}

}
class DD_calendar extends DD_DomField
{
	var			$m_month;
	var			$m_year;
	var			$m_eventloader;

    function __construct ($name, $month, $year, $eventloader)
    {
        $array = array ('name'=>$name);
        parent::DD_DomField ($array, null);
		$this->m_month = intval($month);
		$this->m_year = intval($year);
		$this->m_eventloader = $eventloader;
    }

    function toString ($tagParser)
    {

	/************************************
	** CONTENT
	************************************/

	$monthNames = Array(
		"January"
		, "February"
		, "March"
		, "April"
		, "May"
		, "June"
		, "July"
		, "August"
		, "September"
		, "October"
		, "November"
		, "December"
	);

$header=<<<CONTENT
	<table class="calendar" border="0" cellpadding="0" cellspacing="0">
	<caption>
		<div style="padding: 2px 10px; float: left">
			<a href="javascript: DD_gotomonth ({dd:cal.prev_month},{dd:cal.prev_year});">Previous</a>
		</div>
		<div style="padding: 2px 10px; float: right"><a href="javascript: DD_gotomonth ({dd:cal.next_month},{dd:cal.next_year});">Next</a></div>
		{dd:cal.cur_month_name} {dd:cal.cur_year}
	</caption>
	<tr>
		<th>Sun</th>
		<th>Mon</th>
		<th>Tue</th>
		<th>Wed</th>
		<th>Thu</th>
		<th>Fri</th>
		<th>Sat</th>
	</tr> 
CONTENT;

$content=<<<CONTENT
		<li class=group>{dd:group.name}</li>
CONTENT;

$footer=<<<FOOTER
	</table>
FOOTER;
		/************************************
		** SQL Statements
		************************************/
		$rh = new DD_RecordHandler ('cal', false);	

		$prev_year = $this->m_year;
		$next_year = $this->m_year;
		$prev_month = $this->m_month-1;
		$next_month = $this->m_month+1;

		if ($prev_month == 0 ) {
			$prev_month = 12;
			$prev_year = $this->m_year - 1;
		}
		if ($next_month == 13 ) {
			$next_month = 1;
			$next_year = $this->m_year + 1;
		}
		$rh->makeField (array ( 'name'=>'next_month' ,'type'=>'work','value'=>$next_month));
		$rh->makeField (array ( 'name'=>'next_year' ,'type'=>'work','value'=>$next_year));
		$rh->makeField (array ( 'name'=>'prev_month' ,'type'=>'work','value'=>$prev_month));
		$rh->makeField (array ( 'name'=>'prev_year' ,'type'=>'work','value'=>$prev_year));
		$rh->makeField (array ( 'name'=>'cur_month' ,'type'=>'work','value'=>$this->m_month));
		$rh->makeField (array ( 'name'=>'cur_year' ,'type'=>'work','value'=>$this->m_year));
		$rh->makeField (array ( 'name'=>'cur_month_name' ,'type'=>'work', 'value'=>$monthNames[$this->m_month - 1]));
		$rh->makeField (array ( 'name'=>'next_month' ,'type'=>'work','value'=>$next_month));
		$rh->makeField (array ( 'name'=>'next_year' ,'type'=>'work','value'=>$next_year));

		$tagParser->addValueProvider ($rh);

		/************************************
		* LOGIC, PAGE CONSTRUCTION AND POST PROCESSING
		************************************/

		$timestamp = mktime(0,0,0,$this->m_month,1,$this->m_year);
		$maxday = date("t",$timestamp);
		$thismonth = getdate ($timestamp);
		$startday = $thismonth['wday'];
		$thisday = $thismonth['mday'];

		$prev_timestamp = mktime(0,0,0,$prev_month,1,$prev_year);
		$prev_maxday = date("t",$prev_timestamp);

		if ($startday == 0)
			$cal_mindate = $timestamp;
		else
			$cal_mindate = $timestamp = mktime(0,0,0,$prev_month,($prev_maxday - $startday) + 1,$prev_year);
		if (($startday + $maxday) % 7 == 0)
			$cal_maxdate = $timestamp = mktime(0,0,0,$this->m_month,$maxday,$this->m_year);
		else
			$cal_maxdate = $timestamp = mktime(0,0,0,$next_month,7 - (($startday + $maxday) % 7),$next_year);

		$rh->makeField (array ( 'name'=>'cal_maxdate' ,'datatype'=>'date', 'type'=>'work','value'=>date("y-m-d", $cal_maxdate)));
		$rh->makeField (array ( 'name'=>'cal_mindate' ,'datatype'=>'date', 'type'=>'work','value'=>date("y-m-d", $cal_mindate)));

		if (isset ($this->m_eventloader))
			$this->m_eventloader->load ($cal_mindate, $cal_maxdate, $tagParser);

		$text = $tagParser->parseContent ($header);

		for ($i=0; $i<($maxday+$startday); $i++) 
		{
			if(($i % 7) == 6 )
				$weekend = ' weekend';
			else if(($i % 7) == 0 )
			{
				$weekend = ' weekend';
				$text .= "<tr>";
			}
			else
				$weekend = '';
			if($i < $startday) 
			{
				$year = $prev_year;
				$month = $prev_month;
				$day = (($i - $startday + 1) + $prev_maxday);
				$not_cur_month = ' not-current-month';
			}
			else 
			{
				$year = $this->m_year;
				$month = $this->m_month;
				$day = $i - $startday + 1;
				$not_cur_month = '';
			}
			$thedate = mktime(0,0,0,$month,$day,$year);
			$text .= '<td class="'. $not_cur_month . '"><div class="daynum">'. $day . '</div>';
			if (isset ($this->m_eventloader))
			{
				$text .= $this->m_eventloader->getDateControls ($thedate, $tagParser);
			}
			$text .= '<div class="day' . $not_cur_month . $weekend . '">';
			if (isset ($this->m_eventloader))
			{
				$text .= $this->m_eventloader->getDaysEvents ($thedate, $tagParser);
			}
			$text .= '</div></td>';
			if(($i % 7) == 6 ) 
				$text .= "</tr>";
		}
		for ($d = 1, $r = $i % 7; $r != 0 && $r <=6; $r++, $d++)
		{
			$thedate = mktime(0,0,0,$next_month,$d,$next_year);
			$text .= '<td class="not-current-month"><div class="daynum">'. $d . '</div>';
			if (isset ($this->m_eventloader))
			{
				$text .= $this->m_eventloader->getDateControls ($thedate, $tagParser);
			}
			
			$text .= '<div class="day not-current-month">';
			$text .= $this->m_eventloader->getDaysEvents ($thedate, $tagParser);
			$text .= '</div></td>';
			if(($i % 7) == 6 ) 
				$text .= "</tr>";
		}
		$text .= $tagParser->parseContent ($footer);
		return $text;
	}
}
