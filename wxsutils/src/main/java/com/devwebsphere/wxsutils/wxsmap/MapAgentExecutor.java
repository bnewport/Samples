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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;
import com.ibm.websphere.objectgrid.datagrid.ReduceGridAgent;

/**
 * This takes an list of Key/Agent pairs and then invokes the agent for each corresponding key and returns
 * a Map with the agent result for each key if not null
 */
public class MapAgentExecutor<K, A extends MapGridAgent, X> implements ReduceGridAgent 
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

	public Object reduce(Session sess, ObjectMap map, Collection arg2) {
		try
		{
			Map<K,X> results = new HashMap<K, X>();
			for(K k : batch.keySet())
			{
				A agent = batch.get(k);
				X x = (X) agent.process(sess, map, k);
				if(x != null)
					results.put(k, x);
			}
			return results;
		}
		catch(Exception e)
		{
			throw new ObjectGridRuntimeException(e);
		}
	}

	public Object reduceResults(Collection arg0) 
	{
		Collection<Map<K,X>> list = arg0;
		Map<K,X> rc = new HashMap<K, X>();
		for(Map<K,X> m : list)
		{
			rc.putAll(m);
		}
		return rc;
	}
	
	public MapAgentExecutor()
	{
 	}
}
