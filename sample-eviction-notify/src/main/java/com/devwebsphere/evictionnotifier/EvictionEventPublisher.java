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
package com.devwebsphere.evictionnotifier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.plugins.LogElement;
import com.ibm.websphere.objectgrid.plugins.LogSequence;
import com.ibm.websphere.objectgrid.plugins.ObjectGridEventListener;

/**
 * This is an ObjectGridEventListener that must be registered with a grid. It
 * takes a list of maps to watch for evicted entries. Any evicted entries are
 * inserted in to the queue map. The queue map is named as a property also.
 * 
 * This places evicted entries in a map which clients can subscribe to. Every
 * event is delivered to every client. Events may be seen multiple times by clients
 * and clients will have a time window to see events at all. This time window is specified
 * by the TTL on the Q map.
 */

public class EvictionEventPublisher implements ObjectGridEventListener 
{
	ObjectGrid grid;
	Set<String> watchMapList = new HashSet<String>();

	/**
	 * This must be set by user to be a comma seperated list
	 * of map names to watch for evictions
	 */
	String mapNameList;

	/**
	 * This is the eviction queue map name. All eviction events are
	 * stored in this map.
	 */
	String evictQMapName;
	List<LogElement.Type> typeFilter;
	
	public void destroy() {
		// TODO Auto-generated method stub

	}

	/**
	 * This is called when the grid is started. It validates the properties on the object
	 * to make sure they are valid.
	 */
	public void initialize(Session sess) 
	{
		// initialize filter to select only evict records
		typeFilter = new ArrayList<LogElement.Type>();
		typeFilter.add(LogElement.EVICT);

		// need the grid to write evict records
		grid = sess.getObjectGrid();
		
		// only do this when on a server or local grid
		if(grid.getObjectGridType() == ObjectGrid.SERVER || grid.getObjectGridType() == ObjectGrid.LOCAL)
		{
			if(mapNameList == null)
				throw new ObjectGridRuntimeException("mapNameList must be specified");
			StringTokenizer tok = new StringTokenizer(mapNameList, ",");
			while(tok.hasMoreElements())
			{
				String m = tok.nextToken();
				BackingMap bmap = grid.getMap(m);
				if(bmap == null)
					throw new ObjectGridRuntimeException("Unknown Map:" + m);
				watchMapList.add(m);
			}
			if(evictQMapName == null)
				throw new ObjectGridRuntimeException("evictQMapName must be specified");
			BackingMap bmap = grid.getMap(evictQMapName);
			if(bmap == null)
				throw new ObjectGridRuntimeException("Unknown evictQ Map:" + evictQMapName);
		}
	}

	public void transactionBegin(String arg0, boolean arg1) {
	}

	/**
	 * This is called whenever a transaction is committed. For server shards only, we look for
	 * any evicted entries in maps that it's configured to watch.
	 */
	public void transactionEnd(String tx_id, boolean arg1, boolean arg2,
			Collection rawList) 
	{
		// only watch transactions on the grid server side or if it's a local grid
		if(grid.getObjectGridType() == ObjectGrid.SERVER || grid.getObjectGridType() == ObjectGrid.LOCAL)
		{
			try
			{
				Collection<LogSequence> list = (Collection<LogSequence>)rawList;
				
				Session sess = null;
				ObjectMap evictQ = null;
				// this is a LogSequence for each Map modified in the transaction
				int counter = 0;
				for(LogSequence l : list)
				{
					String mapName = l.getMapName();
					// if this is a map we're watching then
					if(watchMapList.contains(mapName))
					{
						// get all the evicted entries in this transaction
						Iterator<LogElement> iter = l.getChangesByTypes(typeFilter);
						while(iter.hasNext())
						{
							LogElement le = iter.next();
							if(sess == null) 
							{
								// create a session if we don't have one
								sess = grid.getSession();
								evictQ = sess.getMap(evictQMapName);
								sess.begin();
							}
							
							// create an EvictEntry and insert it in the q map.
							EvictEntry ee = new EvictEntry();
							ee.key = (Serializable)le.getCacheEntry().getKey();
							ee.value = (Serializable)le.getBeforeImage();
							ee.mapName = mapName;
							
							// create a key
							StringBuilder key = new StringBuilder();
							key.append(tx_id); key.append(":");
							key.append(counter++);
							evictQ.insert(key.toString(), ee);
						}
					}
				}
				// commit the transaction if we have one
				if(sess != null)
					sess.commit();
			}
			catch(ObjectGridException e)
			{
				throw new ObjectGridRuntimeException(e);
			}
		}
	}

	public final String getMapNameList() {
		return mapNameList;
	}

	public final void setMapNameList(String mapNameList) {
		this.mapNameList = mapNameList;
	}

	public final String getEvictQMapName() {
		return evictQMapName;
	}

	/**
	 * The name of the map used as a queue for evicted entries.
	 * @param evictQMapName
	 */
	public final void setEvictQMapName(String evictQMapName) {
		this.evictQMapName = evictQMapName;
	}

}
