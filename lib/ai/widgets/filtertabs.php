<?php

require_once ("dd/DD_domfields.php");
require_once ("ai/properties.php");

/***************************************************************************
*** CLASS
*****************************************************************************/
/* Color Widget
 */
class Filter extends DD_DomField
{
    private         $m_activeTabs;
    private         $m_url;
    private         $m_sql;
    function __construct ($array, $extension)
    {
        parent::DD_DomField ($array, $extension);
        $this->setState ($array);
        if (isset ($extension))
            $this->setState ($extension);

        // Validate
        if (!isset ($this->m_listsource))
            die ("Need a list source for field " . $this->m_name);
    }
    function setState ($array)
    {
        if (isset ($array ['listsource']))
            $this->m_listsource = $array ['listsource'];
    }



class Filter e
{
<table>
<tr>
	<td class='{dd:tabs.tab1_style}'><a href='{dd:tabs.tab1_href}'>{dd:tabs.tab1.name}</a></td>
	<td class='{dd:tabs.tab1_style}'><a href='{dd:tabs.tab1_href}'>{dd:tabs.tab2.name}</a></td>
	<td class='{dd:tabs.tab1_style}'><a href='{dd:tabs.tab1_href}'>{dd:tabs.tab3.name}</a></td>
	<td class='{dd:tabs.tab1_style}'><a href='{dd:tabs.tab1_href}'>{dd:tabs.tab4.name}</a></td>
	<td class='{dd:tabs.tab1_style}'><a href='{dd:tabs.tab1_href}'>{dd:tabs.tab5.name}</a></td>
	<td class='{dd:tabs.tab1_style}'><a href='{dd:tabs.tab1_href}'>{dd:tabs.tab6.name}</a></td>
	<td class='{dd:tabs.tab1_style}'><a href='{dd:tabs.tab1_href}'>{dd:tabs.tab7.name}</a></td>
	<td class='{dd:tabs.tab1_style}'><a href='{dd:tabs.tab1_href}'>{dd:tabs.tab9.name}</a></td>
	<td class='{dd:tabs.tab1_style}'><a href='{dd:tabs.tab1_href}'>{dd:tabs.tab10.name}</a></td>
	<td class='{dd:tabs.tab1_style}'><a href='{dd:tabs.tab1_href}'>{dd:tabs.tab11.name}</a></td>
	<td class='{dd:tabs.tab1_style}'><a href='{dd:tabs.tab1_href}'>{dd:tabs.tab12.name}</a></td>
	<td class='tab-filler'>&nbsp;</td>
</tr>
</table>
}
?>
