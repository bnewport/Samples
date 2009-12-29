//
//This sample program is provided AS IS and may be used, executed, copied and
//modified without royalty payment by customer (a) for its own instruction and
//study, (b) in order to develop applications designed to run with an IBM
//WebSphere product, either for customer's own internal use or for redistribution
//by customer, as part of such an application, in customer's own products. "
//
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2009
//All Rights Reserved * Licensed Materials - Property of IBM
//
package com.devwebsphere.rediswxs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import com.devwebsphere.purequery.loader.ScalarKey;

/**
 * This is a type safe version of Redis which encodes the map name based on the types
 * and whether a near cache is desired or not.
 * @author bnewport
 *
 * @param <K>
 * @param <V>
 */
public final class RedisMapWithStringKeyAdapter<V extends Serializable>  implements IRedis<String, V>
{
	/**
	 * The ObjectMap name for key/scalar value entries
	 */
	final String mapName;
	/**
	 * The ObjectMap name for key/list entries
	 */
	final String listMapName;
	/**
	 * The ObjectMap name for key/set entries
	 */
	final String setMapName;
	/**
	 * This is used to actually interact with the grid using the mapnames
	 */
	final IRedisLowLevel r;
	final CacheUsage useCache;
	
	/**
	 * Need to pass in the classes directly as you can't get the class
	 * from the type variable which is wierd.
	 * @param isNear If true then use the near cache
	 * @param key The Class for the key, must match the K type
	 * @param value The Class for the value, must match the V type
	 */
	public RedisMapWithStringKeyAdapter(CacheUsage isNear, Class<V> value, IRedisLowLevel rr)
	{
		useCache = isNear;
		String tail = null;
		if(value == String.class)
		{
			tail = "string-string";
		}
		else if(value == Long.class)
		{
			tail = "string-long";
		}
		if(tail == null)
			throw new IllegalArgumentException("Unsupported type combination");

		mapName = tail;
		listMapName = "list-head-" + tail;
		setMapName = "set-" + tail;
		r = rr;
	}
	
	public String getMapName()
	{
		return mapName;
	}
	
	public V get(String key) 
	{
		return (V)r.get(useCache, mapName, new ScalarKey(key));
	}

	public void invalidate(String key)
	{
		r.invalidate(mapName, new ScalarKey(key));
	} 
	
	public long incr(String key) {
		return r.incr(mapName, new ScalarKey(key));
	}

	public long incrby(String key, long delta) 
	{
		return r.incrby(mapName, new ScalarKey(key), delta);
	}

	public int llen(String key) {
		return r.llen(listMapName, new ScalarKey(key));
	}

	public V lpop(String key) {
		return (V)r.lpop(listMapName, new ScalarKey(key));
	}

	public void lpush(String key,
			V value) {
		r.lpush(listMapName, new ScalarKey(key), value);
	}

	public ArrayList<V> lrange(
			String key, int low, int high) {
		return r.lrange(listMapName, new ScalarKey(key), low, high);
	}

	public boolean ltrim(String key, int size) {
		return r.ltrim(listMapName, new ScalarKey(key), size);
	}

	public boolean remove(String key) {
		boolean rc =  r.remove(mapName, new ScalarKey(key));
		// remove cached copy
		//r.invalidate(mapName, key);
		return rc;
	}

	public V rpop(String key) {
		return (V)r.rpop(listMapName, new ScalarKey(key));
	}

	public void rpush(String key, V value) {
		r.rpush(listMapName, new ScalarKey(key), value);
	}

	public boolean sadd(String key, V value) {
		return r.sadd(setMapName, new ScalarKey(key), value);
	}

	public int scard(String key) {
		return r.scard(setMapName, new ScalarKey(key));
	}

	public void set(String key, V value) {
		r.set(useCache, mapName, new ScalarKey(key), value);
		// remove cached copy
		//r.invalidate(mapName, key);
	}

	public boolean sismember(String key, V value) {
		return r.sismember(setMapName, new ScalarKey(key), value);
	}

	public List<V> smembers(String key) {
		return r.smembers(setMapName, new ScalarKey(key));
	}

	public boolean srem(String key, V value) {
		return r.srem(setMapName, new ScalarKey(key), value);
	}
	
	public void invokeAsyncOperation(String eventName, Map<String,String> properties)
	{
		r.invokeAsyncOperation(eventName, properties);
	}
}
