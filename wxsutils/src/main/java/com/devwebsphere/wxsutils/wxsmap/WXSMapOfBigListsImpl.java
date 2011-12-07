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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.EvictionType;
import com.devwebsphere.wxsutils.WXSMapOfLists;
import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.filter.Filter;
import com.devwebsphere.wxsutils.jmx.listset.WXSMapOfListsMBeanImpl;
import com.devwebsphere.wxsutils.jmx.listset.WXSMapOfListsMBeanManager;
import com.devwebsphere.wxsutils.wxsagent.WXSReduceAgent;
import com.devwebsphere.wxsutils.wxsmap.BigListHead.LR;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.datagrid.EntryErrorValue;

public class WXSMapOfBigListsImpl<K extends Serializable, V extends Serializable>
		extends WXSBaseMap implements WXSMapOfLists<K, V> {
	static Logger logger = Logger.getLogger(WXSMapOfBigListsImpl.class
			.getName());
	static LazyMBeanManagerAtomicReference<WXSMapOfListsMBeanManager> wxsMapOfListsMBeanManager = new LazyMBeanManagerAtomicReference<WXSMapOfListsMBeanManager>(
			WXSMapOfListsMBeanManager.class);

	String listName;

	public static String getListHeadMapName(String listName) {
		StringBuilder sb = new StringBuilder("LHEAD.");
		sb.append(listName);
		return sb.toString();
	}

	public static String getListBucketMapName(String listName) {
		StringBuilder sb = new StringBuilder("LBUCK.");
		sb.append(listName);
		return sb.toString();
	}

	public static String getListDirtySetMapName(String listName) {
		StringBuilder sb = new StringBuilder("LDIRTY.");
		sb.append(listName);
		return sb.toString();
	}

	public static String getListEvictionSetMapName(String listName) {
		StringBuilder sb = new StringBuilder("SEVICT.");
		sb.append(listName);
		return sb.toString();
	}

	public static String getListEvictionListMapName(String listName) {
		StringBuilder sb = new StringBuilder("LEVICT.");
		sb.append(listName);
		return sb.toString();
	}

	public static String getListLeaseMapName(String listName) {
		StringBuilder sb = new StringBuilder("LCKDIRTY.");
		sb.append(listName);
		return sb.toString();
	}

	public WXSMapOfBigListsImpl(WXSUtils utils, String listName) {
		super(utils, getListHeadMapName(listName));
		this.listName = listName;
	}

	public int llen(K key) {
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef()
				.getBean(grid.getName(), listName);
		long start = System.nanoTime();
		try {
			BigListLenAgent<V> a = new BigListLenAgent<V>();
			Map<K, Object> rc = tls.getMap(mapName).getAgentManager()
					.callMapAgent(a, Collections.singletonList(key));
			Object rcV = rc.get(key);
			if (rcV != null && rcV instanceof EntryErrorValue) {
				logger.log(Level.SEVERE, "llen failed");
				throw new ObjectGridRuntimeException(rcV.toString());
			}
			Integer size = (Integer) rcV;
			mbean.getLenMetrics().logTime(System.nanoTime() - start);
			return size;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getLenMetrics().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	public boolean isEmpty(K key) {
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef()
				.getBean(grid.getName(), listName);
		long start = System.nanoTime();
		try {
			BigListIsEmptyAgent<V> a = new BigListIsEmptyAgent<V>();
			Map<K, Object> rc = tls.getMap(mapName).getAgentManager()
					.callMapAgent(a, Collections.singletonList(key));
			Object rcV = rc.get(key);
			if (rcV != null && rcV instanceof EntryErrorValue) {
				logger.log(Level.SEVERE, "llen failed");
				throw new ObjectGridRuntimeException(rcV.toString());
			}
			Boolean b = (Boolean) rcV;
			mbean.getLenMetrics().logTime(System.nanoTime() - start);
			return b;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getLenMetrics().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	public V lpop(K key) {
		return pop(key, LR.LEFT, null);
	}

	public V lpop(K key, K dirtyKey) {
		return pop(key, LR.LEFT, dirtyKey);
	}

	V pop(K key, LR isLeft, K dirtyKey) {
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef()
				.getBean(grid.getName(), listName);
		long start = System.nanoTime();
		try {
			BigListPopAgent<K, V> a = new BigListPopAgent<K, V>();
			a.isLeft = isLeft;
			a.dirtyKey = dirtyKey;
			Map<K, Object> rc = tls.getMap(mapName).getAgentManager()
					.callMapAgent(a, Collections.singletonList(key));
			Object rcV = rc.get(key);
			if (rcV != null && rcV instanceof EntryErrorValue) {
				logger.log(Level.SEVERE, "pop failed");
				throw new ObjectGridRuntimeException(rcV.toString());
			}
			mbean.getPopMetrics().logTime(System.nanoTime() - start);
			if (rcV instanceof BigListPopAgent.EmptyMarker) {
				rcV = null;
			}
			return (V) rcV;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getPopMetrics().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	public void lpush(K key, V value) {
		lpush(key, value, null);
	}

	public void lpush(K key, V value, K dirtySet) {
		List<BulkPushItem<V>> list = new ArrayList<BulkPushItem<V>>(1);
		list.add(new BulkPushItem<V>(value, null));
		push(key, list, LR.LEFT, dirtySet);
	}

	public void lpush(K key, List<V> values) {
		push(key, convertToBulkList(values), LR.LEFT, null);
	}

	static public <V extends Serializable> List<BulkPushItem<V>> convertToBulkList(
			List<V> values) {
		ArrayList<BulkPushItem<V>> list = new ArrayList<WXSMapOfLists.BulkPushItem<V>>(
				values.size());
		for (V v : values) {
			list.add(new BulkPushItem<V>(v, null));
		}
		return list;
	}

	public void lpush(K key, List<V> values, K dirtySet) {
		push(key, convertToBulkList(values), LR.LEFT, dirtySet);
	}

	void push(K key, List<BulkPushItem<V>> values, LR isLeft, K dirtyKey) {
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef()
				.getBean(grid.getName(), listName);
		long start = System.nanoTime();
		try {
			BigListPushAgent<K, V> pushAgent = new BigListPushAgent<K, V>();
			pushAgent.isLeft = isLeft;
			pushAgent.keys = Collections.singletonList(key);
			pushAgent.values = Collections.singletonList(values);
			pushAgent.dirtyKey = dirtyKey;
			Object rc = tls.getMap(mapName).getAgentManager()
					.callReduceAgent(pushAgent, Collections.singletonList(key));
			if (rc != null && !(rc instanceof Boolean)) {
				logger.log(Level.SEVERE, "push failed: " + rc.toString());
				throw new ObjectGridRuntimeException(rc.toString());
			}
			Boolean rcB = (Boolean) rc;
			if (!rcB) {
				throw new ObjectGridRuntimeException("Push failed");
			}
			mbean.getPushMetrics().logTime(System.nanoTime() - start);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getPushMetrics().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	public ArrayList<V> lrange(K key, int low, int high) {
		return lrange(key, low, high, null);
	}

	public ArrayList<V> lrange(K key, int low, int high, Filter filter) {
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef()
				.getBean(grid.getName(), listName);
		long start = System.nanoTime();
		try {
			BigListRangeAgent<V> a = new BigListRangeAgent<V>();
			a.low = low;
			a.high = high;
			a.filter = filter;
			Map<K, Object> rc = tls.getMap(mapName).getAgentManager()
					.callMapAgent(a, Collections.singletonList(key));
			Object rcV = rc.get(key);
			if (rcV != null && rcV instanceof EntryErrorValue) {
				logger.log(Level.SEVERE, "range failed");
				throw new ObjectGridRuntimeException(rcV.toString());
			}
			mbean.getRangeMetrics().logTime(System.nanoTime() - start);
			return (ArrayList<V>) rcV;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getRangeMetrics().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	public ArrayList<V> popAll(K key) {
		return popAll(key, null);
	}

	public ArrayList<V> popAll(K key, K dirtyKey) {
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef()
				.getBean(grid.getName(), listName);
		long start = System.nanoTime();
		try {
			BigListPopAllAgent<K, V> a = new BigListPopAllAgent<K, V>();
			a.dirtyKey = dirtyKey;
			Map<K, Object> rc = tls.getMap(mapName).getAgentManager()
					.callMapAgent(a, Collections.singletonList(key));
			Object rcV = rc.get(key);
			if (rcV != null && rcV instanceof EntryErrorValue) {
				logger.log(Level.SEVERE, "popAll failed");
				throw new ObjectGridRuntimeException(rcV.toString());
			}
			mbean.getRangeMetrics().logTime(System.nanoTime() - start);
			return (ArrayList<V>) rcV;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getRangeMetrics().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	public List<V> lpop(K key, int numItems) {
		return popNItems(LR.LEFT, key, numItems, null, false);
	}

	public List<V> lpop(K key, int numItems, K dirtyKey, boolean releaseLease) {
		return popNItems(LR.LEFT, key, numItems, dirtyKey, releaseLease);
	}

	public List<V> lpop(K key, int numItems, K dirtyKey) {
		return lpop(key, numItems, dirtyKey, false);
	}

	public List<V> rpop(K key, int numItems) {
		return popNItems(LR.RIGHT, key, numItems, null, false);
	}

	public List<V> rpop(K key, int numItems, K dirtyKey, boolean releaseLease) {
		return popNItems(LR.RIGHT, key, numItems, dirtyKey, releaseLease);
	}

	public List<V> rpop(K key, int numItems, K dirtyKey) {
		return rpop(key, numItems, dirtyKey, false);
	}

	public int rremove(K key, int numItems) {
		return rremove(key, numItems, null, false);
	}

	public int lremove(K key, int numItems) {
		return lremove(key, numItems, null, false);
	}

	public int rremove(K key, int numItems, K dirtyKey, boolean releaseLease) {
		return removeNItems(LR.RIGHT, key, numItems, dirtyKey, releaseLease);
	}

	public int lremove(K key, int numItems, K dirtyKey, boolean releaseLease) {
		return removeNItems(LR.LEFT, key, numItems, dirtyKey, releaseLease);
	}

	private ArrayList<V> popNItems(LR isLeft, K key, int numItems, K dirtyKey,
			boolean releaseLease) {
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef()
				.getBean(grid.getName(), listName);
		long start = System.nanoTime();
		try {
			BigListPopNItemsAgent<K, V> a = new BigListPopNItemsAgent<K, V>();
			a.dirtyKey = dirtyKey;
			a.isLeft = isLeft;
			a.numItems = numItems;
			a.releaseLease = releaseLease;
			Map<K, Object> rc = tls.getMap(mapName).getAgentManager()
					.callMapAgent(a, Collections.singletonList(key));
			Object rcV = rc.get(key);
			if (rcV != null && rcV instanceof EntryErrorValue) {
				logger.log(Level.SEVERE, "popNItems failed");
				throw new ObjectGridRuntimeException(rcV.toString());
			}
			mbean.getRangeMetrics().logTime(System.nanoTime() - start);
			return (ArrayList<V>) rcV;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getRangeMetrics().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	private int removeNItems(LR isLeft, K key, int numItems, K dirtyKey,
			boolean releaseLease) {
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef()
				.getBean(grid.getName(), listName);
		long start = System.nanoTime();
		try {
			BigListRemoveNItemsAgent<K, V> a = new BigListRemoveNItemsAgent<K, V>();
			a.dirtyKey = dirtyKey;
			a.isLeft = isLeft;
			a.numItems = numItems;
			a.releaseLease = releaseLease;
			Map<K, Object> rc = tls.getMap(mapName).getAgentManager()
					.callMapAgent(a, Collections.singletonList(key));
			Object rcV = rc.get(key);
			if (rcV != null && rcV instanceof EntryErrorValue) {
				logger.log(Level.SEVERE, "popNItems failed");
				throw new ObjectGridRuntimeException(rcV.toString());
			}
			mbean.getRangeMetrics().logTime(System.nanoTime() - start);
			return ((Integer) rcV).intValue();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getRangeMetrics().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	public void rtrim(K key, int size) {
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef()
				.getBean(grid.getName(), listName);
		long start = System.nanoTime();
		try {
			BigListTrimAgent<V> a = new BigListTrimAgent<V>();
			a.size = size;
			Map<K, Object> rc = tls.getMap(mapName).getAgentManager()
					.callMapAgent(a, Collections.singletonList(key));
			Object rcV = rc.get(key);
			if (rcV != null && rcV instanceof EntryErrorValue) {
				logger.log(Level.SEVERE, "rtrim failed");
				throw new ObjectGridRuntimeException(rcV.toString());
			}
			mbean.getTrimMetrics().logTime(System.nanoTime() - start);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getTrimMetrics().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	public V rpop(K key) {
		return pop(key, LR.RIGHT, null);
	}

	public V rpop(K key, K dirtyKey) {
		return pop(key, LR.RIGHT, dirtyKey);
	}

	public void rpush(K key, V value) {
		rpush(key, value, null);
	}

	public void rpush(K key, V value, K dirtyKey) {
		List<V> list = new ArrayList<V>(1);
		list.add(value);
		push(key, convertToBulkList(list), LR.RIGHT, dirtyKey);
	}

	public void rpush(Map<K, List<BulkPushItem<V>>> items) {
		rpush(items, null);
	}

	public void rpush(Map<K, List<BulkPushItem<V>>> items, K dirtyKey) {
		bulkPushAll(bmap, items, LR.RIGHT, dirtyKey);
	}

	public void lpush(Map<K, List<BulkPushItem<V>>> items) {
		lpush(items, null);
	}

	public void lpush(Map<K, List<BulkPushItem<V>>> items, K dirtySet) {
		bulkPushAll(bmap, items, LR.LEFT, dirtySet);
	}

	public void rpush(K key, List<V> values) {
		rpush(key, values, null);
	}

	public void rpush(K key, List<V> values, K dirtySet) {
		push(key, convertToBulkList(values), LR.RIGHT, dirtySet);
	}

	public void remove(K key) {
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef()
				.getBean(grid.getName(), listName);
		long start = System.nanoTime();
		try {
			BigListRemoveAgent<V> a = new BigListRemoveAgent<V>();
			Map<K, Object> rc = tls.getMap(mapName).getAgentManager()
					.callMapAgent(a, Collections.singletonList(key));
			Object rcV = rc.get(key);
			if (rcV != null && rcV instanceof EntryErrorValue) {
				logger.log(Level.SEVERE, "remove(K) failed");
				throw new ObjectGridRuntimeException(rcV.toString());
			}
			mbean.getRemoveMetrics().logTime(System.nanoTime() - start);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getRemoveMetrics().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	void bulkPushAll(BackingMap bmap, Map<K, List<BulkPushItem<V>>> batch,
			LR side, K dirtyKey) {
		BigListPushAgent.Factory<K, V> factory = new BigListPushAgent.Factory<K, V>(
				dirtyKey, side);
		WXSReduceAgent.callReduceAgentAll(utils, factory, batch, bmap,
				Boolean.TRUE);
	}

	public void setEvictionPolicyFor(K key, EvictionType type,
			int evictionTimeInMinutes) {
	}

	public void lcpush(K key, V value, Filter condition) {
		lcpush(key, value, condition, null);
	}

	public void lcpush(K key, V value, Filter condition, K dirtyKey) {
		List<BulkPushItem<V>> list = new ArrayList<BulkPushItem<V>>(1);
		list.add(new BulkPushItem<V>(value, condition));
		push(key, list, LR.LEFT, dirtyKey);
	}

	public void rcpush(K key, V value, Filter condition) {
		rcpush(key, value, condition, null);
	}

	public void rcpush(K key, V value, Filter condition, K dirtyKey) {
		List<BulkPushItem<V>> list = new ArrayList<BulkPushItem<V>>(1);
		list.add(new BulkPushItem<V>(value, condition));
		push(key, list, LR.RIGHT, dirtyKey);
	}

	public void evict(K key, EvictionType type, int intervalSeconds) {
		WXSMapOfListsMBeanImpl mbean = wxsMapOfListsMBeanManager.getLazyRef()
				.getBean(grid.getName(), listName);
		long start = System.nanoTime();
		try {
			BigListSetEvictionAgent<V> a = new BigListSetEvictionAgent<V>();
			a.eType = type;
			a.intervalSeconds = intervalSeconds;
			Map<K, Object> rc = tls.getMap(mapName).getAgentManager()
					.callMapAgent(a, Collections.singletonList(key));
			Object rcV = rc.get(key);
			if (rcV != null && rcV instanceof EntryErrorValue) {
				logger.log(Level.SEVERE, "evict(K) failed");
				throw new ObjectGridRuntimeException(rcV.toString());
			}
			mbean.getRemoveMetrics().logTime(System.nanoTime() - start);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getRemoveMetrics().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}
}
