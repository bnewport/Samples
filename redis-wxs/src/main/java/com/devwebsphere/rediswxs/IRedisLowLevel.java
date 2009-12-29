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
 * This provides a data structures API for IBM WebSphere eXtreme Scale. This is modeled
 * after the redis open source project API.
 * @author bnewport
 *
 * @see RedisClient#getSelf()
 */
interface IRedisLowLevel 
{
	public <K extends Serializable, V extends Serializable> void set(
			CacheUsage useCache, String name, K key, V value);

	public <K extends Serializable, V extends Serializable> V get(CacheUsage useCache, String name,
			K key);
	
	public <K extends Serializable> void invalidate(String name, K key);

	public <K extends Serializable> long incr(String name, K key);

	public <K extends Serializable> long incrby(String name, K key, long delta);

	public <K extends Serializable> boolean ltrim(String name, K key, int size);

	public <K extends Serializable, V extends Serializable> void lpush(
			String name, K key, V value);

	public <K extends Serializable, V extends Serializable> V lpop(String name,
			K key);

	public <K extends Serializable, V extends Serializable> V rpop(String name,
			K key);

	public <K extends Serializable, V extends Serializable> void rpush(
			String name, K key, V value);

	public <K extends Serializable, V extends Serializable> ArrayList<V> lrange(
			String name, K key, int low, int high);

	public <K extends Serializable, V extends Serializable> boolean sadd(
			String name, K key, V value);

	public <K extends Serializable, V extends Serializable> boolean srem(
			String name, K key, V value);

	public <K extends Serializable> int scard(String name, K key);

	public <K extends Serializable> int llen(String name, K key);

	public <K extends Serializable> boolean remove(String name, K key);

	public <K extends Serializable, V extends Serializable> boolean sismember(
			String name, K key, V value);

	public <K extends Serializable, V extends Serializable> List<V> smembers(
			String name, K key);

	public void invokeAsyncOperation(String eventName, Map<String,String> properties);
}