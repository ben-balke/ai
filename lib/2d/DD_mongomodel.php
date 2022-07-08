<?php
class DD_MongoQuery
{
	var		$m_collection = null;
	var		$m_filter = null;
	var		$m_options = null;

	function __construct ($collection, $filter, $options)
	{
		$this->m_collection = $collection;
		$this->m_filter = $filter;
		$this->m_options = $options;
	}

	function getCollection ()
	{
			return $this->m_collection;
	}
	function getArray ($jsonstring, $tagParser)
	{
		if ($jsonstring == null)
		{
			return [];
		}
		if (!isset ($tagParser))
			return json_decode ($jsonstring);
		$parsed = $tagParser->parseContent ($jsonstring);
		if ($parsed == null)
		{
			return [];
		}
		$array = json_decode ($parsed);
		var_dump ($array);
		return $array;
	}

	function getFilterArray ($tagParser)
	{
		if ($this->m_filter == null)
				return [];
		return $this->m_filter;
	}
	function getOptionsArray ($tagParser)
	{
		if ($this->m_options == null)
			return [];
		return $this->m_options;
		//return $this->getArray ($this->m_options, $tagParser);
	}
}

class DD_MongoModel extends DD_Model
{
	var			$m_connect;
	var			$m_manager;

	var			$m_query; // DD_MongoQuery
	var			$m_insert = array ();
	var			$m_update = array ();
	var			$m_delete = array ();
	var			$m_iterator;
	var			$m_cursor;
	var			$m_mongoquery;
	var			$m_eof = true;

	function __construct ($array, $extension = null)
	{
		$autofields = null;
		if (isset ($array ['autofields']))
			$autofields = $array ['autofields'];
		if (isset ($extention ['autofields']))
			$autofields = $extension ['autofields'];
		parent::__construct ($array['name'], $autofields);
		parent::setState ($array);
        self::setState ($array);
        if (isset ($extension))
		{
			parent::setState ($extension);
            self::setState ($extension);
		}
	}
    function setState (&$array)
    {
        if (isset ($array ['connect']))
            $this->m_connect = $array ['connect'];
        if (isset ($array ['query']))
            $this->m_query = $array ['query'];

		foreach($array as $name=>$value)
		{
			if (strncmp ($name, "insert", 6) == 0)
			{
            	$this->m_insert [$name] = $value;
			}
			else if (strncmp ($name, "update", 6) == 0)
			{
            	$this->m_update [$name] = $value;
			}
			else if (strncmp ($name, "delete", 6) == 0)
			{
            	$this->m_delete [$name] = $value;
			}
		}
    }

	function quickSelect ($connect, $query/*DD_MongoQuery*/, $tagParser = null)
	{
		$rslt = false;
		$conn = DD_SqlPoolGetConn ($connect);
		if (!isset ($conn))
		{
			die ("DD_SqlModel::quickSelect(): Cannot find connection string" . $connect);
		}
		if (isset ($tagParser))
		{
			$queryHandle = $conn->query ($tagParser->parseSql ($sql));
		}
		else
		{
			$queryHandle = $conn->query ($sql);
		}
		$row = $conn->nextNamedRecord ($queryHandle);
		if (isset ($row))
		{
			//parent::setFromArray ($row, null, false);

			foreach($row as $name=>$value)
			{
				if (isset ($this->m_fields [$name]))
					$field = $this->m_fields [$name];
				else
					$field = $this->makeField (array ('name'=>$name));
				
				$field->set ($value);
			}
			$rslt = true;
		}
		$conn->freeQuery ($queryHandle);
		return $rslt;
	}
	function changeQuery ($query)
	{
		$this->m_query = $query;
	}

	function query ($tagParser = null)
	{
		if (!isset ($this->m_manager))
		{
			$this->m_manager = new MongoDB\Driver\Manager($this->m_connect);
			if (!isset ($this->m_manager))
			{
				die ("DD_MongoModel::query(): Cannot find connection string" . $this->m_connect);
			}
		}
		$this->m_mongoquery = new MongoDB\Driver\Query($this->m_query->getFilterArray ($tagParser), $this->m_query->getOptionsArray ($tagParser));

		$this->m_cursor = $this->m_manager->executeQuery($this->m_query->getCollection (), $this->m_mongoquery);
		if (!isset ($this->m_cursor))
		{
			return null;
		};
		$this->m_iterator = new \IteratorIterator($this->m_cursor);
		$this->m_iterator->rewind ();
		DD_Model::query ($tagParser);
		$this->m_eof = false;
	}
	function nextRecord ()
	{
		if ($this->m_eof)
			return false;

		$row = $this->m_iterator->current();
		if (isset ($row))
		{
				/*
				 * This is sort of messed up but we must make an array from
				 * the document object that is a stdclass.
				 */
			$array = [];
			foreach ($row as $key => $object) {
					$array[$key] =  $object; // . ' ' . 
					//var_dump ($object, false);
			}
			DD_Model::nextRecord ();
			parent::setFromArray ($array, null, true);
			try
			{
				$this->m_iterator->next();
			}
			catch (\MongoDB\Driver\Exception\RuntimeException $ex)
			{
				$this->m_eof = true;
			}
			return true;
		}
		return false;
	}
	function freeQuery ()
	{
		$this->m_iterator = null;
		$this->m_cursor = null;
		$this->m_mongoquery = null;
	}
	function execute ($sql, $tagParser = null)
	{

			/*
		$this->m_conn = DD_SqlPoolGetConn ($this->m_connect);
		if (!isset ($this->m_conn))
		{
			die ("DD_SqlModel::execute(): Cannot find connection string" . $this->m_connect);
		}
		if (isset ($tagParser))
		{
			$this->m_conn->execute ($tagParser->parseSql ($sql));
		}
		else
		{
			$this->m_conn->execute ($sql);
		}
			 */
	}
	/**
	 * Execute all the delete statements.  It is assumed that 
	 * all the fields have received there values already by
	 * a call to setFieldsFromArray().  Call setRecordNo to setup
	 * the recordExt for multirecord record handlers first.
	 * See DD_page.php for details.
	 */
	function deleteRecord ($tagParser)
	{
		foreach($this->m_delete as $name=>$statement)
			$this->execute ($statement, $tagParser);
	}
	/**
	 * Execute all the insert statements.  The keysource
	 * properties are processed for each field and all
	 * the inserts are executes.  See deleteRecord Comments for 
	 * multirecord.
	 */
	function insertRecord ($tagParser)
	{
		$this->doKeySources ($tagParser);
		foreach($this->m_insert as $name=>$statement)
			$this->execute ($statement, $tagParser);
	}
	/**
	 * Execute all the update statements. See deleteRecord Comments for 
	 * multirecord.
	 */
	function updateRecord ($tagParser)
	{
		foreach($this->m_update as $name=>$statement)
			$this->execute ($statement, $tagParser);
	}
	function prepareForRecord ($tagParser)
	{
		parent::prepareForRecord ($tagParser);
	}
}
?>
