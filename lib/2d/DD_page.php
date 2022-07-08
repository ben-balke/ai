<?php
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/lib/2d/DD_page.php,v 1.1 2016/02/16 21:18:00 secwind Exp $
*************************************************************************/

require_once '2d/DD_tagparser.php'; 
require_once '2d/DD_sql.php';
require_once '2d/DD_domfields.php';
require_once '2d/DD_model.php';
require_once '2d/DD_valueproviderglyph.php';

define("DD_POSTLOGINPATH", "DuckDigit.LoginPath");
define("DD_AUTHORIZED", "DuckDigit.Authorized");
define("DD_LOGINCOUNT", "DuckDigit.LoginCount");
define("DD_USERVP", "user");
define("DD_LOGINSUBMIT", "DD_LOGIN");
define("DD_REFERER", "DuckDigit.Referer.");
define("DD_REFERER_ARG", "dd_referer");
define('DD_INSERT',1);
define('DD_UPDATE',2);
define('DD_DELETE',3);

class DD_Session
{
	var			$m_type;
	var			$m_connString;
	var			$m_sql;				// validates and loads session
	var			$m_sqlbyid;			// Loads the session from id.
	var			$m_successSql;		// executed after successful m_sql.
	var			$m_denySql;			// executed after unsuccessful m_sql.
	var			$m_debug;
	var			$m_authrole;
	var			$m_loginpageurl;  	// Page to redirect to login with.
	var			$m_postloginurl;  	// Page to redirect to after login is successful.  This is only used if we
								  	// Don't get redirected from a protected page (ie when the index page has
									// a login box).
	var			$m_isloginpage; 	// true if we are currently processing the login page.
	var			$m_prefix = ''; 	// appended to the session fields for supporting multiple sessions.  If this is set the
									// session is not destroyed.
	var			$m_realm = 'Authentication Required';
									// Realm for Basic authentication only.
	var			$m_require_https = false; 	// If set to true then you can only access these pages from https.
	/**
	 * Provide a connection string and a sql statement in the style
	 * select * from authtable where username = {auth.username} and password = {auth.password}
	 * If this is the login page then set isloginpage to true;
	 */
	function __construct ($array = null, $isloginpage = false)
	{
		if (!isset ($array))
		{
			$this->m_type = 'none';
		}
		else
		{
			if (isset ($array['prefix']))
				$this->m_prefix = $array ['prefix'];
			if (isset ($array['type']))
				$this->m_type = $array ['type'];
			if (isset ($array['connect']))
				$this->m_connString = $array ['connect'];
			if (isset ($array['sql']))
				$this->m_sql = $array ['sql'];
			if (isset ($array['sqlbyid']))
				$this->m_sqlbyid = $array ['sqlbyid'];
			if (isset ($array['debug']))
				$this->m_debug = $array ['debug'];
			if (isset ($array['successSql']))
				$this->m_successSql = $array ['successSql'];
			if (isset ($array['denySql']))
				$this->m_denySql = $array ['denySql'];
			if (isset ($array['authrole']))
				$this->m_authrole = $array ['authrole'];
			if (isset ($array['loginpageurl']))
				$this->m_loginpageurl = $array ['loginpageurl'];
			if (isset ($array['postloginurl']))
				$this->m_postloginurl = $array ['postloginurl'];
			if (isset ($array['realm']))
				$this->m_realm = $array ['realm'];
			if (isset ($array['require_https']))
				$this->m_require_https = $array ['require_https'];
		}
		$this->m_isloginpage = $isloginpage;
	}

	function doAuth ($tagParser)
	{
		if (isset ($this->m_type))
		{
			if ($this->m_type == "basic")
			{
				$this->doBasicAuth ($tagParser);
			}
			else if ($this->m_type == "html")
			{
				$this->doHtmlAuth ($tagParser);
			}
			else if ($this->m_type == "none")
			{
				$this->doNoAuth ($tagParser);
			}
		}
	}
	function doNoAuth ($tagParser)
	{
		$tagParser->addValueProvider (new DD_ArrayValueProvider ("server", $_SERVER));
	}
	function checkHttps ()
	{
		if ($this->m_require_https == true && !isset ($_SERVER ['HTTPS']))
		{
        	header("HTTP/1.0 403 Forbidden");
        	exit;
		}
		return true;
	}
	/**
	 * Performs basic authentication on using the provided sql statement.
	 * If the authentication succeeds or the user is already authenticated,
	 * the "user" value provider is added to the $tagParser.
	 */
	function doBasicAuth ($tagParser)
	{
		$this->checkHttps ();
		if (session_id() == "") 
		{ 
			session_start(); 
		} 
    	if (!isset($_SERVER['PHP_AUTH_USER'])) 
    	{
        	header("WWW-Authenticate: Basic realm=\"$this->m_realm\"");
        	header("HTTP/1.0 401 Unauthorized");
        	// only reached if authentication fails
        	exit;
    	} 
    	else 
    	{
			/**
			 * If DD_AUTHORIZED is already set then we are authenticated and 
			 * a session is already established.  Simply add the "user" valueprovider
			 * to the tagParser.
			 */
			if (isset($_SESSION [$this->m_prefix . DD_AUTHORIZED]))
			{
				if ($this->m_debug) @error_log (@var_export ($_SESSION [$this->m_prefix . DD_AUTHORIZED], true));
				$tagParser->addValueProvider ($_SESSION [$this->m_prefix . DD_AUTHORIZED]);
				$tagParser->addValueProvider (new DD_ArrayValueProvider ("server", $_SERVER));
			}
			else
			{
				$vp = new DD_ArrayValueProvider ("auth", array ("username"=>$_SERVER['PHP_AUTH_USER'],
					"password"=>$_SERVER['PHP_AUTH_PW']));
				$tp = new DD_TagParser ();
				$tp->addValueProvider ($vp);
				$parsedSql = $tp->parseSql ($this->m_sql);
				if ($this->m_debug) @error_log ("DD_auth::doBasicAuth(): parsedSql: " . $parsedSql);

				$conn = DD_SqlPoolGetConn ($this->m_connString);
				if (!isset ($conn))
				{
        			header("HTTP/1.0 500 Server Error");
    				die ('Sql Connection Authorize Error');
				}

				$qh = $conn->query ($parsedSql);
				if (!isset ($qh))
				{
        			header("HTTP/1.0 500 Server Error");
    				die ('Sql Authorize Error');
				}
				$row = $conn->nextNamedRecord ($qh);
				if (isset ($row))
				{
					if ($this->m_debug) @error_log (@var_export ($row, true));
					$uservp = new DD_ArrayValueProvider (DD_USERVP, $row);
					if (isset ($this->m_authrole))
					{
						if (isset ($row[$this->m_authrole]))
						{
							$uservp->appendDelimitedStringAsFields (
								$row[$this->m_authrole]);
						}
					}
					$tagParser->addValueProvider ($uservp);
					$tagParser->addValueProvider (new DD_ArrayValueProvider ("server", $_SERVER));
    				$_SESSION[$this->m_prefix . DD_AUTHORIZED] = $uservp;
					if (isset ($this->m_successSql))
					{
						$conn->execute ($tagParser->parseSql ($this->m_successSql));
					}
				}
				else
				{
        			header("WWW-Authenticate: Basic realm=\"$this->m_realm\"");
        			header("HTTP/1.0 401 Unauthenticated.InvalidUser");
    				die ('Invalid Authorization');
				}
				$conn->freeQuery ($qh);
			}
    	}   	
	}
	/**
	 * Performs HTML Page authentication using the provided sql statement.
	 * If the authentication succeeds or the user is already authenticated,
	 * the "user" value provider is added to the $tagParser.
	 */
	function doHtmlAuth ($tagParser)
	{
		$this->checkHttps ();
		if (session_id() == "") 
		{ 
			session_start(); 
		} 
		/**
		 * If DD_AUTHORIZED is already set then we are authenticated and 
		 * a session is already established.  Simply add the "user" valueprovider
		 * to the tagParser.
		 */
		if (isset($_SESSION [$this->m_prefix . DD_AUTHORIZED]))
		{
			if ($this->m_debug) @error_log (@var_export ($_SESSION [$this->m_prefix . DD_AUTHORIZED], true));
			$tagParser->addValueProvider ($_SESSION [$this->m_prefix . DD_AUTHORIZED]);
			$tagParser->addValueProvider (new DD_ArrayValueProvider ("server", $_SERVER));
			if ($this->m_isloginpage)
			{
					/* 
					 * If we end up back at the login page and we are already
					 * authorized then try to goto the post login page.
					 */
				$this->gotoPostLoginSuccess ();
			}
			$this->saveReferer ();
		}
		else
		{
			if (!$this->m_isloginpage)
			{
				if ($this->m_debug) @error_log ('Establish postloginpath: ' . 
					$_SESSION [$this->m_prefix . DD_POSTLOGINPATH]);
				$_SESSION [$this->m_prefix . DD_POSTLOGINPATH] = $_SERVER ['REQUEST_URI'];
				$_SESSION [$this->m_prefix . DD_LOGINCOUNT] = 0;
				$newLoc = 'Location: ' . $this->m_loginpageurl;
				if ($this->m_debug) @error_log ("DD_auth::doHtmlAuth(): not the login page redirecting: " . 
					$newLoc . " Saving: ". $_SESSION [$this->m_prefix . DD_POSTLOGINPATH]);
				header($newLoc);
    			exit (0);
			}
			if ($this->m_debug) @error_log ("DD_LOGIN: " . $_POST[DD_LOGINSUBMIT]);
			if (isset ($_POST[DD_LOGINSUBMIT]))
			{
				$vp = new DD_ArrayValueProvider ("auth", array ("username"=>$_POST['DD_USERNAME'],
					"password"=>$_POST['DD_PASSWORD']));
				$tp = new DD_TagParser ();
				$tp->addValueProvider ($vp);
				$parsedSql = $tp->parseSql ($this->m_sql);

				if ($this->m_debug) @error_log ("DD_auth::doHtmlAuth(): parsedSql: " . $parsedSql);

				$conn = DD_SqlPoolGetConn ($this->m_connString);
				if (!isset ($conn))
				{
        			header("HTTP/1.0 500 Server Error");
    				die ('Sql Connection Authorize Error');
				}

				$qh = $conn->query ($parsedSql);
				if (!isset ($qh))
				{
        			header("HTTP/1.0 500 Server Error");
    				die ('Sql Authorize Error');
				}
				$row = $conn->nextNamedRecord ($qh);
				if (isset ($row))
				{
					if ($this->m_debug) @error_log (@var_export ($row, true));
					$uservp = new DD_ArrayValueProvider (DD_USERVP, $row);
					if (isset ($this->m_authrole))
					{
						if (isset ($row[$this->m_authrole]))
						{
							$uservp->appendDelimitedStringAsFields (
								$row[$this->m_authrole]);
						}
					}
					$tagParser->addValueProvider ($uservp);
					$tagParser->addValueProvider (new DD_ArrayValueProvider ("server", $_SERVER));
    				$_SESSION[$this->m_prefix . DD_AUTHORIZED] = $uservp;
					if (isset ($this->m_successSql))
					{
						$conn->execute ($tagParser->parseSql ($this->m_successSql));
					}
					$conn->freeQuery ($qh);
					if (!$this->gotoPostLoginSuccess ())
					{
        				header("HTTP/1.0 500 Server Error");
    					die ('DD_session: doHtmlAuth: NO POST LOGIN URL is provided');
					}
					exit (0);
				}
				else
				{
					$_SESSION [$this->m_prefix . DD_LOGINCOUNT] += 1;
				}
				$conn->freeQuery ($qh);
			}
    	}   	
	}
	function getInvalidLoginAttempts ()
	{
		return $_SESSION [$this->m_prefix . DD_LOGINCOUNT];
	}
	function clearInvalidLoginAttempts ()
	{
		$_SESSION [$this->m_prefix . DD_LOGINCOUNT] = 0;
	}
	function gotoPostLoginSuccess ()
	{
		if (isset ($_SESSION [$this->m_prefix . DD_POSTLOGINPATH]))
		{
			if ($this->m_debug) @error_log ("Using Post login path " .  $_SESSION [$this->m_prefix . DD_POSTLOGINPATH]);
				header ('Location: ' . $_SESSION [$this->m_prefix . DD_POSTLOGINPATH]);
			unset ($_SESSION [$this->m_prefix . DD_POSTLOGINPATH]);
		}
		else if (isset ($this->m_postloginurl))
		{
			if ($this->m_debug) @error_log ("no post login path using " .  $this->m_postloginurl);
				header('Location: ' . $this->m_postloginurl);
		}
		else
			return false;
		return true;
	}
	function logout ()
	{
		if (session_id() == "") 
		{ 
			session_start(); 
		} 
		if (isset ($this->m_prefix))
		{
    		unset ($_SESSION[$this->m_prefix . DD_AUTHORIZED]);
		}
		else
		{
			session_unset(); 
			session_destroy(); 
		}
	}
		/*
		 * This function loads the session by ID.  It populates the 
		 * valueprovider and sets the session up to reflect a user
		 * that has logged in.
		 */
	function loadSessionById ($id, $tagParser = null)
	{
		$rslt = false;
		if (session_id() == "") 
		{ 
			session_start(); 
		} 
		$vp = new DD_ArrayValueProvider ("auth", array ("id"=>$id));
		$tp = new DD_TagParser ();
		$tp->addValueProvider ($vp);
		$parsedSql = $tp->parseSql ($this->m_sqlbyid);

		if ($this->m_debug) @error_log ("DD_auth::loadSessionById(): parsedSql: " . $parsedSql);

		$conn = DD_SqlPoolGetConn ($this->m_connString);
		if (!isset ($conn))
		{
      			header("HTTP/1.0 500 Server Error");
   			die ('Sql Connection Authorize Error');
		}

		$qh = $conn->query ($parsedSql);
		if (!isset ($qh))
		{
       		header("HTTP/1.0 500 Server Error");
    		die ('Sql Authorize Error');
		}
		$row = $conn->nextNamedRecord ($qh);
		if (isset ($row))
		{
			if ($this->m_debug) @error_log (@var_export ($row, true));
			$uservp = new DD_ArrayValueProvider (DD_USERVP, $row);
			if (isset ($this->m_authrole))
			{
				if (isset ($row[$this->m_authrole]))
				{
					$uservp->appendDelimitedStringAsFields (
						$row[$this->m_authrole]);
				}
			}
			if (isset ($tagParser))
			{
				$tagParser->addValueProvider ($uservp);
				$tagParser->addValueProvider (new DD_ArrayValueProvider ("server", $_SERVER));
			}
    		$_SESSION[$this->m_prefix . DD_AUTHORIZED] = $uservp;
			$conn->freeQuery ($qh);
			$rslt = true;
		}
		return $rslt;
	}
	function reloadUserById ($tagParser)
	{
		$rslt = false;
		$conn = DD_SqlPoolGetConn ($this->m_connString);
		if (!isset ($conn))
		{
      			header("HTTP/1.0 500 Server Error");
   			die ('Sql Connection Authorize Error');
		}
		$uservp = $tagParser->getValueProvider (DD_USERVP);
		if (!isset ($uservp))
		{
			die ('Cannot locate uservp');
		}
		$vp = new DD_ArrayValueProvider ("auth", array ("id"=>$uservp->getValue ('id')));
		$tp = new DD_TagParser ();
		$tp->addValueProvider ($vp);
		$parsedSql = $tp->parseSql ($this->m_sqlbyid);

		$qh = $conn->query ($parsedSql);
		if (!isset ($qh))
		{
       		header("HTTP/1.0 500 Server Error");
    		die ('Sql Authorize Error');
		}
		$row = $conn->nextNamedRecord ($qh);
		if (isset ($row))
		{
			foreach ($row as $name=>$value)
			{
				$uservp->setValue ($name, $row [$name]);
			}
			$conn->freeQuery ($qh);
			$rslt = true;
		}
		return $rslt;
	}
	// Save the referer into the session if dd_referer is set
	function saveReferer ()
	{
		if (isset ($_GET[DD_REFERER_ARG]))
			$referer = $_GET[DD_REFERER_ARG];
		else if (isset ($_POST[DD_REFERER_ARG])) 
			$referer = $_POST[DD_REFERER_ARG];
		if (isset ($referer) && isset ($_SERVER ["HTTP_REFERER"]))
		{
    		$_SESSION[$this->m_prefix . DD_REFERER . $referer] = $_SERVER ["HTTP_REFERER"];
			if ($this->m_debug) @error_log ("Saving Referer " . $referer . " as " . $_SERVER ["HTTP_REFERER"]);
			
		}
	}
	// Retreive and clear any referer.  If they have two sessions doing the same thing then we are 
	// in trouble.  We probably need some type of push pop put that is even messed up based on an
	// overlapping example
	function getReferer ($referer)
	{
		$sessionvar = $this->m_prefix . DD_REFERER . $referer;
		if (isset ($_SESSION[$sessionvar]))
		{
    		$refererurl = $_SESSION[$sessionvar];
			unset ($_SESSION[$sessionvar]);
			return $refererurl;
		}
		return null;
	}
	function addSessionVar ($name, $value)
	{
    	$_SESSION[$this->m_prefix . $name] = $value;
	}
	function removeSessionVar ($name)
	{
    	unset ($_SESSION[$this->m_prefix . $name]);
	}
	function getSessionVar ($name)
	{
		if (array_key_exists ($this->m_prefix . $name, $_SESSION))
		{
			return $_SESSION[$this->m_prefix . $name];
		}
		return null;
	}
	function addUserVar ($name, $value)
	{
    	$uservp = $_SESSION[$this->m_prefix . DD_AUTHORIZED];
    	$uservp->setValue ($name, $value);
	}
	function removeUserVar ($name)
	{
    	$uservp = $_SESSION[$this->m_prefix . DD_AUTHORIZED];
    	$uservp->setValue ($name, null);
	}
	function getUserVar ($name)
	{
		if (array_key_exists ($this->m_prefix . DD_AUTHORIZED, $_SESSION))
		{
    		$uservp = $_SESSION[$this->m_prefix . DD_AUTHORIZED];
			if (isset ($uservp))
			{
				return $uservp->getValue ($name);
			}
		}
		return null;
	}
}

class DD_Page extends DD_FieldValueProvider
{
	var			$m_authpolicy;
	var			$m_models;
	var			$m_formFields;
	var			$m_tp;

	function __construct ($name, $noheaders = false, $userequest = false)
	{
		parent::__construct ($name);
		if (!$noheaders)
		{
            //header("Last-Modified: " . gmdate("D, d M Y H:i:s") . " GMT"); // always modified
            //header("Cache-Control: private, no-store, no-cache, must-revalidate, max-stale=0, max-age=0"); // HTTP/1.1
            //header("Cache-Control: post-check=0, pre-check=0", false);
            //header("Pragma: no-cache"); // HTTP/1.0
            //header("Keep-Alive: timeout=3, max=993"); // HTTP/1.0
            //header("Expires: Mon, 26 Jul 1997 05:00:00 GMT"); // Date in the past

			header("Cache-Control: no-cache, must-revalidate"); // HTTP/1.1
			header("Expires: Sat, 26 Jul 1997 05:00:00 GMT"); // Date in the past
			Header('Pragma: no-cache');

		}
		$this->m_tp = new DD_TagParser ();
		global $DD_params;
		if (isset ($DD_params))
		{
			$this->m_tp->addValueProvider (new DD_ArrayValueProvider ("dd", $DD_params));
		}
		$this->m_models = array ();
		if ($userequest == false)
		{
			switch ($_SERVER['REQUEST_METHOD'])
			{
			case "POST":
				$this->m_tp->addValueProvider (new DD_ArrayValueProvider ("http", $_POST));
				break;
			case "GET":
				$this->m_tp->addValueProvider (new DD_ArrayValueProvider ("http", $_GET));
				break;
			}
		}
		else
		{
			$this->m_tp->addValueProvider (new DD_ArrayValueProvider ("http", $_REQUEST));
		}
		$this->m_tp->addValueProvider ($this);
		$this->m_tp->addValueProvider (new DD_GlyphValueProvider);
	}
	function verifyRoles ($requiredRoles, $message)
	{
		$uservp = $this->m_tp->getValueProvider ("user");
		if (isset ($uservp))
		{
			$roles = @explode (',', $requiredRoles);
			foreach ($roles as $id=>$name)
			{
				$role = $uservp->getValue ($name, MODE_NORMAL);
				if (isset ($role))
				{
					return true;
				}
			}
		}
		global $DD_params;
		if (isset ($DD_params) && isset ($DD_params['noaccessurl']))
		{
			$header = 'Location: ' . $DD_params ['noaccessurl'] . '?message=' . urlencode ($message);
		}
		else
		{
			$header = 'HTTP/1.1 403 Forbidden';
		}
		header($header);
		exit;
	}
	/**
	 * The page was not found sed 401
	 */
	function notFound ()
	{
		$header = 'HTTP/1.1 404 Not Found';
		header($header);
		exit;
	}
	function addModel ($mod)
	{
		$this->m_models [$mod->m_name] = $mod;
		$this->m_tp->addValueProvider ($mod);
	}

	function outputContent ($text, $editmode = true)
	{
		if ($editmode == true)
		{
			print $this->m_tp->parseContent ($text);
		}
		else
		{
			$this->m_tp->setMode (MODE_VALUE);
			print $this->m_tp->parseContent ($text);
			$this->m_tp->restoreMode ();
		}
	}

	function prepareForRecord ($tg = null)
	{
		parent::prepareForRecord ($this->m_tp);
	}
	/** 
	 * process all the models for a post.  The action is determined based on the 
	 * disposition of the DD_insert, DD_update, and DD_delete POST parameter.  If DD_records is
	 * set then a multirecord form is assumed and the recordExtension facility is used
	 * to set the field values from the numbered post paremeters.  In a multi record situation, the
	 * models are processed sequentially for all records in the order they are added to the page. 
	 * This means that different models will not have access to the others fields values in sequence.
	 */
	function processPost ()
	{
		if (isset ($_POST['DD_insert']))
			$type = DD_INSERT;
		else if (isset ($_POST['DD_update']))
			$type = DD_UPDATE;
		else if (isset ($_POST['DD_delete']))
			$type = DD_DELETE;
		else return;
		
		if (isset ($_POST['DD_records']))
		{
			$rcds = $_POST['DD_records'];
			foreach($this->m_models as $name=>$mod)
			{
				if ($mod->m_multirecord)
				{
					for ($r = 1; $r <= $rcds; $r++)
					{
						$mod->setRecordNo ($r);
	    				$mod->setFieldsFromArray ($_POST, false);
						if (!$mod->isMultiRecordSkipped ())
						{
							switch ($type)
							{
							case DD_INSERT: $mod->insertRecord ($this->m_tp); break;
							case DD_UPDATE: $mod->updateRecord ($this->m_tp); break;
							case DD_DELETE: $mod->deleteRecord ($this->m_tp); break;
							}
						}
					}
				}
				else
				{
	    			$mod->setFieldsFromArray ($_POST, false);
					switch ($type)
					{
					case DD_INSERT: $mod->insertRecord ($this->m_tp); break;
					case DD_UPDATE: $mod->updateRecord ($this->m_tp); break;
					case DD_DELETE: $mod->deleteRecord ($this->m_tp); break;
					}
				}
			}
		}
		else
		{
			foreach($this->m_models as $name=>$mod)
			{
	    		$mod->setFieldsFromArray ($_POST, false);
			}
			switch ($type)
			{
			case DD_INSERT: $this->doInsert (); break;
			case DD_UPDATE: $this->doUpdate (); break;
			case DD_DELETE: $this->doDelete (); break;
			}
		}
		return $type;
	}
	function doInsert ()
	{
		foreach($this->m_models as $name=>$mod) 
		{ 
			$mod->insertRecord ($this->m_tp); 
		} 
	}
	function doUpdate () 
	{ 
		foreach($this->m_models as $name=>$mod) 
		{ 
			$mod->updateRecord ($this->m_tp);
		}
	}
	function doDelete ()
	{
		foreach($this->m_models as $name=>$mod)
		{
			$mod->deleteRecord ($this->m_tp);
		}
	}
	function addValueProviderFromSql ($name, $connString, $sql)
	{
		$vp = null;
		$conn = DD_SqlPoolGetConn ($connString);
		if (!isset ($conn))
		{
        	header("HTTP/1.0 500 Server Error");
    		die ('Sql Connection Authorize Error');
		}

		$qh = $conn->query ($this->m_tp->parseSql ($sql));
		if (!isset ($qh))
		{
       		header("HTTP/1.0 500 Server Error");
    		die ('Sql Query Error');
		}
		$row = $conn->nextNamedRecord ($qh);
		if (isset ($row))
		{
			$vp = new DD_ArrayValueProvider ($name, $row);
			$this->m_tp->addValueProvider ($vp);
		}
		$conn->freeQuery ($qh);
		return $vp;
	}
	function executeSql ($connString, $sql)
	{
		$conn = DD_SqlPoolGetConn ($connString);
		if (!isset ($conn))
		{
        	header("HTTP/1.0 500 Server Error");
    		die ('Sql Connection Authorize Error');
		}
		$conn->execute ($this->m_tp->parseSql ($sql));
	}
	function getValue ($name, $mode = MODE_VALUE, $tagParser = null)
	{
		if ($name == 'dd_milliseconds')
		{
			return @round(@microtime(true) * 1000);
		}
		return parent::getValue ($name, $mode, $tagParser);
	}

	function flush ()
	{
		ob_flush ();
		flush ();
	}

	function flushAll()
	{
		ob_flush ();
		sleep (2);
		flush ();
		ob_end_flush ();
	}
}
?>
