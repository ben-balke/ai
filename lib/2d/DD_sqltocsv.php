<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/lib/2d/DD_sqltocsv.php,v 1.1 2016/02/16 21:18:00 secwind Exp $
*************************************************************************/

// Example:
//		DD_SqlToCSVFactory $stx = new DD_SqlToCSVFactory ("sql.ops", $page->m_tp);
//		$csv = $stx->generateRecords ('order', "select * from pdp_quote where status = 'N'");
//		print $csv;
//
class DD_SqlToCSVFactory
{
	var 		$m_tablename;
	var			$m_csv_output = null;
	var			$m_conn;
	var			$m_tagParser;
	var			$m_echo = FALSE;
	var			$m_stream = null;
	
	function __construct ($connect, $tagParser)
	{
		
		$this->m_conn = DD_SqlPoolGetConn ($connect);
		if (!isset ($this->m_conn))
		{
			die ("DD_SqlToCSVFactory::generateRecords(): Cannot find connection string" . $connect);
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
		header("Content-type: text/x-csv");
    	header("Content-Disposition: attachment; filename=$filename");
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
		return $this->m_csv_output;
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


		while ($row = $this->m_conn->nextNamedRecord ($queryHandle))
		{
			if ($useheader && !isset ($header))
			{
				$sep = '';
				$header = '';
				foreach($row as $name=>$value)
				{
					$header .= $sep . '"' . $name . '"';
					$sep = ',';
				}
				$this->m_csv_output .= $header . "\r\n";
			}
			$sep = '';
			foreach($row as $name=>$value)
			{
				if (isset ($value))
				{

      				if ( preg_match( '/\\r|\\n|,|"/', $value ) )
      				{
        				$this->m_csv_output .= $sep . '"' . str_replace( '"', '""', $value ) . '"';
      				}
					else
					{
						$this->m_csv_output .= $sep . '"' . $value . '"'; 
					}
				}
				else
				{
					$this->m_csv_output .= $sep . '""'; 
				}
				$sep = ',';
			}
			$this->m_csv_output .= "\r\n";
			if ($this->m_echo == TRUE)
			{
				echo $this->m_csv_output;
				$this->m_csv_output = "";
			}
			if (isset ($this->m_stream))
			{
				fwrite($this->m_stream, $this->m_csv_output);
				$this->m_csv_output = "";
			}
		} 
		if ($this->m_echo == TRUE)
		{
			echo $this->m_csv_output;
			$this->m_csv_output = "";
		}
	}
}
?>
