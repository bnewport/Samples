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
package com.devwebsphere.manualpartition.data.keyutils;

import com.devwebsphere.manualpartition.data.MyKey;

/**
 * This is an object whose hash code is a specific partition. This is returned as the routing object
 * from MyKey
 * @see MyKey#ibmGetPartition()
 */
public class FixedHashInteger 
{
	/**
	 * The partition to use
	 */
	int partition;
	
	public FixedHashInteger(int p)
	{
		partition = p;
	}

	/**
	 * WXS calls this and modulos it with the number of partitions.
	 */
	public int hashCode()
	{
		return partition;
	}
}