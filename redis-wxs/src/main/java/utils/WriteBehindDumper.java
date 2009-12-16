//
//This sample program is provided AS IS and may be used, executed, copied and
//modified without royalty payment by customer (a) for its own instruction and
//study, (b) in order to develop applications designed to run with an IBM
//WebSphere product, either for customer's own internal use or for redistribution
//by customer, as part of such an application, in customer's own products. "
//
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2009
//All Rights Reserved * Licensed Materials - Property of IBM
//
package utils;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.UndefinedMapException;
import com.ibm.websphere.objectgrid.plugins.ObjectGridEventGroup;
import com.ibm.websphere.objectgrid.writebehind.FailedUpdateElement;
import com.ibm.websphere.objectgrid.writebehind.WriteBehindLoaderConstants;

/**
 * Write behind expects transactions to the Loader to succeed. If a transaction for a key fails then
 * it inserts an entry in a Map called PREFIX + mapName. The application should be checking this
 * map for entries to dump out write behind transaction failures. The application is responsible for
 * analyzing and then removing these entries. These entries can be large as they include the key, before
 * and after images of the value and the exception itself. Exceptions can easily be 20k on their own.
 * 
 * The class is registered with the grid and an instance is created per primary shard in a JVM. It creates a single thread
 * and that thread then checks each write behind error map for the shard, prints out the problem and then removes the
 * entry.
 * 
 * This means there will be one thread per shard. If the shard is moved to another JVM then the deactivate method stops the
 * thread.
 * @author bnewport
 *
 */
public class WriteBehindDumper implements ObjectGridEventGroup.ShardEvents, Callable<WriteBehindDumper>
{
	volatile boolean isShardActive;
	Future<WriteBehindDumper> thread;
	ObjectGrid grid;
	ScheduledExecutorService pool;

	/**
	 * When a primary shard is activated then make one thread that will periodically check
	 * the write behind error maps and print out any problems
	 */
	public void shardActivated(ObjectGrid grid) 
	{
		isShardActive = true;
		this.grid = grid;
		thread = pool.schedule(this, 20, TimeUnit.SECONDS);
	}

	public void shardDeactivate(ObjectGrid arg0) 
	{
		// signal the thread to stop
		isShardActive = false;
		thread.cancel(false);
		while(!thread.isDone())
		{
			try
			{
				Thread.sleep(500);
			}
			catch(InterruptedException e)
			{
			}
		}
	}

	/**
	 * Simple test to see if the map has write behind enabled and if so then return
	 * the name of the error map for it.
	 * @param mapName The map to test
	 * @return The name of the write behind error map if it exists otherwise null
	 */
	public String getWriteBehindNameIfPossible(String mapName)
	{
		BackingMap map = grid.getMap(mapName);
		if(map.getWriteBehind() != null)
		{
			return WriteBehindLoaderConstants.WRITE_BEHIND_FAILED_UPDATES_MAP_PREFIX + mapName;
		}
		else
			return null;
	}

	/**
	 * This runs for each shard. It checks if each map has write behind enabled and if it does then it prints out any write behind
	 * transaction errors and then removes the record.
	 */
	
	public WriteBehindDumper call() throws Exception
	{
		if(isShardActive)
		{
			long blockTimeMS = 20000L;
			try
			{
				Session sess = grid.getSession();
				// only user defined maps are returned here, no system maps like write behind maps are in
				// this list.
				Iterator<String> iter = grid.getListOfMapNames().iterator();
				boolean foundErrors = false;
				// iterate over all the current Maps
				while(iter.hasNext())
				{
					String origName = iter.next();
					
					// if it's a write behind error map
					String name = getWriteBehindNameIfPossible(origName);
					if(name != null) 
					{
						// try to remove blocks of N errors at a time
						ObjectMap errorMap = null;
						try
						{
							errorMap = sess.getMap(name);
						}
						catch(UndefinedMapException e)
						{
							// at startup, the error maps may not exist yet, patience...
							continue;
						}
						// try to dump out up to ten records at once
						sess.begin();
						for(int counter = 0; counter < 100; ++counter)
						{
							Integer seqKey = (Integer)errorMap.getNextKey(1L);
							if(seqKey != null)
							{
								foundErrors = true;
								FailedUpdateElement elem = (FailedUpdateElement)errorMap.get(seqKey);
								System.out.println("WriteBehind Dumper ( " + origName + ") for key (" + elem.getCacheEntry().getKey() + ") Exception: " + elem.getThrowable().toString());
								errorMap.remove(seqKey);
								counter--;
							}
							else
								break;
						}
						sess.commit();
					}
					// loop faster if there are errors
					if(foundErrors)
						blockTimeMS = 1000L;
				}
			}
			catch(ObjectGridException e)
			{
				System.out.println("Exception " + e.toString());
				e.printStackTrace();
			}
			thread = pool.schedule(this, blockTimeMS, TimeUnit.MILLISECONDS);
		}
		return this;
	}

	public ScheduledExecutorService getPool() {
		return pool;
	}

	public void setPool(ScheduledExecutorService pool) {
		this.pool = pool;
	}
}
