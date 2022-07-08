/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/ai/adapters/sagitta/PickDefiner.java,v 1.2 2010/04/22 18:41:39 secwind Exp $
==================================================================================================*/
package com.duckdigit.ai.adapters.sagitta;

import java.util.ArrayList;

public interface PickDefiner
{
	String getFile();

	int[] getVarTypes();

	String getFilterTemplate();

	int getCacheSize();

	int getMaxRecordSize();
}
