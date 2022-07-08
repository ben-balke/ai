DD_Ag = window.navigator.userAgent;
DD_BVers = parseInt (DD_Ag.charAt (DD_Ag.indexOf ("/") + 1), 10);

var _info = navigator.userAgent;
var _ns = false;
var _ie = (_info.indexOf("MSIE") > 0 && _info.indexOf("Win") > 0 && _info.indexOf("Windows 3.1") < 0);
var _ns = (navigator.appName.indexOf("Netscape")>=0 && ((_info.indexOf("Win") > 0 && _info.indexOf("Win16") < 0 && java.lang.System.getProperty("os.version").indexOf("3.5")<0)||(_info.indexOf("Sun")>0)||(_info.indexOf("Linux")>0)));

function IsIE() { return _ie; }
function IsNetscape () { return _ns; }
function DD_IEStyl(s) { return document.all.tags("div")[s].style; }
function DD_NSStyl(s) { return DD_SFindElement(s,0); }

var DD_alertObject = null;

function DD_setAlertObject (id)
{
	DD_alertObject = id;
}
function DD_alert (text)
{
	if (DD_alertObject == null)
	{
		alert (text);
	}
	else
	{
		var obj = DD_FindObjById (DD_alertObject);
		obj.innerHTML = text;
		DD_ShowDiv (DD_alertObject);
	}
}

function DD_getBrowserWindowSize()
{
	var winW = 630, winH = 460;
	if (parseInt(navigator.appVersion)>3)
	{
		if (navigator.appName=="Netscape")
		{
			winW = window.innerWidth;
			winH = window.innerHeight;
		}
		if (navigator.appName.indexOf("Microsoft")!=-1)
		{
			winW = document.body.offsetWidth;
			winH = document.body.offsetHeight;
		}
	}
	var rval =
	{
		width: winW,
		height: winH
	};
	return rval;
}

function DD_FindObjById( id )
{
	if (document.getElementById)
		var returnVar = document.getElementById(id);
	else if (document.all)
		var returnVar = document.all[id];
	else if (document.layers)
		var returnVar = document.layers[id];
	return returnVar;
}


function DD_GetObjectY( oElement )
{
	var iReturnValue = 0;
	while( oElement != null ) 
	{
		iReturnValue += oElement.offsetTop;
		oElement = oElement.offsetParent;
	}
	return iReturnValue;
}

function DD_GetObjectX( oElement )
{
	var iReturnValue = 0;
	while( oElement != null ) 
	{
		iReturnValue += oElement.offsetLeft;
		oElement = oElement.offsetParent;
	}
	return iReturnValue;
}

function DD_ShowDivAndPositionAtObject(id, toid)
{
	var obj = DD_FindObjById (id);
	var toobj = DD_FindObjById (toid);
	var win = DD_getBrowserWindowSize ();
	var objSize = $(id).getDimensions ();
	var toObjSize = $(toid).getDimensions ();
	objX = DD_GetObjectX (toobj);
	objY = DD_GetObjectY (toobj);
	if (objX + objSize.width > win.width)
	{
		objX = objX + toObjSize.width - objSize.width;
	}
	obj.style.top = objY;
	obj.style.left = objX;
	var theBody = document.getElementsByTagName('body')[0];
	theBody.appendChild (obj);
	obj.style.display='block';
}

function DD_ShowDivAndCenter(id)
{
	var div = DD_FindObjById (id);
	var wDim = DD_getBrowserWindowSize();
	var dDim = $(id).getDimensions(div);
	objY = ((wDim.height - dDim.height) / 2);
	objX = ((wDim.width - dDim.width) / 2);
	s_left = document.viewport.getScrollOffsets ().left;
	s_top = document.viewport.getScrollOffsets ().top;
	div.style.top = (objY + s_top) + 'px';
	div.style.left = (objX + s_left) + 'px';
	window.document.body.appendChild (div);
	div.style.display='block';
}


function DD_ShowDiv (id)
{
	obj = DD_FindObjById (id);
	obj.style.display='block';
}
function DD_HideDiv (id)
{
	obj = DD_FindObjById (id);
	obj.style.display='none';
}

function DD_ShowObject (id)
{
	obj = DD_FindObjById (id);
	obj.style.display='';

}
function DD_HideObject (id)
{
	obj = DD_FindObjById (id);
	obj.style.display='none';
}


function DD_FindElement (n, ly)
{
	if (DD_BVers < 4)
	{
		return document[n];
	}
	var curDoc = ly ? ly.document : document;
	var elem = curDoc[n];
	if (!elem)
	{
		for (var i=0;i<curDoc.layers.length;i++)
		{
			elem = DD_FindElement (n, curDoc.layers[i]);
			if (elem)
				return elem;
		}
	}
	return elem;
}

function DD_SetValue (formName, fieldName, text)
{
	var form  = DD_FindElement (formName);
	var field = form.elements [fieldName];
	field.value = text;
}

function DD_URLPopupShow(formName, popupName, target)
{
	var form  = DD_FindElement (formName);
	var popup = form.elements [popupName];
	window.open (popup.options [popup.selectedIndex].value, target);
	//popup.selectedIndex = 0;
}
function DD_URLPopupShowFrame(formName, popupName, target)
{
	var form  = DD_FindElement (formName);
	var popup = form.elements [popupName];
	parent.frames[1].location.replace (popup.options [popup.selectedIndex].value);
	//popup.selectedIndex = 0;
}

function DD_GetFrame (window, frameName)
{
	for (i = 0; i < window.frames.length; i++)
	{
		if (window.frames [i].name == frameName)
		{
			return window.frames [i];
		}
	}
	return null;
}

function DD_ToUpperCase (formName, fieldName)
{
	var form  = DD_FindElement (formName);
	var field = form.elements [fieldName];
	field.value = field.value.toUpperCase ();
}
function DD_ToLowerCase (formName, fieldName)
{
	var form  = DD_FindElement (formName);
	var field = form.elements [fieldName];
	field.value = field.value.toLowerCase ();
}
function DD_DateParse (formName, fieldName)
{
	var form  = DD_FindElement (formName);
	var field = form.elements [fieldName];
	field.value = Date.parse (field.value);
}
function DD_FormatDollarF (formName, fieldName)
{
	var form  = DD_FindElement (formName);
	var field = form.elements [fieldName];
	DD_ValidateDate (field)
}

var	months = new Array ("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");

function DD_ValidateTime (field, format)
{
	return true;
}
/*
function DD_ValidateTimenever (field, format)
{
	var 	timeStr = field.value;

	if 
	for (i = 0; i < timeStr.length; i++)
	{
		
	}
}
*/
function DD_ValidateTimeEx (field, format)
{
	var timeStr = field.value;
	var timePat = /^(\d{1,2}):(\d{2})(:(\d{2}))?(\s?(AM|am|PM|pm))?$/;

	var matchArray = timeStr.match(timePat);
	if (matchArray == null) 
	{
		DD_alert("Time is not in a valid format.");
		return false;
	}
	hour = matchArray[1];
	minute = matchArray[2];
	second = matchArray[4];
	ampm = matchArray[6];

	if (second=="") { second = null; }
	if (ampm=="") { ampm = null }

	if (hour < 0  || hour > 23) 
	{
		DD_alert("Hour must be between 1 and 12. (or 0 and 23 for military time)");
		return false;
	}
	if (hour <= 12 && ampm == null) 
	{
		if (confirm("Please indicate which time format you are using.  OK = Standard Time, CANCEL = Military Time")) 
		{
			DD_alert("You must specify AM or PM.");
			return false;
   		}
	}
	if  (hour > 12 && ampm != null) 
	{
		DD_alert("You can't specify AM or PM for military time.");
		return false;
	}
	if (minute < 0 || minute > 59) 
	{
		DD_alert ("Minute must be between 0 and 59.");
		return false;
	}
	if (second != null && (second < 0 || second > 59)) 
	{
		DD_alert ("Second must be between 0 and 59.");
		return false;
	}
	return true;
}
//
// Validates an input field for a correct date.  A newly formated date
// is inserted into the field upon validation.  The format argument is
// either 0 or null for DD-MMM-YYYY format, or 1 for MM-DD-YYYY format.
//
function DD_ValidateDate (field, format)
{
	if (field.value == '')
	{
		return true;
	}
	var dateStr = field.value;
	var month;
	var day;
	var year;
	//
	// Checks for the following valid date formats:
	// MM/DD/YY   MM/DD/YYYY   MM-DD-YY   MM-DD-YYYY
	// Also separates date into month, day, and year variables
	//
	var datePat = /^(\d{1,2})(\/|-|\s)(\d{1,2})(\/|-|\s)(\d{1}|\d{2}|\d{4})$/;

	// To require a 4 digit year entry, use this line instead:
	// var datePat = /^(\d{1,2})(\/|-)(\d{1,2})\2(\d{4})$/;

	var matchArray = dateStr.match(datePat); // is the format ok?
	if (matchArray == null)
	{
		datePat = /^(\d{2})(\d{2})(\d{2}|\d{4})$/;
		matchArray = dateStr.match(datePat); // is the format ok?
		if (matchArray == null)
		{
			datePat = /^(\d{1,2})(\/|-|\s)(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)(\/|-|\s)(\d{1}|\d{2}|\d{4})$/i;
			matchArray = dateStr.match(datePat); // is the format ok?
			if (matchArray == null)
			{
				DD_alert ("Date is not in a valid format.");
				return false;
			}
			for (var i = 0; i < months.length; i++)
			{
				if (matchArray [3].toLowerCase () == months [i].toLowerCase ())
				{
					month = i + 1;
					break;
				}
			}
			matchArray [3] = matchArray [1];
			matchArray [1] = '0' + month;

		}
		else
		{
			matchArray [5] = matchArray [3];
			matchArray [3] = matchArray [2];
		}
	}

	month = parseInt (matchArray[1], 10); // parse date into variables
	day = parseInt (matchArray[3], 10);
	year = parseInt (matchArray[5], 10);

	var		now = new Date ();
	if (year < 100)
	{
		if (year < (now.getYear () % 100) + 50)
		{
			year = (Math.floor ((now.getFullYear () / 100)) * 100) + year % 100;
		}
		else
		{
			year = (Math.floor (((now.getFullYear () - 100) / 100)) * 100) + year % 100;
		}
	}

	if (month < 1 || month > 12)
	{ // check month range
		DD_alert("Month (" + month + ") must be between 1 and 12.");
		return false;
	}
	if (day < 1 || day > 31)
	{
		DD_alert("Day (" + day + ") must be between 1 and 31.");
		return false;
	}
	if ((month==4 || month==6 || month==9 || month==11) && day==31)
	{
		DD_alert("Month "+month+" doesn't have 31 days!")
		return false
	}
	if (month == 2)
	{ // check for february 29th
		var isleap = (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0));
		if (day>29 || (day==29 && !isleap))
		{
			DD_alert("February " + year + " doesn't have " + day + " days!");
			return false;
		}
	}
	if (format != null && format == 1)
	{
		field.value = month + "/" + day + "/" + year;
	}
	else
	{
		field.value = day + "-" + months [month - 1] + "-" + year;
	}
	//DD_alert (field.value);
	//var date = DD_ToDate (field.value);
	return true;  // date is valid
}

//
// Turns a date string from DD_ValidateDate into a javascript
// date type.  Supports all the date formats of DD_ValidateDate.
// The time portion of the date is set to zero and may be inconsistent
// between implementations but is is garranteed to be consistent with
// an implementation.
//
function DD_ToDate (dateStr)
{
	var date;
	var day;
	var year;
	var	month;

	var datePat = /^(\d{1,2})(-)(\S{3})(-)(\d{4})$/;

	var matchArray = dateStr.match(datePat); // is the format ok?

	if (matchArray == null)
	{
		datePat = /^(\d{1,2})(-)(\d{1,2})(-)(\d{4})$/;
		matchArray = dateStr.match(datePat); // is the format ok?
		if (matchArray == null)
		{
			return null;
		}
		month = parseInt (matchArray[1], 10) - 1;
		day = parseInt (matchArray[3], 10);
		year = parseInt (matchArray[5], 10);
	}
	else
	{
		day = parseInt (matchArray[1], 10);
		year = parseInt (matchArray[5], 10);
		for (i = 0; i < 12; i++)
		{
			if (months [i] == matchArray [3])
			{
				month = i;
			}
		}
	}
	return new Date (year, month, day, 0, 0, 0);
}

function DD_DateToString (d)
{
	return d.getMonth () + 1 + "-" + d.getDate () + "-" + d.getFullYear ();
}

function DD_DateAddDays (d, days)
{
	return new Date (d.getTime () + (days * 60 * 60 * 24 * 1000));
}
//
// Sets two date fields values based on a string which is one of the
// following:
// 		lm= last month
//		lq = last quarter
//		ly = last year
// 		tm= this month
//		tq = this quarter
//		ty = this year
// 		nm= next month
//		nq = next quarter
//		ny = next year
//		-### = back ### days from today.
//		+### = forward ### days from today.
//
function DD_SetDateRange (fromfield, tofield, value)
{
	var qtr;		// Quarter number 0 - 3.
	if (value == "")
	{
		return;
	}
	var d = new Date ();
	switch (value.charAt (0))
	{
	case 'a':
		fromfield.value = "1-1-1970";
		tofield.value = "1-1-2099";
		break;

	case '-':
		tofield.value = DD_DateToString (d);
		e = DD_DateAddDays (d, -value.substring (1));
		fromfield.value = DD_DateToString (e);
		break;
	case '+':
		fromfield.value = DD_DateToString (d);
		e = DD_DateAddDays (d, value.substring (1));
		tofield.value = DD_DateToString (e);
		break;

	case 'l':		////////////////////// LAST MQY ////////////////////////
		switch (value.charAt (1))
		{
		case 'd':
			d = DD_DateAddDays (d, -1);
			tofield.value = DD_DateToString (d);
			fromfield.value = DD_DateToString (d);
			break;
		case 'm':
			d.setDate (1);
			d = DD_DateAddDays (d, -1);
			tofield.value = DD_DateToString (d);
			d.setDate (1);
			fromfield.value = DD_DateToString (d);
			break;
		case 'q':
			qtr = Math.floor(d.getMonth () / 3) - 1;
			if (qtr < 0)
			{
				qtr = 3;
				d.setFullYear (d.getFullYear () - 1);
			}
			d.setDate (1);
			d.setMonth (qtr * 3);
			fromfield.value = DD_DateToString (d);
			d = DD_DateAddDays (d, 94);  // Force 3 months ahead.  31 days * 3 + 1 day
			d.setDate (1);
			d = DD_DateAddDays (d, -1);  // Move Back One Day.
			tofield.value = DD_DateToString (d);
			break;
		case 'y':
			d.setFullYear (d.getFullYear () - 1);
			d.setDate (1);
			d.setMonth (0);
			fromfield.value = DD_DateToString (d);
			d.setDate (31);
			d.setMonth (11);
			tofield.value = DD_DateToString (d);
			break;
		}
		break;

	case 'n':		////////////////////// NEXT MQY ////////////////////////
		switch (value.charAt (1))
		{
		case 'd':
			d = DD_DateAddDays (d, 1);
			tofield.value = DD_DateToString (d);
			fromfield.value = DD_DateToString (d);
			break;
		case 'm':
			d.setDate (1);
			d = DD_DateAddDays (d, 32);
			d.setDate (1);
			fromfield.value = DD_DateToString (d);
			d = DD_DateAddDays (d, 32);
			d.setDate (1);
			d = DD_DateAddDays (d, -1);  // Move Back One Day.
			tofield.value = DD_DateToString (d);
			break;
		case 'q':
			qtr = Math.floor(d.getMonth () / 3) + 1;
			if (qtr > 3)
			{
				qtr = 0;
				d.setFullYear (d.getFullYear () + 1);
			}
			d.setMonth (qtr * 3);
			d.setDate (1);
			fromfield.value = DD_DateToString (d);
			d = DD_DateAddDays (d, 94);  // Force 3 months ahead.  31 days * 3 + 1 day
			d.setDate (1);
			d = DD_DateAddDays (d, -1);  // Move Back One Day.
			tofield.value = DD_DateToString (d);
			break;
		case 'y':
			d.setFullYear (d.getFullYear () + 1);
			d.setMonth (0);
			d.setDate (1);
			fromfield.value = DD_DateToString (d);
			d.setMonth (11);
			d.setDate (31);
			tofield.value = DD_DateToString (d);
			break;
		}
		break;


	case 't':		////////////////////// THIS MQY ////////////////////////
		switch (value.charAt (1))
		{
		case 'd':
			tofield.value = DD_DateToString (d);
			fromfield.value = DD_DateToString (d);
			break;
		case 'm':
			d.setDate (1);
			fromfield.value = DD_DateToString (d);
			d = DD_DateAddDays (d, 32);
			d.setDate (1);
			d = DD_DateAddDays (d, -1);
			tofield.value = DD_DateToString (d);
			break;
		case 'q':
			qtr = Math.floor( d.getMonth () / 3 );
			d.setDate (1);
			d.setMonth (qtr * 3);
			fromfield.value = DD_DateToString (d);
			d = DD_DateAddDays (d, 94);  // Force 3 months ahead.  31 days * 3 + 1 day
			d.setDate (1);
			d = DD_DateAddDays (d, -1);  // Move Back One Day.
			tofield.value = DD_DateToString (d);
			break;
		case 'y':
			d.setMonth (0);
			d.setDate (1);
			fromfield.value = DD_DateToString (d);
			d.setMonth (11);
			d.setDate (31);
			tofield.value = DD_DateToString (d);
			break;
		}
		break;
	}
}


function DD_CheckNum (data)
{
	  // checks if all characters
	var valid = "0123456789.";	 // are valid numbers or a "."
	var ok = 1;
	var checktemp;
	for (var i=0; i<data.length; i++)
	{
		checktemp = "" + data.substring(i, i+1);
		if (valid.indexOf(checktemp) == "-1")
		{
			return 0;
		}
	}
	return 1;
}


function DD_FormatDollarF (formName, fieldName)
{
	var form  = DD_FindElement (formName);
	var field = form.elements [fieldName];
	DD_FormatDollar (field);
}

function DD_StripNumber(numstr)
{
	var 	Num = "";
	for (i = 0; i < numstr.length; i++)
	{
		switch (numstr.charAt (i))
		{
			case ' ':
			case '$':
			case ',':
				break;
			default:
				Num = Num + numstr.charAt (i);
				break;
		}
	}
	return Num;
}
function DD_FormatDollar (field)
{
	var		Num = '';
	var		numstr = field.value;

	for (i = 0; i < numstr.length; i++)
	{
		switch (numstr.charAt (i))
		{
			case ' ':
			case '$':
			case ',':
				break;
			default:
				Num = Num + numstr.charAt (i);
				break;
		}
	}
	if (numstr.length == 0)
	{
		return true;
	}

	dec = Num.indexOf(".");
	end = ((dec > -1) ? "" + Num.substring (dec, Num.length) : ".00");
	Num = "" + parseInt(Num, 10);

	var temp1 = "";
	var temp2 = "";

	if (DD_CheckNum (Num) == 0)
	{
		return false;
	}
	else
	{

		if (end.length > 3)
		{
			end = end.substring (0, 3);
		}
		else
		{
			if (end.length == 2)
				end += "0";
			if (end.length == 1)
				end += "00";
			//if (end == "")
				//end += ".00";
		}

		var count = 0;
		for (var k = Num.length-1; k >= 0; k--)
		{
			var oneChar = Num.charAt (k);
			if (count == 3)
			{
				temp1 += ",";
				temp1 += oneChar;
				count = 1;
				continue;
			}
			else
			{
				temp1 += oneChar;
				count ++;
			}
		}
		for (var k = temp1.length-1; k >= 0; k--)
		{
			var oneChar = temp1.charAt (k);
			temp2 += oneChar;
		}
		temp2 = "$" + temp2 + end;
		field.value = temp2;
   }
   return true;
}
function DD_Repaint()
{
	if ( navigator.appName == 'Netscape')
	{
		if (parseInt(navigator.appVersion, 10) < 4)
		{
			window.history.go(0);
		}
		else
		{
			if (navigator.platform == 'Win32' || navigator.platform == 'Win16')
			{
				window.history.go(0);
			}
		}
	}
}


var	zmlMouseDownX = 0;
var	zmlMouseDownY = 0;
//
// Creates a pop window with the specified url and
// size at the position of the last mouse down event.
// Add the following to body: <BODY ONLOAD="DD_SetupPopup ();">
//
function DD_PopupWindow(myurl, width, height)
{
	DD_PopupNamedWindow (myurl, "Add_from_Src_to_Dest", width, height, 'yes', 'yes');
}

//
// Creates a pop window with the specified url and
// size at the position of the last mouse down event.
// Add the following to body: <BODY ONLOAD="DD_SetupPopup ();">
//
function DD_PopupNamedWindow(myurl, name, width, height, resize, scroll)
{
	var 	win;
	var		leftPos = zmlMouseDownX;
	var		topPos = zmlMouseDownY;

	var screenWidth=screen.width
	var screenHeight=screen.height

	if (zmlMouseDownY == 0)
	{
		leftPos=Math.round((screenWidth/2)-(width/2))
		topPos=Math.round((screenHeight/2)-(height/2))
	}
	if (width + leftPos + 16 > screenWidth)
	{
		leftPos=Math.round (screenWidth - width - 16);
	}
	if (height + topPos + 36 > screenHeight)
	{
		topPos=Math.round (screenHeight - height - 36);
	}


	var props = 'scrollBars=' + scroll + 
				',resizable=' + resize + 
				',toolbar=no,menubar=no,' +
				'location=no directories=no,width=' + width + 
				',height=' + height + ',top=' + topPos + 
				',left=' + leftPos;

	win = window.open(myurl, name, props);
	win.focus ();
}
function DD_GetWindowProperty (propString, prop)
{
	var		idx;
	var		pos;
	var		propval;
	idx = propString.indexOf (prop+ '=');
	if (idx != -1)
	{
		pos = idx + prop.length + 1;
		idx = propString.indexOf (',', pos);
		if (idx != -1)
		{
			propval = propString.substr (pos, idx - pos);
		}
		else
		{
			propval = propString.substr (pos);
		}
		return propval;
	}
	return '';
}
function DD_PopupNamedWindowWithProperties(myurl, name, properties)
{
	var 	win;
	var		leftPos = zmlMouseDownX;
	var		topPos = zmlMouseDownY;
	var 	width = parseInt (DD_GetWindowProperty (properties, 'width'), 10);
	var		height = parseInt (DD_GetWindowProperty (properties, 'height'), 10);
	var 	screenWidth=screen.width;
	var 	screenHeight=screen.height;

	if (zmlMouseDownY == 0)
	{
		leftPos=Math.round((screenWidth/2)-(width/2))
		topPos=Math.round((screenHeight/2)-(height/2))
	}
	else
	{
		if ((16 + width + leftPos) > screenWidth)
		{
			leftPos=Math.round (screenWidth - width - 16);
		}
		if ((36 + height + topPos) > screenHeight)
		{
			topPos=Math.round (screenHeight - height - 36);
		}
	}
	properties = 'left=' + leftPos + ',top=' + topPos + ',' + properties;
	win = window.open(myurl, name, properties);
	win.focus ();
}
//
// Sets a field of a form by form and field name.
//
function DD_SetField (form, field, value)
{
	eval ('window.document.' + form + '.' + field + '.value=' + "value");
}

function DD_SetParentField(form, field, value, closeflag)
{
	if (self.top.opener != null)
	{
		self.top.opener.DD_SetField(form, field, value);
	}
	if (closeflag == true)
	{
		window.top.close();
	}
}


//
// The following two functions are used to track mouse down events
// for Internet Explorer and Netscape Navigator respectively.
//
function DD_IEMouseDown (e)
{
	zmlMouseDownX = event.screenX;
	zmlMouseDownY = event.screenY;
}
function DD_NNMouseDown (e)
{
	zmlMouseDownX = e.screenX;
	zmlMouseDownY = e.screenY;
}
//
// Sets up the avility to pop up windows at the position
// of the last mouse down message.
//
function DD_SetupPopup ()
{
	if (!IsIE())
	{
		window.captureEvents (Event.MOUSEDOWN);
		window.onmousedown = DD_NNMouseDown;
	}
	else
	{
		window.onmousedown = DD_IEMouseDown;
		document.onmousedown = DD_IEMouseDown;
	}
}

function DD_CheckFileSuffix (value, suffixes)
{
	var dotidx = value.lastIndexOf ('.');
	if (dotidx == -1)
	{
		return false;
	}
	var suffix = value.substr (dotidx + 1);
	while ((i = suffixes.indexOf (' ')) != -1)
	{
		suffixes = suffixes.substr (0, i) + suffixes.substr (i + 1);
	}
	while ((i = suffixes.indexOf ('*')) != -1)
	{
		suffixes = suffixes.substr (0, i) + suffixes.substr (i + 1);
	}
	while ((i = suffixes.indexOf ('.')) != -1)
	{
		suffixes = suffixes.substr (0, i) + suffixes.substr (i + 1);
	}
	suffixes = ',' + suffixes + ',';
	suffixes = suffixes.toLowerCase ();
	suffix = ',' + suffix + ',';
	suffix = suffix.toLowerCase ();
	if (suffixes.indexOf (suffix) == -1)
	{
		return false;
	}
	return true;
}

function DD_IsEmailValid (email) 
{
	invalidChars = " /:,;"
	if (email == " " ) 
	{
		return false
	}
	for (i=0; i<invalidChars.length; i++) 
	{
		badChar = invalidChars.charAt(i)
		if (email.indexOf(badChar,0) != -1)
		{
			return false
		}
	}
	atPos = email.indexOf("@",1)
	if (atPos == -1) 
	{
		return false
	}
	if (email.indexOf("@",atPos+1) != -1) 
	{
		return false
	}
	periodPos = email.indexOf(".",atPos)
	if (periodPos == -1) 
	{
		return false
	}
	if (periodPos+3 >  email.length) 
	{
		return false
	}
	return true					 
}

function DD_ValidateRequired (field, msg)
{
	if (field.value != null && field.value != "")
	{
		return true;
	}
	DD_alert (msg);
	field.focus ();
	field.select ();
	return false;
}

function DD_ValidateMultiEmail (field, msg)
{
	emails = field.value;
	while ((eidx = emails.indexOf (',')) != -1)
	{
		email = emails.substr (0, eidx);
		if (!DD_IsEmailValid (email))
		{
			return false;
		}
		emails = emails.substr (eidx + 1);
	}
	if (!DD_IsEmailValid (emails))
	{
		DD_alert (msg + " [" + emails + "]");
		return false;
	}
	return true;
}
function DD_ValidateEmail (field, msg)
{
	if (field.value == '')
	{
		return true;
	}
	if (DD_IsEmailValid (field.value))
	{
		return true;
	}
	DD_alert (msg);
	field.focus ();
	field.select ();
	return false;

}

function DD_ValidateNum (field, msg)
{
	if (field.value == '')
	{
		return true;
	}
	if (DD_CheckNum (field.value))
	{
		return true;
	}
	DD_alert (msg);
	field.focus ();
	field.select ();
	return false;

}
function DD_ValidateIntegerRange (field, low, hi, msg)
{
	if (DD_CheckNum (field.value))
	{
		num = parseInt (field.value, 10);
		if (num >= low && num <= hi)
			return true;
	}
	DD_alert (msg);
	field.focus ();
	field.select ();
	return false;

}

function DD_ValidateFloatRange (field, low, hi, msg)
{
	if (DD_CheckNum (field.value))
	{
		num = parseFloat (field.value, 10);
		if (num >= low && num <= hi)
			return true;
	}
	DD_alert (msg);
	field.focus ();
	field.select ();
	return false;
}

function DD_ValidateFileSuffix (field, suffixes, msg)
{
	if (DD_CheckFileSuffix (field.value, suffixes) == true)
	{
		return true;
	}
	DD_alert (msg);
	field.focus ();
	field.select ();
	return false;
}

function DD_GetRadioValue (radiobut)
{
	var		checkedButton = "";
	for (var i = 0; i < radiobut.length; i++)
	{
		if (radiobut [i].checked == "1")
		{
			checkedButton = radiobut [i].value;
			break;
		}
	}
	return checkedButton;
}

function DD_GetSelectValue (select)
{
	return select.options [select.selectedIndex].value;
}

function DD_StripLeadingZeros (str)
{
	i = 0;
	for (; i < str.length && str.charAt (i) == '0'; i++);
	return str.substr (i);
}
function DD_GetChecked (checkbox)
{
	return checkbox.checked;
}

function DD_ValidateAlphaNum (field, msg)
{
	value = field.value;
	for (i = 0; i < value.length; i++)
	{
		ch = value.charAt (i);
		if  (!((ch >= '0' && ch <= '9') ||
			(ch >= 'a' && ch <= 'z') ||
			(ch >= 'A' && ch <= 'Z')))
		{
			DD_alert (msg);
			field.focus ();
			field.select ();
			return false;
		}
	}
	return true;

}
function DD_ValidateSym (field, msg)
{
	value = field.value;
	for (i = 0; i < value.length; i++)
	{
		ch = value.charAt (i);
		if  (!((ch >= '0' && ch <= '9') ||
			(ch >= 'a' && ch <= 'z') ||
			(ch >= 'A' && ch <= 'Z') ||
			(ch == '_')))
		{
			DD_alert (msg);
			field.focus ();
			field.select ();
			return false;
		}
	}
	return true;

}

function DD_SetNamedValue (objname, text)
{
	var obj = eval (objname);
	obj.value = text;
}

function DD_GetNamedValue (objname)
{
	var obj = eval (objname);
	return obj.value;
}

function DD_SetSelectByText (obj, text)
{
	var		i;
	for (i = 0; i < obj.length; i++)
	{
		if (obj.options [i].text == text)
		{
			obj.selectedIndex = i;
			return true;
		}
	}
	obj.selectedIndex = -1;
	return false;
}

function DD_SetSelectByValue (selectObj, value)
{
	for (i = 0; i < selectObj.options.length; i++)
	{
		if (selectObj.options [i].value == value)
		{
			selectObj.selectedIndex = i;
			return true;
		}
	}
	return false;
}
function DD_ValidateDollar (field, msg)
{
	if (field.value == '')
	{
		return true;
	}
	if (!DD_FormatDollar (field))
	{
		DD_alert (msg);
		field.focus ();
		field.select ();
		return false;
	}
	return true;
}

function DD_ValidateRequiredRadio (field, msg)
{
	for (var i = 0; i < field.length; i++)
	{
		if (field [i].checked == "1")
		{
			return true;
		}
	}
	DD_alert (msg);
	field[0].focus ();
	//field[0].select ();
	return false;
}

function DD_ValidateRequiredSelect (field, msg, testzero)
{
	var minidx = 0;
	if (testzero == true)
	{
		minidx = 1;
	}
	if (field.selectedIndex < minidx)
	{
		DD_alert (msg);
		field.focus ();
		return false;
	}
	return true;
}

function DD_ValidateDateField(field, msg)
{
	if (field.value == '')
	{
		return true;
	}
	if (DD_ValidateDate (field, 1) == false)
	{
		DD_alert (msg);
		field.focus ();
		return false;
	}
	return true;
}
function DD_CheckToText (cb, mf)
{
	var chval = cb.value;

	if (cb.checked)
	{
 		if (mf.value.length != 0)
			mf.value += ',';
		mf.value += chval;
	}
	else
	{
		var 	mfval = mf.value;
		if (mfval == chval)
		{
			mf.value = '';
		}else
		{
			idx = mfval.indexOf (',' + chval + ',');
			if (idx != -1)
			{
				mf.value = mfval.substring (0, idx) + mfval.substring (idx + chval.length + 1);
				return true;
			}
			idx = mfval.indexOf (chval + ',');
			if (idx == 0)
			{
				mf.value = mfval.substring (chval.length + 1);
				return true;
			}
			idx = mfval.indexOf (',' + chval);
			if (idx == mfval.length - chval.length - 1)
			{
				mf.value = mfval.substring (0, idx);
			}
		}
	}
}

function DD_ConfirmFields (fld1, fld2, msg)

{
	if (fld1.value != null && fld1.value != "" &&
		fld2.value != null && fld2.value != "" &&
		fld1.value == fld2.value)

	{

		return true;
	}

	DD_alert (msg);
	fld1.focus ();
	fld1.select ();
	return false;
}

function DD_FormatPhone (data)
{
	  // checks if all characters
	var newdata = "";
	var valid = "0123456789";	 // are valid numbers or a "."
	var ok = 1;
	var ch;
	for (var i=0; i<data.length; i++)
	{
		ch = data.substring(i, i+1);
		if (valid.indexOf (ch) != -1)
		{
			newdata = newdata + ch;
		}
	}
	if (newdata.length == 7)
	{
		newdata = newdata.substring (0, 3) + '-' + newdata.substring (3);
	}
	else if (newdata.length == 10)
	{
		newdata = newdata.substring (0, 3) + '-' + newdata.substring (3, 6)
			+ '-' + newdata.substring (6);
	}
	else
	{
		newdata = null;
	}

	return newdata;
}

function DD_FindNextSep (text)
{
	for (i = 0; i < text.length; i++)
	{
		ch = text.charAt (i);
		switch (ch)
		{
		case ',':
		case '\n':
		case '\r':
		case ' ':
		case ';':
			return i;
		}
	}

	return -1;
}

function DD_ValidateTextAreaOfEmails (field, msg)
{
	var 	bad = '';
	var		rslt = '';
	var		emails = field.value;
	var		msg_nl = "\n";
	if (DD_alertObject != null)
	{
		msg_nl = "<br>";
	}
	while ((eidx = DD_FindNextSep (emails)) != -1)
	{
		if (eidx == 0)
		{
			emails = emails.substr (1);
		}
		else
		{
			email = emails.substr (0, eidx);
			if (!DD_IsEmailValid (email))
			{
				bad += email + msg_nl;
			}
			emails = emails.substr (eidx + 1);
			rslt += email + '\n';
		}
	}
	if (!DD_IsEmailValid (emails))
	{
		bad += emails;
	}
	rslt += emails;
	field.value = rslt;
	if (bad != '')
	{
		DD_alert (msg + msg_nl + msg_nl + bad);
		return false;
	}
	return true;
}

// Use a SPAN tag with the ID.  This will replace it.
function DD_ClientSideInclude(id, url) 
{
	var req = false;
	// For Safari, Firefox, and other non-MS browsers
	if (window.XMLHttpRequest) 
	{
		try 
		{
			req = new XMLHttpRequest();
		} catch (e) {
			req = false;
		}
	} 
	else if (window.ActiveXObject) 
	{
		// For Internet Explorer on Windows
		try 
		{
			req = new ActiveXObject("Msxml2.XMLHTTP");
		} 
		catch (e) 
		{
			try 
			{
				req = new ActiveXObject("Microsoft.XMLHTTP");
			} 
			catch (e) 
			{
				req = false;
			}
		}
	}
 	var element = DD_FindObjById(id);
 	if (!element) 
	{
		alert("Bad id " + id + 
	 		"passed to clientSideInclude." +
	 		"You need a div or span element " +
	 		"with this id in your page.");
			return;
 	}
	if (req) 
	{
		// Synchronous request, wait till we have it all
		req.open('GET', url, false);
		req.send(null);
		element.innerHTML = req.responseText;
	} 
	else 
	{
		element.innerHTML =
	 		"Sorry, your browser does not support " +
				"XMLHTTPRequest objects. This page requires " +
				"Internet Explorer 5 or better for Windows, " +
				"or Firefox for any system, or Safari. Other " +
				"compatible browsers may also exist.";
	}
}

