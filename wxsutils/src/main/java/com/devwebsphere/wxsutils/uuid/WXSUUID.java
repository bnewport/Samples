//
//This sample program is provided AS IS and may be used, executed, copied and
//modified without royalty payment by customer (a) for its own instruction and 
//study, (b) in order to develop applications designed to run with an IBM 
//WebSphere product, either for customer's own internal use or for redistribution 
//by customer, as part of such an application, in customer's own products. "
//
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2005
//All Rights Reserved * Licensed Materials - Property of IBM
//
package com.devwebsphere.wxsutils.uuid;


import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.WXSUtils;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;

/**
 * This generates unique string identifiers in a grid. These are guaranteed unique
 * unless the grid loses data. This requires a Map that will contain int/long pairs. This
 * map must be synchronously replicated to guarantee no duplicates.
 *
 */
public class WXSUUID 
{
	static Logger logger = Logger.getLogger(WXSUUID.class.getName());
	
	private static volatile Integer partitionId = null;
	private static long assignedClusterId;
	private static String prefix;
	private static AtomicLong currentLocalId = new AtomicLong();
	
	/**
	 * Construct a UUID generator. UUID counters are stored in the Map mapName
	 * The map has an integer key and a long value
	 * @param grid
	 * @param mapName
	 * @throws ObjectGridException
	 */
	public WXSUUID(ObjectGrid grid, String mapName)
		throws ObjectGridException
	{
		initPrefix(grid, mapName);
	}

	void initPrefix(ObjectGrid grid, String mapName)
		throws ObjectGridException
	{
		if(logger.isLoggable(Level.FINE))
		{
			logger.fine( "Pulling prefix from grid");
		}
		Session sess = grid.getSession();
		while(true)
		{
			try
			{
				BackingMap bmap = grid.getMap(mapName);
				Random r = new Random(System.currentTimeMillis());
				partitionId = new Integer(r.nextInt(bmap.getPartitionManager().getNumOfPartitions()));
				sess.begin();
				ObjectMap map = sess.getMap(mapName);
				Long partitionIdValue = (Long)map.getForUpdate(partitionId);
				if(partitionIdValue == null)
				{
					partitionIdValue = new Long(Long.MIN_VALUE);
					map.insert(partitionId, partitionIdValue);
				}
				else
				{
					partitionIdValue=new Long(partitionIdValue.longValue() + 1);//rk changed
					map.update(partitionId, partitionIdValue);
				}
				assignedClusterId = partitionIdValue.longValue();
				sess.commit();
				prefix = Long.toString(partitionId) + ":" + Long.toString(assignedClusterId) + ":";
				//System.out.println("Prefix for cluster uuid is " + prefix);
				if(logger.isLoggable(Level.FINE))
				{
				   logger.fine( "Prefix for cluster uuid is " + prefix);						   
				}
				break;
			}
			catch(Exception e)
			{
				if(sess.isTransactionActive())
				{
					sess.rollback();
				}
				if(WXSUtils.isRetryable(e))
				{							
					continue;
				}
				else
				{
					logger.log(Level.SEVERE, "getNextClusterID exception", e);
				}
			}
		}
	}
	/**
	 * This returns a grid wide UUID.
	 */
	public String getNextClusterUUID()
		throws ObjectGridRuntimeException
	{
		String rc = prefix + Long.toString(currentLocalId.getAndIncrement());
		if( logger.isLoggable(Level.FINE))
		{
			logger.fine( "New UUID is " + rc);
		}
		return rc;
	}
}
