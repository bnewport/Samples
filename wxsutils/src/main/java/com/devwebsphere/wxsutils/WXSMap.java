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
package com.devwebsphere.wxsutils;

import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;

/**
 * This is a simplified interface to a WXS Map. It throws runtime exceptions and is completely
 * thread safe. It doesn't use transactions. It provides implementations for commonly used
 * Map methods like put, bulk methods and so on.
 *
 */
public class WXSMap 
{
	BackingMap bmap;
	ThreadLocalSession tls;
	WXSUtils utils;
	String mapName;
	
	WXSMap(WXSUtils utils, String mapName)
	{
		this.mapName = mapName;
		this.utils = utils;
		bmap = utils.grid.getMap(mapName);
		tls = new ThreadLocalSession(utils);
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
			throw new ObjectGridRuntimeException(e);
		}
	}

	/**
	 * Fetch a value from the Map
	 * @param <K>
	 * @param <V>
	 * @param k
	 * @return
	 */
	public <K,V> V get(K k)
	{
		try
		{
			return (V)tls.getMap(mapName).get(k);
		}
		catch(Exception e)
		{
			throw new ObjectGridRuntimeException(e);
		}
	}

	/**
	 * Fetch all the values for the specified keys. Null is returned if the key
	 * isn't found.
	 * @param <K>
	 * @param <V>
	 * @param keys
	 * @return
	 */
	public <K,V> Map<K,V> getAll(Collection<K> keys)
	{
		return utils.getAll(keys, bmap);
	}

	/**
	 * Set the value for the key. If the entry doesn't exist then it
	 * is inserted otherwise it's updated.
	 * @param <K>
	 * @param <V>
	 * @param k
	 * @param v
	 */
	public <K,V> void put(K k, V v)
	{
		try
		{
			InsertAgent<K, V> a = new InsertAgent<K, V>();
			a.batch = new Hashtable<K, V>();
			a.batch.put(k, v);
			tls.getMap(mapName).getAgentManager().callReduceAgent(a, Collections.singletonList(k));
		}
		catch(Exception e)
		{
			throw new ObjectGridRuntimeException(e);
		}
	}

	/**
	 * Parallel put all the entries.
	 * @param <K>
	 * @param <V>
	 * @param batch
	 */
	public <K,V> void putAll(Map<K,V> batch)
	{
		utils.putAll(batch, bmap);
	}

	/**
	 * Remove the entry from the Map
	 * @param <K>
	 * @param <V>
	 * @param k
	 * @return The last value otherwise null
	 */
	public <K,V> V remove(K k)
	{
		try
		{
			return (V)tls.getMap(mapName).remove(k);
		}
		catch(Exception e)
		{
			throw new ObjectGridRuntimeException(e);
		}
	}

	/**
	 * Remove all entries with these keys
	 * @param <K>
	 * @param keys
	 */
	public <K> void removeAll(Collection<K> keys)
	{
		utils.removeAll(keys, bmap);
	}

	/**
	 * Check if the entry exists for the key
	 * @param <K>
	 * @param k
	 * @return
	 */
	public <K> boolean contains(K k)
	{
		try
		{
			return tls.getMap(mapName).containsKey(k);
		}
		catch(Exception e)
		{
			throw new ObjectGridRuntimeException(e);
		}
	}
}
