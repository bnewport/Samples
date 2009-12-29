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

/**
 * This is a type safe version of Redis which encodes the map name based on the types
 * and whether a near cache is desired or not.
 * @author bnewport
 *
 * @param <K>
 * @param <V>
 */
public final class RedisMapAdapter<K extends Serializable, V extends Serializable>  implements IRedis<K, V>
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
	public RedisMapAdapter(CacheUsage isNear, Class<K> key, Class<V> value, IRedisLowLevel rr)
	{
		useCache = isNear;
		String tail = null;
		if(key == String.class)
		{
			if(value == String.class)
			{
				tail = "string-string";
			}
			else if(value == Long.class)
			{
				tail = "string-long";
			}
		}
		else if(key == Long.class)
		{
			if(value == String.class)
			{
				tail = "long-string";
			}
			else if(value == Long.class)
			{
				tail = "long-long";
			}
		}
		if(tail == null)
			throw new IllegalArgumentException("Unsupported type combination");

		mapName = tail;
		listMapName = "list-head-" + tail;
		setMapName = "set-" + tail;
		r = rr;
	}
	
	public V get(K key) 
	{
		return (V)r.get(useCache, mapName, key);
	}

	public void invalidate(K key)
	{
		r.invalidate(mapName, key);
	} 
	
	public long incr(K key) {
		return r.incr(mapName, key);
	}

	public long incrby(K key, long delta) 
	{
		return r.incrby(mapName, key, delta);
	}

	public int llen(K key) {
		return r.llen(listMapName, key);
	}

	public V lpop(K key) {
		return (V)r.lpop(listMapName, key);
	}

	public void lpush(K key,
			V value) {
		r.lpush(listMapName, key, value);
	}

	public ArrayList<V> lrange(
			K key, int low, int high) {
		return r.lrange(listMapName, key, low, high);
	}

	public boolean ltrim(K key, int size) {
		return r.ltrim(listMapName, key, size);
	}

	public boolean remove(K key) {
		boolean rc =  r.remove(mapName, key);
		// remove cached copy
		//r.invalidate(mapName, key);
		return rc;
	}

	public V rpop(K key) {
		return (V)r.rpop(listMapName, key);
	}

	public void rpush(K key, V value) {
		r.rpush(listMapName, key, value);
	}

	public boolean sadd(K key, V value) {
		return r.sadd(setMapName, key, value);
	}

	public int scard(K key) {
		return r.scard(setMapName, key);
	}

	public void set(K key, V value) {
		r.set(useCache, mapName, key, value);
		// remove cached copy
		//r.invalidate(mapName, key);
	}

	public boolean sismember(K key, V value) {
		return r.sismember(setMapName, key, value);
	}

	public List<V> smembers(K key) {
		return r.smembers(setMapName, key);
	}

	public boolean srem(K key, V value) {
		return r.srem(setMapName, key, value);
	}
	
	public void invokeAsyncOperation(String eventName, Map<String,String> properties)
	{
		r.invokeAsyncOperation(eventName, properties);
	}

	public String getMapName()
	{
		return mapName;
	}
}
