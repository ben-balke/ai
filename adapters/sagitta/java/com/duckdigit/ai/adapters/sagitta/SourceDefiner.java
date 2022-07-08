
/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/ai/adapters/sagitta/SourceDefiner.java,v 1.2 2010/04/22 18:41:39 secwind Exp $
==================================================================================================*/
package com.duckdigit.ai.adapters.sagitta;

import java.util.ArrayList;

public interface SourceDefiner {
	String getClassString();

	int getVarCount();

	int[] getVarTypes();

	int[] getVarLengths();

	void setOffice (String office);

	ArrayList getVarDecls();

	ArrayList getVarNames();

	ArrayList getKeyNames();

	boolean checkUpdate();

	boolean checkKey(
		    String      p_sField );

	int getUpdateIndex(
		    String      p_sField );
}
