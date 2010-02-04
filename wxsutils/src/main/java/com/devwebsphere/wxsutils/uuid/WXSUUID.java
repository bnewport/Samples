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
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;

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
	public WXSUUID(WXSUtils utils, String mapName)
		throws ObjectGridException
	{
		initPrefix(utils, mapName);
	}

	void initPrefix(WXSUtils utils, String mapName)
	{
		if(logger.isLoggable(Level.FINE))
		{
			logger.fine( "Pulling prefix from grid");
		}
		
		// use a key for random partition
		Random r = new Random(System.currentTimeMillis());
		BackingMap bmap = utils.getObjectGrid().getMap(mapName);
		partitionId = new Integer(r.nextInt(bmap.getPartitionManager().getNumOfPartitions()));
		
		// increment the long in that partition using partition id as key
		Long partitionIdValue = utils.atomic_increment(mapName, partitionId);

		// initialize global unique prefix
		assignedClusterId = partitionIdValue.longValue();
		prefix = Long.toString(partitionId) + ":" + Long.toString(assignedClusterId) + ":";
		if(logger.isLoggable(Level.FINE))
		{
		   logger.fine( "Prefix for cluster uuid is " + prefix);						   
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
