<?php 
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/lib/2d/DD_sqltoxml.php,v 1.1 2016/02/16 21:18:00 secwind Exp $
*************************************************************************/

$G_XML_TR =
				array( "&" => "&amp;", "<" => "&lt;", ">" => "&gt;", "\"" => "&quot;", "\n" => "<br/>", "\r" => "");

// Example:
//		DD_SqlToXmlFactory $stx = new DD_SqlToXmlFactory ("sql.ops", $page->m_tp);
//		$xml = $stx->generateRecords ('order', "select * from pdp_quote where status = 'N'");
//		print $xml;
//
class DD_SqlToXmlFactory
{
	var 		$m_tablename;
	var			$m_xml_output;
	var			$m_conn;
	var			$m_tagParser;
	var			$m_echo = FALSE;
	var			$m_newline = "\n";
	
	function DD_SqlToXmlFactory ($connect, $tagParser, $usenewlines = true)
	{
		
		$this->m_conn = DD_SqlPoolGetConn ($connect);
		if (!isset ($this->m_conn))
		{
			die ("DD_SqlToXmlFactory::generateRecords(): Cannot find connection string" . $connect);
		}
		$this->setNewlines ($usenewlines);
		$this->m_tagParser = $tagParser;
		$this->xmlHeader ();
	}
	/**
	 * Sends the output using echo 
	 */
	function setEcho ($enable /* TRUE or FALSE */)
	{
		$this->m_echo = $enable;
	}
	function setNewlines ($enable)
	{
		$this->m_newline = $enable == true ? "\n" : "";

	}
    function recordCallback (&$row, &$tagParser)
    {
    }
	function xmlHeader ()
	{
		$this->m_xml_output = "<?xml version=\"1.0\"?>" . $this->m_newline; 
	}
	function getResult ()
	{
		return $this->m_xml_output;
	}
	function generateRecords ($entityname, $select)
	{
		$count = 0;
		if (isset ($this->m_tagParser))
		{
			$queryHandle = $this->m_conn->query ($this->m_tagParser->parseSql ($select));
		}
		else
		{
			$queryHandle = $this->m_conn->query ($select);
		}

		$this->m_xml_output .= "<" . $entityname . "s>" . $this->m_newline; 

		while ($row = $this->m_conn->nextNamedRecord ($queryHandle))
		{
			$count++;
			$this->m_xml_output .= "\t<" . $entityname . ">" . $this->m_newline; 
			$this->recordCallback ($row, $this->m_tagParser);
			foreach($row as $name=>$value)
			{
				if (isset ($value))
				{
					global $G_XML_TR;
					$value = strtr($value, $G_XML_TR);
					$this->m_xml_output .= "\t\t<$name>" . $value . "</$name>" . $this->m_newline; 
				}
				else
				{
					$this->m_xml_output .= "\t\t<$name></$name>" . $this->m_newline; 
				}
			}
			$this->postRecordCallback ($entityname, $row);
			$this->m_xml_output .= "\t</" . $entityname . ">" . $this->m_newline; 
			if ($this->m_echo == TRUE)
			{
				echo $this->m_xml_output;
				$this->m_xml_output = "";
			}
		} 
		$this->m_xml_output .= "</" . $entityname . "s>" . $this->m_newline; 
		if ($this->m_echo == TRUE)
		{
			echo $this->m_xml_output;
			$this->m_xml_output = "";
		}
		return $count;
	}
	
		/**
		 * This is where you call back to create line items under the 
		 * header records etc.
		 */
	function postRecordCallback ($entityname, &$row)
	{

	}
}
?>
