var timeout	= 500;
var closetimer	= 0;
var ddmenuitem	= 0;

// open hidden layer
function mopen(id)
{	
	// cancel close timer
	mcancelclosetime();

	// close old layer
	if(ddmenuitem)
	{
		ddmenuitem.style.visibility = 'hidden';
		//ddmenuitem.style.display = 'none';
	}

	// get new layer and show it
	ddmenuitem = document.getElementById(id);
	ddmenuitem.style.visibility = 'visible';
	//ddmenuitem.style.display = 'block';

}
// close showed layer
function mclose()
{
	if(ddmenuitem) 
	{
		ddmenuitem.style.visibility = 'hidden';
		//ddmenuitem.style.display = 'none';
	}
}

// go close timer
function mclosetime()
{
	if (!closetimer)
	{
		closetimer = window.setTimeout(mclose, timeout);
	}
}

// cancel close timer
function mcancelclosetime()
{
	if(closetimer)
	{
		window.clearTimeout(closetimer);
		closetimer = 0;
	}
}

// close layer when click-out
//document.onclick = mclose; 

