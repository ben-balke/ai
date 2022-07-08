<?php
require_once ('ai/tabproperties.php');
/*
 * This table defines the properties that are associated to a users profile.  It cooresponds to the users 
 * table in the database.  All of these properties are set using the form widget/set_admin_props.php.  Each field is setup
 * with an array that defines a DOM field.  IF YOU CHANGE THE FIELD NAMES YOU MUST CHANGE THE DATABASE TABLE!!!!!  IF YOU DO
 * NOT, YOU DIE!!! - ben
 */
$widget_limit_options = array (3=>3,4=>4,5=>5,8=>8,10=>10,15=>15,20=>20,25=>25,30=>30,40=>40,50=>50,75=>75,100=>100);
$widget_showdetail = array ('Y'=>'Show','N'=>'Hide');
$widget_top_sort_options = array ('revenue desc'=>'Revenue','premium desc'=>'Premium');
$widget_sort_options = array ('revenue desc'=>'Revenue','premium desc'=>'Premium','itemname'=>'Name','itemcount desc'=>'Policies','itemcode'=>'Code','commission desc'=>'Commission %');
$widget_cust_policy_sort_options = array (
	'policy_no'=>'Policy #'
	,'coverage'=>'Coverage'
	,'insuror'=>'Carrier'
	,'premium desc'=>'Premium'
	,'revenue desc'=>'Revenue'
	,'commission desc'=>'% Comm'
	,'effdate'=>'Effective Date'
	,'expdate'=>'Expireation Date'
);
$widget_cust_carrier_sort_options = array (
	'description'=>'Carrier'
	,'premium desc'=>'Premium'
	,'revenue desc'=>'Revenue'
	,'commission desc'=>'%Comm'
	,'code'=>'Carrier Code'
);
$widget_cust_coverage_sort_options = array (
	'description'=>'Coverage'
	,'premium desc'=>'Premium'
	,'revenue desc'=>'Revenue'
	,'commission desc'=>'% Comm'
	,'code'=>'Coverage Code'
);

$widget_customer_sort_options = array (
	'cust_name'=>'Customer'
	,'cust_code'=>'Customer Code'
	,'cust_premium desc'=>'Premium'
	,'cust_revenue desc'=>'Revenue'
	,'cust_percent desc'=>'% Comm'
	,'policy_count, cust_revenue desc'=>'Policies'
	,'claim_count desc'=>'Claims'
);
$widget_policy_sort_options = array (
	'policy_no'=>'Policy Number'
	,'cust_name'=>'Customer'
	,'cust_code'=>'Customer Code'
	,'coverage'=>'Coverage'
	,'pol_premium desc'=>'Premium'
	,'pol_revenue desc'=>'Revenue'
	,'pol_percent desc'=>'% Comm'
	,'expdate'=>'Expiration Date'
	,'effdate'=>'Effective Date'
);
$AdminProperties = array
(
	'active_office_id'=>array ('name'=>'active_office_id','default'=>'{dd:user.active_office_id}','type'=>'select','label'=>'Office:', 'listsource'=>new DD_SqlListSource ('sql.ai', "select id,  name || ' (' || to_char(lastupdate,'DD-Mon-YY HH:mi am') || ')' from ai_office o left outer join ai_office_security s on (s.office_id = o.id and s.user_id = {user.id}) where active = 'Y' and s.status = 'A' order by name"))
	,'n_customers'=>array ('name'=>'n_customers','default'=>'{dd:user.n_customers}','type'=>'select','label'=>'Per Page:', 'listsource'=>new DD_ArrayListSource ($widget_limit_options))
	,'sort_customers'=>array ('name'=>'sort_customers','default'=>'{dd:user.sort_customers}','type'=>'select','label'=>'Sort By:', 'listsource'=>new DD_ArrayListSource ($widget_customer_sort_options))

	,'n_policies'=>array ('name'=>'n_policies','default'=>'{dd:user.n_policies}','type'=>'select','label'=>'Per Page:', 'listsource'=>new DD_ArrayListSource ($widget_limit_options))
	,'sort_policies'=>array ('name'=>'sort_policies','default'=>'{dd:user.sort_policies}','type'=>'select','label'=>'Sort By:', 'listsource'=>new DD_ArrayListSource ($widget_policy_sort_options))

	,'n_top_customers'=>array ('name'=>'n_top_customers','default'=>'{dd:user.n_top_customers}','type'=>'select','label'=>'Show:', 'listsource'=>new DD_ArrayListSource ($widget_limit_options))
	,'sort_top_customers'=>array ('name'=>'sort_top_customers','default'=>'{dd:user.sort_top_customers}','type'=>'select','label'=>'Sort By:', 'listsource'=>new DD_ArrayListSource ($widget_top_sort_options))

	,'n_top_carriers'=>array ('name'=>'n_top_carriers','default'=>'{dd:user.n_top_carriers}','type'=>'select','label'=>'Show:', 'listsource'=>new DD_ArrayListSource ($widget_limit_options))
	,'sort_top_carriers'=>array ('name'=>'sort_top_carriers','default'=>'{dd:user.sort_top_carriers}','type'=>'select','label'=>'Sort By:', 'listsource'=>new DD_ArrayListSource ($widget_top_sort_options))

	,'n_top_coverages'=>array ('name'=>'n_top_coverages','default'=>'{dd:user.n_top_coverages}','type'=>'select','label'=>'Show:', 'listsource'=>new DD_ArrayListSource ($widget_limit_options))
	,'sort_top_coverages'=>array ('name'=>'sort_top_coverages','default'=>'{dd:user.sort_top_coverages}','type'=>'select','label'=>'Sort By:', 'listsource'=>new DD_ArrayListSource ($widget_top_sort_options))


	,'n_top_carriers_by_cust'=>array ('name'=>'n_top_carriers_by_cust','default'=>'{dd:user.n_top_carriers_by_cust}','type'=>'select','label'=>'Show:', 'listsource'=>new DD_ArrayListSource ($widget_limit_options))
	,'sort_top_carriers_by_cust'=>array ('name'=>'sort_top_carriers_by_cust','default'=>'{dd:user.sort_top_carriers_by_cust}','type'=>'select','label'=>'Sort By:', 'listsource'=>new DD_ArrayListSource ($widget_cust_carrier_sort_options))

	,'n_top_coverages_by_cust'=>array ('name'=>'n_top_coverages_by_cust','default'=>'{dd:user.n_top_coverages_by_cust}','type'=>'select','label'=>'Show:', 'listsource'=>new DD_ArrayListSource ($widget_limit_options))
	,'sort_top_coverages_by_cust'=>array ('name'=>'sort_top_coverages_by_cust','default'=>'{dd:user.sort_top_coverages_by_cust}','type'=>'select','label'=>'Sort By:', 'listsource'=>new DD_ArrayListSource ($widget_cust_coverage_sort_options))
	,'sort_policies_by_cust'=>array ('name'=>'sort_policies_by_cust','default'=>'{dd:user.sort_policies_by_cust}','type'=>'select','label'=>'Sort By:', 'listsource'=>new DD_ArrayListSource ($widget_cust_policy_sort_options))


	/*
	 * Report Page screens.
	 */
	,'sort_bycov'=>array ('name'=>'sort_bycov','default'=>'{dd:user.sort_bycov}','type'=>'select','label'=>'Sort By:', 'listsource'=>new DD_ArrayListSource ($widget_sort_options))
	,'sort_byprod'=>array ('name'=>'sort_byprod','default'=>'{dd:user.sort_byprod}','type'=>'select','label'=>'Sort By:', 'listsource'=>new DD_ArrayListSource ($widget_sort_options))
	,'sort_bystate'=>array ('name'=>'sort_bystate','default'=>'{dd:user.sort_bystate}','type'=>'select','label'=>'Sort By:', 'listsource'=>new DD_ArrayListSource ($widget_sort_options))
	,'sort_byins'=>array ('name'=>'sort_byins','default'=>'{dd:user.sort_byins}','type'=>'select','label'=>'Sort By:', 'listsource'=>new DD_ArrayListSource ($widget_sort_options))
	,'sort_bypayee'=>array ('name'=>'sort_bypayee','default'=>'{dd:user.sort_bypayee}','type'=>'select','label'=>'Sort By:', 'listsource'=>new DD_ArrayListSource ($widget_sort_options))
	,'sort_bycsr'=>array ('name'=>'sort_bycsr','default'=>'{dd:user.sort_bycsr}','type'=>'select','label'=>'Sort By:', 'listsource'=>new DD_ArrayListSource ($widget_sort_options))
	,'sort_bysiccode'=>array ('name'=>'sort_bysiccode','default'=>'{dd:user.sort_bysiccode}','type'=>'select','label'=>'Sort By:', 'listsource'=>new DD_ArrayListSource ($widget_sort_options))
	,'sort_bysicmajor'=>array ('name'=>'sort_bysicmajor','default'=>'{dd:user.sort_bysicmajor}','type'=>'select','label'=>'Sort By:', 'listsource'=>new DD_ArrayListSource ($widget_sort_options))
	,'sort_bylob'=>array ('name'=>'sort_bylob','default'=>'{dd:user.sort_bylob}','type'=>'select','label'=>'Sort By:', 'listsource'=>new DD_ArrayListSource ($widget_sort_options))

	/* NEW for TAB PAGES */
	,'sort_forprod'=>array ('name'=>'sort_forprod','default'=>'{dd:user.sort_forprod}','type'=>'select','label'=>'Sort By:', 'listsource'=>new DD_ArrayListSource ($widget_sort_options))
	,'tab_forprod'=>array ('name'=>'tab_forprod','default'=>'{dd:user.tab_forprod}','type'=>'tab','onclick'=>'tabOption({value}); return false;','label'=>'Show Producer By:', 'listsource'=>new DD_ArrayListSource (TabProperties::makeTabArray ('producer')))

	,'sort_forcsr'=>array ('name'=>'sort_forcsr','default'=>'{dd:user.sort_forcsr}','type'=>'select','label'=>'Sort By:', 'listsource'=>new DD_ArrayListSource ($widget_sort_options))
	,'tab_forcsr'=>array ('name'=>'tab_forcsr','default'=>'{dd:user.tab_forcsr}','type'=>'tab','onclick'=>'tabOption({value}); return false;','label'=>'Show Producer By:', 'listsource'=>new DD_ArrayListSource (TabProperties::makeTabArray ('servicer')))

	,'sort_forpayee'=>array ('name'=>'sort_forpayee','default'=>'{dd:user.sort_forpayee}','type'=>'select','label'=>'Sort By:', 'listsource'=>new DD_ArrayListSource ($widget_sort_options))
	,'tab_forpayee'=>array ('name'=>'tab_forpayee','default'=>'{dd:user.tab_forpayee}','type'=>'tab','onclick'=>'tabOption({value}); return false;','label'=>'Show Producer By:', 'listsource'=>new DD_ArrayListSource (TabProperties::makeTabArray ('payee')))

	,'sort_forins'=>array ('name'=>'sort_forins','default'=>'{dd:user.sort_forins}','type'=>'select','label'=>'Sort By:', 'listsource'=>new DD_ArrayListSource ($widget_sort_options))
	,'tab_forins'=>array ('name'=>'tab_forins','default'=>'{dd:user.tab_forins}','type'=>'tab','onclick'=>'tabOption({value}); return false;','label'=>'Show Producer By:', 'listsource'=>new DD_ArrayListSource (TabProperties::makeTabArray ('insuror')))

	,'sort_forsiccode'=>array ('name'=>'sort_forsiccode','default'=>'{dd:user.sort_forsiccode}','type'=>'select','label'=>'Sort By:', 'listsource'=>new DD_ArrayListSource ($widget_sort_options))
	,'tab_forsiccode'=>array ('name'=>'tab_forsiccode','default'=>'{dd:user.tab_forsiccode}','type'=>'tab','onclick'=>'tabOption({value}); return false;','label'=>'Show Producer By:', 'listsource'=>new DD_ArrayListSource (TabProperties::makeTabArray ('siccode')))

	,'sort_forsicmajor'=>array ('name'=>'sort_forsicmajor','default'=>'{dd:user.sort_forsicmajor}','type'=>'select','label'=>'Sort By:', 'listsource'=>new DD_ArrayListSource ($widget_sort_options))
	,'tab_forsicmajor'=>array ('name'=>'tab_forsicmajor','default'=>'{dd:user.tab_forsicmajor}','type'=>'tab','onclick'=>'tabOption({value}); return false;','label'=>'Show Producer By:', 'listsource'=>new DD_ArrayListSource (TabProperties::makeTabArray ('sicmajor')))

	,'sort_forcov'=>array ('name'=>'sort_forcov','default'=>'{dd:user.sort_forcov}','type'=>'select','label'=>'Sort By:', 'listsource'=>new DD_ArrayListSource ($widget_sort_options))
	,'tab_forcov'=>array ('name'=>'tab_forcov','default'=>'{dd:user.tab_forcov}','type'=>'tab','onclick'=>'tabOption({value}); return false;','label'=>'Show Producer By:', 'listsource'=>new DD_ArrayListSource (TabProperties::makeTabArray ('coverage')))

	,'sort_forstate'=>array ('name'=>'sort_forstate','default'=>'{dd:user.sort_forstate}','type'=>'select','label'=>'Sort By:', 'listsource'=>new DD_ArrayListSource ($widget_sort_options))
	,'tab_forstate'=>array ('name'=>'tab_forstate','default'=>'{dd:user.tab_forstate}','type'=>'tab','onclick'=>'tabOption({value}); return false;','label'=>'Show Producer By:', 'listsource'=>new DD_ArrayListSource (TabProperties::makeTabArray ('state')))

	,'sort_forlob'=>array ('name'=>'sort_forlob','default'=>'{dd:user.sort_forlob}','type'=>'select','label'=>'Sort By:', 'listsource'=>new DD_ArrayListSource ($widget_sort_options))
	,'tab_forlob'=>array ('name'=>'tab_forlob','default'=>'{dd:user.tab_forlob}','type'=>'tab','onclick'=>'tabOption({value}); return false;','label'=>'Show Line of Business By:', 'listsource'=>new DD_ArrayListSource (TabProperties::makeTabArray ('lob')))
);
$PropertyUpdate = array ('name'=>'DD_update' ,'type'=>'submit' ,'default'=>'Save' ,'html'=>'class="button"');

?>
