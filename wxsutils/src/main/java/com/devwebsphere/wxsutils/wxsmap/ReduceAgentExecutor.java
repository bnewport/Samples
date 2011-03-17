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
package com.devwebsphere.wxsutils.wxsmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.ReduceGridAgent;

/**
 * This agent is used to execute an Agent instance for each key using a single
 * RPC call.
 *
 * @param <K> The key of the Map
 * @param <A> The agent type to execute.
 */
public class ReduceAgentExecutor<K, A extends ReduceGridAgent> implements ReduceGridAgent 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6568906743945108310L;
	
	public Map<K, A> batch;

	public Object reduce(Session sess, ObjectMap map) 
	{
		return null;
	}

	/**
	 * This is called to execute all the agent/key pairs in batch
	 * within a single transaction;.
	 */
	public Object reduce(Session sess, ObjectMap map, Collection arg2) {
		try
		{
			Object result = null;
			ArrayList<Object> list = new ArrayList(2);
			list.add(null); list.add(null);
			for(K k : batch.keySet())
			{
				A agent = batch.get(k);
				Object x = agent.reduce(sess, map, Collections.singleton(k));
				if(result == null)
					result = x;
				else
				{
					list.set(0, result); list.set(1, x);
					result = agent.reduceResults(list);
				}
			}
			return result;
		}
		catch(Exception e)
		{
			throw new ObjectGridRuntimeException(e);
		}
	}

	/**
	 * This combines the results from reduce operations above
	 * on the client.
	 */
	public Object reduceResults(Collection arg0) 
	{
		K first = batch.keySet().iterator().next();
		A agent = batch.get(first);
		return agent.reduceResults(arg0);
	}
	
	public ReduceAgentExecutor()
	{
 	}
}
