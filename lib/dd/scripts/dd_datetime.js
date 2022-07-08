/** 
 * Copyright duckdigit technologies, inc. ALL RIGHTS RESERVED 2011
 */
var dd_datetimePopups = new Array ();
var dd_datetimeInitiated = false;

function DD_loadDatetimeDynamics ()
{
	if (dd_datetimeInitiated == false)
	{
		pclass = 'dd_datetime';
		var inputs = document.getElementsByTagName("input");
		for(x=0;x<inputs.length-1;x++)
		{
			var		f = inputs[x];
			if(f.className == 'dd_datetime')
			{
				dd_datetimePopups [f.name] = new Epoch(f.name + 'popup','popup',
					$(f.name + 'day'),false);
			}
			else if (f.className == 'dd_date')
			{
				dd_datetimePopups [f.name] = new Epoch(f.name + 'popup','popup',
					$(f.name),false);
			}
		}
		dd_datetimeInitiated = true;
	}
}

function DD_doDateClick (name)
{
	DD_loadDatetimeDynamics ();
	dd_datetimePopups [name].toggle ();
}

function DD_ValidateDatetimeField (field, displayname, required)
{
	name = field.name;
	var		fday = $(name + 'day');
	var		fhour = $(name + 'hour');
	var		fminute = $(name + 'minute');
	var		fampm = $(name + 'ampm');

	if (required == false && fday.value == '')
	{
		field.value = '';
		return true;
	}
	if (
		!DD_ValidateRequired (fday, "Please enter or select " + displayname + " date.") ||
		!DD_ValidateDateField (fday, "Please enter or select a valid " + displayname + " date.") ||
		!DD_ValidateRequiredSelect (fhour, "Please complete the " + displayname + " time.", true) ||
		!DD_ValidateRequiredSelect (fminute, "Please complete the " + displayname + " time.", true) ||
		!DD_ValidateRequiredSelect (fampm, "Please select AM or PM.", false) )
	{
		return false;
	}
	d = DD_ToDate (fday.value);
	h = fhour.options [fhour.selectedIndex].text;
	if (fampm.selectedIndex == 1)
	{
		if (h != 12)
			h = 12 + parseInt (h);
	}
	else
	{
		if (h == 12)
			h = 0;
	}
	field.value = DD_DateToSQL (d) + ' ' + h + ':' + fminute.options [fminute.selectedIndex].text + ':00 ';
	return true;
}

/* Note that there is already a ValidateDateField in duckdigit.js.  I have added a 2 to extend it.
 */
function DD_ValidateDateField2 (field, displayname, required)
{
	if (required == false && field.value == '')
	{
		field.value = '';
		return true;
	}
	if (
		!DD_ValidateRequired (field, "Please enter or select " + displayname + " date.") ||
		!DD_ValidateDateField (field, "Please enter or select a valid " + displayname + " date."))
	{
		return false;
	}
	return true;
}
