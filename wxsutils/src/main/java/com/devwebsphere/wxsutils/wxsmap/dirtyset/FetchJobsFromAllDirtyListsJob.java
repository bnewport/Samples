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
package com.devwebsphere.wxsutils.wxsmap.dirtyset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.WXSMapOfLists;
import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.multijob.JobExecutor;
import com.devwebsphere.wxsutils.multijob.MultipartTask;
import com.devwebsphere.wxsutils.multijob.SinglePartTask;
import com.devwebsphere.wxsutils.wxsmap.BigListPushAgent;
import com.devwebsphere.wxsutils.wxsmap.SetAddRemoveAgent;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;

/**
 * This will pull dirty lists from a specific dirty set. It returns the keys
 * for the lists which are dirty.
 * @author bnewport
 * @see FetchJobsFromAllDirtyListsJob#getAllDirtyKeysInGrid(ObjectGrid, String, Serializable)
 *
 * @param <K>
 * @param <V>
 */
public class FetchJobsFromAllDirtyListsJob <K extends Serializable, V extends Serializable> implements MultipartTask<PartitionResult<V>, ArrayList<V>>
{
	static Logger logger = Logger.getLogger(FetchJobsFromAllDirtyListsJob.class.getName());

	/**
	 * extractResult will set this to the last bucket visited on the grid side during
	 * the last call. The next iteration starts at this plus one.
	 */
	int lastVisitedBucket;

	/**
	 * This is called to convert from the network form to the
	 * client form. Its the same in this case.
	 */
	public ArrayList<V> extractResult(PartitionResult<V> rawRC) 
	{
		lastVisitedBucket = rawRC.nextBucket;
		return rawRC.result;
	}

	/**
	 * This will check a single partition for all keys for the single set called
	 * dirtyKey within this partition. It iterates over every bucket for
	 * that set and skips empty buckets
	 * @author bnewport
	 *
	 * @param <K>
	 * @param <V>
	 */
	static public class FetchDirtyJobsInPartitionTask <K extends Serializable, V extends Serializable> implements SinglePartTask<PartitionResult<V>, ArrayList<V>>
	{
		K dirtyKey;
		String listMapName;
		int nextBucket;
		
		static Logger logger = Logger.getLogger(FetchDirtyJobsInPartitionTask.class.getName());
		/**
		 * 
		 */
		private static final long serialVersionUID = 1722977140374061823L;
		
		public FetchDirtyJobsInPartitionTask() {}

		/**
		 * This can be called to check if a partition result is
		 * empty and not interesting for clients
		 */
		public boolean isResultEmpty(ArrayList<V> result) 
		{
			return result.isEmpty();
		}

		/**
		 * This will start at nextBucket and scan it and subsequent buckets for non empty set.
		 * The result is a PartitionResult that indicates the last bucket checked
		 * and the keys found if any.
		 */
		public PartitionResult<V> process(Session sess) 
		{
			try
			{
				ObjectMap setMap = sess.getMap(BigListPushAgent.getDirtySetMapNameForListMap(listMapName));
				Set<V> set = null;
				int i = nextBucket;
				for(i = nextBucket; i < SetAddRemoveAgent.NUM_BUCKETS; ++i)
				{
					set = (Set<V>)setMap.get(SetAddRemoveAgent.getBucketKeyForBucket(dirtyKey, i));
					if(set != null)
					{
						break;
					}
				}
				PartitionResult<V> rc = new PartitionResult<V>();
				if(set != null)
					rc.result = new ArrayList<V>(set);
				else
					rc.result = new ArrayList<V>();
				rc.nextBucket = i;
				return rc;
			}
			catch(Exception e)
			{
				logger.log(Level.SEVERE, "Exception", e);
				throw new ObjectGridRuntimeException(e);
			}
		}

	}
	
	JobExecutor<PartitionResult<V>, ArrayList<V>> je;
	K dirtyKey;
	String listMapName;
	
	public FetchJobsFromAllDirtyListsJob(ObjectGrid ogclient, String listMapName, K dirtyKey)
	{
		this.dirtyKey = dirtyKey;
		this.listMapName = listMapName;
		je = new JobExecutor<PartitionResult<V>, ArrayList<V>>(ogclient, this);
	}
	
	public SinglePartTask<PartitionResult<V>, ArrayList<V>> createTaskForPartition(
			SinglePartTask<PartitionResult<V>, ArrayList<V>> previousTask) 
	{
		// prevtask is null when called for first time for a partition
		if(previousTask == null)
		{
			// start fetching buckets at bucket 0
			FetchDirtyJobsInPartitionTask<K,V> t = new FetchDirtyJobsInPartitionTask<K,V>();
			t.dirtyKey = dirtyKey;
			t.listMapName = listMapName;
			t.nextBucket = 0;
			return t;
		}
		else
		{
			if(lastVisitedBucket < SetAddRemoveAgent.NUM_BUCKETS)
			{
				// keep going on this partition at the next set bucket
				FetchDirtyJobsInPartitionTask<K,V> t = new FetchDirtyJobsInPartitionTask<K,V>();
				t.dirtyKey = dirtyKey;
				t.listMapName = listMapName;
				t.nextBucket = lastVisitedBucket + 1;
				return t;
			}
			else
			{
				// time to move to the next partition
				return null;
			}
		}
	}
	
	
	/**
	 * This is just a delegate to the JobExecutor. This can return Lists of zero size. Only
	 * a null return indicates the end of the operation.
	 * @return
	 */
	public ArrayList<V> getNextResult()
	{
		return je.getNextResult();
	}
	
	/**
	 * This will visit all partitions for the specified client
	 * grid connections
	 * @param ogClient The grid to visit
	 * @return # of partitions visited
	 */
	static public <K extends Serializable,V extends Serializable> int visitAllPartitions(ObjectGrid ogClient, String listMapName, K dirtyKey)
	{
		FetchJobsFromAllDirtyListsJob<K,V> job = new FetchJobsFromAllDirtyListsJob<K,V>(ogClient, listMapName, dirtyKey);
		int count = 0;
		while(true)
		{
			ArrayList<V> list = job.getNextResult();
			if(list == null)
				break;
			++count;
			// jobs retrieved in list, can be empty 
		}
		return count;
	}
	
	/**
	 * This code illustrates how to get a ALL of the dirty keys from the grid. It just
	 * repeatedly calls getNextResult until it returns null. Then it has visited every
	 * partition. This will return all the keys in a bucket until every bucket in the
	 * grid has been examined. Empty buckets are skipped
	 * @param <K>
	 * @param <V>
	 * @param ogClient
	 * @param listMapName
	 * @param dirtyKey
	 * @return
	 */
	public static <K extends Serializable, V extends Serializable> Set<V> getAllDirtyKeysInGrid(ObjectGrid ogClient, String listMapName, K dirtyKey)
	{
		WXSUtils utils = new WXSUtils(ogClient);
//		WXSMapOfLists<V, String> listMap = utils.getMapOfLists("BigList");
		
		// You need a new one of these for each whole grid iteration. Once it getNextResult returns
		// null then make a new one
		FetchJobsFromAllDirtyListsJob<K, V> job = new FetchJobsFromAllDirtyListsJob<K, V>(ogClient, listMapName, dirtyKey);

		// we'll add all the dirty list keys to this set
		Set<V> result = new HashSet<V>();
		while(true)
		{
			// get the next block of dirty list keys
			ArrayList<V> r = job.getNextResult();
			// when r is null then the whole grid has been checked
			if(r != null)
			{
				for(V listKey : r)
				{
//					String theJob = listMap.rpop(listKey);
					// do something with theJob
				}
				// add to set
				result.addAll(r);
			}
			else
				break;
		}
		// job is now of no use, do not reuse it, create a new one
		return result;
	}
}
