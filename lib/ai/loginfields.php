<?php

$dd_username = array (
	'type'=>'text',
	'name'=>'DD_USERNAME',
	'label'=>_L('Username'),
	'maxlength'=>128,
	'size'=>20,
);

$dd_rememberme = array (
	'type'=>'checkbox',
	'name'=>'DD_REMEMBERME',
	'label'=>_L('Remember Me'),
	'checked'=>'Y',
	'unchecked'=>'N',
	'default'=>'N',
);

$dd_password = array (
	'type'=>'password',
	'name'=>'DD_PASSWORD',
	'label'=>_L('Password'),
	'maxlength'=>20,
	'size'=>20,
);

$dd_login = array (
	'type'=>'submit',
	'name'=>'DD_LOGIN',
	'default'=>_L('Login'),
	'html'=>"class=login-button",
);

$as_month = array (
	'type'=>'select',
	'name'=>'month',
	'noselection'=>_L('Month') . ":",
 	'listsource'=>new DD_ArrayListSource (array (
		'1'=>_L('Jan'),
		'2'=>_L('Feb'),
		'3'=>_L('Mar'),
		'4'=>_L('Apr'),
		'5'=>_L('May'),
		'6'=>_L('Jun'),
		'7'=>_L('Jul'),
		'8'=>_L('Aug'),
		'9'=>_L('Sep'),
		'10'=>_L('Oct'),
		'11'=>_L('Nov'),
		'12'=>_L('Dec'),))
);

$as_day = array (
	'type'=>'select',
	'name'=>'day',
	'noselection'=>_L('Day') . ":",
 	'listsource'=>new DD_RangeListSource (1, 31)
);
$as_year = array (
	'type'=>'select',
	'name'=>'year',
	'noselection'=>_L('Year') . ":",
 	'listsource'=>new DD_RangeListSource (date ("Y"), 1900, -1)
);
?>
