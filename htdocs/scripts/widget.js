var			widgetControlId = null;
var			widgetFirstClick = false;
document.onclick=WIDGET_checkClickOutOfEditControl; 
function WIDGET_checkClickOutOfEditControl (e)
{ 
	if (widgetControlId == null)
	{
		return;
	}
	if (widgetFirstClick == true)
	{
		widgetFirstClick = false;
		return;
	}
	var target = (e && e.target) || (event && event.srcElement); 
	var obj = document.getElementById(widgetControlId); 
	var parent = WIDGET_IsObjectChildOf (target, obj); 
	if (!parent)
	{
		DD_HideDiv (widgetControlId);
		widgetControlId = null;
	} 
} 
function WIDGET_IsObjectChildOf (t, obj)
{ 
	while(t.parentNode)
	{ 
		if (t == obj)
		{ 
			return true;
		} 
		t=t.parentNode;
	} 
	return false; 
} 
function WIDGET_showControlDiv (id)
{

	if (widgetControlId != null)
	{
		DD_HideDiv (widgetControlId);
	}
	widgetFirstClick = true;
	widgetControlId = id + '-edit';
	DD_ShowDivAndPositionAtObject (widgetControlId, id + '-control');
	
}
function WIDGET_hideControlDiv ()
{
	if (widgetControlId != null)
	{
		DD_HideDiv (widgetControlId);
		widgetControlId = null;
	}
}

function process_WIDGET_loadContent (responseText, responseStatus)
{
    if (responseStatus==200)
    {
        var result = responseText.parseJSON ();
        if (result.result == 'GOOD')
        {
        	//alert(result.content);
			$(result.targetid).innerHTML = result.content;
        }
        return 10001;
    }
    else
    {
        alert(responseStatus + ' -- Error Processing Request from Server');
        alert(responseText);
    }
}
/*
 * WIDGET_loadContent ()
 * Loads content into an object based on the target id and an 
 * URL on the server.
 * targetid is the destination of the content.
 * url is the base url to the servers method that returns JSON.
 * args are the arguments to the url that are included they must be escaped 
 * individually.
 */
function  WIDGET_loadContent (targetid, url, args)
{
	if (targetid != undefined && targetid != '' && url != undefined)
	{
		parameters = '&targetid=' + escape (targetid);
		if (args != undefined)
			parameters += "&" + args;

		//alert (targetid + ": " + url);
	    var myrequest = new ajaxObject (url, process_WIDGET_loadContent);
		//alert (parameters);
    	myrequest.update (parameters, 'GET');
	}
	else if (url != undefined)
	{
		window.location = url + "?" + args;
	}
	else
	{
		alert ("targetid is not defined");
	}
}
