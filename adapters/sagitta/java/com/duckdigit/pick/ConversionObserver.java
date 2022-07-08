
/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/sagitta/java/com/duckdigit/pick/ConversionObserver.java,v 1.2 2010/04/22 18:41:42 secwind Exp $
==================================================================================================*/
package com.duckdigit.pick;

import java.io.*;
import java.util.*;

public interface ConversionObserver
{
	public void reportTruncation (String value);
	public void reportBadNumber (String value, String msg);
	public int getMaxSize ();
}

