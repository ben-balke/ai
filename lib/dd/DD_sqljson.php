<?php
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/lib/dd/DD_sqljson.php,v 1.3 2010/04/19 02:02:19 secwind Exp $
*************************************************************************/

require_once 'dd/DD_sql.php';
require_once 'dd/DD_json.php';
class DD_SqlJson
{
	var			$m_select;
	var			$m_connect;
	var			$m_conn;
	function DD_SqlJson ($connect, $select)
	{
		$this->m_select = $select;
		$this->m_connect = $connect;
	}
	function recordCallback (&$row, &$tagParser)
	{
	}
	function getJson ($tagParser = null)
	{
		$queryHandle;
		$rslt = null;
		$this->m_conn = DD_SqlPoolGetConn ($this->m_connect);
		if (!isset ($this->m_conn))
		{
			die ("DD_SqlRecordHandler::query(): Cannot find connection string" . $this->m_connect);
		}
		if (isset ($tagParser))
		{
			$queryHandle = $this->m_conn->query ($tagParser->parseSql ($this->m_select));
		}
		else
		{
			$queryHandle = $this->m_conn->query ($this->m_select);
		}

		$row = $this->m_conn->nextNamedRecord ($queryHandle);
		if (isset ($row))
		{
			$this->recordCallback ($row, $tagParser);
			$rslt = DD_json::encode ($row);
		}
		$this->m_conn->freeQuery ($queryHandle);
		return $rslt;
	}
	function getMultiRowJson ($name, $tagParser = null)
	{
		$queryHandle;
		$rslt = null;
		$this->m_conn = DD_SqlPoolGetConn ($this->m_connect);
		if (!isset ($this->m_conn))
		{
			die ("DD_SqlRecordHandler::query(): Cannot find connection string" . $this->m_connect);
		}
		if (isset ($tagParser))
		{
			$queryHandle = $this->m_conn->query ($tagParser->parseSql ($this->m_select));
		}
		else
		{
			$queryHandle = $this->m_conn->query ($this->m_select);
		}

			/*
			 * Support multiple rows.
			 */
		while (($row = $this->m_conn->nextNamedRecord ($queryHandle)) != null)
		{
			if (isset ($row))
			{
				$this->recordCallback ($row, $tagParser);
				if (!isset ($rslt))
					$rslt .= '{"' . $name .'": [';
				else
					$rslt .= ',';
				$rslt .= DD_json::encode ($row);
			}
		}
		if (isset ($rslt))
		{
			$rslt .= '] }';
		}
		$this->m_conn->freeQuery ($queryHandle);
		return $rslt;
	}
	function close ()
	{
		if (isset ($this->m_conn))
		{
			$this->m_conn->disconnect ();
		}
	}
}
?>
