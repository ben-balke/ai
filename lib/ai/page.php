<?php
require_once "ai/version.php";
require_once "ai/properties.php";
class AI_Page extends DD_Page
{
	var		$m_currentMenuName;
	var		$m_kpiformcontent =
		'<form style="margin: 0px"><input type="hidden" name="id" id="id" value="{dd:@http.id}"><table><tr><td>Performance Indicator</td><td>Date Range ({dd:kpi.begindate} to {dd:kpi.enddate})</td></tr> <tr><td>{dd:kpi.kpi_type} </td><td>{dd:-kpi.daterange}</td></tr></table> </form>';

	function AI_page ($name, $currentMenuName)
	{
		parent::DD_Page ($name);
		$this->m_currentMenuName = $currentMenuName;
	}
	function prepare ()
	{
		$as = new DD_ArrayValueProvider ("ai", 
			array (
				"bodyhead"=>$this->makeBodyHead (), 
				"bodyfoot"=>$this->makeBodyFoot ()
			));
		$this->m_tp->addValueProvider ($as);
		
	}
	function makeMenuLink ($url, $linkname)
	{
		$tabtype = "other";
		if (isset ($this->m_currentMenuName))
		{
			if ($linkname == $this->m_currentMenuName)
			{
				$tabtype = "current";
			}
		}
		
		return "<a  href='{$url}'>{$linkname}</a>";
	}
	function makeBodyHead ()
	{
		global $ai_version;
		global $ai_license;
		$content =<<<CONTENT
<div id="main_body">
<noscript>
<h3><font color="red">This application needs Javascript enabled.  <br />Please contact your system administrator for instructions<br /> on how to enable Javascript with your browser.</font></h3>
</noscript>

<script type="text/javascript">
function selectOffice (id)
{
	f = $('setoffice');
	f.active_office_id.value = id;
	f.submit ();
}
</script>

<form id="setoffice" name="setoffice" method="post" action="{dd:dd.path}widget/set_admin_props.php">
<input type="hidden" name="DD_update" id="DD_update" value="" />
<input type="hidden" name="active_office_id" id="active_office_id" value="" />
<input type="hidden" name="widgetnextpage" id="widgetnextpage" value="{dd:dd.httppath}index.php" />
</form>

<table cellpadding="0" cellspacing="0" align="center">
<tr><td>
	  <table width="100%" border="0" align="center" cellpadding="3" cellspacing="0">
		<tr>
		<td align="left">Agency Insight $ai_version($ai_license). Welcome {dd:user.username}</td>
		<td nowrap="nowrap" align="right">
CONTENT;
		global $AdminProperties;
		$offices = $this->makeField ($AdminProperties['active_office_id'],
    		array ('type'=>'links','onclick'=>"selectOffice ({value});",'name'=>'offices'));
		$offices->prepareForRecord ($this->m_tp);



		$text = $this->m_tp->parseContent ($content);
		$path = $this->m_tp->parseContent ("{dd:dd.path}");
					/**
		 			* WARNING!!!!! IF you change these please change the $currentMenuName 
					* settings in the including files!!!!
		 			*/
		$text .= $this->makeMenuLink ("{$path}index.php", "Home");
		$text .= ' | ';
		$text .= $this->makeMenuLink ("http://www.duckdigit.com/my", "Support");
		$text .= ' | ';
		$text .= $this->makeMenuLink ("{$path}admin/index.php", "Admin");
		$text .= ' | ';
		$text .= $this->makeMenuLink ("{$path}logout.php", "Logout");

		$officeid = $this->m_tp->getValue ('user.active_office_id');
		$officelogo = "office{$officeid}.jpg";
		global $DD_officeImagePath;
		global $DD_officeImageRoot;
		if (@file_exists ($DD_officeImageRoot . $officelogo))
		{
			$custlogo = $this->makeField (array ('name'=>'custlogo', 'type'=>'work',
				'value'=>$DD_officeImagePath . $officelogo));
		}

		$text .= $this->m_tp->parseContent ('
			</td></tr>
			<tr><td class="top2" colspan="2">
				<div style="float: right"><img src="{dd:-page.custlogo}" border="0" alt="Customer Logo"/></div>
				<a href="{dd:dd.path}"><img src="{dd:dd.logo}" border="0" alt="Agency Insight" /></a></td></tr> 
			<tr><td colspan="2" nowrap="nowrap"> 
<ul id="sddm">
    <li><a href="#" onmouseover="mopen(\'office\')" onmouseout="mclosetime()">Offices</a>
        <div id="office" onmouseover="mcancelclosetime()" onmouseout="mclosetime()">
        	<a class="separator" href="{dd:dd.path}index.php">Home</a>
			{dd:page.offices}
        </div>
    </li>
    <li><a href="#" onmouseover="mopen(\'book\')" onmouseout="mclosetime()">Book</a>
        <div id="book" onmouseover="mcancelclosetime()" onmouseout="mclosetime()">
        	<a href="{dd:dd.path}querybyprod.php">By Producer</a>
        	<a href="{dd:dd.path}querybycsr.php">By Servicer</a>
        	<a href="{dd:dd.path}querybypayee.php">By Payee</a>
        	<a href="{dd:dd.path}querybyins.php">By Insuror</a>
        	<a href="{dd:dd.path}querybysicmajor.php">By Sic Code</a>
        	<a href="{dd:dd.path}querybycov.php">By Coverage</a>
        	<a href="{dd:dd.path}querybystate.php">By State</a>
        	<a href="{dd:dd.path}querybylob.php">By Performance Groups</a>
        </div>
    </li>
    <li><a href="#" onmouseover="mopen(\'performance\')" onmouseout="mclosetime()">Performance</a>
        <div id="performance" onmouseover="mcancelclosetime()" onmouseout="mclosetime()">
        	<a href="{dd:dd.path}kpi/querybyprod.php?daterange=mtd&kpi_type=N">By Producer</a>
        	<a href="{dd:dd.path}kpi/querybycsr.php?daterange=mtd&kpi_type=N">By Servicer</a>
        	<a href="{dd:dd.path}kpi/querybypayee.php?daterange=mtd&kpi_type=N">By Payee</a>
        	<a href="{dd:dd.path}kpi/querybyins.php?daterange=mtd&kpi_type=N">By Insuror</a>
        	<a href="{dd:dd.path}kpi/querybysicmajor.php?daterange=mtd&kpi_type=N">By Sic Code</a>
        	<a href="{dd:dd.path}kpi/querybycov.php?daterange=mtd&kpi_type=N">By Coverage</a>
        	<a href="{dd:dd.path}kpi/querybystate.php?daterange=mtd&kpi_type=N">By State</a>
        	<a href="{dd:dd.path}kpi/querybylob.php?daterange=mtd&kpi_type=N">By Performance Groups</a>
        </div>
    </li>
    <li><a href="#" onmouseover="mopen(\'m3\')" onmouseout="mclosetime()">Browse</a>
        <div id="m3" onmouseover="mcancelclosetime()" onmouseout="mclosetime()">
				<a href="{dd:dd.path}customerlist.php">Customers</a>
				<a href="{dd:dd.path}policylist.php">Policies</a>
        </div>
    </li>
    <li><a href="#" onmouseover="mopen(\'m4\')" onmouseout="mclosetime()">Configure</a>
        <div id="m4" onmouseover="mcancelclosetime()" onmouseout="mclosetime()">
        	<a href="#" class="separator">My Preferences</a>
        	{dd:-user.admin}<a href="{dd:dd.path}admin/office_list.php">Offices</a>
        	{dd:-user.admin}<a href="{dd:dd.path}admin/user_list.php">Users</a>
        	{dd:-user.admin}<a href="{dd:dd.path}admin/kpi_group_list.php">Performance Groups</a>
        </div>
    </li>
    <li><a href="#" onmouseover="mopen(\'help\')" onmouseout="mclosetime()">Help</a>
        <div id="help" onmouseover="mcancelclosetime()" onmouseout="mclosetime()">
        	<a href="http://www.duckdigit.com/my" class="separator" target="_blank">Support</a>
        	<a href="#">Agency Insight Version ' . $ai_version . '</a>
        </div>
    </li>
    <li><a href="{dd:dd.path}logout.php">Logout</a></li>
</ul>
<div style="clear:both"></div>
			</td> </tr>	
			</table>
			<div id="outer2">');
		return $text;
	}

	function makeBodyFoot ()
	{
		return $this->m_tp->parseContent ("<br /><br />{dd:dd.foot}
		</div>
		</td></tr></table>
		</div>");
	}

	public function prepareKpiForm ()
	{
		if (isset ($_GET ['kpi_type']))
		{
			$dateoptions = array (
				'fytd'=>'This Fiscal Year to Date'
				,'ytd'=>'This Year to Date' 
				,'mtd'=>'This Month to Date' 
				,'y'=>'This Year' 
				,'m'=>'This Month' 
				,'yd'=>'Yesterday' 
				,'lm'=>'Last Month' 
				,'lfy'=>'Last Fiscal Year' 
				,'lfytd'=>'Last Fiscal Year to Date' 
				,'lytd'=>'Last Year to Date' 
				,'ly'=>'Last Year' 
				,'r12'=>'Rolling 12' 
				);
			$kpioptions = array (
				'N'=>'New Business'
				,'L'=>'Lost Business' 
				,'R'=>'Renewal Business'
				);
			$kpiclauses = array (
				'N'=>"trantype = 'N'"
				,'L'=> "(trantype = 'L' or trantype = 'C')"
				,'R'=>"trantype = 'R'"
				);

			$select = "select {http.daterange} as daterange ,{http.kpi_type} as kpi_type ";
			switch ($_GET ['daterange'])
			{
			case 'fytd': 
				$select .= ", ai_current_year_start ({user.active_office_id}, true) as begindate
						, date_trunc ('day', now ()) as enddate";
				break;
			case 'ytd':
				$select .= ", ai_current_year_start ({user.active_office_id}, false) as begindate
						, date_trunc ('day', now ()) as enddate";
				break;
			case 'mtd':
				$select .= " , date_trunc ('month', now ()) as begindate
						, date_trunc ('day', now ()) as enddate";
				break;
			case 'yd':
				$select .= " , now () - interval '1 day'  as begindate
						, now () - interval '1 day' as enddate";
				break;
			case 'y':
				$select .= " , ai_current_year_start ({user.active_office_id}, false) as begindate
						, ai_current_year_end ({user.active_office_id}, false) as enddate";
				break;
			case 'm':
				$select .= ", date_trunc ('month', now ()) as begindate
						, date_trunc ('month', now ()) + interval '1 month' - interval '1 day' as enddate";
				break;
			case 'lm':
				$select .= ", date_trunc ('month', now ()) - interval '1 month' as begindate
						, date_trunc ('month', now ()) - interval '1 day' as enddate";
				break;
			case 'ly':
				$select .= ", ai_current_year_start ({user.active_office_id}, false) - interval '1 year' as begindate
						, ai_current_year_end ({user.active_office_id}, false) - interval '1 year' as enddate";
				break;

			case 'lfy':
				$select .= ", ai_current_year_start ({user.active_office_id}, true) - interval '1 year' as begindate
						, ai_current_year_end ({user.active_office_id}, true) - interval '1 year' as enddate";
				break;
			case 'lfytd':
				$select .= ", ai_current_year_start ({user.active_office_id}, true) - interval '1 year' as begindate
						, date_trunc ('day', now ()) - interval '1 year' as enddate";
				break;
			case 'lytd':
				$select .= ", ai_current_year_start ({user.active_office_id}, false) - interval '1 year' as begindate
						, date_trunc ('day', now ()) - interval '1 year' as enddate";
				break;
			case 'r12':
				$select .= ", date_trunc ('month', now ()) - interval '12 month' as begindate
						, date_trunc ('month', now ()) - interval '1 day' as enddate";
				break;
			default:
				return;
			}
			$rh = new DD_SqlRecordHandler (array (
				'name'=>'kpi'
				,'connect'=>'sql.ai'
				,'select'=>$select
			));
			$this->addRecordHandler ($rh);
			$rh->makeField (array ('name'=>'begindate','datatype'=>'date'));
			$rh->makeField (array ('name'=>'enddate','datatype'=>'date'));
			$rh->makeField (array ('name'=>'daterange','type'=>'select',
				'listsource'=>new DD_ArrayListSource ($dateoptions), 'html'=>'onchange="this.form.submit ();"'));
			$rh->makeField (array ('name'=>'kpi_type','type'=>'select',
				'listsource'=>new DD_ArrayListSource ($kpioptions), 'html'=>'onchange="this.form.submit ();"'));
			$clause = $rh->makeField (array ('name'=>'kpi_clause'));
			$desc = $rh->makeField (array ('name'=>'kpi_desc'));
			$daterange_desc = $rh->makeField (array ('name'=>'kpi_daterange_desc'));

			$rh->prepareForRecord ($this->m_tp);
			$rh->query ($this->m_tp);
			$rh->nextRecord ();
			$desc->set   ($kpioptions [$_GET ['kpi_type']]);
			$clause->set ($kpiclauses [$_GET ['kpi_type']]);
			$daterange_desc->set ($dateoptions [$_GET ['daterange']]);
			$rh->freeQuery ();
		}
		else
		{
			//die ("No kpi type");
		}
	}
}
?>
