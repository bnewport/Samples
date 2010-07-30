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
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.ReduceGridAgent;

/**
 * This is used to insert a chunk of records for a given partition using a single hop.
 * It returns TRUE if the operation succeeded. It works two ways. An insert style 
 * and a put style. The insert style just does an insert. It doesn't check if the
 * record existed already. The put style always does a get first and if it exists then
 * do an update, if it didn't exist then do an insert. A Put is especially expensive when
 * a Loader is plugged in to the Map as the get may trigger a SQL SELECT for each key
 * potentially.
 * 
 * Applications preloading data from a database in to the grid should use insertAll. The grid
 * maps need to be empty in this case so the application should do a clear on the grid first
 * 
 * @see WXSUtils#insertAll(java.util.Map, com.ibm.websphere.objectgrid.BackingMap)
 * @see WXSUtils#putAll(java.util.Map, com.ibm.websphere.objectgrid.BackingMap)
 */
public class InsertAgent<K,V> implements ReduceGridAgent 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6568906743945108310L;
	
	java.util.Map<K,V> batch;
	
	/**
	 * For a preload especially with a Loader plugged in then we don't want to do a get
	 */
	boolean doGet;

	public Object reduce(Session sess, ObjectMap map) 
	{
		return null;
	}

	public Object reduce(Session sess, ObjectMap map, Collection arg2) {
		AgentMBeanImpl agent = WXSUtils.getAgentMBeanManager().getBean(sess.getObjectGrid().getName(), this.getClass().getName());
		long startNS = System.nanoTime();
		try
		{
			Session s = sess.getObjectGrid().getSession();
			ObjectMap m = s.getMap(map.getName());
			s.beginNoWriteThrough();
			ArrayList keys = new ArrayList(batch.keySet());
			if(doGet)
			{
				// do a get first
				m.getAll(keys);
			}
			// then do a put. Just a put won't work as it will treat
			// all entries as inserts.
			m.putAll(batch);
			s.commit();
			agent.getKeysMetric().logTime(System.nanoTime() - startNS);
			return Boolean.TRUE;
		}
		catch(Exception e)
		{
			e.printStackTrace();
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
