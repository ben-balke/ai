<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/lib/2d/DD_sqltohtml.php,v 1.1 2016/02/16 21:18:00 secwind Exp $
*************************************************************************/

// Example:
//		DD_SqlToHTMLFactory $stx = new DD_SqlToHTMLFactory ("sql.ops", $page->m_tp);
//		$html = $stx->generateRecords ('order', "select * from pdp_quote where status = 'N'");
//		print $html;
//
class DD_SqlToHTMLFactory
{
	var 		$m_tablename;
	var			$m_html_output = null;
	var			$m_conn;
	var			$m_tagParser;
	var			$m_echo = FALSE;
	var			$m_stream = null;
	
	function __construct ($connect, $tagParser)
	{
		
		$this->m_conn = DD_SqlPoolGetConn ($connect);
		if (!isset ($this->m_conn))
		{
			die ("DD_SqlToHTMLFactory::generateRecords(): Cannot find connection string" . $connect);
		}
		$this->m_tagParser = $tagParser;
	}
	function setOutputStream ($stream)
	{
		$this->m_stream = $stream;
		$this->m_echo = FALSE;
	}
	function setHeaders ($filename)
	{
		header("Pragma: public");
		header("Cache-Control: max-age=0"); 
    	//header("Cache-Control: must-revalidate, post-check=0, pre-check=0");
		header("Content-type: text/html");
	}
	/**
	 * Sends the output using echo 
	 */
	function setEcho ($enable /* TRUE or FALSE */)
	{
		$this->m_echo = $enable;
	}
	
	function getResult ()
	{
		return $this->m_html_output;
	}
	function generateRecords ($select, $useheader = TRUE)
	{
		if (isset ($this->m_tagParser))
		{
			$queryHandle = $this->m_conn->query ($this->m_tagParser->parseSql ($select));
		}
		else
		{
			$queryHandle = $this->m_conn->query ($select);
		}

		$this->m_html_output = '';

		while ($row = $this->m_conn->nextNamedRecord ($queryHandle))
		{
			if ($useheader && !isset ($header))
			{
				$header = '<tr>';
				foreach($row as $name=>$value)
				{
					$header .= '<th>' . $name . '</th>';
				}
				$this->m_html_output .= $header . "</tr>";
			}
			$this->m_html_output .= '<tr>';
			foreach($row as $name=>$value)
			{
				if (isset ($value))
				{
					//$this->m_html_output .= '<td>' .  @htmlspecialchars ($value) . '</td>';
					$this->m_html_output .= '<td>' .  $value . '</td>';
				}
				else
				{
					$this->m_html_output .= '<td>&nbsp;</td>';
				}
			}
			$this->m_html_output .= '</tr>';
			if ($this->m_echo == TRUE)
			{
				echo $this->m_html_output;
				$this->m_html_output = "";
			}
			if (isset ($this->m_stream))
			{
				fwrite($this->m_stream, $this->m_html_output);
				$this->m_html_output = "";
			}
		} 
		if ($this->m_echo == TRUE)
		{
			echo $this->m_html_output;
			$this->m_html_output = "";
		}
	}
}
?>
