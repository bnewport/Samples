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
package com.devwebsphere.wxsutils.jmx.agent;

/**
 * This MBean tracks statistics for a specific Agent. The metrics are grouped two ways. The Partition metrics
 * are for all Agent method that don't take keys. These metric interact with the partition as a whole. The Key
 * metrics are for all Agent methods that take a Collection<K> keys.
 *
 */
public interface AgentMBean 
{
	/**
	 * The name of the AgentClass
	 * @return
	 */
	String getClassName();
	
	/**
	 * The server time right now. Makes client side charting more accurate.
	 * @return
	 */
	Long getQueryTimeMS();

	/**
	 * The number of times a partition method was called.
	 * @return
	 */
	Integer getPartitionCount();
	Double getPartitionTimeMinMS();
	Double getPartitionTimeMaxMS();
	Double getPartitionTimeAvgMS();
	Double getPartitionTotalTimeMS();
	Integer getPartitionExceptionCount();
	String getPartitionLastExceptionString();

	Integer getKeysCount();
	Double getKeysTimeMinMS();
	Double getKeysTimeMaxMS();
	Double getKeysTimeAvgMS();
	Double getKeysTotalTimeMS();
	Integer getKeysExceptionCount();
	String getKeysLastExceptionString();
}
