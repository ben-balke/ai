DD_Ag = window.navigator.userAgent;
DD_BVers = parseInt (DD_Ag.charAt (DD_Ag.indexOf ("/") + 1), 10);

var _info = navigator.userAgent;
var _ie = (_info.indexOf("MSIE") > 0 && _info.indexOf("Win") > 0 && _info.indexOf("Windows 3.1") < 0);

function IsIE() { return _ie; }

var DD_alertObject = null;

function DD_AddOnloadFunc(func)
{
	var oldonload = window.onload;
	if(typeof window.onload != "function")
		window.onload = func;
	else
		window.onload = function() 
		{
			oldonload();
			func();
		}
}
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
		$(id).html (text);
		$(id).show ();
	}
}

function f_clientWidth() {
	return f_filterResults (
		window.innerWidth ? window.innerWidth : 0,
		document.documentElement ? document.documentElement.clientWidth : 0,
		document.body ? document.body.clientWidth : 0
	);
}
function f_clientHeight() {
	return f_filterResults (
		window.innerHeight ? window.innerHeight : 0,
		document.documentElement ? document.documentElement.clientHeight : 0,
		document.body ? document.body.clientHeight : 0
	);
}
function f_scrollLeft() {
	return f_filterResults (
		window.pageXOffset ? window.pageXOffset : 0,
		document.documentElement ? document.documentElement.scrollLeft : 0,
		document.body ? document.body.scrollLeft : 0
	);
}
function f_scrollTop() {
	return f_filterResults (
		window.pageYOffset ? window.pageYOffset : 0,
		document.documentElement ? document.documentElement.scrollTop : 0,
		document.body ? document.body.scrollTop : 0
	);
}
function f_filterResults(n_win, n_docel, n_body) {
	var n_result = n_win ? n_win : 0;
	if (n_docel && (!n_result || (n_result > n_docel)))
		n_result = n_docel;
	return n_body && (!n_result || (n_result > n_body)) ? n_body : n_result;
}

function DD_getViewportSize ()
{
	w = f_clientWidth();
	h = f_clientHeight();	
	var rslt = { width : w , height : h }
	//alert (rslt.width + " : " + rslt.height);
	return rslt;
}
function DD_getBrowserWindowSize()
{
	var winW = 630, winH = 460;
	if (document.body && document.body.offsetWidth) 
	{
 		winW = document.body.offsetWidth;
 		winH = document.body.offsetHeight;
	}
	if (document.compatMode=='CSS1Compat' &&
    	document.documentElement &&
    	document.documentElement.offsetWidth ) 
	{
 		winW = document.documentElement.offsetWidth;
 		winH = document.documentElement.offsetHeight;
	}
	if (window.innerWidth && window.innerHeight) 
	{
 		winW = window.innerWidth;
 		winH = window.innerHeight;
	}
	var rval =
	{
		width: winW,
		height: winH
	};
	return rval;
}

function DD_getDocumentWindowSize ()
{
	var body = document.body;
	var html = document.documentElement;  
	var height = Math.max( body.scrollHeight, body.offsetHeight, html.clientHeight, html.scrollHeight, html.offsetHeight ); 
	var width = Math.max( body.scrollWidth, body.offsetWidth, html.clientWidth, html.scrollWidth, html.offsetWidth ); 
	var rval =
	{
		width: width,
		height: height
	};
	return rval;
}

function DD_FindObjById( id )
{
	return $('#' + id);
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

function DD_ShowDivAndPositionAtObject(id, anchorid)
{

	var objX, objY;
	var s_left = f_scrollLeft (); //document.viewport.getScrollOffsets ().left;
	var s_top = f_scrollTop (); //document.viewport.getScrollOffsets ().top;
	var obj = DD_FindObjById (id);
	var anchorobj = DD_FindObjById (anchorid);
	//var win = DD_getBrowserWindowSize ();
	var win = DD_getViewportSize();
	//alert (win.width + ":" + win.height);
	var objSize = DD_getObjectSize (id);
	anchorObjX = DD_GetObjectX (anchorobj);
	anchorObjY = DD_GetObjectY (anchorobj);
	if (anchorObjX - s_left + objSize.width > win.width)
	{
		objX = s_left + win.width - objSize.width;
	}
	else
	{
		objX = anchorObjX
	}
	if (anchorObjY - s_top + objSize.height > win.height)
	{
		objY = s_top + win.height - objSize.height;
	}
	else
	{
		objY = anchorObjY
	}

	obj.style.left = objX + 'px';
	obj.style.top = objY + 'px';
	window.document.body.appendChild (obj);
	obj.style.display='block';
}

function DD_styleToInt (style)
{
	var rslt = parseInt(style);
	if (isNaN (rslt))
	{
		return 0;
	}
	return rslt;
}
function DD_getObjectSize (id)
{
	var theDiv = $(id);
	var dDim = theDiv.getDimensions();
	//dDim.width += DD_styleToInt(theDiv.style.paddingLeft) + DD_styleToInt(theDiv.style.paddingRight); //Total Padding Width
	dDim.width += DD_styleToInt(theDiv.style.marginLeft) + DD_styleToInt(theDiv.style.marginRight); //Total Margin Width
	dDim.width += DD_styleToInt(theDiv.style.borderLeftWidth) + DD_styleToInt(theDiv.style.borderRightWidth); //Total Border Width
	//dDim.height += DD_styleToInt(theDiv.style.paddingTop) + DD_styleToInt(theDiv.style.paddingBottom); //Total Padding Width
	dDim.height += DD_styleToInt(theDiv.style.marginTop) + DD_styleToInt(theDiv.style.marginBottom); //Total Margin Width
	dDim.height += DD_styleToInt(theDiv.style.borderTopWidth) + DD_styleToInt(theDiv.style.borderBottomWidth); //Total Border Width
	return dDim;
}


function DD_CenterDiv (id)
{
	var div = DD_FindObjById (id);
	//var wDim = DD_getBrowserWindowSize();
	var wDim = DD_getViewportSize();
	var dDim = DD_getObjectSize (id);
	objY = ((wDim.height - dDim.height) / 2);
	objX = ((wDim.width - dDim.width) / 2);
	s_left = f_scrollLeft(); //document.viewport.getScrollOffsets ().left;
	s_top = f_scrollTop(); //document.viewport.getScrollOffsets ().top;
	div.style.top = (objY + s_top) + 'px';
	div.style.left = (objX + s_left) + 'px';
	return div;
}
function DD_ShowDivAndCenter(id)
{
	div = DD_CenterDiv (id);
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
function DD_ShowDivInline (id)
{
     obj = DD_FindObjById (id);
     obj.style.display='inline';
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
			datePat = /^(\d{1,2})(\/)(\d{1,2})(\/)(\d{4})$/;
			matchArray = dateStr.match(datePat); // is the format ok?
			if (matchArray == null)
			{
				alert ("nomatch");
				return null;
			}
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

function DD_DateToSQL (d)
{
	return d.getFullYear () + "-" + (d.getMonth () + 1) + "-" + d.getDate ()
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

function DD_ValidateFckRequired (field, msg)
{
	var fckframe = $(field.id + "___Frame");
	if (fckframe.contentWindow.FCK.GetXHTML(true) != "")
	{
		return true;
	}
	DD_alert (msg);
	fckframe.contentWindow.FCK.Focus ();
	return false;
}

function DD_GetFckValue (field)
{
	var fckframe = $(field.id + "___Frame");
	return fckframe.contentWindow.FCK.GetXHTML(true);
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
function DD_CheckRadioByValue (radiobut, value)
{
	for (var i = 0; i < radiobut.length; i++)
	{
		if (radiobut [i].value == value)
		{
			radiobut [i].checked = '1';
			break;
		}
	}
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
// Appends a value, text pair to the select object provided.
function DD_AddSelectOption (selectObj, value, text)
{
	var elOpt = document.createElement ('option');
	elOpt.text = text;
	elOpt.value = value;
	try {
		selectObj.add (elOpt, null);
	}
	catch (ex)
	{
		selectObj.add (elOpt);
	}
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

function DD_CheckToInt (cb, mf)
{
     var chval = parseInt (cb.value);
     var ival;

     if (cb.checked)
     {
             if (mf.value.length != 0)
             {
                     ival = parseInt (mf.value) | (1 << chval);
                     mf.value = ival;
             }
     }
     else
     {
             if (mf.value.length == 0)
                     mval = 0;
             else
                     mval = parseInt (mf.value);

             ival = mval & ~(1 << chval);
             mf.value = ival;
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
