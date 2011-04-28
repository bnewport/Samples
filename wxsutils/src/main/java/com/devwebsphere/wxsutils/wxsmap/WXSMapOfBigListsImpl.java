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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.EvictionType;
import com.devwebsphere.wxsutils.WXSMapOfLists;
import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.filter.Filter;
import com.devwebsphere.wxsutils.jmx.listset.WXSMapOfListsMBeanImpl;
import com.devwebsphere.wxsutils.jmx.listset.WXSMapOfListsMBeanManager;
import com.devwebsphere.wxsutils.wxsmap.BigListHead.LR;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.datagrid.EntryErrorValue;

public class WXSMapOfBigListsImpl<K extends Serializable,V extends Serializable> extends WXSBaseMap implements WXSMapOfLists<K, V> 
{
	static Logger logger = Logger.getLogger(WXSMapOfBigListsImpl.class.getName());
	static LazyMBeanManagerAtomicReference<WXSMapOfListsMBeanManager> wxsMapOfListsMBeanManager = new LazyMBeanManagerAtomicReference<WXSMapOfListsMBeanManager>(WXSMapOfListsMBeanManager.class);
	
	String listName;
	
	public static String getListHeadMapName(String listName)
	{
		StringBuilder sb = new StringBuilder("LHEAD.");
		sb.append(listName);
		return sb.toString();
	}

	public static String getListBucketMapName(String listName)
	{
		StringBuilder sb = new StringBuilder("LBUCK.");
		sb.append(listName);
		return sb.toString();
	}
	
	public static String getListDirtySetMapName(String listName)
	{
		StringBuilder sb = new StringBuilder("LDIRTY.");
		sb.append(listName);
		return sb.toString();
	}
	
	public static String getListEvictionSetMapName(String listName)
	{
		StringBuilder sb = new StringBuilder("SEVICT.");
		sb.append(listName);
		return sb.toString();
	}
	
	public static String getListEvictionListMapName(String listName)
	{
		StringBuilder sb = new StringBuilder("LEVICT.");
		sb.append(listName);
		return sb.toString();
	}
	
	public WXSMapOfBigListsImpl(WXSUtils utils, String listName)
	{
		super(utils, getListHeadMapName(listName));
		this.listName = listName;
	}

	public int llen(K key) 
	{
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef().getBean(grid.getName(), listName);
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

	public boolean isEmpty(K key) 
	{
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef().getBean(grid.getName(), listName);
		long start = System.nanoTime();
		try
		{
			BigListIsEmptyAgent<V> a = new BigListIsEmptyAgent<V>();
			Map<K,Object> rc = tls.getMap(mapName).getAgentManager().callMapAgent(a, Collections.singletonList(key));
			Object rcV = rc.get(key);
			if(rcV != null && rcV instanceof EntryErrorValue)
			{
				logger.log(Level.SEVERE, "llen failed");
				throw new ObjectGridRuntimeException(rcV.toString());
			}
			Boolean b = (Boolean)rcV;
			mbean.getLenMetrics().logTime(System.nanoTime() - start);
			return b;
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
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef().getBean(grid.getName(), listName);
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

	public void lpush(K key, V value)
	{
		lpush(key, value, null);
	}
	
	public void lpush(K key, V value, K dirtySet)
	{
		List<V> list = new ArrayList<V>(1);
		list.add(value);
		push(key, list, LR.LEFT, dirtySet, null);
	}
	
	public void lpush(K key, List<V> values)
	{
		lpush(key, values, null);
	}
	
	public void lpush(K key, List<V> values, K dirtySet)
	{
		push(key, values, LR.LEFT, dirtySet, null);
	}
	
	void push(K key, List<V> values, LR isLeft, K dirtyKey, Filter cfilter) {
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef().getBean(grid.getName(), listName);
		long start = System.nanoTime();
		try
		{
			BigListPushAgent<K, V> pushAgent = new BigListPushAgent<K, V>();
			pushAgent.isLeft = isLeft;
			pushAgent.values = new HashMap<K, List<V>>();
			pushAgent.values.put(key, values);
			pushAgent.dirtyKey = dirtyKey;
			pushAgent.cfilter = cfilter;
			Object rc = tls.getMap(mapName).getAgentManager().callReduceAgent(pushAgent, Collections.singletonList(key));
			if(rc != null && !(rc instanceof Boolean))
			{
				logger.log(Level.SEVERE, "push failed: " + rc.toString());
				throw new ObjectGridRuntimeException(rc.toString());
			}
			Boolean rcB = (Boolean)rc;
			if(!rcB)
			{
				throw new ObjectGridRuntimeException("Push failed");
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
	
	public ArrayList<V> lrange(K key, int low, int high)
	{
		return lrange(key, low, high, null);
	}
	
	public ArrayList<V> lrange(K key, int low, int high, Filter filter) {
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef().getBean(grid.getName(), listName);
		long start = System.nanoTime();
		try
		{
			BigListRangeAgent<V> a = new BigListRangeAgent<V>();
			a.low = low;
			a.high = high;
			a.filter = filter;
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
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef().getBean(grid.getName(), listName);
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
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef().getBean(grid.getName(), listName);
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

	public void rpush(K key, V value) 
	{
		rpush(key, value, null);
	}
	
	public void rpush(K key, V value, K dirtyKey) 
	{
		List<V> list = new ArrayList<V>(1);
		list.add(value);
		push(key, list, LR.RIGHT, dirtyKey, null);
	}

	public void rpush(Map<K, List<V>> items)
	{
		rpush(items, null);
	}
	
	public void rpush(Map<K, List<V>> items, K dirtyKey)
	{
		bulkPushAll(bmap, items, LR.RIGHT, dirtyKey);
	}
	
	public void lpush(Map<K, List<V>> items)
	{
		lpush(items, null);
	}
	
	public void lpush(Map<K, List<V>> items, K dirtySet)
	{
		bulkPushAll(bmap, items, LR.LEFT, dirtySet);
	}
	
	public void rpush(K key, List<V> values)
	{
		rpush(key, values, null);
	}
	
	public void rpush(K key, List<V> values, K dirtySet) 
	{
		push(key, values, LR.RIGHT, dirtySet, null);
	}
	
	public void remove(K key)
	{
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef().getBean(grid.getName(), listName);
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

	void bulkPushAll(BackingMap bmap, Map<K,List<V>> batch, LR side, K dirtyKey)
	{
		if(batch.size() > 0)
		{
			Map<Integer, Map<K,List<V>>> pmap = WXSUtils.convertToPartitionEntryMap(bmap, batch);
			Iterator<Map<K,List<V>>> items = pmap.values().iterator();
			ArrayList<Future<?>> results = new ArrayList<Future<?>>();
			CountDownLatch doneSignal = new CountDownLatch(pmap.size());
			while(items.hasNext())
			{
				Map<K,List<V>> perPartitionEntries = items.next();
				// we need one key for partition routing
				// so get the first one
				K key = perPartitionEntries.keySet().iterator().next();
				
				// invoke the agent to add the batch of records to the grid
				BigListPushAgent<K, V> agent = new BigListPushAgent<K, V>();
				agent.dirtyKey = dirtyKey;
				agent.isLeft = side;
				agent.values = perPartitionEntries;
				// Push all keys/lists for one partition using the first key as a routing key
				Future<?> fv = utils.getExecutorService().submit(new WXSUtils.CallReduceAgentThread(utils.getObjectGrid(), bmap.getName(), key, agent, doneSignal));
				results.add(fv);
			}
	
			WXSUtils.blockForAllFuturesToFinish(doneSignal);
			if(!WXSUtils.areAllFuturesTRUE(results))
			{
				logger.log(Level.SEVERE, "pushAll failed because of a server side exception");
				throw new ObjectGridRuntimeException("pushAll failed");
			}
		}
	}

	public void setEvictionPolicyFor(K key, EvictionType type,
			int evictionTimeInMinutes) 
	{
	}

	public void lcpush(K key, V value, Filter condition) 
	{
		lcpush(key, value, condition, null);
	}

	public void lcpush(K key, V value, Filter condition, K dirtyKey) {
		List<V> list = new ArrayList<V>(1);
		list.add(value);
		push(key, list, LR.LEFT, dirtyKey, condition);
	}

	public void rcpush(K key, V value, Filter condition) {
		rcpush(key, value, condition, null);
	}

	public void rcpush(K key, V value, Filter condition, K dirtyKey) {
		List<V> list = new ArrayList<V>(1);
		list.add(value);
		push(key, list, LR.RIGHT, dirtyKey, condition);
	}

	public void evict(K key, EvictionType type, int intervalSeconds) 
	{
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef().getBean(grid.getName(), listName);
		long start = System.nanoTime();
		try
		{
			BigListSetEvictionAgent<V> a = new BigListSetEvictionAgent<V>();
			a.eType = type;
			a.intervalSeconds = intervalSeconds;
			Map<K,Object> rc = tls.getMap(mapName).getAgentManager().callMapAgent(a, Collections.singletonList(key));
			Object rcV = rc.get(key);
			if(rcV != null && rcV instanceof EntryErrorValue)
			{
				logger.log(Level.SEVERE, "evict(K) failed");
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
