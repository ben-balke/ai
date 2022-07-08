/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/util/lang/ClassUtil.java,v 1.2 2010/04/22 18:41:48 secwind Exp $
==================================================================================================*/
package com.duckdigit.util.lang;

public class ClassUtil
{
	/**
	 * Determines of a given class implements an interface
	 * at any levels of its derivations.
	 * @param theclass	A Class object to search.
	 * @param interface	String containing the interface name.
	 * @return boolean	true if the interface is implemented.
	 */
	public static boolean doesImplementInterface (Class theclass, String interfacename)
	{
		boolean		rslt = false;
		//BBB System.out.println ("Checking name: " + theclass.getName ());
		if (!theclass.getName ().equals (interfacename))
		{
			if (!theclass.isInterface ())
			{
				Class		interfaces [] = theclass.getInterfaces ();
				for (int i = 0; i < interfaces.length; i++)
				{
					//BBB System.out.println ("Checking Interface: " + interfaces [i].getName ());
					if (interfaces [i].getName ().equals (interfacename))
					{
						return true;
					}
				}
				Class		superClass = theclass.getSuperclass ();
				if (superClass != null)
				{
					rslt = doesImplementInterface (superClass, interfacename);
				}
			}
		}
		else
		{
			rslt = true;
		}
		return rslt;
	}
	/**
	 * Determines of a given class extends a specific class
	 * at any levels of its derivations.
	 * @param theclass	A Class object to search.
	 * @param interface	String containing the class name.
	 * @return boolean	true if the interface is implemented.
	 */
	public static boolean doesExtendClass (Class theclass, String classname)
	{
		boolean			rslt = false;
		do {
			if (theclass.getName ().equals (classname))
			{
				rslt = true;
				break;
			}
		    theclass = theclass.getSuperclass();
		} while (theclass != null);
		return rslt;
	}
}
