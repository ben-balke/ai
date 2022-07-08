<?php
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/lib/2d/DD_sqltoarray.php,v 1.1 2016/02/16 21:18:00 secwind Exp $
*************************************************************************/

require_once '2d/DD_sql.php';

class DD_SqlToArray
{
	var			$m_select;
	var			$m_connect;
	var			$m_conn;
	function DD_SqlToArray ($connect, $select)
	{
		$this->m_select = $select;
		$this->m_connect = $connect;
	}
	function recordCallback (&$row, &$tagParser)
	{
	}
	function makeArray ($name, $tagParser = null)
	{
		$array = array ();
		$queryHandle;
		$rslt = null;
		$this->m_conn = DD_SqlPoolGetConn ($this->m_connect);
		if (!isset ($this->m_conn))
		{
			die ("DD_SqlSqlToArray::query(): Cannot find connection string" . $this->m_connect);
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
		while (($row = $this->m_conn->nextRecord ($queryHandle)) != null)
		{
			if (isset ($row))
			{
				if (count ($row) == 1)
					$array [] = $row [0];
				else
					$array [$row[0]] = $row [1];
			}
		}
		$this->m_conn->freeQuery ($queryHandle);
		return $array;
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
