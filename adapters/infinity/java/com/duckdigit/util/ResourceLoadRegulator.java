/*==================================================================================================
* Copyright (c) DuckDigit Technologies, Inc.  2008,2009
* ALL RIGHTS RESERVED
* $Header: /home/cvsroot/ai/adapters/infinity/java/com/duckdigit/util/ResourceLoadRegulator.java,v 1.1 2010/10/19 00:43:59 secwind Exp $
==================================================================================================*/
package com.duckdigit.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

public abstract class ResourceLoadRegulator
{
    /** Instance identifier. */
    private Object      m_oInstance;
	/** Maximum number of claims allowed on the resource. */
	private int         m_iSize;
    /** Next claim slot to be used. */
	private int         m_iNext;
    /** Pool of claims. */
	private ArrayList   m_Pool;
    /** Last used time for each resource. */
	private Date        m_LastUsed[];
    /** Time duration, in ticks, after which the claim can be reused while still
        held by the prior user. */
	private int         m_iRateLim;

	/*==========================================================================
	===== Thread assignments.
	==========================================================================*/
	private HashMap     m_Usage;

	/**
		Create a resource regulator with the specified collection size and
		usage limit.
        @param p_oInstance [Object]
                            Instance identifier. Typically used to provide
                                application-specific context used to manage the
                                resource.
		@param int(1)       Collection size.
		@param int(2)       Interval between resource accesses that allows
							re-use by a new claimant.
	 */
    public ResourceLoadRegulator(
            Object  p_oInstance,
		    int     p_iSize,
			int     p_iRateLim )
	{
		int     iRs;

        m_oInstance = p_oInstance;
		m_iSize = p_iSize;
		m_iRateLim = p_iRateLim;

		m_Pool = new ArrayList( m_iSize );
		for (iRs = 0; iRs < m_iSize; iRs++)
		{
			m_Pool. add( iRs, null );
		}

		m_LastUsed = new Date[ m_iSize ];
		m_Usage = new HashMap();
		m_iNext = 0;
    }
    
    /**
        Exposes the instance data for this regulator.
     
        @return Object      Describing the resource.
     */
    public Object describeInstance()
    {
        return m_oInstance;
    }

	/**
		Allocate a resource.
		Template service required for implementation by specializations.
		@return Object      Resource.
		@throws Exception   Resulting from allocation process.
	 */
	public abstract Object allocateResource()
		throws Exception;

	/**
		Determine whether resource is still usable.
		Template service required for implementation by specializations.
		@param Object       Resource.
	 */
	public abstract boolean checkValid(
		    Object      p_Rsrc );

	public synchronized void markInvalid(
		    Object      p_Rsrc )
	{
	   int			 iRs;
	   Iterator		 itTh;
	   Thread		 th;
	   
	   freeResource( p_Rsrc );
	   
	   // Remove the resource from the pool.
	   for (iRs = 0; iRs < m_iSize; iRs++)
	   {
		  if (p_Rsrc == m_Pool. get( iRs ))
		  {
			 m_Pool. set( iRs, null );
			 break;
		  }
	   }
	   
	   // Now remove threads that reference the resource.
	   itTh = m_Usage. keySet(). iterator();
	   while (itTh. hasNext())
	   {
		  th = (Thread) itTh. next();
		  if (p_Rsrc == m_Usage. get( th ))
		  {
			 itTh. remove();
		  }
	   }
	}

	/**
		Frees resource.
		Template service required for implementation by specializations.
		@param Object       Resource.
	 */
	public abstract void freeResource(
		    Object      p_Rsrc );

	/**
		Obtain resource associated with this thread, or assign one from the
		pool.
		<p>
		We will only create a new resource if currently allocated resources
		are heavily loaded.
		<p>
		If a resource has become corrupted, then it is returned to threads
		already using it, but is bypassed in thread reuse. If all resources are
		corrupted, we return null.
	 */
	public synchronized Object claimResource()
		throws Exception
	{
		Thread          cTh;
		Object          rsrc;

		Date            now;
		Date            used;
		int             iRsrc;

		cTh = Thread. currentThread();

		// Disassociate dead threads from the resource pool.
		purgeThreads();

		// Find resource assigned to this thread.
		rsrc = m_Usage. get( cTh );
		if (null != rsrc)
		{
			if (checkValid( rsrc ))
			{
				return rsrc;
			}
			else
			{
				releaseClaim();
			}
		}

		rsrc = null;

		// Attempt to re-use lightly loaded or unclaimed resources first.
		now = new Date();
		for (iRsrc=0; iRsrc<m_iSize; iRsrc++)
		{
			rsrc = m_Pool. get( iRsrc );
			if (null == rsrc) continue;

			used = m_LastUsed[ iRsrc ];
			if (!m_Usage. containsValue( rsrc ) ||
				((long) m_iRateLim < (now.getTime() - used.getTime())))
			{
				if (checkValid( rsrc ))
				{
					m_LastUsed[ iRsrc ] = now;
					break;
				}
			}
			rsrc = null;    // Ensure resource assignment is null if last
						    // resource tested is invalid.
		}

		if (null == rsrc)
		{
			// Attempt to create a new resource in the pool.
			for (iRsrc=0; iRsrc<m_Pool.size(); iRsrc++)
			{
				if (null == m_Pool. get( iRsrc ))
				{
					// Pool is not full - create a new resource.
					m_Pool. set(
								iRsrc,
								allocateResource() );
					rsrc = m_Pool. get( iRsrc );
					m_LastUsed[ iRsrc ] = now;
				}
			}
		}

		iRsrc = m_iNext;
		while (null == rsrc)
		{
			// Okay. We'll have to risk re-assigning a resource that was
			// recently used. Only criteria is its validity.
			rsrc = m_Pool. get( m_iNext );
			if (!checkValid( rsrc ))
			{
				rsrc = null;
			}
			else
			{
				m_LastUsed[ m_iNext ] = now;
			}

			m_iNext = (m_iNext + 1) % m_iSize;
			if ((null == rsrc) && (m_iNext == iRsrc))
			{
				// Pool is full of invalid resources!!!
				return null;
			}
		}

		m_Usage. put( cTh, rsrc );
		return rsrc;
	}

	/**
		Release a claim on a resource.
	 */
	public void releaseClaim()
	{
		Thread          cTh;

		cTh = Thread. currentThread();
		if (null != m_Usage. remove( cTh ))
        {
            try {
                purgeResources();
            }
            catch (Exception  ex) {}
        }
	}

	/**
		Close the resource associated with this thread. This enables us to
		free dead resources.
	 */
	public synchronized void closeResource()
	{
		Thread          cTh;
		Object          rsrc;

		cTh = Thread. currentThread();
		rsrc = m_Usage. get( cTh );
		if (null == rsrc) return;

		markInvalid( rsrc );
	}

	/**
		Purge inactive threads and free unused resources.
	 */
	public synchronized void purgeResources()
		throws Exception
	{
		Collection      inUse;
		int             iRs;
		Object          rsrc;

		purgeThreads();

		// Free resources that are no longer referenced in the usage map.
		inUse = m_Usage. values();
		for (iRs = 0; iRs < m_iSize; iRs++)
		{
			rsrc = m_Pool. get( iRs );
			if (null == rsrc) continue;

			if (!inUse. contains( rsrc ))
			{
				freeResource( rsrc );
				m_Pool. set( iRs, null );
				m_LastUsed[ iRs ] = null;
			}
		}
	}

	private void purgeThreads()
	{
		Iterator        itTh;
		Thread          th;

		// Remove all dead threads from the usage map.
		itTh = m_Usage. keySet(). iterator();
		while (itTh. hasNext())
		{
			th = (Thread) itTh. next();
			if (!th.isAlive())
			{
				itTh. remove();
			}
		}
	}

	/**
		Obtain all resources for analysis.
	 */
	public synchronized void getResources(
		Collection      p_RsrcColl )
	{
		p_RsrcColl. addAll( m_Pool );
	}
}
