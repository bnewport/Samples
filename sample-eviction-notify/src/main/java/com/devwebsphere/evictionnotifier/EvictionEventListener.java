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

import java.util.ArrayList;
import java.util.Iterator;

import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.query.ObjectQuery;

/**
 * This is a listener class that can be called from a client. The method getNextEntry should
 * be polled to retrieve evicted records. An evicted record may be seen multiple times
 *
 */
public class EvictionEventListener 
{
	ObjectGrid grid;

	String evictQMapName;
	
	int currentPartition;
	ArrayList<EvictEntry> entriesForCurrentPartition;
	Iterator<EvictEntry> iter;
	
	Session sess;
	final int numPartitions;
	final int maxEntriesForPartition;

	/**
	 * This constructs the eviction event listener
	 * @param grid The client grid reference
	 * @param qName The name of the eviction queue map being used by the EvictionEventPublisher plugin
	 * @throws ObjectGridException
	 * @see EvictionEventPublisher
	 */
	public EvictionEventListener(ObjectGrid grid, String qName)
		throws ObjectGridException
	{
		this.grid = grid;
		evictQMapName = qName;
		sess = grid.getSession();
		currentPartition = 0;
		iter = null;
		BackingMap qmap = grid.getMap(evictQMapName);
		if(qmap == null)
			throw new RuntimeException("Queue map doesn't exist in grid: " + evictQMapName);
		numPartitions = qmap.getPartitionManager().getNumOfPartitions();
		maxEntriesForPartition = 2000;
	}

	/**
	 * This is called repeatedly by the client to retrieve the next evicted entry. It iterates
	 * across all partitioned in the grid in a round robin fashion to get the current
	 * list of evicted entries. These entries will be evicted eventually (look at the objectgrid.xml file).
	 * @return An evicted entry or NULL if none exist
	 * @throws ObjectGridException
	 */
	public synchronized EvictEntry getNextEntry()
		throws ObjectGridException
	{
		// return next record from current partition if any
		if(iter != null && iter.hasNext())
		{
			return iter.next(); 
		}
		else
		{
			iter = null;
			// otherwise
			// scan partitions looking for one with entries
			for(int count = 0; count < numPartitions; ++count)
			{
				try
				{
					// move to next partition circular
					currentPartition++;
					currentPartition %= numPartitions;
					// get all eviction entries in that partition
					sess.begin();
					sess.setTransactionIsolation(Session.TRANSACTION_READ_UNCOMMITTED);
					ObjectQuery q = sess.createObjectQuery("SELECT e FROM " + evictQMapName + " e ");
					// target the query at the current partition
					q.setPartition(currentPartition);
					q.setMaxResults(maxEntriesForPartition); // reasonable limit per partition?
					Iterator<EvictEntry> result_iter = q.getResultIterator();
					
					entriesForCurrentPartition = new ArrayList<EvictEntry>(maxEntriesForPartition);
					while(result_iter.hasNext())
						entriesForCurrentPartition.add(result_iter.next());
					
					iter = entriesForCurrentPartition.iterator();
				}
				catch(Exception e)
				{
					iter = null;
				}
				finally
				{
					if(sess.isTransactionActive())
						sess.rollback();
				}
				// if there is an entry return it
				if(iter != null && iter.hasNext())
				{
					return iter.next();
				}
				// otherwise move to next partition
			}
			// we tried every partition so return null for now
			return null;
		}
	}
}
