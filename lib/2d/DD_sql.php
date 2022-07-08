<?php
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/lib/2d/DD_sql.php,v 1.2 2016/03/16 18:47:27 secwind Exp $
*************************************************************************/
class DD_SqlDuplicateKeyException extends Exception
{
	public function errorMessage()
	{
		return "Duplicate Key Exception";
	}
}

class DD_SqlPool
{
	var		$m_databases;
	function __construct ()
	{
		$this->m_databases = array ();
	}
	function addConn (&$conn)
	{
		$this->m_databases [$conn->m_name] = $conn;
	}
	function getConn ($name)
	{
		$conn = $this->m_databases [$name];
		if (!isset ($conn))
		{
			die ("DD_SqlPool::getConn(): Cannot find connection:" . $name);
		}
		if (!$conn->isConnected ())
		{
			$conn->connect ();
		}
		return $conn;
	}
}

$DD_SqlPool = new DD_SqlPool ();

function DD_SqlPoolGetConn ($connString)
{
	global $DD_SqlPool;
	return $DD_SqlPool->getConn ($connString);
}

function DD_SqlPoolAddConn ($conn)
{
	global $DD_SqlPool;
	return $DD_SqlPool->addConn ($conn);
}

/**
 * DD_SqlConnection is an abstract class for SqlConnections.  It is used to add
 * and access generic SQL capabilites of specialized SQL systems such as MySql and 
 * Postgress etc.  
 */
class DD_SqlConnection
{
	var		$m_name;
	var		$m_host;
	var		$m_user;
	var		$m_pass;
	var		$m_db;
	var		$m_conn;

	function __construct ($name, $host, $user, $pass, $db)
	{
		$this->m_name = $name;
		$this->m_host = $host;
		$this->m_user = $user;
		$this->m_pass = $pass;
		$this->m_db = $db;
	}
	function connect ()
	{
		die ("Should never get here.");
	}
	function disconnect ()
	{
		die ("Should never get here.");
	}
	function isConnected ()
	{
		return isset ($this->m_conn);
	}
	function query ($sql)
	{
		die ("Should never get here.");
	}
	function nextRecord ($queryHandle)
	{
		die ("Should never get here.");
	}
	function nextNamedRecord ($queryHandle)
	{
		die ("Should never get here.");
	}
	function freeQuery ($queryHandle)
	{
		die ("Should never get here.");
	}
	function execute ($sql)
	{
		die ("Should never get here.");
	}
	function getTables ()
	{
		die ("getTables(): Should never get here.");
	}
	function getCreateTable ($table)
	{
		die ("getCreateTable(): Should never get here.");
	}
}

/**
 * DD_MySqlConnection specializes the DD_SqlConnection class for MySql database
 * servers.  This class uses persistant connections.
 */
class DD_MySqlConnection extends DD_SqlConnection
{
	/**
	 * Establishes a SQL Connection from a named array.
	 */
	function __construct ($array)
	{
		parent::__construct (
			$array ['name'],
			$array['host'],
			$array['user'], 
			$array ['pass'], 
			$array ['db']);
	}
	//function __construct ($name, $host, $user, $pass, $db)
	//{
		//parent::__construct ($name, $host, $user, $pass, $db);
	//}
	function connect ()
	{
		$this->m_conn = mysql_pconnect($this->m_host, $this->m_user, $this->m_pass) or 
			die ('DD_MySqlConnection::connect(): Error connecting to mysql:' . $this->m_host . 
				"\nError: (" . mysql_errno() . ") " . mysql_error()); 
		mysql_select_db($this->m_db, $this->m_conn) or 
			die("DD_MySqlConnection::connect(): <b>". $this->m_name . 
				"</b> A fatal MySQL selecting database error occured</b>.\n<br><br>" .
				"\nError: (" . mysql_errno() . ") " . mysql_error());
	}

	function disconnect ()
	{
		mysql_close($this->m_conn);
	}

	function query ($sql)
	{
		$queryHandle =  mysql_query ($sql, $this->m_conn) or 
			die("<b>DD_MySqlConnection:query(): Error in query</b>.\n<br /><br />" .
				"Sql: " . $sql . "\nError: (" . mysql_errno() . ") " . mysql_error());
		return $queryHandle;
	}

	function nextRecord ($queryHandle)
	{
		$row = mysql_fetch_array($queryHandle); 
		if ($row == false)
			return null;
		return $row;
	}
	function nextNamedRecord ($queryHandle)
	{
		$row = mysql_fetch_assoc($queryHandle); 
		if ($row == false)
			return null;
		return $row;
	}
	function freeQuery ($queryHandle)
	{
		mysql_free_result($queryHandle);
	}
	function execute ($sql)
	{
		if (mysql_query ($sql, $this->m_conn) == false)
		{
			if (mysql_errno() == 1062)
				throw new DD_SqlDuplicateKeyException ($sql);
			die("<b>DD_MySqlConnection: execute(): Error in query</b>.\n<br /><br />" .
				"Sql: " . $sql . "\nError: (" . mysql_errno() . ") " . mysql_error());
		}
		return mysql_affected_rows($this->m_conn); 
	}

	/****************
	ADMIN FUNCTIONS
	****************/
	function getTables ()
	{
		$sql = 'SHOW TABLES';
		$tables = array();
		$result = mysql_query($sql, $this->m_conn) or
			die("<b>DD_MySqlConnection:getTables(): Error in query</b>.\n<br /><br />" .
				"Sql: " . $sql . "\nError: (" . mysql_errno() . ") " . mysql_error());
		while($row = mysql_fetch_row($result))
		{
			$tables[] = $row[0];
		}
		mysql_free_result($result);
		return $tables;
	}
	function getCreateTable ($table)
	{
		$sql = 'SHOW CREATE TABLE ' . $table;
		$tables = array();
		$result = mysql_query($sql, $this->m_conn) or
			die("<b>DD_MySqlConnection:getTables(): Error in query</b>.\n<br /><br />" .
				"Sql: " . $sql . "\nError: (" . mysql_errno() . ") " . mysql_error());
		$createTable = null;
		if ($row = mysql_fetch_row($result))
		{
			$createTable = $row [1];
		}
		mysql_free_result($result);
		return $createTable;
	}

}
/**
 * DD_PgSqlConnection specializes the DD_SqlConnection class for Postgres database
 * servers.  This class uses persistant connections.
 */
class DD_PgSqlConnection extends DD_SqlConnection
{
	/**
	 * Establishes a SQL Connection from a named array.
	 */
	function __construct ($array)
	{
		parent::__construct (
			$array ['name'],
			$array['host'],
			$array['user'], 
			$array ['pass'], 
			$array ['db']);
	}
	function connect ()
	{
		$this->m_conn = pg_pconnect("host=" . $this->m_host . " dbname=" . $this->m_db . " user=" .  $this->m_user . " password=" . $this->m_pass) or 
			die ('DD_PgSqlConnection::connect(): Error connecting to pgsql:' . $this->m_host); 
	}

	function disconnect ()
	{
		pg_close($this->m_conn);
	}

	function query ($sql)
	{
		$queryHandle =  pg_query ($this->m_conn, $sql) or 
			die("<b>DD_PgSqlConnection:query(): Error in query</b>.\n<br /><br />" .
				"Sql: " . $sql . "\nError: (" . pg_last_Error($this->m_conn) . ") ");
		return $queryHandle;
	}

	function nextRecord ($queryHandle)
	{
		$row = pg_fetch_array($queryHandle); 
		if ($row == false)
			return null;
		return $row;
	}
	function nextNamedRecord ($queryHandle)
	{
		$row = pg_fetch_assoc($queryHandle); 
		if ($row == false)
			return null;
		return $row;
	}

	function freeQuery ($queryHandle)
	{
		pg_free_result($queryHandle);
	}
	function execute ($sql)
	{
		if (($rslt = pg_query ($this->m_conn, $sql)) == false)
		{
			if (strstr (pg_last_error(), "duplicate key"))
				throw new DD_SqlDuplicateKeyException ($sql);
			die("<b>DD_PgSqlConnection: execute(): Error in query</b>.\n<br /><br />" .
				"Sql: " . $sql . "\nError: (" . pg_last_error($this->m_conn) . ") ");
		}
		return pg_affected_rows($rslt); 
	}
	static function _pgSubArray (&$pgArray)
	{
		preg_match('/^{(.*)}$/', $pgArray, $matches);
		if (count ($matches) == 0)
		{
			return $matches;
		}
	    $phpArr = str_getcsv($matches[1]);
		return $phpArr;
	}
}

/**
 * DD_MySqlConnection specializes the DD_SqlConnection class for MySql database
 * servers.  This class uses persistant connections.
 */
class DD_PDOConnection extends DD_SqlConnection
{
	/**
	 * Establishes a SQL Connection from a named array.
	 */
	function __construct ($array)
	{
		parent::__construct (
			$array ['name'],
			$array['host'],
			$array['user'], 
			$array ['pass'], 
			$array ['db']);
	}
	function connect ()
	{
		// for example 'mysql:host=localhost'
		//
		try {
			$this->m_conn = new PDO($this->m_host . ';dbname=' . $this->m_db . ';charset=utf8mb4', $this->m_user, $this->m_pass);	
			$this->m_conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
		}
		catch (PDOException $ex)
		{
			die ('DD_PDOConnection::connect(): Error connecting to host:' . $this->m_host . 
				"\nError: (" . $ex->getMessage () . ") ");
		}
	}

	function disconnect ()
	{
		$this->m_conn = null;
	}

	function query ($sql)
	{
		try {
			$queryHandle =  $this->m_conn->query($sql);
		}
		catch (PDOException $ex)
		{
			die("<b>DD_PDOConnection:query(): Error in query</b>.\n<br /><br />" .
				"Sql: " . $sql . "\nError: (" . $ex.getMessage () . ") ");
		}
		return $queryHandle;
	}

	function nextRecord ($queryHandle)
	{
		$row = $queryHandle->fetch ();
		if ($row == false)
			return null;
		return $row;
	}
	function nextNamedRecord ($queryHandle)
	{
		$row = $queryHandle->fetch(PDO::FETCH_ASSOC);
		if ($row == false)
			return null;
		return $row;
	}
	function freeQuery ($queryHandle)
	{
	}
	function execute ($sql)
	{
		$rowseffected = 0;
		try
		{
			$rowseffected = $this->m_conn->exec($sql);
			var_export ($stmt, false);
		}
		catch (PDOException $ex)
		{
			if ($ex->errorInfo[1] == 1062) 
				throw new DD_SqlDuplicateKeyException ($sql);
			die("<b>DD_MySqlConnection: execute(): Error in query</b>.\n<br /><br />" .
				"Sql: " . $sql . "\nError: (" . mysql_errno() . ") " . mysql_error());
		}
		return $rowseffected;
	}

	/****************
	ADMIN FUNCTIONS
	****************/
	function getTables ()
	{
		$sql = 'SHOW TABLES';
		$tables = array();
		$query = $this->query ($sql) or
			die("<b>DD_MySqlConnection:getTables(): Error in query</b>.\n<br /><br />" .
				"Sql: " . $sql . "\nError: (" . mysql_errno() . ") " . mysql_error());
		while($row = $this->nextRecord ($query))
		{
			$tables[] = $row[0];
		}
		$this->freeQuery ($query);
		return $tables;
	}
	function getCreateTable ($table)
	{
		$sql = 'SHOW CREATE TABLE ' . $table;
		$tables = array();
		$query = $this->query ($sql) or
			die("<b>DD_MySqlConnection:getTables(): Error in query</b>.\n<br /><br />" .
				"Sql: " . $sql . "\nError: (" . mysql_errno() . ") " . mysql_error());
		$createTable = null;
		while($row = $this->nextRecord ($query))
		{
			$createTable = $row [1];
		}
		$this->freeQuery ($query);
		return $createTable;
	}

}

?>
