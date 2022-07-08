package com.duckdigit.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
    Alternating regulator is intended to manage access to a resource whose
    location may vary over time. When the resource is moved, prior claims are
    honored until the client thread attempts to re-acquire the connection. Then
    the allocation is shifted to the new location.
 
    The client must occasionally invoke determineLocation() to identify the
    active ResrouceLoadRegulator from among the alternates. The client registers
    the alternates with an identifying object that is used by specializations
    to determine the last activation time of the source managed by each
    alternate.

 * @author bkbalke
 */
public abstract class AlternatingRegulator
{
    HashMap         m_Locations;
    
    /** Creates a new instance of AlternatingRegulator */
    public AlternatingRegulator()
    {
        m_Locations = new HashMap();
    }
    
    /**
        Publishes the registered regulators.
     
        @return [java.util.Iterator]
     */
    public Iterator getRegulators()
    {
        return m_Locations. values(). iterator();
    }
    
    /**
        Registers a possible resource location.
     
        @param p_Locator [Object]
                                Location key.
        @param p_Alternate [ResourceLoadRegulator]
                                Managing claims on the specific location.
     */
    public void registerLocation(
          Object                    p_Locator,
          ResourceLoadRegulator     p_Alternate )
    {
        m_Locations.put(
                    p_Locator,
                    p_Alternate );
    }
    
    /**
        Determines which of the resource locations is most recent.
     
        @returns [ResourceLoadRegulator]
     */
    public ResourceLoadRegulator locateCurrent()
    {
        Iterator                itL;
        Object                  oLoc;
        Date                    aTime;
        ResourceLoadRegulator   aRLR;
        Date                    tTime;
        ResourceLoadRegulator   tRLR;
        
        aTime = null;
        aRLR = null;
        
        
        itL = m_Locations. keySet(). iterator();

        if (1 == m_Locations.size())
        {
            return (ResourceLoadRegulator) m_Locations. get( itL. next() );
        }
        
        while (itL.hasNext())
        {
            oLoc = itL. next();
            tRLR = (ResourceLoadRegulator) m_Locations. get( oLoc );
            tTime = dateLocation(
                                oLoc,
                                tRLR );
            if (null == tTime) continue;
            
            if (null == aTime)
            {
                aTime = tTime;
                aRLR = tRLR;
            }
            else
            {
                if (tTime. after( aTime ))
                {
                    aTime = tTime;
                    aRLR = tRLR;
                }
            }
        }
        
        return aRLR;
    }
    
    /**
        Provides the date and time that a resource was updated at the given
        location. To be defined by specializations.
     
        @param p_oLocation [Object]             Specifying the location.
        @param p_Reg [ResourceLoadRegulator]    The resource manager.
     */
    public abstract Date dateLocation(
          Object                    p_oLocation,
          ResourceLoadRegulator     p_Reg );
    
    public void updateClaims(
          ResourceLoadRegulator     p_Active )
    {
        Iterator                    itR;
        ResourceLoadRegulator       reg;
        
        itR = m_Locations. values(). iterator();
        while (itR. hasNext())
        {
            reg = (ResourceLoadRegulator) itR. next();
            if (reg != p_Active)
            {
                reg. releaseClaim();
            }
        }
    }
}
