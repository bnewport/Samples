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
package com.devwebsphere.wxsutils.multijob.pingall;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.multijob.JobExecutor;
import com.devwebsphere.wxsutils.multijob.MultipartTask;
import com.devwebsphere.wxsutils.multijob.SinglePartTask;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.Session;

/**
 * This simply invokes some code on every partition. This can be useful
 * if firewalls will close inactive sockets after some time period. Executing
 * this task will touch every container and so long as this is done within
 * the firewall timeout period then sockets to the grid containers can
 * be kept alive.
 * @author bnewport
 * @see PingAllPartitionsJob#visitAllPartitions(ObjectGrid)
 *
 */
public class PingAllPartitionsJob implements MultipartTask<Boolean, Boolean>
{
	static Logger logger = Logger.getLogger(PingAllPartitionsJob.class.getName());

	/**
	 * This is called to convert from the network form to the
	 * client form. Its the same in this case.
	 */
	public Boolean extractResult(Boolean rawRC) 
	{
		return rawRC;
	}

	static public class PingSinglePartitionTask implements SinglePartTask<Boolean, Boolean>
	{
		static Logger logger = Logger.getLogger(PingSinglePartitionTask.class.getName());
		/**
		 * 
		 */
		private static final long serialVersionUID = 1722977140374061823L;
		
		public PingSinglePartitionTask() {}

		/**
		 * This can be called to check if a partition result is
		 * empty and not interesting for clients
		 */
		public boolean isResultEmpty(Boolean result) 
		{
			return true;
		}

		public Boolean process(Session sess) 
		{
			String aMap = (String)sess.getObjectGrid().getListOfMapNames().iterator().next();
			int partitionId = sess.getObjectGrid().getMap(aMap).getPartitionId();
			logger.log(Level.INFO, "Ping to partition " + partitionId);
			return Boolean.TRUE;
		}

	}
	
	JobExecutor<Boolean, Boolean> je;
	
	public PingAllPartitionsJob(ObjectGrid ogclient)
	{
		je = new JobExecutor<Boolean, Boolean>(ogclient, this);
	}
	
	public SinglePartTask<Boolean, Boolean> createTaskForPartition(
			SinglePartTask<Boolean, Boolean> previousTask) {
		// prevtask is null when called for first time for a partition
		if(previousTask == null)
		{
			PingSinglePartitionTask t = new PingSinglePartitionTask();
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
	public Boolean getNextResult()
	{
		return je.getNextResult();
	}
	
	public JobExecutor<Boolean, Boolean> getJE()
	{
		return je;
	}

	/**
	 * This will visit all partitions for the specified client
	 * grid connections
	 * @param ogClient The grid to visit
	 * @return # of partitions visited
	 */
	static public int visitAllPartitions(ObjectGrid ogClient)
	{
		PingAllPartitionsJob job = new PingAllPartitionsJob(ogClient);
		int count = 0;
		while(job.getJE().getNextResult() != null) count++;
		return count;
	}
}
