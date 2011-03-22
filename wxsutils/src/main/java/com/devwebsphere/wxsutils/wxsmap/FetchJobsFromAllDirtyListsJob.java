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
package com.devwebsphere.wxsutils.wxsmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.multijob.JobExecutor;
import com.devwebsphere.wxsutils.multijob.MultipartTask;
import com.devwebsphere.wxsutils.multijob.SinglePartTask;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;

/**
 * This simply invokes some code on every partition. This can be useful
 * if firewalls will close inactive sockets after some time period. Executing
 * this task will touch every container and so long as this is done within
 * the firewall timeout period then sockets to the grid containers can
 * be kept alive.
 * @author bnewport
 * @see FetchJobsFromAllDirtyListsJob#visitAllPartitions(ObjectGrid)
 *
 */
public class FetchJobsFromAllDirtyListsJob <K extends Serializable, V extends Serializable> implements MultipartTask<ArrayList<V>, ArrayList<V>>
{
	static Logger logger = Logger.getLogger(FetchJobsFromAllDirtyListsJob.class.getName());

	static public class FetchDirtyJobsInPartitionTask <K, V> implements SinglePartTask<ArrayList<V>, ArrayList<V>>
	{
		K dirtyKey;
		String listMapName;
		static Logger logger = Logger.getLogger(FetchDirtyJobsInPartitionTask.class.getName());
		/**
		 * 
		 */
		private static final long serialVersionUID = 1722977140374061823L;
		
		public FetchDirtyJobsInPartitionTask() {}

		/**
		 * This is called to convert from the network form to the
		 * client form. Its the same in this case.
		 */
		public ArrayList<V> extractResult(ArrayList<V> rawRC) 
		{
			return rawRC;
		}

		/**
		 * This can be called to check if a partition result is
		 * empty and not interesting for clients
		 */
		public boolean isResultEmpty(ArrayList<V> result) 
		{
			return result.isEmpty();
		}

		public ArrayList<V> process(Session sess) 
		{
			try
			{
				ObjectMap setMap = sess.getMap(BigListPushAgent.getDirtySetMapNameForListMap(listMapName));
				return new ArrayList<V>();
			}
			catch(Exception e)
			{
				logger.log(Level.SEVERE, "Exception", e);
				throw new ObjectGridRuntimeException(e);
			}
		}

	}
	
	JobExecutor<ArrayList<V>, ArrayList<V>> je;
	K dirtyKey;
	String listMapName;
	
	public FetchJobsFromAllDirtyListsJob(ObjectGrid ogclient, String listMapName, K dirtyKey)
	{
		this.dirtyKey = dirtyKey;
		this.listMapName = listMapName;
		je = new JobExecutor<ArrayList<V>, ArrayList<V>>(ogclient, this);
	}
	
	public SinglePartTask<ArrayList<V>, ArrayList<V>> createTaskForPartition(
			SinglePartTask<ArrayList<V>, ArrayList<V>> previousTask) {
		// prevtask is null when called for first time for a partition
		if(previousTask == null)
		{
			FetchDirtyJobsInPartitionTask<K,V> t = new FetchDirtyJobsInPartitionTask<K,V>();
			t.dirtyKey = dirtyKey;
			t.listMapName = listMapName;
			return t;
		}
		else
		{
			// only need one run per partition so return null
			// second time it's called for a partition so it
			// moves to the next partition
			return null;
		}
	}
	
	
	/**
	 * This is just a delegate to the JobExecutor. This can return Maps of zero size. Only
	 * a null return indicates the end of the operation.
	 * @return
	 */
	public ArrayList<V> getNextResult()
	{
		return je.getNextResult();
	}
	
	public JobExecutor<ArrayList<V>, ArrayList<V>> getJE()
	{
		return je;
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
			ArrayList<V> list = job.getJE().getNextResult();
			if(list == null)
				break;
			++count;
			// jobs retrieved in list, can be empty 
		}
		return count;
	}
}
