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
package com.devwebsphere.wxsutils.wxsmap;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanImpl;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;


public class SetAddRemoveAgent<V extends Serializable> implements MapGridAgent 
{
	static public int NUM_BUCKETS = 211;
	public boolean isAdd;
	public V[] values;
	/**
	 * 
	 */
	private static final long serialVersionUID = 8842082032401137638L;
	
	static public <K, V> String getBucketKeyForValue(K key, V value)
		throws ObjectGridException
	{
		int vhash = value.hashCode() % NUM_BUCKETS;
		return getBucketKeyForBucket(key, vhash);
	}

	static public <K, V> String getBucketKeyForBucket(K key, int b)
		throws ObjectGridException
	{
		StringBuilder sb = new StringBuilder(key.toString());
		sb.append("#");
		sb.append(b);
		return sb.toString();
	}
	/**
	 * This is not optimized for very large V[]s.
	 * @param <V>
	 * @param sess
	 * @param map
	 * @param key
	 * @param isAdd
	 * @param values
	 */
	static public <V> void add(Session sess, ObjectMap map, Object key, boolean isAdd, V[] values)
	{
		AgentMBeanImpl mbean = WXSUtils.getAgentMBeanManager().getBean(sess.getObjectGrid().getName(), SetAddRemoveAgent.class.getName());
		long startNS = System.nanoTime();
		try
		{
			map.getForUpdate(key); // token lock to prevent deadlocks
			for(V value : values)
			{
				String bucketKey = getBucketKeyForValue(key, value);
				Set<V> s = (Set<V>)map.getForUpdate(bucketKey);
				if(s != null)
				{
					if(isAdd)
						for(V v : values)
							s.add(v);
					else
						for(V v : values)
							s.remove(v);
					map.update(bucketKey, s);
				}
				else
				{
					s = new HashSet<V>();
					if(isAdd)
						for(V v : values)
							s.add(v);
					map.insert(bucketKey, s);
				}
			}
			mbean.getKeysMetric().logTime(System.nanoTime() - startNS);
		}
		catch(ObjectGridException e)
		{
			mbean.getKeysMetric().logException(e);
			e.printStackTrace();
			throw new ObjectGridRuntimeException(e);
		}
	}

	public Object process(Session sess, ObjectMap map, Object key) 
	{
		SetAddRemoveAgent.add(sess, map, key, isAdd, values);
		return Boolean.TRUE;
	}
	public Map processAllEntries(Session arg0, ObjectMap arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
