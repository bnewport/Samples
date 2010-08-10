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
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	static Logger logger = Logger.getLogger(MTLRUCache.class.getName());
	ArrayList<LRUCache<K,V>> caches;
	final int numPaths = 31;
	AtomicLong hitCounter = new AtomicLong();
	AtomicLong missCounter = new AtomicLong();
	String name;
	boolean isHitRateLoggingEnabled;
	
	public MTLRUCache(String name, int cacheSize, boolean showHitRate)
	{
		this.isHitRateLoggingEnabled = showHitRate;
		this.name = name;
		caches = new ArrayList<LRUCache<K,V>>(numPaths);
		int isize = (cacheSize + caches.size() - 1) / numPaths;
		for(int i = 0; i < numPaths; ++i)
		{
			caches.add(new LRUCache<K, V>(isize));
		}
		if(showHitRate)
		{
			logger.log(Level.INFO, "Hit rate logging enabled for " + name);
		}
	}

	private LRUCache<K, V> getCacheForKey(K k)
	{
		int index = Math.abs(k.hashCode()) % numPaths;
		return caches.get(index);
	}
	
	public V get(K k)
	{
		V rc = getCacheForKey(k).get(k);
		if(rc != null)
		{
			long hits = hitCounter.incrementAndGet();
			if(isHitRateLoggingEnabled && hits % 5000 == 0)
			{
				double hr = getHitRate();
				int hrRounded = (int)(hr * 100);
				boolean showHR = false;
				// if its poor, show frequently
				if(hr < 0.5)
					showHR = true;
				else
					// otherwise, show infrequently
					if(hits % 20000 == 0)
						showHR = true;
				if(showHR)
					logger.log(Level.INFO, "Hit rate for " + name + " cache is " + hrRounded + "%");
			}
		}
		return rc;
	}
	
	public void put(K k, V v)
	{
		getCacheForKey(k).put(k, v);
		missCounter.incrementAndGet();
	}

	public double getHitRate()
	{
		long hc = hitCounter.get();
		long mc = missCounter.get();
		return MTLRUCache.calculateHitRate(hc, mc);
	}
	
	
	static public double calculateHitRate(long hc, long mc)
	{
		long total = hc + mc;

		if(total != 0)
			return (double)hc / (double)(total);
		else
			return 0.0;
	}
	
	public void clear()
	{
		for(LRUCache<K, V> c : caches)
		{
			c.clear();
		}
	}
}
