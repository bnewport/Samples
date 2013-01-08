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
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.multijob.JobExecutor;
import com.devwebsphere.wxsutils.multijob.MultipartTask;
import com.devwebsphere.wxsutils.multijob.PartitionIterator;
import com.devwebsphere.wxsutils.multijob.PartitionIterators;
import com.devwebsphere.wxsutils.multijob.SinglePartTask;
import com.devwebsphere.wxsutils.wxsmap.BigListPushAgent;
import com.devwebsphere.wxsutils.wxsmap.SetAddRemoveAgent;
import com.devwebsphere.wxsutils.wxsmap.WXSMapOfBigListsImpl;
import com.ibm.websphere.objectgrid.ObjectGrid;

/**
 * This will pull dirty lists from a specific dirty set. It returns the keys for the lists which are
 * dirty.
 * 
 * @author bnewport
 * @see FetchJobsFromAllDirtyListsJob#getAllDirtyKeysInGrid(ObjectGrid, String, Serializable)
 * 
 * @param <K>
 * @param <V>
 */
public class FetchJobsFromAllDirtyListsJob<K extends Serializable, V extends Serializable> implements
		MultipartTask<PartitionResult<V>, ArrayList<DirtyKey<V>>> {
	static Logger logger = Logger.getLogger(FetchJobsFromAllDirtyListsJob.class.getName());

	/**
	 * extractResult will set this to the last bucket visited on the grid side during the last call.
	 * The next iteration starts at this plus one.
	 */
	int lastVisitedBucket;
	long leaseTimeMS = 0;

	/**
	 * The is called to extract the current partition result from the returned PartitionResult. It
	 * also pulls out the current bucket in use in this call also. This is called once per
	 * SingleTask call by the loops.
	 */
	public ArrayList<DirtyKey<V>> extractResult(PartitionResult<V> rawRC) {
		// The next bucket visited is this plus one
		lastVisitedBucket = rawRC.nextBucket;
		return rawRC.result;
	}

	JobExecutor<PartitionResult<V>, ArrayList<DirtyKey<V>>> je;
	K setKey;
	String listMapName;

	int desiredMaxKeysPerTrip = Integer.MAX_VALUE;

	/**
	 * Setting this to a non zero value will 'lock' dirty keys from being returned to other clients
	 * for this amount of time. If the dirty key is emptied before then the lock is removed
	 * automatically
	 * 
	 * @param x
	 */
	public void setLeaseTimeMS(long x) {
		leaseTimeMS = x;
	}

	public FetchJobsFromAllDirtyListsJob(ObjectGrid ogclient, String listMapName, K setKey) {
		this(ogclient, listMapName, setKey, PartitionIterators.descending(ogclient));
	}
	
	public FetchJobsFromAllDirtyListsJob(ObjectGrid ogclient, String listMapName, K setKey, PartitionIterator partitionIter) {
		this.setKey = setKey;
		this.listMapName = listMapName;
		je = new JobExecutor<PartitionResult<V>, ArrayList<DirtyKey<V>>>(ogclient, this, partitionIter);
	}

	public SinglePartTask<PartitionResult<V>, ArrayList<DirtyKey<V>>> createTaskForPartition(
			SinglePartTask<PartitionResult<V>, ArrayList<DirtyKey<V>>> previousTask) {
		// prevtask is null when called for first time for a partition
		if (previousTask == null) {
			// start fetching buckets at bucket 0
			FetchDirtyJobsInPartitionTask<K, V> t = new FetchDirtyJobsInPartitionTask<K, V>();
			t.setKey = setKey;
			t.setMapName = BigListPushAgent.getDirtySetMapNameForListMap(listMapName);
			t.nextBucket = 0;
			t.desiredMaxKeys = desiredMaxKeysPerTrip;
			t.leaseMapName = BigListPushAgent.getDirtySetLockMapNameForListMap(listMapName);
			t.leasePeriodMS = leaseTimeMS;
			return t;
		} else {
			if (lastVisitedBucket < SetAddRemoveAgent.NUM_BUCKETS) {
				// keep going on this partition at the next set bucket
				FetchDirtyJobsInPartitionTask<K, V> t = new FetchDirtyJobsInPartitionTask<K, V>();
				t.setKey = setKey;
				t.setMapName = BigListPushAgent.getDirtySetMapNameForListMap(listMapName);
				t.nextBucket = lastVisitedBucket;
				t.desiredMaxKeys = desiredMaxKeysPerTrip;
				t.leaseMapName = BigListPushAgent.getDirtySetLockMapNameForListMap(listMapName);
				t.leasePeriodMS = leaseTimeMS;
				return t;
			} else {
				// time to move to the next partition
				return null;
			}
		}
	}

	/**
	 * This is just a delegate to the JobExecutor. This can return Lists of zero size. Only a null
	 * return indicates the end of the operation. This returns all keys in a partition at a time.
	 * This can be potentially expensive as well as holding a lock on the dirty set during that
	 * time. Pushes using keys for the next partition will block during this time.
	 * 
	 * @return
	 */
	public ArrayList<DirtyKey<V>> getNextResult() {
		desiredMaxKeysPerTrip = Integer.MAX_VALUE;
		return je.getNextResult();
	}

	/**
	 * This is just a delegate to the JobExecutor. This can return Lists of zero size. Only a null
	 * return indicates the end of the operation. This method will attempt to fetch keys in blocks
	 * of at least maxDesiredKeys. This can often ways to limit the time the dirty set is locked.
	 * This can improve concurrency between pullers fetching dirty keys and pushing.
	 * 
	 * @return
	 */
	public ArrayList<DirtyKey<V>> getNextResult(int maxDesiredKeys) {
		if (maxDesiredKeys <= 0) {
			logger.log(Level.WARNING, "maxDesired Keys should be > 0");
			maxDesiredKeys = 1000;
		}
		desiredMaxKeysPerTrip = maxDesiredKeys;
		return je.getNextResult();
	}

	/**
	 * This will visit all partitions for the specified client grid connections
	 * 
	 * @param ogClient
	 *            The grid to visit
	 * @return # of partitions visited
	 */
	static public <K extends Serializable, V extends Serializable> int visitAllPartitions(ObjectGrid ogClient, String listMapName,
			String leaseMapName, K dirtyKey) {
		FetchJobsFromAllDirtyListsJob<K, V> job = new FetchJobsFromAllDirtyListsJob<K, V>(ogClient, listMapName, dirtyKey);
		int count = 0;
		while (true) {
			ArrayList<DirtyKey<V>> list = job.getNextResult();
			if (list == null)
				break;
			++count;
			// jobs retrieved in list, can be empty
		}
		return count;
	}

	public static <K extends Serializable, V extends Serializable> List<V> getAllDirtyKeysInGrid(ObjectGrid ogClient, String listName, K dirtyKey) {
		return getAllDirtyKeysInGrid(ogClient, listName, dirtyKey, 0L);
	}

	/**
	 * This code illustrates how to get a ALL of the dirty keys from the grid. It just repeatedly
	 * calls getNextResult until it returns null. Then it has visited every partition. This will
	 * return all the keys in a bucket until every bucket in the grid has been examined. Empty
	 * buckets are skipped. This isn't really meant to be called by an application. This returns ALL
	 * the dirty keys. Imagine a grid with 1m dirty keys in it. How much heap space would a Set of
	 * 1m dirty keys take? How long to serialize? Hence, copy the method below in to your
	 * application and process the returns keys from getNextResult immediately before looping around
	 * to call getNextResult again. The keys are returned in FIFO order. The first list to be pushed
	 * is at the beginning of the list. Note, this is a per fetch only ordering. If you fetch only
	 * 5k keys then the list is ordered only with respect to the keys actually returned.
	 * 
	 * @param <K>
	 * @param <V>
	 * @param ogClient
	 * @param listMapName
	 * @param dirtyKey
	 * @param leaseTimeMS
	 *            This is the time a dirty key will be 'locked' for this client before being
	 *            provided to other clients
	 * @return
	 */
	public static <K extends Serializable, V extends Serializable> List<V> getAllDirtyKeysInGrid(ObjectGrid ogClient, String listName, K dirtyKey,
			long leaseTimeMS) {
		// You need a new one of these for each whole grid iteration. Once it getNextResult returns
		// null then make a new one
		String listHeadMapName = WXSMapOfBigListsImpl.getListHeadMapName(listName);
		FetchJobsFromAllDirtyListsJob<K, V> job = new FetchJobsFromAllDirtyListsJob<K, V>(ogClient, listHeadMapName, dirtyKey);
		job.setLeaseTimeMS(leaseTimeMS);

		// we'll add all the dirty list keys to this set
		TreeSet<DirtyKey<V>> result = new TreeSet<DirtyKey<V>>();
		while (true) {
			// get the next block of dirty list keys
			// this array r is sorted in first dirtied order
			ArrayList<DirtyKey<V>> r = job.getNextResult();
			// when r is null then the whole grid has been checked
			if (r != null) {
				result.addAll(r);
			} else
				break;
		}
		// now copy sorted results in to a list
		List<V> rc = new ArrayList<V>(result.size());
		for (DirtyKey<V> v : result)
			rc.add(v.getValue());
		return rc;
	}
}
