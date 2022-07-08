<?php
class ADMIN_Page extends DD_Page
{
	function ADMIN_page ($name, $currentMenuName)
	{
		parent::DD_Page ($name);
	}
	function prepare ()
	{
		$as = new DD_ArrayValueProvider ("ai", 
			array (
				"bodyhead"=>$this->makeBodyHead ($currentMenuName), 
				"bodyfoot"=>$this->makeBodyFoot ()
			));
		$this->m_tp->addValueProvider ($as);
		
	}
	function makeMenuLink ($url, $linkname, $currentMenuName)
	{
		$tabtype = "other";
		if (isset ($currentMenuName))
		{
			if ($linkname == $currentMenuName)
			{
				$tabtype = "current";
			}
		}
		
		$text .=  "<a  href='{$url}'>{$linkname}</a>";
		return $text;
	}
	function makeBodyHead ($currentMenuName = null)
	{
		$content =<<<CONTENT
<div id="main_body">
<noscript>
<h3><font color="red">This application needs Javascript enabled.  <br />Please contact your system administrator for instructions<br /> on how to enable Javascript with your browser.</font></h3>
</noscript>
<table cellpadding="0" cellspacing="0" align="center">
<tr><td>
	  <table width="100%" border="0" align="center" cellpadding="3" cellspacing="0">
		<tr>
		<td align="left">Welcome {dd:user.username}</td>
		<td nowrap="nowrap" align="right">
CONTENT;

		$text = $this->m_tp->parseContent ($content);
		$path = $this->m_tp->parseContent ("{dd:dd.path}");
					/**
		 			* WARNING!!!!! IF you change these please change the $currentMenuName 
					* settings in the including files!!!!
		 			*/
		$text .= $this->makeMenuLink ("{$path}index.php", "Home", $currentMenuName);
		$text .= ' | ';
		$text .= $this->makeMenuLink ("{$path}admin/index.php", "Admin Home", $currentMenuName);
		$text .= ' | ';
		$text .= $this->makeMenuLink ("{$path}logout.php", "Logout", $currentMenuName);
		$text .= $this->m_tp->parseContent ('</td></tr><tr><td class="top2" colspan="2"><img src="{dd:dd.logo}" border="0" alt="AgencyInsight" /> </td></tr> <tr><td> Admin Tasks: <a href="{dd:dd.path}admin/user_list.php">Manage Users</a> | 
		<a href="{dd:dd.path}admin/office_list.php">Manage Offices</a> |
		<a href="{dd:dd.path}admin/kpi_group_list.php">KPI Groups</a>
		</td> </tr>	</table><div id="outer2">');
		return $text;
	}

	function makeBodyFoot ()
	{
		return $this->m_tp->parseContent ("<br /><br />{dd:dd.foot}
		</div>
		</td></tr></table>
		</div>");
	}
}
?>
