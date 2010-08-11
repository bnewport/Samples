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
package com.devwebsphere.wxslucene;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple LRU cache using JDK LinkedHashMap. This is used as a block cache for blocks fetched within
 * a directory. All files within a directory share the same block cache.
 * @author bnewport
 *
 * @param <K>
 * @param <V>
 */
public class LRUCache<K,V> 
{
	LinkedHashMap<K, V> map;
	private int cacheSize;
	static final float hashTableLoadFactor = 0.75f;
	
	/**
	 * This creates a non thread safe LRU cache of a certain size
	 * @param cacheSize
	 */
	public LRUCache(int cacheSize)
	{
		this.cacheSize = cacheSize;
		int hashTableCapacity = (int)Math.ceil(cacheSize/hashTableLoadFactor) + 1;
		map = new LinkedHashMap<K, V>(hashTableCapacity, hashTableLoadFactor, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<K, V> eldest)
			{
				return size() > LRUCache.this.cacheSize;
			}
		};
	}
	
	public synchronized V get(K k)
	{
		return map.get(k);
	}
	
	public synchronized void put(K k, V v)
	{
		map.put(k, v);
	}
	
	public synchronized void clear()
	{
		map.clear();
	}
}
