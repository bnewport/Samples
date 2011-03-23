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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.WXSMapOfLists;
import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.filter.Filter;
import com.devwebsphere.wxsutils.jmx.listset.WXSMapOfListsMBeanImpl;
import com.devwebsphere.wxsutils.jmx.listset.WXSMapOfListsMBeanManager;
import com.devwebsphere.wxsutils.wxsmap.BigListHead.LR;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.datagrid.EntryErrorValue;

public class WXSMapOfBigListsImpl<K extends Serializable,V extends Serializable> extends WXSBaseMap implements WXSMapOfLists<K, V> 
{
	static Logger logger = Logger.getLogger(WXSMapOfBigListsImpl.class.getName());
	static LazyMBeanManagerAtomicReference<WXSMapOfListsMBeanManager> wxsMapOfListsMBeanManager = new LazyMBeanManagerAtomicReference<WXSMapOfListsMBeanManager>(WXSMapOfListsMBeanManager.class);
	
	public WXSMapOfBigListsImpl(WXSUtils utils, String mapName)
	{
		super(utils, mapName);
	}

	public int llen(K key) 
	{
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef().getBean(grid.getName(), mapName);
		long start = System.nanoTime();
		try
		{
			BigListLenAgent<V> a = new BigListLenAgent<V>();
			Map<K,Object> rc = tls.getMap(mapName).getAgentManager().callMapAgent(a, Collections.singletonList(key));
			Object rcV = rc.get(key);
			if(rcV != null && rcV instanceof EntryErrorValue)
			{
				logger.log(Level.SEVERE, "llen failed");
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
		return pop(key, LR.LEFT, null);
	}

	public V lpop(K key, K dirtyKey)
	{
		return pop(key, LR.LEFT, dirtyKey);
	}
	
	V pop(K key, LR isLeft, K dirtyKey) {
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef().getBean(grid.getName(), mapName);
		long start = System.nanoTime();
		try
		{
			BigListPopAgent<K, V> a = new BigListPopAgent<K, V>();
			a.isLeft = isLeft;
			a.dirtyKey = dirtyKey;
			Map<K,Object> rc = tls.getMap(mapName).getAgentManager().callMapAgent(a, Collections.singletonList(key));
			Object rcV = rc.get(key);
			if(rcV != null && rcV instanceof EntryErrorValue)
			{
				logger.log(Level.SEVERE, "pop failed");
				throw new ObjectGridRuntimeException(rcV.toString());
			}
			mbean.getPopMetrics().logTime(System.nanoTime() - start);
			if(rcV instanceof BigListPopAgent.EmptyMarker)
			{
				rcV = null;
			}
			return (V)rcV;
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getPopMetrics().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	public void lpush(K key, V value, K... dirtySet)
	{
		push(key, value, LR.LEFT, dirtySet);
	}
	
	void push(K key, V value, LR isLeft, K[] dirtySet) {
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef().getBean(grid.getName(), mapName);
		long start = System.nanoTime();
		if(dirtySet != null && dirtySet.length > 1)
			throw new ObjectGridRuntimeException("push does not allow multiple dirtySet key");
		try
		{
			BigListPushAgent<K, V> pushAgent = new BigListPushAgent<K, V>();
			pushAgent.isLeft = isLeft;
			pushAgent.value = value;
			if(dirtySet != null && dirtySet.length == 1)
				pushAgent.dirtyKey = dirtySet[0];
			Map<K,Object> rc = tls.getMap(mapName).getAgentManager().callMapAgent(pushAgent, Collections.singletonList(key));
			Object rcV = rc.get(key);
			if(rcV != null && rcV instanceof EntryErrorValue)
			{
				logger.log(Level.SEVERE, "push failed: " + rcV.toString());
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

	public ArrayList<V> lrange(K key, int low, int high, Filter... filters) {
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef().getBean(grid.getName(), mapName);
		long start = System.nanoTime();
		if(filters.length > 1)
		{
			throw new ObjectGridRuntimeException("Only one filter can be specified");
		}
		try
		{
			BigListRangeAgent<V> a = new BigListRangeAgent<V>();
			a.low = low;
			a.high = high;
			Map<K,Object> rc = tls.getMap(mapName).getAgentManager().callMapAgent(a, Collections.singletonList(key));
			Object rcV = rc.get(key);
			if(rcV != null && rcV instanceof EntryErrorValue)
			{
				logger.log(Level.SEVERE, "range failed");
				throw new ObjectGridRuntimeException(rcV.toString());
			}
			mbean.getRangeMetrics().logTime(System.nanoTime() - start);
			return (ArrayList<V>)rcV;
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getRangeMetrics().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	public ArrayList<V> popAll(K key)
	{
		return popAll(key, null);
	}
	
	public ArrayList<V> popAll(K key, K dirtyKey) {
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef().getBean(grid.getName(), mapName);
		long start = System.nanoTime();
		try
		{
			BigListPopAllAgent<K, V> a = new BigListPopAllAgent<K, V>();
			a.dirtyKey = dirtyKey;
			Map<K,Object> rc = tls.getMap(mapName).getAgentManager().callMapAgent(a, Collections.singletonList(key));
			Object rcV = rc.get(key);
			if(rcV != null && rcV instanceof EntryErrorValue)
			{
				logger.log(Level.SEVERE, "popAll failed");
				throw new ObjectGridRuntimeException(rcV.toString());
			}
			mbean.getRangeMetrics().logTime(System.nanoTime() - start);
			return (ArrayList<V>)rcV;
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getRangeMetrics().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	public void rtrim(K key, int size) {
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef().getBean(grid.getName(), mapName);
		long start = System.nanoTime();
		try
		{
			BigListTrimAgent<V> a = new BigListTrimAgent<V>();
			a.size = size;
			Map<K,Object> rc = tls.getMap(mapName).getAgentManager().callMapAgent(a, Collections.singletonList(key));
			Object rcV = rc.get(key);
			if(rcV != null && rcV instanceof EntryErrorValue)
			{
				logger.log(Level.SEVERE, "rtrim failed");
				throw new ObjectGridRuntimeException(rcV.toString());
			}
			mbean.getTrimMetrics().logTime(System.nanoTime() - start);
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getTrimMetrics().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	public V rpop(K key) {
		return pop(key, LR.RIGHT, null);
	}
	
	public V rpop(K key, K dirtyKey)
	{
		return pop(key, LR.RIGHT, dirtyKey);
	}

	public void rpush(K key, V value, K... dirtySet) 
	{
		push(key, value, LR.RIGHT, dirtySet);
	}

	public void remove(K key)
	{
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef().getBean(grid.getName(), mapName);
		long start = System.nanoTime();
		try
		{
			BigListRemoveAgent<V> a = new BigListRemoveAgent<V>();
			Map<K,Object> rc = tls.getMap(mapName).getAgentManager().callMapAgent(a, Collections.singletonList(key));
			Object rcV = rc.get(key);
			if(rcV != null && rcV instanceof EntryErrorValue)
			{
				logger.log(Level.SEVERE, "remove(K) failed");
				throw new ObjectGridRuntimeException(rcV.toString());
			}
			mbean.getRemoveMetrics().logTime(System.nanoTime() - start);
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getRemoveMetrics().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}
}
