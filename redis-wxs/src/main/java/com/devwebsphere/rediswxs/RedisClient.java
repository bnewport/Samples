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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import com.devwebsphere.rediswxs.agent.Incr;
import com.devwebsphere.rediswxs.agent.Remove;
import com.devwebsphere.rediswxs.agent.Set;
import com.devwebsphere.rediswxs.agent.list.LLen;
import com.devwebsphere.rediswxs.agent.list.LRange;
import com.devwebsphere.rediswxs.agent.list.LTrim;
import com.devwebsphere.rediswxs.agent.list.Pop;
import com.devwebsphere.rediswxs.agent.list.Push;
import com.devwebsphere.rediswxs.agent.set.SRemove;
import com.devwebsphere.rediswxs.agent.set.SetAdd;
import com.devwebsphere.rediswxs.agent.set.SetCard;
import com.devwebsphere.rediswxs.agent.set.SetIsMember;
import com.devwebsphere.rediswxs.agent.set.SetMembers;
import com.devwebsphere.rediswxs.jmx.RedisMBean;
import com.devwebsphere.rediswxs.jmx.RedisMBeanImpl;
import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.jmx.loader.LoaderMBeanManager;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.AgentManager;
import com.ibm.websphere.objectgrid.datagrid.EntryErrorValue;

public class RedisClient implements IRedisLowLevel 
{
	RedisMBeanImpl mbean;
	WXSUtils wxsutils;
	
	/**
	 * This class is stored in a ThreadLocal and keeps a session to the cached
	 * and uncached clients.
	 *
	 */
	static class ThreadStuff
	{
		Session cachesession;
		Session nocachesession;
	}
	
	static final Logger logger = Logger.getLogger(RedisClient.class.getName());
	
	/**
	 * This implements a ThreadLocal which keeps an ObjectGrid Session for the thread. This isn't very
	 * sophisticated and would only work if a single grid was used in a JVM. You'd need a per grid
	 * Session if multiple grids are used.
	 * @author bnewport
	 *
	 */
	final class ThreadLocalSession extends ThreadLocal<ThreadStuff>
	{
		protected ThreadStuff initialValue()
		{
			try
			{
				ThreadStuff v = new ThreadStuff();
				// the client MUST be initialized before using a threadlocal
				// otherwise cacheog/nocacheog will be null
				v.cachesession = cacheog.getSession();
				v.nocachesession = nocacheog.getSession();
				return v;
			}
			catch(Exception e)
			{
				logger.log(Level.SEVERE, "Cannot get session", e);
				throw new IllegalStateException("Cannot get session", e);
			}
		}
		
		public Session getCacheSession()
		{
			return get().cachesession;
		}
		
		public Session getNoCacheSession()
		{
			return get().nocachesession;
		}
		
		/**
		 * This returns the ObjectMap for a given map
		 * @param cached True if a near cached map is required
		 * @param name The map name
		 * @return An ObjectGrid for this thread
		 */
		public ObjectMap getMap(CacheUsage c, String name)
		{
			try
			{
				if(c == CacheUsage.NEARCACHE)
					return getCacheSession().getMap(name);
				else
					return getNoCacheSession().getMap(name);
			}
			catch(ObjectGridException e)
			{
				logger.log(Level.SEVERE, "getMap failed", e);
				throw new ObjectGridRuntimeException(e);
			}
		}
	}
	
	/**
	 * This is a client reference for the near cache client
	 */
	ObjectGrid cacheog;
	/**
	 * This is a client reference for the non near cached client
	 */
	ObjectGrid nocacheog;
	
	/**
	 * This is a thread local for this client
	 */
	public ThreadLocalSession thread = new ThreadLocalSession();

	/**
	 * This initializes a Redis client to WebSphere eXtreme Scale. If the cep parameter is null then a local WXS test instance
	 * is created within this JVM. This takes about 20 seconds on a MacBook Pro. Otherwise the cep is a comma seperated list of
	 * host:port pairs for the catalog servers for WXS.
	 * @param cep Comma seperated host:port pairs OR null
	 */
	RedisClient(String cep)
	{
		if(cep != null)
		{
			// use the client og override xml file with the near cache disabled
			nocacheog = WXSUtils.connectClient(cep, "Grid", "/clientobjectgrid.xml");
			// use the client og override xml file with the near cache turned on
			cacheog = nocacheog;
//			cacheog = WXSUtils.connectClient(cep, "Grid", "/clientwithcacheobjectgrid.xml");
		}
		else
		{
			// start a local WXS catalog and container for debugging
//			String ogxml = "/objectgrid_writethrough.xml";

			String depxml = "/deployment.xml";
			String ogxml = "/objectgrid_jdbc_writethrough.xml";
			cacheog = WXSUtils.startTestServer("Grid", ogxml, depxml);
			nocacheog = cacheog;
//			nocacheog = WXSUtils.connectClient("localhost:2809", "Grid", "/clientobjectgrid.xml");
		}
		wxsutils = new WXSUtils(nocacheog);
		initializeJMX();
	}

	public ObjectGrid getNoCacheOG()
	{
		return nocacheog;
	}
	
	public WXSUtils getWXSUtils()
	{
		return wxsutils;
	}
	
	private void initializeJMX()
	{
		try
		{
			mbean = new RedisMBeanImpl();
			MBeanServer jmxServer = LoaderMBeanManager.getServer();
			StandardMBean mbn = new StandardMBean(mbean, RedisMBean.class);
			MBeanInfo info = mbn.getMBeanInfo();
			Hashtable<String, String> props = new Hashtable<String, String>();
			props.put("name", "redis");
			props.put("type", "Redis");
			ObjectName on = new ObjectName("com.ibm.websphere.objectgrid.redis", props);
			jmxServer.registerMBean(mbn, on);
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	/**
	 * Stop the client.
	 */
	public void stop()
	{
		cacheog.destroy();
		nocacheog.destroy();
		WXSUtils.stopContainer();
	}

	/**
	 * Returns the map name suffix for a given key/value type pair
	 * @param <K>
	 * @param <V>
	 * @param keyClass
	 * @param valueClass
	 * @return
	 */
	static public <K,V> String getMapName(Class<K> keyClass, Class<V> valueClass)
	{
		if(keyClass == String.class)
		{
			if(valueClass == Long.class)
				return "string-long";
			if(valueClass == String.class)
				return "string-string";
		}
		if(keyClass == Long.class)
		{
			if(valueClass == Long.class)
				return "long-long";
			if(valueClass == String.class)
				return "long-string";
		}
		logger.log(Level.SEVERE, "Cannot get MapName for" + keyClass.getName() + ":" + valueClass.getName());
		throw new IllegalArgumentException("No map for that key/value");
	}
	
	/* (non-Javadoc)
	 * @see redis.IRedis#set(java.lang.String, K, V)
	 */
	@SuppressWarnings("unchecked")
	public <K extends Serializable, V extends Serializable> void set(CacheUsage isCached, String name, K key, V value)
	{
		long startNS = System.nanoTime();
		Set a = new Set();
		a.value = value;
		
		AgentManager am = thread.getMap(isCached, name).getAgentManager();
		
		// Call the agent. If an agent throws an exception for a given key
		// then the value returned is an EntryErrorValue instance rather than the
		// expected value so we need to check for this.
		// This applies to all the agent calls below also.
		Map<K, Object> m = am.callMapAgent(a, Collections.singleton(key));
		Object rawReturn = m.get(key);
		if(rawReturn instanceof EntryErrorValue)
		{
			EntryErrorValue rc = (EntryErrorValue)rawReturn;
			logAgentError("set", rc);
		}
		mbean.getSetMetrics().logTime(System.nanoTime() - startNS);
	}
	
	/* (non-Javadoc)
	 * @see redis.IRedis#get(java.lang.String, K)
	 */
	@SuppressWarnings("unchecked")
	public <K extends Serializable, V extends Serializable> V get(CacheUsage useCache, String name, K key)
	{
		long startNS = System.nanoTime();
		try
		{
			V value = (V)thread.getMap(useCache, name).get(key);
			mbean.getGetMetrics().logTime(System.nanoTime() - startNS);
			return value;
		}
		catch(ObjectGridException e)
		{
			logger.log(Level.SEVERE, "get failed", e);
			mbean.getSetMetrics().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see redis.IRedis#incr(java.lang.String, K)
	 */
	public <K extends Serializable> long incr(String name, K key)
	{
		return incrby(name, key, 1);
	}
	
	/* (non-Javadoc)
	 * @see redis.IRedis#incrby(java.lang.String, K, long)
	 */
	@SuppressWarnings("unchecked")
	public <K extends Serializable> long incrby(String name, K key, long delta)
	{
		long startNS = System.nanoTime();
		Incr a = new Incr();
		a.delta = delta;
		
		AgentManager am = thread.getMap(CacheUsage.NONEARCACHE, name).getAgentManager();
		List<K> list = Collections.singletonList(key);
		Map<K,Object> m = am.callMapAgent(a, list);
		Object rawReturn = m.get(key);
		if(rawReturn instanceof EntryErrorValue)
		{
			EntryErrorValue rc = (EntryErrorValue)rawReturn;
			logAgentError("incby", rc);
			mbean.getIncrMetrics().logException(rc.getException());
			return 0L;
		}
		mbean.getIncrMetrics().logTime(System.nanoTime() - startNS);
		return ((Long)m.get(key)).longValue();
	}
	
	/* (non-Javadoc)
	 * @see redis.IRedis#ltrim(java.lang.String, K, int)
	 */
	@SuppressWarnings("unchecked")
	public <K extends Serializable> boolean ltrim(String name, K key, int size)
	{
		long startNS = System.nanoTime();
		LTrim a = new LTrim();
		a.size = size;
		
		AgentManager am = thread.getMap(CacheUsage.NONEARCACHE, name).getAgentManager();
		List<K> list = Collections.singletonList(key);
		Map<K,Object> m = am.callMapAgent(a, list);
		Object rawReturn = m.get(key);
		if(rawReturn instanceof EntryErrorValue)
		{
			EntryErrorValue rc = (EntryErrorValue)rawReturn;
			logAgentError("ltrim", rc);
			mbean.getLtrimMetrics().logException(rc.getException());
			return false;
		}
		else
		{
			mbean.getLtrimMetrics().logTime(System.nanoTime() - startNS);
			return ((Boolean)m.get(key)).booleanValue();
		}
	}
	
	/* (non-Javadoc)
	 * @see redis.IRedis#lpush(java.lang.String, K, V)
	 */
	@SuppressWarnings("unchecked")
	public <K extends Serializable, V extends Serializable> void lpush(String name, K key, V value)
	{
		long startNS = System.nanoTime();
		Push a = new Push();
		a.isLeft = true;
		a.value = value;
		
		AgentManager am = thread.getMap(CacheUsage.NONEARCACHE, name).getAgentManager();
		List<K> list = Collections.singletonList(key);
		Map <K, Object> m = am.callMapAgent(a, list);
		Object rawReturn = m.get(key);
		if(rawReturn instanceof EntryErrorValue)
		{
			EntryErrorValue rc = (EntryErrorValue)rawReturn;
			logAgentError("lpush", rc);
			mbean.getLpushMetrics().logException(rc.getException());
		}
		mbean.getLpushMetrics().logTime(System.nanoTime() - startNS);
	}
	
	/* (non-Javadoc)
	 * @see redis.IRedis#lpop(java.lang.String, K)
	 */
	@SuppressWarnings("unchecked")
	public <K extends Serializable, V extends Serializable> V lpop(String name, K key)
	{
		long startNS = System.nanoTime();
		Pop a = new Pop();
		a.isLeft = true;
		
		AgentManager am = thread.getMap(CacheUsage.NONEARCACHE, name).getAgentManager();
		List<K> list = Collections.singletonList(key);
		Map <K, Object> m = am.callMapAgent(a, list);
		Object rawReturn = m.get(key);
		if(rawReturn instanceof EntryErrorValue)
		{
			EntryErrorValue rc = (EntryErrorValue)rawReturn;
			logAgentError("lpop", rc);
			mbean.getLpopMetrics().logException(rc.getException());
			return null;
		}
		else
		{
			mbean.getLpopMetrics().logTime(System.nanoTime() - startNS);
			return (V)rawReturn;
		}
	}
	
	/* (non-Javadoc)
	 * @see redis.IRedis#rpop(java.lang.String, K)
	 */
	@SuppressWarnings("unchecked")
	public <K extends Serializable, V extends Serializable> V rpop(String name, K key)
	{
		long startNS = System.nanoTime();
		Pop a = new Pop();
		a.isLeft = false;
		
		AgentManager am = thread.getMap(CacheUsage.NONEARCACHE, name).getAgentManager();
		List<K> list = Collections.singletonList(key);
		Map <K, Object> m = am.callMapAgent(a, list);
		Object rawReturn = m.get(key);
		if(rawReturn instanceof EntryErrorValue)
		{
			EntryErrorValue rc = (EntryErrorValue)rawReturn;
			logAgentError("rpop", rc);
			mbean.getRpopMetrics().logException(rc.getException());
			return null;
		}
		else
		{
			mbean.getRpopMetrics().logTime(System.nanoTime() - startNS);
			return (V)rawReturn;
		}
	}
	
	/* (non-Javadoc)
	 * @see redis.IRedis#rpush(java.lang.String, K, V)
	 */
	@SuppressWarnings("unchecked")
	public <K extends Serializable, V extends Serializable> void rpush(String name, K key, V value)
	{
		long startNS = System.nanoTime();
		Push a = new Push();
		a.isLeft = false;
		a.value = value;
		
		AgentManager am = thread.getMap(CacheUsage.NONEARCACHE, name).getAgentManager();
		List<K> list = Collections.singletonList(key);
		Map <K, Object> m = am.callMapAgent(a, list);
		Object rawReturn = m.get(key);
		if(rawReturn instanceof EntryErrorValue)
		{
			EntryErrorValue rc = (EntryErrorValue)rawReturn;
			logAgentError("rpush", rc);
			mbean.getRpushMetrics().logException(rc.getException());
		}
		mbean.getRpushMetrics().logTime(System.nanoTime() - startNS);
	}
	
	public void logAgentError(String label, EntryErrorValue er)
	{
		System.out.println(label + " Agent threw exception: " + er.getErrorExceptionString() + " on server " + er.getServerName());
	}
	
	/* (non-Javadoc)
	 * @see redis.IRedis#lrange(java.lang.String, K, int, int)
	 */
	@SuppressWarnings("unchecked")
	public <K extends Serializable, V extends Serializable> ArrayList<V> lrange(String name, K key, int low, int high)
	{
		long startNS = System.nanoTime();
		LRange a = new LRange();
		a.low = low;
		a.high = high;
		
		AgentManager am = thread.getMap(CacheUsage.NONEARCACHE, name).getAgentManager();
		List<K> list = Collections.singletonList(key);
		Map<K,Object> result = am.callMapAgent(a, list);
		Object rawReturn = result.get(key);
		if(rawReturn instanceof EntryErrorValue)
		{
			EntryErrorValue rc = (EntryErrorValue)rawReturn;
			logAgentError("lrange", rc);
			mbean.getLrangeMetrics().logException(rc.getException());
			return new ArrayList<V>();
		}
		else
		{
			mbean.getLrangeMetrics().logTime(System.nanoTime() - startNS);
			return (ArrayList<V>)rawReturn;
		}
	}
	
	/* (non-Javadoc)
	 * @see redis.IRedis#sadd(java.lang.String, K, V)
	 */
	@SuppressWarnings("unchecked")
	public <K extends Serializable, V extends Serializable> boolean sadd(String name, K key, V value)
	{
		long startNS = System.nanoTime();
		SetAdd a = new SetAdd();
		a.value = value;
		
		AgentManager am = thread.getMap(CacheUsage.NONEARCACHE, name).getAgentManager();
		List<K> list = Collections.singletonList(key);
		Map<K, Object> rc = am.callMapAgent(a, list);
		Object rawReturn = rc.get(key);
		if(rawReturn instanceof EntryErrorValue) 
		{
			EntryErrorValue er = (EntryErrorValue)rawReturn;
			logAgentError("sadd", er);
			mbean.getSaddMetrics().logException(er.getException());
			return false;
		}
		mbean.getSaddMetrics().logTime(System.nanoTime() - startNS);
		return (Boolean)rc.get(key);
	}
	
	/* (non-Javadoc)
	 * @see redis.IRedis#srem(java.lang.String, K, V)
	 */
	@SuppressWarnings("unchecked")
	public <K extends Serializable, V extends Serializable> boolean srem(String name, K key, V value)
	{
		long startNS = System.nanoTime();
		SRemove a = new SRemove();
		a.value = value;
		
		AgentManager am = thread.getMap(CacheUsage.NONEARCACHE, name).getAgentManager();
		List<K> list = Collections.singletonList(key);
		Map<K, Object> rc = am.callMapAgent(a, list);
		Object rawReturn = rc.get(key);
		if(rawReturn instanceof EntryErrorValue)
		{
			EntryErrorValue er = (EntryErrorValue)rawReturn;
			logAgentError("srem", er);
			mbean.getSremMetrics().logException(er.getException());
			return false;
		}
		else
		{
			mbean.getSremMetrics().logTime(System.nanoTime() - startNS);
			return (Boolean)rawReturn;
		}
	}
	
	/* (non-Javadoc)
	 * @see redis.IRedis#scard(java.lang.String, K)
	 */
	@SuppressWarnings("unchecked")
	public <K extends Serializable> int scard(String name, K key)
	{
		long startNS = System.nanoTime();
		SetCard a = new SetCard();
		
		AgentManager am = thread.getMap(CacheUsage.NONEARCACHE, name).getAgentManager();
		List<K> list = Collections.singletonList(key);
		Map<K,Object> rc = am.callMapAgent(a, list);
		Object rawReturn = rc.get(key);
		if(rawReturn instanceof EntryErrorValue)
		{
			EntryErrorValue er = (EntryErrorValue)rawReturn;
			logAgentError("scard", er);
			mbean.getScardMetrics().logException(er.getException());
			return new Integer(0);
		}
		else
		{
			mbean.getScardMetrics().logTime(System.nanoTime() - startNS);
			return (Integer)rawReturn;
		}
	}
	
	/* (non-Javadoc)
	 * @see redis.IRedis#llen(java.lang.String, K)
	 */
	@SuppressWarnings("unchecked")
	public <K extends Serializable> int llen(String name, K key)
	{
		long startNS = System.nanoTime();
		LLen a = new LLen();
		
		AgentManager am = thread.getMap(CacheUsage.NONEARCACHE, name).getAgentManager();
		List<K> list = Collections.singletonList(key);
		Map<K,Object> rc = am.callMapAgent(a, list);
		Object rawReturn = rc.get(key);
		if(rawReturn instanceof EntryErrorValue)
		{
			EntryErrorValue er = (EntryErrorValue)rawReturn;
			logAgentError("llen", er);
			mbean.getLlenMetrics().logException(er.getException());
			return new Integer(0);
		}
		else
		{
			mbean.getLlenMetrics().logTime(System.nanoTime() - startNS);
			return (Integer)rawReturn;
		}
	}
	
	/* (non-Javadoc)
	 * @see redis.IRedis#remove(java.lang.String, K)
	 */
	@SuppressWarnings("unchecked")
	public <K extends Serializable> boolean remove(String name, K key)
	{
		long startNS = System.nanoTime();
		Remove a = new Remove();
		
		AgentManager am = thread.getMap(CacheUsage.NEARCACHE, name).getAgentManager();
		List<K> list = Collections.singletonList(key);
		Map<K,Object> rc = am.callMapAgent(a, list);
		Object rawReturn = rc.get(key);
		if(rawReturn instanceof EntryErrorValue)
		{
			EntryErrorValue er = (EntryErrorValue)rawReturn;
			logAgentError("remove", er);
			mbean.getRemoveMetrics().logException(er.getException());
			return false;
		}
		else
		{
			mbean.getRemoveMetrics().logTime(System.nanoTime() - startNS);
			return (Boolean)rawReturn;
		}
	}

	/* (non-Javadoc)
	 * @see redis.IRedis#sismember(java.lang.String, K, V)
	 */
	@SuppressWarnings("unchecked")
	public <K extends Serializable, V extends Serializable> boolean sismember(String name, K key, V value)
	{
		long startNS = System.nanoTime();
		SetIsMember a = new SetIsMember();
		a.value = value;
		
		AgentManager am = thread.getMap(CacheUsage.NONEARCACHE, name).getAgentManager();
		List<K> list = Collections.singletonList(key);
		Map<K,Object> rc = am.callMapAgent(a, list);
		Object rawReturn = rc.get(key);
		if(rawReturn instanceof EntryErrorValue)
		{
			EntryErrorValue er = (EntryErrorValue)rawReturn;
			logAgentError("sismember", er);
			mbean.getSismemberMetrics().logException(er.getException());
			return false;
		}
		else
		{
			mbean.getSismemberMetrics().logTime(System.nanoTime() - startNS);
			return (Boolean)rawReturn;
		}
	}
	
	/* (non-Javadoc)
	 * @see redis.IRedis#smembers(java.lang.String, K)
	 */
	@SuppressWarnings("unchecked")
	public <K extends Serializable, V extends Serializable> List<V> smembers(String name, K key)
	{
		long startNS = System.nanoTime();
		SetMembers a = new SetMembers();
		
		AgentManager am = thread.getMap(CacheUsage.NONEARCACHE, name).getAgentManager();
		List<K> list = Collections.singletonList(key);
		Map<K,Object> result = am.callMapAgent(a, list);
		Object rawReturn = result.get(key);
		if(rawReturn instanceof EntryErrorValue)
		{
			EntryErrorValue rc = (EntryErrorValue)rawReturn;
			logAgentError("smembers", rc);
			mbean.getSmembersMetrics().logException(rc.getException());
			return new ArrayList<V>();
		}
		else
		{
			mbean.getSmembersMetrics().logTime(System.nanoTime() - startNS);
			return (ArrayList<V>)rawReturn;
		}
	}
	
	public <K extends Serializable> void invalidate(String name, K key)
	{
		long startNS = System.nanoTime();
		ObjectMap map = thread.getMap(CacheUsage.NONEARCACHE, name);
		// remove from near cache
		try
		{
			thread.getCacheSession().beginNoWriteThrough();
			map.invalidate(key, true);
			thread.getCacheSession().commit();
			mbean.getInvalidateMetrics().logTime(System.nanoTime() - startNS);
		}
		catch(ObjectGridException e)
		{
			mbean.getInvalidateMetrics().logException(e);
			throw new ObjectGridRuntimeException("Cannot invalidate", e);
		}
	}
	public void invokeAsyncOperation(String eventName, Map<String,String> properties)
	{
		
	}

	public final RedisMBeanImpl getMbean() {
		return mbean;
	}
}

	
	