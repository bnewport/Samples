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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.WXSMap;
import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.filter.Filter;
import com.devwebsphere.wxsutils.filter.set.GridFilteredIndex;
import com.devwebsphere.wxsutils.filter.set.GridFilteredIndex.Operation;
import com.devwebsphere.wxsutils.jmx.wxsmap.WXSMapMBeanImpl;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;

/**
 * This is a simplified interface to a WXS Map. It throws runtime exceptions and is completely
 * thread safe. It doesn't use transactions. It provides implementations for commonly used
 * Map methods like put, bulk methods and so on.
 *
 */
public class WXSMapImpl <K extends Serializable,V extends Serializable> extends WXSBaseMap implements WXSMap<K, V>
{
	static Logger logger = Logger.getLogger(WXSMapImpl.class.getName());
	
	public WXSMapImpl(WXSUtils utils, String mapName)
	{
		super(utils, mapName);
	}

	/**
	 * Clear the Map
	 */
	public void clear()
	{
		try
		{
			tls.getMap(mapName).clear();
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception", e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	/**
	 * Fetch a value from the Map
	 * @param k
	 * @return
	 */
	public V get(K k)
	{
		WXSMapMBeanImpl mbean = WXSUtils.getWXSMapMBeanManager().getBean(grid.getName(), mapName);
		long start = System.nanoTime();
		try
		{
			V rc = (V)tls.getMap(mapName).get(k);
			mbean.getGetMetrics().logTime(System.nanoTime() - start);
			return rc;
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getGetMetrics().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	/**
	 * Fetch all the values for the specified keys. Null is returned if the key
	 * isn't found.
	 * @param keys
	 * @return
	 */
	public Map<K,V> getAll(Collection<K> keys)
	{
		WXSMapMBeanImpl mbean = WXSUtils.getWXSMapMBeanManager().getBean(grid.getName(), mapName);
		long start = System.nanoTime();
		Map<K,V> rc =  utils.getAll(keys, bmap);
		mbean.getGetMetrics().logTime(System.nanoTime() - start);
		return rc;
	}

	/**
	 * Set the value for the key. If the entry doesn't exist then it
	 * is inserted otherwise it's updated.
	 * @param k
	 * @param v
	 */
	public void put(K k, V v)
	{
		WXSMapMBeanImpl mbean = WXSUtils.getWXSMapMBeanManager().getBean(grid.getName(), mapName);
		long start = System.nanoTime();
		try
		{
			InsertAgent<K, V> a = new InsertAgent<K, V>();
			a.doGet = true;
			a.batch = new HashMap<K, V>();
			a.batch.put(k, v);
			Object o = tls.getMap(mapName).getAgentManager().callReduceAgent(a, Collections.singletonList(k));
			if(o instanceof Boolean)
			{
				Boolean b = (Boolean)o;
				if(!b)
				{
					logger.log(Level.SEVERE, "put(K,V) failed");
					throw new ObjectGridRuntimeException("put failed");
				}
			}
			mbean.getPutMetrics().logTime(System.nanoTime() - start);
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getPutMetrics().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	/**
	 * This does a single entry insert. If the key already exists then an exception is thrown.
	 * @param k
	 * @param v
	 */
	public void insert(K k, V v)
	{
		WXSMapMBeanImpl mbean = WXSUtils.getWXSMapMBeanManager().getBean(grid.getName(), mapName);
		long start = System.nanoTime();
		try
		{
			tls.getMap(mapName).insert(k, v);
			mbean.getInsertMetrics().logTime(System.nanoTime() - start);
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getInsertMetrics().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	/**
	 * Parallel put all the entries.
	 * @param batch
	 */
	public void putAll(Map<K,V> batch)
	{
		WXSMapMBeanImpl mbean = WXSUtils.getWXSMapMBeanManager().getBean(grid.getName(), mapName);
		long start = System.nanoTime();
		utils.putAll(batch, bmap);
		mbean.getPutMetrics().logTime(System.nanoTime() - start);
	}
	
	public Map<K, Boolean> cond_putAll(Map<K,V> originalValues, Map<K,V> newValues)
	{
		WXSMapMBeanImpl mbean = WXSUtils.getWXSMapMBeanManager().getBean(grid.getName(), mapName);
		long start = System.nanoTime();
		Map<K, Boolean> rc = utils.cond_putAll(originalValues, newValues, bmap);
		mbean.getPutMetrics().logTime(System.nanoTime() - start);
		return rc;
	}


	/**
	 * Parallel insert all the entries. This does a real insert, not a put (get/update)
	 * @param batch
	 */
	public void insertAll(Map<K,V> batch)
	{
		WXSMapMBeanImpl mbean = WXSUtils.getWXSMapMBeanManager().getBean(grid.getName(), mapName);
		long start = System.nanoTime();
		utils.insertAll(batch, bmap);
		mbean.getInsertMetrics().logTime(System.nanoTime() - start);
	}
	
	/**
	 * Remove the entry from the Map
	 * @param k
	 * @return The last value otherwise null
	 */
	public V remove(K k)
	{
		WXSMapMBeanImpl mbean = WXSUtils.getWXSMapMBeanManager().getBean(grid.getName(), mapName);
		long start = System.nanoTime();
		try
		{
			V rc = (V)tls.getMap(mapName).remove(k);
			mbean.getRemoveMetrics().logTime(System.nanoTime() - start);
			return rc;
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getRemoveMetrics().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	/**
	 * Remove the entry from the Map
	 * @param k
	 * @return The last value otherwise null
	 */
	public void invalidate(K k)
	{
		WXSMapMBeanImpl mbean = WXSUtils.getWXSMapMBeanManager().getBean(grid.getName(), mapName);
		long start = System.nanoTime();
		try
		{
			tls.getMap(mapName).invalidate(k, true);
			mbean.getInvalidateMetrics().logTime(System.nanoTime() - start);
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getInvalidateMetrics().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}
	
	/**
	 * Remove all entries with these keys
	 * @param keys
	 */
	public void removeAll(Collection<K> keys)
	{
		WXSMapMBeanImpl mbean = WXSUtils.getWXSMapMBeanManager().getBean(grid.getName(), mapName);
		long start = System.nanoTime();
		utils.removeAll(keys, bmap);
		mbean.getRemoveMetrics().logTime(System.nanoTime() - start);
	}

	/**
	 * Invalidate all entries with these keys
	 * @param keys
	 */
	public void invalidateAll(Collection<K> keys)
	{
		WXSMapMBeanImpl mbean = WXSUtils.getWXSMapMBeanManager().getBean(grid.getName(), mapName);
		long start = System.nanoTime();
		utils.invalidateAll(keys, bmap);
		mbean.getInvalidateMetrics().logTime(System.nanoTime() - start);
	}

	/**
	 * Check if the entry exists for the key
	 * @param k
	 * @return
	 */
	public boolean contains(K k)
	{
		WXSMapMBeanImpl mbean = WXSUtils.getWXSMapMBeanManager().getBean(grid.getName(), mapName);
		long start = System.nanoTime();
		try
		{
			boolean rc = tls.getMap(mapName).containsKey(k);
			mbean.getContainsMetrics().logTime(System.nanoTime() - start);
			return rc;
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getContainsMetrics().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}
	
	/**
	 * This get an advisory lock on a key. In reality, it inserts a record in a 'lock' map to acquire ownership
	 * of a notional named lock (the name is the key). It will try to acquire the lock for at least timeoutMs.
	 * Once acquired, the lock is permanent until the lock is removed OR evictor by configuring a default evictor
	 * on the lock map.
	 * @param k The name of the lock
	 * @param value Any value, doesn't matter, typically use a Boolean
	 * @param timeOutMS Desired max wait time for a lock
	 * @return true if lock is acquired.
	 */
	public boolean lock(K k, V value, int timeOutMS)
	{
		WXSMapMBeanImpl mbean = WXSUtils.getWXSMapMBeanManager().getBean(grid.getName(), mapName);
		long start = System.nanoTime();
		Session sess = tls.getSession();
		try
		{
			sess.begin();
			ObjectMap map = sess.getMap(mapName);
			map.setLockTimeout(timeOutMS);
			V old = (V)map.getForUpdate(k);
			if(old != null)
			{
				map.update(k, value);
			}
			else
			{
				map.insert(k, value);
			}
			sess.commit();
			mbean.getLockMetrics().logTime(System.nanoTime() - start);
			return true;
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getLockMetrics().logException(e);
		}
		finally
		{
			if(sess.isTransactionActive())
			{
				try { sess.rollback(); } catch(Exception e2) {}
			}
		}
		return false;
	}
	
	/**
	 * Unlock a lock acquired by lock.
	 * @param k The name of the lock
	 */
	public void unlock(K k)
	{
		WXSMapMBeanImpl mbean = WXSUtils.getWXSMapMBeanManager().getBean(grid.getName(), mapName);
		long start = System.nanoTime();
		try
		{
			tls.getMap(mapName).remove(k);
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getUnlockMetrics().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
		mbean.getUnlockMetrics().logTime(System.nanoTime() - start);
	}

	public GridFilteredIndex btwn(String indexName, Serializable low,
			Serializable high, Filter f) {
		GridFilteredIndex<K, V> g = new GridFilteredIndex<K, V>(tls.getObjectGrid(), mapName, indexName, f, low, high);
		return g;
	}

	public GridFilteredIndex<K, V> eq(String indexName, Serializable v, Filter f) {
		GridFilteredIndex<K, V> g = new GridFilteredIndex<K, V>(tls.getObjectGrid(), mapName, indexName, f, Operation.eq, v);
		return g;
	}

	public GridFilteredIndex<K, V> gt(String indexName, Serializable v, Filter f) {
		GridFilteredIndex<K, V> g = new GridFilteredIndex<K, V>(tls.getObjectGrid(), mapName, indexName, f, Operation.gt, v);
		return g;
	}

	public GridFilteredIndex<K, V> gte(String indexName, Serializable v,
			Filter f) {
		GridFilteredIndex<K, V> g = new GridFilteredIndex<K, V>(tls.getObjectGrid(), mapName, indexName, f, Operation.gte, v);
		return g;
	}

	public GridFilteredIndex<K, V> lt(String indexName, Serializable v, Filter f) {
		GridFilteredIndex<K, V> g = new GridFilteredIndex<K, V>(tls.getObjectGrid(), mapName, indexName, f, Operation.lt, v);
		return g;
	}

	public GridFilteredIndex<K, V> lte(String indexName, Serializable v,
			Filter f) {
		GridFilteredIndex<K, V> g = new GridFilteredIndex<K, V>(tls.getObjectGrid(), mapName, indexName, f, Operation.lte, v);
		return g;
	}
}
