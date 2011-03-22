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

public class PartitionResult<V extends Serializable> implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3257173697785502710L;
	ArrayList<V> result;
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
