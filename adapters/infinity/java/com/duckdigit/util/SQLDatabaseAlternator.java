/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/util/SQLDatabaseAlternator.java,v 1.1 2010/10/19 00:43:59 secwind Exp $
==================================================================================================*/
package com.duckdigit.util;

import java.util.Date;

public class SQLDatabaseAlternator
      extends AlternatingRegulator
{
    private SqlConnectProtocol      m_Client;
    
    public SQLDatabaseAlternator(
          SqlConnectProtocol        p_Client )
    {
        m_Client = p_Client;
    }
    
    public Date dateLocation(
          Object                    p_oLocation,
          ResourceLoadRegulator     p_Reg )
    {
        return m_Client. dateLocation( p_oLocation );
    }
}
