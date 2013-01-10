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

import com.devwebsphere.wxsutils.EvictionType;
import com.devwebsphere.wxsutils.WXSMapOfLists;
import com.devwebsphere.wxsutils.filter.Filter;
import com.devwebsphere.wxsutils.wxsmap.BigListHead.LR;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.UndefinedMapException;

/**
 * This is a WXSMapOfLists implementation for use on the server side typically in an Agent. This takes the local session
 * provided to the agent and the name of the list and then allows the various list operations on that list. This all
 * happens using the transaction of the 'agent' or the session provided to this class. This is a slightly different
 * behavior than on the client where each method call is its own transaction. This is NOT the case on the server side.
 * The provided sessions transaction scopes all method calls.
 * 
 * @author bnewport
 * 
 * @param <K>
 * @param <V>
 */
public class ServerSideWXSListMap<K extends Serializable, V extends Serializable> implements WXSMapOfLists<K, V> {
	Session sess;
	ObjectMap map;

	/**
	 * Construct an instance for each Session you can to use with Lists
	 * 
	 * @param sess
	 * @param listName
	 */
	public ServerSideWXSListMap(Session sess, String listName) {
		try {
			String mapName = WXSMapOfBigListsImpl.getListHeadMapName(listName);
			map = sess.getMap(mapName);
			this.sess = sess;
		} catch (UndefinedMapException e) {
			throw new ObjectGridRuntimeException(e);
		}
	}

	public void rtrim(K key, int size) {
		BigListTrimAgent.trim(sess, map, key, size);
	}

	public void lpush(K key, V value) {
		List<BulkPushItem<V>> list = new ArrayList<BulkPushItem<V>>(1);
		list.add(new BulkPushItem<V>(value, null));
		BigListPushAgent.push(sess, map, LR.LEFT, Collections.singletonList(key), Collections.singletonList(list), null);
	}

	public void lcpush(K key, V value, Filter condition) {
		List<BulkPushItem<V>> list = new ArrayList<BulkPushItem<V>>(1);
		list.add(new BulkPushItem<V>(value, condition));
		BigListPushAgent.push(sess, map, LR.LEFT, Collections.singletonList(key), Collections.singletonList(list), null);
	}

	public void lcpush(K key, V value, Filter condition, K dirtyKey) {
		List<BulkPushItem<V>> list = new ArrayList<BulkPushItem<V>>(1);
		list.add(new BulkPushItem<V>(value, condition));
		BigListPushAgent.push(sess, map, LR.LEFT, Collections.singletonList(key), Collections.singletonList(list), dirtyKey);
	}

	public void rcpush(K key, V value, Filter condition) {
		List<BulkPushItem<V>> list = new ArrayList<BulkPushItem<V>>(1);
		list.add(new BulkPushItem<V>(value, condition));
		BigListPushAgent.push(sess, map, LR.RIGHT, Collections.singletonList(key), Collections.singletonList(list), null);
	}

	public void rcpush(K key, V value, Filter condition, K dirtyKey) {
		List<BulkPushItem<V>> list = new ArrayList<BulkPushItem<V>>(1);
		list.add(new BulkPushItem<V>(value, condition));
		BigListPushAgent.push(sess, map, LR.RIGHT, Collections.singletonList(key), Collections.singletonList(list), dirtyKey);
	}

	public void lpush(K key, V value, K dirtySet) {
		List<BulkPushItem<V>> list = new ArrayList<BulkPushItem<V>>(1);
		list.add(new BulkPushItem<V>(value, null));
		BigListPushAgent.push(sess, map, LR.LEFT, Collections.singletonList(key), Collections.singletonList(list), dirtySet);
	}

	public void lpush(K key, List<V> values) {
		BigListPushAgent.push(sess, map, LR.LEFT, Collections.singletonList(key),
				Collections.singletonList(WXSMapOfBigListsImpl.convertToBulkList(values)), null);
	}

	public void lpush(K key, List<V> values, K dirtySet) {
		BigListPushAgent.push(sess, map, LR.LEFT, Collections.singletonList(key),
				Collections.singletonList(WXSMapOfBigListsImpl.convertToBulkList(values)), dirtySet);
	}

	public void lpush(Map<K, List<BulkPushItem<V>>> items) {
		ArrayList<K> keys = new ArrayList<K>(items.keySet());
		ArrayList<List<BulkPushItem<V>>> values = new ArrayList<List<BulkPushItem<V>>>(keys.size());
		for (K k : keys) {
			values.add(items.get(k));
		}

		BigListPushAgent.push(sess, map, LR.LEFT, keys, values, null);
	}

	public void lpush(Map<K, List<BulkPushItem<V>>> items, K dirtySet) {
		ArrayList<K> keys = new ArrayList<K>(items.keySet());
		ArrayList<List<BulkPushItem<V>>> values = new ArrayList<List<BulkPushItem<V>>>(keys.size());
		for (K k : keys) {
			values.add(items.get(k));
		}

		BigListPushAgent.push(sess, map, LR.LEFT, keys, values, dirtySet);
	}

	public void rpush(Map<K, List<BulkPushItem<V>>> items) {
		ArrayList<K> keys = new ArrayList<K>(items.keySet());
		ArrayList<List<BulkPushItem<V>>> values = new ArrayList<List<BulkPushItem<V>>>(keys.size());
		for (K k : keys) {
			values.add(items.get(k));
		}

		BigListPushAgent.push(sess, map, LR.RIGHT, keys, values, null);
	}

	public void rpush(Map<K, List<BulkPushItem<V>>> items, K dirtySet) {
		ArrayList<K> keys = new ArrayList<K>(items.keySet());
		ArrayList<List<BulkPushItem<V>>> values = new ArrayList<List<BulkPushItem<V>>>(keys.size());
		for (K k : keys) {
			values.add(items.get(k));
		}

		BigListPushAgent.push(sess, map, LR.RIGHT, keys, values, dirtySet);
	}

	public V lpop(K key) {
		return (V) BigListPopAgent.pop(sess, map, key, LR.LEFT, null);
	}

	public V lpop(K key, K dirtyKey) {
		return (V) BigListPopAgent.pop(sess, map, key, LR.LEFT, dirtyKey);
	}

	public ArrayList<V> popAll(K key) {
		return BigListPopAllAgent.popAll(sess, map, key, null);
	}

	public ArrayList<V> popAll(K key, K dirtyKey) {
		return BigListPopAllAgent.popAll(sess, map, key, dirtyKey);
	}

	public void remove(K key) {
		BigListRemoveAgent.remove(sess, map, key);
	}

	public V rpop(K key) {
		return (V) BigListPopAgent.pop(sess, map, key, LR.RIGHT, null);
	}

	public V rpop(K key, K dirtyKey) {
		return (V) BigListPopAgent.pop(sess, map, key, LR.RIGHT, dirtyKey);
	}

	public List<V> lpop(K key, int numItems, K dirtyKey, RELEASE releaseLease) {
		return BigListPopNItemsAgent.popNItems(sess, map, key, LR.LEFT, numItems, dirtyKey, releaseLease);
	}

	public List<V> lpop(K key, int numItems, K dirtyKey) {
		return lpop(key, numItems, dirtyKey, RELEASE.WHEN_EMPTY);
	}

	public List<V> lpop(K key, int numItems) {
		return BigListPopNItemsAgent.popNItems(sess, map, key, LR.LEFT, numItems, null, RELEASE.WHEN_EMPTY);
	}

	public List<V> rpop(K key, int numItems, K dirtyKey, RELEASE releaseLease) {
		return BigListPopNItemsAgent.popNItems(sess, map, key, LR.RIGHT, numItems, dirtyKey, releaseLease);
	}

	public List<V> rpop(K key, int numItems, K dirtyKey) {
		return rpop(key, numItems, dirtyKey, RELEASE.WHEN_EMPTY);
	}

	public List<V> rpop(K key, int numItems) {
		return BigListPopNItemsAgent.popNItems(sess, map, key, LR.RIGHT, numItems, null, RELEASE.WHEN_EMPTY);
	}

	public int rremove(K key, int numItems) {
		return BigListRemoveNItemsAgent.removeNItems(sess, map, key, LR.RIGHT, numItems, null, RELEASE.WHEN_EMPTY);
	}

	public int rremove(K key, int numItems, K dirtyKey, RELEASE releaseLease) {
		return BigListRemoveNItemsAgent.removeNItems(sess, map, key, LR.RIGHT, numItems, null, releaseLease);
	}

	public int lremove(K key, int numItems) {
		return BigListRemoveNItemsAgent.removeNItems(sess, map, key, LR.LEFT, numItems, null, RELEASE.WHEN_EMPTY);
	}

	public int lremove(K key, int numItems, K dirtyKey, RELEASE releaseLease) {
		return BigListRemoveNItemsAgent.removeNItems(sess, map, key, LR.LEFT, numItems, null, releaseLease);
	}

	public void rpush(K key, V value) {
		List<BulkPushItem<V>> list = new ArrayList<BulkPushItem<V>>(1);
		list.add(new BulkPushItem<V>(value, null));
		BigListPushAgent.push(sess, map, LR.RIGHT, Collections.singletonList(key), Collections.singletonList(list), null);
	}

	public void rpush(K key, V value, K dirtySet) {
		List<BulkPushItem<V>> list = new ArrayList<BulkPushItem<V>>(1);
		list.add(new BulkPushItem<V>(value, null));
		BigListPushAgent.push(sess, map, LR.RIGHT, Collections.singletonList(key), Collections.singletonList(list), dirtySet);
	}

	public void rpush(K key, List<V> values) {
		BigListPushAgent.push(sess, map, LR.RIGHT, Collections.singletonList(key),
				Collections.singletonList(WXSMapOfBigListsImpl.convertToBulkList(values)), null);
	}

	public void rpush(K key, List<V> values, K dirtySet) {
		BigListPushAgent.push(sess, map, LR.RIGHT, Collections.singletonList(key),
				Collections.singletonList(WXSMapOfBigListsImpl.convertToBulkList(values)), dirtySet);
	}

	public ArrayList<V> lrange(K key, int low, int high) {
		return BigListRangeAgent.range(sess, map, key, low, high, null);
	}

	public ArrayList<V> lrange(K key, int low, int high, Filter filter) {
		return BigListRangeAgent.range(sess, map, key, low, high, filter);
	}

	public int llen(K key) {
		return BigListLenAgent.size(sess, map, key);
	}

	public boolean isEmpty(K key) {
		return BigListIsEmptyAgent.isEmpty(sess, map, key);
	}

	public void evict(K key, EvictionType type, int intervalSeconds) {
		// TODO Auto-generated method stub

	}

}
