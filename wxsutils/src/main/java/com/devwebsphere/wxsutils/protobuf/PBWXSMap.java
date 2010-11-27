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
package com.devwebsphere.wxsutils.protobuf;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxssearch.ByteArrayKey;
import com.devwebsphere.wxsutils.WXSMap;
import com.devwebsphere.wxsutils.WXSUtils;
import com.google.protobuf.AbstractMessage;

public class PBWXSMap<K extends AbstractMessage, V extends AbstractMessage> extends WXSMap<K, V> 
{
	static Logger logger = Logger.getLogger(PBWXSMap.class.getName());
	Class<K> keyClazz;
	Class<V> valueClazz;
	
	// this static method inflates a byte[] to a key
	Method keyParseFromMethod;
	// this static method inflates a byte[] to a value
	Method valueParseFromMethod;

	// the underlying map
	WXSMap<ByteArrayKey, byte[]> map;
	
	@Override
	public void clear() 
	{
		map.clear();
	}

	@Override
	public boolean contains(K k) 
	{
		ByteArrayKey key = new ByteArrayKey(k.toByteArray());
		return map.contains(key);
	}

	K inflateKey(byte[] b)
	{
		try
		{
			Object[] args = new Object[] { b };
			// static method Object parseFrom(byte[] b)
			K k = (K)valueParseFromMethod.invoke(null, args);
			return k;
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception decoding protobuf key byte[]", e);
			throw new IllegalArgumentException("Exception decoding protobuf byte[] for " + keyClazz.getName(), e);
		}
	}
	
	V inflateValue(byte[] b)
	{
		try
		{
			Object[] args = new Object[] { b };
			// static method Object parseFrom(byte[] b)
			V v = (V)valueParseFromMethod.invoke(null, args);
			return v;
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception decoding protobuf value byte[]", e);
			throw new IllegalArgumentException("Exception decoding protobuf byte[] for " + valueClazz.getName(), e);
		}
	}
	
	@Override
	public V get(K k) 
	{
		byte[] b = map.get(new ByteArrayKey(k.toByteArray()));
		if(b != null)
		{
			return inflateValue(b);
		}
		else
			return null;
	}

	@Override
	public Map<K, V> getAll(Collection<K> keys) 
	{
		Collection<ByteArrayKey> kb = convertPBListToList(keys);
		// TODO Auto-generated method stub
		Map<ByteArrayKey, byte[]> rc = map.getAll(kb);
		Map<K, V> a = new HashMap<K, V>();
		Iterator<ByteArrayKey> rk = rc.keySet().iterator();
		while(rk.hasNext())
		{
			ByteArrayKey k = rk.next();
			a.put(inflateKey(k.getBytes()), inflateValue(rc.get(k)));
		}
		return a;
	}

	@Override
	public void insert(K k, V v) 
	{
		ByteArrayKey bk = new ByteArrayKey(k.toByteArray());
		map.insert(bk, v.toByteArray());
	}

	ByteArrayKey getKey(K k)
	{
		return new ByteArrayKey(k.toByteArray());
	}
	
	Map<ByteArrayKey, byte[]> convertPBMapToMap(Map<K, V> m)
	{
		Map<ByteArrayKey, byte[]> a = new HashMap<ByteArrayKey, byte[]>();
		Iterator<K> keys = m.keySet().iterator();
		while(keys.hasNext())
		{
			K k = keys.next();
			a.put(getKey(k), m.get(k).toByteArray());
		}
		return a;
	}
	
	Collection<ByteArrayKey> convertPBListToList(Collection<K> c)
	{
		Collection<ByteArrayKey> a = new ArrayList<ByteArrayKey>();
		Iterator<K> keys = c.iterator();
		while(keys.hasNext())
		{
			K k = keys.next();
			a.add(getKey(k));
		}
		return a;
	}
	
	@Override
	public void insertAll(Map<K, V> batch) 
	{
		Map<ByteArrayKey, byte[]> m = convertPBMapToMap(batch);
		map.insertAll(m);
	}

	@Override
	public boolean lock(K k, V value, int timeOutMS) 
	{
		return map.lock(getKey(k), value.toByteArray(), timeOutMS);
	}

	@Override
	public void put(K k, V v) 
	{
		map.put(getKey(k), v.toByteArray());
	}

	@Override
	public void putAll(Map<K, V> batch) 
	{
		Map<ByteArrayKey, byte[]> m = convertPBMapToMap(batch);
		map.putAll(m);
	}

	@Override
	public V remove(K k) {
		byte[] a = map.remove(getKey(k));
		if(a != null)
			return inflateValue(a);
		else
			return null;
	}

	@Override
	public void invalidate(K k) {
		map.invalidate(getKey(k));
	}
	
	@Override
	public void removeAll(Collection<K> keys) 
	{
		Collection<ByteArrayKey> a = convertPBListToList(keys);
		map.removeAll(a);
	}

	@Override
	public void invalidateAll(Collection<K> keys) 
	{
		Collection<ByteArrayKey> a = convertPBListToList(keys);
		map.invalidateAll(a);
	}

	@Override
	public void unlock(K k) {
		map.unlock(getKey(k));
	}

	/**
	 * This is public because putting it on WXSUtils means everyone would need protobuf
	 * on their classpath whether used or not.
	 * @param utils The grid connection
	 * @param mapName 
	 * @param keyClazz Key class, must be the protobuf generated stub for the key
	 * @param valueclazz Value class, must be the protobuf generated stub for the value
	 */
	public PBWXSMap(WXSUtils utils, String mapName, Class<K> keyClazz, Class<V> valueclazz)
	{
		this.keyClazz = keyClazz;
		this.valueClazz = valueclazz;
		this.map = utils.getCache(mapName);

		// protobuf put the inflate aka parseFrom method on a static on the generated stub
		// so we need to jump through some hoops to make it work
		try
		{
			Class[] parseFromArgs = new Class[] { byte[].class };
			keyParseFromMethod = keyClazz.getMethod("parseFrom", parseFromArgs);
			valueParseFromMethod = valueClazz.getMethod("parseFrom", parseFromArgs);
		}
		catch(NoSuchMethodException e)
		{
			logger.log(Level.SEVERE, "No parseFrom methods found, impossible?");
			throw new IllegalStateException("No parseFrom method on key or value class:" + keyClazz.getName() + ":" + valueClazz.getName());
		}
	}
}
