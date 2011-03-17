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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.jmx.listset.ListLenAgent;
import com.devwebsphere.wxsutils.jmx.listset.ListPopAgent;
import com.devwebsphere.wxsutils.jmx.listset.ListPushAgent;
import com.devwebsphere.wxsutils.jmx.listset.ListRangeAgent;
import com.devwebsphere.wxsutils.jmx.listset.ListTrimAgent;
import com.devwebsphere.wxsutils.jmx.listset.WXSMapOfListsMBeanImpl;
import com.devwebsphere.wxsutils.jmx.listset.WXSMapOfListsMBeanManager;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.datagrid.EntryErrorValue;

public class WXSMapOfListsImpl<K,V extends Serializable> extends WXSBaseMap implements WXSMapOfLists<K, V> 
{
	static Logger logger = Logger.getLogger(WXSMapOfListsImpl.class.getName());
	static LazyMBeanManagerAtomicReference<WXSMapOfListsMBeanManager> wxsMapOfListsMBeanManager = new LazyMBeanManagerAtomicReference<WXSMapOfListsMBeanManager>(WXSMapOfListsMBeanManager.class);
	
	protected WXSMapOfListsImpl(WXSUtils utils, String mapName)
	{
		super(utils, mapName);
	}

	public int llen(K key) 
	{
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef().getBean(grid.getName(), mapName);
		long start = System.nanoTime();
		try
		{
			ListLenAgent<V> a = new ListLenAgent<V>();
			Map<K,Object> rc = tls.getMap(mapName).getAgentManager().callMapAgent(a, Collections.singletonList(key));
			Object rcV = rc.get(key);
			if(rcV != null && rcV instanceof EntryErrorValue)
			{
				logger.log(Level.SEVERE, "put(K,V) failed");
				throw new ObjectGridRuntimeException(rcV.toString());
			}
			Integer size = (Integer)rcV;
			mbean.getLenMetrics().logTime(System.nanoTime() - start);
			return size;
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getLenMetrics().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	public V lpop(K key)
	{
		return pop(key, true);
	}
	
	V pop(K key, boolean isLeft) {
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef().getBean(grid.getName(), mapName);
		long start = System.nanoTime();
		try
		{
			ListPopAgent<V> a = new ListPopAgent<V>();
			a.isLeft = isLeft;
			Map<K,Object> rc = tls.getMap(mapName).getAgentManager().callMapAgent(a, Collections.singletonList(key));
			Object rcV = rc.get(key);
			if(rcV != null && rcV instanceof EntryErrorValue)
			{
				logger.log(Level.SEVERE, "put(K,V) failed");
				throw new ObjectGridRuntimeException(rcV.toString());
			}
			mbean.getPopMetrics().logTime(System.nanoTime() - start);
			return (V)rcV;
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getPopMetrics().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	public void lpush(K key, V value)
	{
		push(key, value, true);
	}
	
	void push(K key, V value, boolean isLeft) {
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef().getBean(grid.getName(), mapName);
		long start = System.nanoTime();
		try
		{
			ListPushAgent<V> pushAgent = new ListPushAgent<V>();
			pushAgent.isLeft = isLeft;
			pushAgent.value = value;
			Map<K,Object> rc = tls.getMap(mapName).getAgentManager().callMapAgent(pushAgent, Collections.singletonList(key));
			Object rcV = rc.get(key);
			if(rcV != null && rcV instanceof EntryErrorValue)
			{
				logger.log(Level.SEVERE, "put(K,V) failed");
				throw new ObjectGridRuntimeException(rcV.toString());
			}
			mbean.getPushMetrics().logTime(System.nanoTime() - start);
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getPushMetrics().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	public ArrayList<V> lrange(K key, int low, int high) {
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef().getBean(grid.getName(), mapName);
		long start = System.nanoTime();
		try
		{
			ListRangeAgent<V> a = new ListRangeAgent<V>();
			a.low = low;
			a.high = high;
			Map<K,Object> rc = tls.getMap(mapName).getAgentManager().callMapAgent(a, Collections.singletonList(key));
			Object rcV = rc.get(key);
			if(rcV != null && rcV instanceof EntryErrorValue)
			{
				logger.log(Level.SEVERE, "put(K,V) failed");
				throw new ObjectGridRuntimeException(rcV.toString());
			}
			ArrayList<V> l = (ArrayList<V>)rcV;
			mbean.getRangeMetrics().logTime(System.nanoTime() - start);
			return l;
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getRangeMetrics().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	public boolean rtrim(K key, int size) {
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef().getBean(grid.getName(), mapName);
		long start = System.nanoTime();
		try
		{
			ListTrimAgent<V> a = new ListTrimAgent<V>();
			a.newSize = size;
			Map<K,Object> rc = tls.getMap(mapName).getAgentManager().callMapAgent(a, Collections.singletonList(key));
			Object rcV = rc.get(key);
			if(rcV != null && rcV instanceof EntryErrorValue)
			{
				logger.log(Level.SEVERE, "put(K,V) failed");
				throw new ObjectGridRuntimeException(rcV.toString());
			}
			Boolean l = (Boolean)rcV;
			mbean.getTrimMetrics().logTime(System.nanoTime() - start);
			return l;
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getTrimMetrics().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	public V rpop(K key) {
		return pop(key, false);
	}

	public void rpush(K key, V value) 
	{
		push(key, value, false);
	}

	public ArrayList<V> remove(K key)
	{
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef().getBean(grid.getName(), mapName);
		long start = System.nanoTime();
		try
		{
			ArrayList<V> rc = (ArrayList<V>)tls.getMap(mapName).remove(key);
			mbean.getRemoveMetrics().logTime(System.nanoTime() - start);
			return rc;
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getRemoveMetrics().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}
}
