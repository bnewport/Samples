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
package com.devwebsphere.wxsutils;

import java.util.ArrayList;
import java.util.Collection;

import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanImpl;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanManager;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.ReduceGridAgent;

/**
 * This is used to insert a chunk of records for a given partition using a single hop.
 * It returns TRUE if the operation succeeded
 */
public class InsertAgent<K,V> implements ReduceGridAgent 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6568906743945108310L;
	
	java.util.Map<K,V> batch;

	public Object reduce(Session sess, ObjectMap map) 
	{
		return null;
	}

	public Object reduce(Session sess, ObjectMap map, Collection arg2) {
		AgentMBeanImpl agent = AgentMBeanManager.getBean(this.getClass().getName());
		long startNS = System.nanoTime();
		try
		{
			Session s = sess.getObjectGrid().getSession();
			ObjectMap m = s.getMap(map.getName());
			s.beginNoWriteThrough();
			ArrayList keys = new ArrayList(batch.keySet());
			// do a get first
			m.getAll(keys);
			// then do a put. Just a put won't work as it will treat
			// all entries as inserts.
			m.putAll(batch);
			s.commit();
			agent.getKeysMetric().logTime(System.nanoTime() - startNS);
			return Boolean.TRUE;
		}
		catch(Exception e)
		{
			agent.getKeysMetric().logException(e);
			return Boolean.FALSE;
		}
	}

	/**
	 * Combine the Boolean results of the process calls using
	 * AND
	 */
	public Object reduceResults(Collection arg0) 
	{
		boolean rc = true;
		for(Object o : arg0)
		{
			if(o instanceof Boolean)
			{
				Boolean b = (Boolean)o;
				rc = rc && b;
			}
			else
			{
				rc = false;
			}
			if(!rc) break;
		}
		return rc;
	}
	
	public InsertAgent()
	{
 	}
}
