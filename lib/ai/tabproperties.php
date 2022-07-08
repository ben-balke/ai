<?php
class TabProperties
{
	static $s_tabs = array (
		'producer'=>array 
		(
			'itemid_column'=>'p.producer_id',
			'filter_arg'=>'producer_id',
			'code_column'=>'s.code',
			'name_column'=>'s.name',
			'join'=>'left outer join cd_staff s on (s.office_id = p.office_id and s.id = p.producer_id)',
			'tabname'=>'Producers',
			'link'=>'queryforprod.php',
		),
		'servicer'=>array 
		(
			'itemid_column'=>'p.servicer_id',
			'filter_arg'=>'servicer_id',
			'code_column'=>'s.code',
			'name_column'=>'s.name',
			'join'=>'left outer join cd_staff s on (s.office_id = p.office_id and s.id = p.servicer_id)',
			'tabname'=>'Servicers',
			'link'=>'queryforcsr.php',
		),
		'payee'=>array 
		(
			'itemid_column'=>'p.payee_id',
			'filter_arg'=>'payee_id',
			'code_column'=>'i.code',
			'name_column'=>'i.name',
			'join'=>'left outer join cd_insuror i on (i.office_id = p.office_id and i.id = p.payee_id)',
			'tabname'=>'Payees',
			'link'=>'queryforpayee.php',
		),
		'insuror'=>array 
		(
			'itemid_column'=>'p.insuror_id',
			'filter_arg'=>'insuror_id',
			'code_column'=>'i.code',
			'name_column'=>'i.name',
			'join'=>'left outer join cd_insuror i on (i.office_id = p.office_id and i.id = p.insuror_id)',
			'tabname'=>'Insurors',
			'link'=>'queryforins.php',
		),
		'sicmajor'=>array 
		(
			'itemid_column'=>'p.sicmajor',
			'filter_arg'=>'sicmajor',
			'code_column'=>'p.sicmajor',
			'name_column'=>'s.name',
			'join'=>'left outer join cd_sicmajor s on (s.id = p.sicmajor)',
			'tabname'=>'SIC Codes',
			'link'=>'queryforsicmajor.php',
		),
		/*'siccode'=>array 
		(
			'itemid_column'=>'p.siccode',
			'filter_arg'=>'siccode',
			'code_column'=>'p.siccode',
			'name_column'=>'s.name',
			'join'=>'left outer join cd_siccode s on (s.id = p.siccode)',
			'tabname'=>'SIC Codes',
			'link'=>'queryforsic.php',
		),*/
		'coverage'=>array 
		(
			'itemid_column'=>'p.coverage_id',
			'filter_arg'=>'coverage_id',
			'code_column'=>'c.code',
			'name_column'=>'c.name',
			'join'=>'left outer join cd_coverage c on (c.office_id = p.office_id and c.id = p.coverage_id)',
			'tabname'=>'Coverages',
			'link'=>'queryforcov.php',
		),
		'state'=>array 
		(
			'itemid_column'=>'p.state',
			'filter_arg'=>'state',
			'code_column'=>'p.state',
			'name_column'=>'s.name',
			'join'=>'left outer join cd_state s on (s.id = p.state)',
			'tabname'=>'States',
			'link'=>'queryforstate.php',
		),
		'lob'=>array 
		(
			'itemid_column'=>'p.kpi_group',
			'filter_arg'=>'kpi_group',
			'code_column'=>'k.code',
			'name_column'=>'k.name',
			'join'=>'left outer join ai_kpi_group k on (k.id = p.kpi_group)',
			'tabname'=>'Performance Groups',
			'link'=>'queryforlob.php',
		),
	);
	public static function getTab ($name)
	{
		return TabProperties::$s_tabs [$name];
	}
	public static function makeTabArray ($excludeTab)
	{
		$rslt = array ();
		foreach (TabProperties::$s_tabs as $name=>$props)
		{
			if ($name != $excludeTab)
			{
				$rslt [$name] = $props ['tabname'];
			}
		}
		return $rslt;
	}
}
?>
