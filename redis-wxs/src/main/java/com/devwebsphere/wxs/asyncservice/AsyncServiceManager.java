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
package com.devwebsphere.wxs.asyncservice;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Future;

import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.Session;

/**
 * This allows services to be asynchronously called in a durable manner using exactly once semantics.
 * @author bnewport
 *
 */
public interface AsyncServiceManager 
{
	/**
	 * This returns a cluster wide unique ID. The ID is unique within the lifecycle of the ObjectGrid so long as data loss does not occur. That is
	 * a partitions primary and replicas do not fail simultaneously AND sync replication is used.
	 * @return A unique identifier.
	 * @throws ObjectGridException
	 */
	public String getNextClusterUUID()
		throws ObjectGridRuntimeException;
	
	/**
	 * This sends an async service request and returns a future that allows the result to be retrieved. The message
	 * is sent before the method returns regardless of any active transaction.
	 * @param message The message and handler
	 * @return The Future to retrieve the result later
	 * @throws ObjectGridException
	 */
	public <T> Future<T> sendAsyncMessage(Serializable message)
		throws ObjectGridRuntimeException;
	
	/**
	 * This starts a Job within this JVM if the current transaction commits. The job
	 * will run on the backup JVM if this one fails before it runs so long
	 * as it was 'replicated' over there.
	 * @param localSession The transaction to use for scheduling the job
	 * @param job The job to run
	 * @throws ObjectGridRuntimeException
	 */
	public void scheduleDurableLocalJob(Session localSession, Job job)
		throws ObjectGridRuntimeException;

	/**
	 * This processes the exactly one message on every partition and returns the result of doing this
	 * in a Map keyed by partition id.
	 * @param multicastMessage
	 * @return A map with the result for each partition
	 * @throws ObjectGridException
	 */
	public <T> Future<Map<Integer, T>> sendAsyncAllPartitions(Serializable multicastMessage)
		throws ObjectGridRuntimeException;

	/**
	 * This returns a serializable form of the specific Future. The Future must be one
	 * returned by one of the send methods of this interface.
	 * @param f
	 * @return
	 */
	public <T> SerializedFuture<T> serialize(Future<T> f);
}
