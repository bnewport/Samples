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

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import com.devwebsphere.purequery.loader.ScalarKey;


/**
 * This is the main class. Call the initialize method with the catalog endpoints
 * or NULL at the start of your application and then use the X_X variables to
 * interact with the store or c_X_X for the near cached equivalent.
 * 
 * @see IRedis
 *
 */
public class R
{
	/**
	 * The shared client connect to the grid.
	 */
	static public RedisClient r;
	
	/**
	 * This allows String/String entries to be worked with
	 */
	static public IRedis<String, String> str_str;
	
	/**
	 * This allows String/Long entries to be worked with
	 */
	static public IRedis<String, Long> str_long;
	/**
	 * This allows Long/String entries to be worked with
	 */
	static public IRedis<Long, String> long_str;
	/**
	 * This allows Long/Long entries to be worked with
	 */
	static public IRedis<Long, Long> long_long;

	static public IRedis<String, String> c_str_str;
	static public IRedis<String, Long> c_str_long;
	static public IRedis<Long, String> c_long_str;
	static public IRedis<Long, Long> c_long_long;

	/**
	 * This MUST be called when your application starts to initialize the client
	 * If the cep is null then a grid is started within the client JVM to host the
	 * grid servers locally. This is useful for debugging
	 * @param cep The catalog service end points (h:p,h:p,h:p)
	 */
	static public void initialize(String cep)
	{
		// initialize multi meta data dictionary
		metaData = new AtomicReference<Map<Class<?>,MultiMetaData<?>>>();
		metaData.compareAndSet(null, new Hashtable<Class<?>, MultiMetaData<?>>());
		
		// shared client connect to the grid, this actually contains
		// two connections, one with near cache and one without
		r = new RedisClient(cep);
		// create non caches adapters sharing the no near cache client
		str_str = new RedisMapWithStringKeyAdapter<String>(CacheUsage.NONEARCACHE, String.class, r);
		str_long = new RedisMapWithStringKeyAdapter<Long>(CacheUsage.NONEARCACHE, Long.class, r);
		long_str = new RedisMapAdapter<Long, String>(CacheUsage.NONEARCACHE, Long.class, String.class, r);
		long_long = new RedisMapAdapter<Long, Long>(CacheUsage.NONEARCACHE, Long.class, Long.class, r);
		
		// create near caches adapters sharing the near cache client
		c_str_str = new RedisMapWithStringKeyAdapter<String>(CacheUsage.NEARCACHE, String.class, r);
		c_str_long = new RedisMapWithStringKeyAdapter<Long>(CacheUsage.NEARCACHE, Long.class, r);
		c_long_str = new RedisMapAdapter<Long, String>(CacheUsage.NEARCACHE, Long.class, String.class, r);
		c_long_long = new RedisMapAdapter<Long, Long>(CacheUsage.NEARCACHE, Long.class, Long.class, r);
	}
	
	static AtomicReference<Map<Class<?>, MultiMetaData<?>>> metaData;

	static public <X> MultiMetaData<X> getMetaData(Class<X> clazz)
	{
		MultiMetaData<X> rc = null;
		Map<Class<?>, MultiMetaData<?>> map = metaData.get();
		rc = (MultiMetaData<X>)map.get(clazz);
		if(rc == null)
		{
			while(true)
			{
				// get current meta data map
				Map<Class<?>, MultiMetaData<?>> last = metaData.get();
				
				// if this class is there, fine
				if(last.containsKey(clazz))
					return (MultiMetaData<X>)last.get(clazz);
				
				// otherwise, merge this new class with whats there
				MultiMetaData<X> md = new MultiMetaData<X>(clazz);
				Map<Class<?>, MultiMetaData<?>> copy = new ConcurrentHashMap<Class<?>, MultiMetaData<?>>();
				copy.putAll(last);
				copy.put(clazz, md);
				// try put it in, we may not succeed if someone gets there
				// first
				metaData.compareAndSet(last, copy);
			}
		}
		return rc;
	}

	/**
	 * Multi attribute fetch
	 * @param <O> The class type to fetch
	 * @param prefix The routable key without the braces {}
	 * @param clazz The class to use
	 * @return An instance of O or classz with the attributes or NULL if not found
	 */
	static public <O> O multiGet(String prefix, Class<O> clazz)
	{
		try
		{
			MultiMetaData<O> md = getMetaData(clazz);
			return md.get(prefix);
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	/**
	 * Multiattribute put. Put the attributes of the object using the
	 * attribute entry notation.
	 * @param <O>
	 * @param prefix The routable key for all attributes
	 * @param object
	 */
	static public <O> void multiPut(String prefix, O pojo)
	{
		try
		{
			MultiMetaData<O> md = (MultiMetaData<O>) getMetaData(pojo.getClass());
			md.put(prefix, pojo);
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * This removes all attributes entries for a given POJO
	 * @param <O>
	 * @param prefix
	 * @param clazz
	 */
	static public <O> void multiRemove(String prefix, Class<O> clazz)
	{
		try
		{
			MultiMetaData<O> md = (MultiMetaData<O>) getMetaData(clazz);
			md.remove(prefix);
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
