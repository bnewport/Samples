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

import java.util.ArrayList;

/**
 * LRUCache is a very simple cache concurrency size and is fully synchronized. This
 * would lead to poor performance on a multi-threaded application. This wrapper creates
 * 31 different LRUCaches and hashes the keys across them. This effectively creates
 * 31 synch paths through the code and significantly improves concurrency as a result.
 * @author bnewport
 *
 * @param <K>
 * @param <V>
 */
public class MTLRUCache <K,V>
{
	ArrayList<LRUCache<K,V>> caches;
	final int numPaths = 31;
	
	public MTLRUCache(int cacheSize)
	{
		caches = new ArrayList<LRUCache<K,V>>(numPaths);
		int isize = (cacheSize + caches.size() - 1) / numPaths;
		for(int i = 0; i < numPaths; ++i)
		{
			caches.add(new LRUCache<K, V>(isize));
		}
	}

	private LRUCache<K, V> getCacheForKey(K k)
	{
		int index = Math.abs(k.hashCode()) % numPaths;
		return caches.get(index);
	}
	
	public V get(K k)
	{
		return getCacheForKey(k).get(k);
	}
	
	public void put(K k, V v)
	{
		getCacheForKey(k).put(k, v);
	}
	
	public void clear()
	{
		for(LRUCache<K, V> c : caches)
		{
			c.clear();
		}
	}
}
