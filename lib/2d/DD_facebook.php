<?php
/*************************************************************************
** copyright (c) DuckDigit Technologies Inc. 2008 ALL RIGHTS RESERVED
** Author: Ben Balke
** $Header: /home/cvsroot/ai/lib/2d/DD_facebook.php,v 1.1 2016/02/16 21:18:00 secwind Exp $
*************************************************************************/

require_once '2d/DD_tagparser.php'; 
require_once '2d/DD_sql.php';
require_once '2d/DD_domfields.php';
require_once '2d/DD_recordhandlers.php';
//require_once '2d/DD_fb_recordhandlers.php';
require_once 'fb/facebook.php';

define("DD_AUTHORIZED", "DuckDigit.Authorized");
define("DD_LOGINCOUNT", "DuckDigit.LoginCount");
define("DD_USERVP", "user");
define("DD_LOGINSUBMIT", "DD_LOGIN");

class DD_FacebookSession
{
	var			$m_uid;
	var			$m_fbkey;			// Facebook api key
	var			$m_fbsecret;		// Facebook api secret
	var			$m_facebook;		// Facebook Object 
	var			$m_successSql;		// executed after successful m_sql.
	var			$m_denySql;			// executed after unsuccessful m_sql.
	var			$m_sql;
	var			$m_debug;
	var			$m_authrole;
	var			$m_prefix = ''; 	// appended to the session fields for supporting multiple sessions.  If this is set the
									// session is not destroyed.
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
			if (isset ($array['fbkey']))
				$this->m_fbkey = $array ['fbkey'];
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
			if (isset ($array['fbsecret']))
				$this->m_fbsecret = $array ['fbsecret'];
			if (isset ($array['require_https']))
				$this->m_require_https = $array ['require_https'];
		}
		$this->m_isloginpage = $isloginpage;
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
	function doAuth ($tagParser)
	{
		if ($this->m_debug) @error_log (' doAuth ' . $this->m_uid );
		$this->checkHttps ();
		$this->m_facebook = new Facebook ($this->m_fbkey, $this->m_fbsecret);
			/**
			 * Check for the profile session.  It means we are a tab that was added to 
			 * the users profile.  We are already established here and all we 
			 * need to do is call set_user from the POST parameters.
			 */
		if (isset ($_POST) && isset ($_POST['fb_sig_profile_session_key']))
		{
			$session_key = $_POST['fb_sig_profile_session_key'];
			$this->m_uid = $_POST['fb_sig_profile_user'];
			$this->m_facebook->set_user ($this->m_uid, $session_key);
		}
		else
		{
			/**
			 * this is the stand along application.  Require authentication.
			 */
			$this->m_uid = $this->m_facebook->require_login();
		}
		if (isset($_SESSION [$this->m_prefix . DD_AUTHORIZED]))
		{
			if ($this->m_debug) @error_log (' Already Authed ' . $this->m_uid );
		}
		if ($this->m_debug) @error_log (' I AM HERE ' . $this->m_uid );
			/**
			 * If DD_AUTHORIZED is already set then we are authenticated and 
			 * a session is already established.  Simply add the "user" valueprovider
			 * to the tagParser.
			 */
		if (isset($_SESSION [$this->m_prefix . DD_AUTHORIZED]))
		{
			if ($this->m_debug) @error_log ('already: ' . @var_export ($_SESSION [$this->m_prefix . DD_AUTHORIZED], true));
			$tagParser->addValueProvider ($_SESSION [$this->m_prefix . DD_AUTHORIZED]);
			$tagParser->addValueProvider (new DD_ArrayValueProvider ("server", $_SERVER));
		}
		else
		{
			if (isset ($this->m_sql))
			{
				$vp = new DD_ArrayValueProvider ("auth", array ("uid"=>$this->m_uid));
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
					if ($this->debug) @error_log (@var_export ($row, true));
					$uservp = new DD_ArrayValueProvider (DD_USERVP, $row);
						// Add the Facebook user_id.
					$uservp->setValue ('uid', $this->m_uid);
					if (isset ($this->m_authrole))
					{
						if (isset ($row[$this->m_authrole]))
						{
							$uservp->appendDelimitedStringAsFields (
								$row[$this->m_authrole]);
						}
					}
				}
				$conn->freeQuery ($qh);
				$tagParser->addValueProvider ($uservp);
				$tagParser->addValueProvider (new DD_ArrayValueProvider ("server", $_SERVER));
    			$_SESSION[$this->m_prefix . DD_AUTHORIZED] = $uservp;
				if (isset ($this->m_successSql))
				{
					$conn = DD_SqlPoolGetConn ($this->m_connString);
					$conn->execute ($tagParser->parseSql ($this->m_successSql));
				}
			}
			else
			{
				if ($this->m_debug) @error_log ('hey hey');
				$uservp = new DD_ArrayValueProvider (DD_USERVP, array ("uid"=>$this->m_uid,'joke'=>'dog'));
				$tagParser->addValueProvider ($uservp);
				$tagParser->addValueProvider (new DD_ArrayValueProvider ("server", $_SERVER));
    			$_SESSION[$this->m_prefix . DD_AUTHORIZED] = $uservp;
				if ($this->m_debug) @error_log ('Adding: ' . $this->m_uid . ' '  . @var_export ($_SESSION [$this->m_prefix . DD_AUTHORIZED], true));
			}
    	}   	
	}
}
?>
