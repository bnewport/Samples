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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.jmx.wxsmap.WXSMapMBeanImpl;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.ReduceGridAgent;

public class LockAgent<K,V> implements ReduceGridAgent 
{
	static Logger logger = Logger.getLogger(LockAgent.class.getName());
	/**
	 * 
	 */
	private static final long serialVersionUID = 6568906743945108310L;
	
	public K key;
	public V value;
	int timeOutMS;
	
	boolean isLock;
	
	public Object reduce(Session sess, ObjectMap map) 
	{
		return null;
	}

	public Object reduce(Session sess, ObjectMap map, Collection arg2) {
		WXSMapMBeanImpl mbean = WXSUtils.getWXSMapMBeanManager().getBean(sess.getObjectGrid().getName(), map.getName());
		long startNS = System.nanoTime();
		try
		{
			mbean.getLockMetrics().logTime(System.nanoTime() - startNS);
			V old = (V)map.getForUpdate(key);
			Boolean rc = Boolean.FALSE;
			if(isLock)
			{
				map.setLockTimeout(timeOutMS);
				if(old == null)
				{
					map.insert(key, value);
					rc = Boolean.TRUE;
				}
			}
			else
			{
				if(old != null)
				{
					map.remove(key);
					rc = Boolean.TRUE;
				}
			}
			mbean.getLockMetrics().logTime(System.nanoTime() - startNS);
			return rc;
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getLockMetrics().logException(e);
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
	
	public LockAgent()
	{
 	}
}
