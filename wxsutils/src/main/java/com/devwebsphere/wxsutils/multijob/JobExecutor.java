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
package com.devwebsphere.wxsutils.multijob;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.WXSUtils;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.UndefinedMapException;
import com.ibm.websphere.objectgrid.datagrid.AgentManager;
import com.ibm.websphere.objectgrid.datagrid.EntryErrorValue;

/**
 * This takes a MultiPartTask and works with it to execute a series of SinglePartTasks on each partition in the grid.
 * 
 * @author bnewport
 * 
 * @param <R>
 *            The user consumable type returned from each SinglePartTask
 * @param <V>
 *            The raw value returned directly from SinglePartTask
 */
public class JobExecutor<V extends Serializable, R> {
	static Logger logger = Logger.getLogger(JobExecutor.class.getName());
	MultipartTask<V, R> mtask;
	ObjectGrid ogclient;
	PartitionIterator partitionItr;
	int currentPartition = -1;
	SinglePartTask<V, R> currTask;

	/**
	 * This constructs an instance that will use the specified client grid and MultiPartTask
	 * 
	 * @param ogclient
	 * @param m
	 */
	public JobExecutor(ObjectGrid ogclient, MultipartTask<V, R> m) {
		this(ogclient, m, PartitionIterators.descending(ogclient));
	}

	public JobExecutor(ObjectGrid ogclient, MultipartTask<V, R> m, PartitionIterator partitionItr) {
		this.mtask = m;
		this.ogclient = ogclient;
		this.partitionItr = partitionItr;
		currTask = null;
	}

	/**
	 * This is called to get the next set of results. It's possible for implementations to return empty collections as a
	 * return value for this method. This does not mean there is no more data. You should only stop iterating on
	 * getNextResult then it returns null.
	 * 
	 * @return The next block or null if no more data
	 */
	public R getNextResult() {
		try {

			// check if there is another task for the current partition
			if (currTask != null) {
				currTask = mtask.createTaskForPartition(currTask);
			}

			// finished the current partition
			if (currTask == null) {
				// time to move to next partition
				if (partitionItr.hasNext()) {
					currentPartition = partitionItr.next();
					currTask = mtask.createTaskForPartition(null);
				} else {
					// reset executor
					partitionItr.reset();
					currentPartition = -1;
					currTask = null;
					return null;
				}
			}
			// currTask needs to be sent to current partition
			Object key = Integer.valueOf(currentPartition);
			Session sess = ogclient.getSession();
			AgentManager amgr = sess.getMap(WXSUtils.routingMapName).getAgentManager();
			JobAgent<V, R> agent = new JobAgent<V, R>(currTask);
			// invoke the SingleTaskPart on this specified partition
			Map<Integer, Object> agent_result = amgr.callMapAgent(agent, Collections.singleton(key));
			Object value = agent_result.get(key);
			if (value != null && value instanceof EntryErrorValue) {
				logger.log(Level.SEVERE, "Grid side exception occurred: " + value.toString());
				throw new ObjectGridRuntimeException("Grid side exception occurred for key: " + key + ": " + value.toString());
			}

			// extract the user exposed object from the return value
			R r = mtask.extractResult((V) value);
			return r;
		} catch (UndefinedMapException e) {
			logger.log(Level.SEVERE, "The map " + WXSUtils.routingMapName + " MUST be defined in the grid");
			throw new ObjectGridRuntimeException("The map " + WXSUtils.routingMapName + " MUST be defined in the grid!");
		} catch (ObjectGridException e) {
			logger.log(Level.SEVERE, "Unexpected exception", e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	public ObjectGrid getObjectGrid() {
		return ogclient;
	}

}
