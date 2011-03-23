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

/**
 * This is returned from the SingleTask to the MultiTask. The intent is for
 * the current singletask to return the bucket in use to the multitask.
 * @author bnewport
 *
 * @param <V>
 */
public class PartitionResult<V extends Serializable> implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3257173697785502710L;
	
	/**
	 * The set of keys from the current bucket
	 */
	ArrayList<V> result;
	/**
	 * The current bucket. The next bucket to visit is this plus one.
	 */
	int nextBucket;
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder("PartitionResult<[");
		for(V v : result)
		{
			sb.append(v.toString());
			sb.append(",");
		}
		sb.append("], next=");
		sb.append(Integer.toString(nextBucket));
		return sb.toString();
	}
}
