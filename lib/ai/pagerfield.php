<?php

class PagerField extends DD_DomField
{
	var		$m_pagesize = 20;
	var		$m_pagesizeAttr;
	var		$m_curpage = 1;
	var		$m_curpageAttr;

	var		$m_url;
	var		$m_urlAttr;
	var		$m_connect;
	var		$m_countsql = 0;
	var		$m_offset = 0;
	var		$m_numrows = 0;

	function __construct ($array, $extension = null)
	{
        parent::DD_DomField ($array, $extension);
        $this->setState ($array);
        if (isset ($extension))
            $this->setState ($extension);
        if (!isset ($this->m_countsql) || !isset ($this->m_connect))
		{
			die ("{$this->m_name} must have countsql and connect provided as properties.");
		}
    }
    function setState (&$array)
    {
        if (isset ($array ['pagesize']))
            $this->m_pagesizeAttr = $array ['pagesize'];
        if (isset ($array ['curpage']))
            $this->m_curpageAttr = $array ['curpage'];
        if (isset ($array ['countsql']))
            $this->m_countsql = $array ['countsql'];
        if (isset ($array ['connect']))
            $this->m_connect = $array ['connect'];
        if (isset ($array ['url']))
            $this->m_urlAttr = $array ['url'];
    }
	function getOffset () 
	{ 
		return $this->m_offset; 
	}


	function makePageSize ()
	{
		$text = "Records on Page: <input name=pagesize class=dataField type=text value={$this->pagesize} size=4 maxlength=4>";
		return $text;
	}

    function prepareForRecord ($tagParser)
    {
        parent::prepareForRecord ($tagParser);
		if(isset($this->m_pagesizeAttr))
		{
			$pagesize = $tagParser->parseContent ($this->m_pagesizeAttr);
			if (isset($pagesize) && $pagesize != "")
				$this->m_pagesize = $pagesize;
		}
		if(isset($this->m_curpageAttr))
		{
			$curpage = $tagParser->parseContent ($this->m_curpageAttr);
			if (isset ($curpage) && $curpage != "")
				$this->m_curpage = $curpage;
		}
		if(isset($this->m_urlAttr))
		{
			$this->m_url = $tagParser->parseContent ($this->m_urlAttr);
		}
		$this->m_offset = ($this->m_curpage - 1) * $this->m_pagesize;

        $conn = DD_SqlPoolGetConn ($this->m_connect);
        $result = $conn->query($tagParser->parseSql ($this->m_countsql));
        if ($row = $conn->nextRecord ($result))
        {
            if (isset ($row[0]))
            {
                $this->m_numrows = $row[0];
            }
        }
        $conn->freeQuery ($result);
	}

	function getLimitString ()
	{
		return ' OFFSET ' . $this->m_offset . ' LIMIT ' . $this->m_pagesize;
	}
	function makeEnabledButton ($title, $pageno, $image, $width)
	{
		global $DD_imagePath;
      	$text = "<button class='button' title='{$title}' onclick=\"javascript: window.location='{$this->m_url}&pagesize={$this->m_pagesize}&page={$pageno}';\"> <img src='{$DD_imagePath}{$image}' alt='{$title}' align='absmiddle' border='0' width='{$width}' height='11'> </button>";
		return $text;
	}
	function makeDisabledButton ($title, $image, $width)
	{
		global $DD_imagePath;
      	$text = "<button class='button' disabled title='{$title}'> <img src='{$DD_imagePath}{$image}' alt='{$title}' align='absmiddle' border='0' width='{$width}' height='11'> </button>";
		return $text;
	}

	function toString ($tagParser)
	{
		$maxPage = ceil($this->m_numrows / $this->m_pagesize);
		if ($this->m_curpage > 1)
		{
			$text = $this->makeEnabledButton ('Start', 1, "start.gif", 13);
			$text .= $this->makeEnabledButton ('Previous', $this->m_curpage - 1, "previous.gif", 8);
		}
		else
		{
			$text = $this->makeDisabledButton ('Start', "start_off.gif", 13);
			$text .= $this->makeDisabledButton ('Previous', "previous_off.gif", 8);
		}
		$text .=  "<select name=getpage onchange=\"window.location='{$this->m_url}&pagesize={$this->m_pagesize}&page=' + (this.selectedIndex + 1)\">";
		for($page = 1; $page <= $maxPage; $page++)
		{
   			if ($page == $this->m_curpage)
   			{
      			$nav = "<option value=$page selected>Page $page of $maxPage ($this->m_numrows Records)</option> "; // no need to create a link to current page
   			}
   			else
   			{
      			$nav = "<option value=$page>Page $page</option> "; // no need to create a link to current page
   			} 
			$text .= $nav;
		}
		$text .= "</select>";
		if ($this->m_curpage < $maxPage)
		{
			$text .= $this->makeEnabledButton ('Next', $this->m_curpage + 1, "next.gif", 8);
			$text .= $this->makeEnabledButton ('End', $maxPage, "end.gif", 13);
		}
		else
		{
			$text .= $this->makeDisabledButton ('Next', "next_off.gif", 8);
			$text .= $this->makeDisabledButton ('End', "end_off.gif", 13);
		}
		//$text .= "&nbsp;&nbsp; " . $this->m_numrows . " Record(s) " . $maxPage . " Pages found\n";
		return $text;
	}
}
?>
