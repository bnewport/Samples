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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanImpl;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;

public class SetAddRemoveAgent<V extends Serializable> implements MapGridAgent 
{
	static Logger logger = Logger.getLogger(SetAddRemoveAgent.class.getName());
	
	static enum Operation { ADD, REMOVE};
	
	/**
	 * This splits a set in to this many subsets. Each subset
	 * is maintained in the map using the key "keyName#NNN" where NNN
	 * is the bucket number and keyName is the .toString() of the
	 * user supplied key. Buckets exist only if there
	 * are elements in them. This constant is used by ALL the
	 * WXSUtils Set logic.
	 * The buckey keys are ALWAYS strings.
	 */
	static public int NUM_BUCKETS = 2003;
	
	/**
	 * Add or remove specifier
	 */
	public Operation op;
	/**
	 * The values to either add or remove from the set
	 */
	public V[] values;
	/**
	 * 
	 */
	private static final long serialVersionUID = 8842082032401137638L;

	/**
	 * Given a value, return the bucket key that could hold that
	 * value.
	 * @param <K> The key type of this set
	 * @param <V> The values in the set have this type
	 * @param key The user key for this set
	 * @param value The value whose bucket is required
	 * @return The key to use for this bucket
	 * @throws ObjectGridException
	 */
	static public <K, V> String getBucketKeyForValue(K key, V value)
		throws ObjectGridException
	{
		int vhash = Math.abs(value.hashCode()) % NUM_BUCKETS;
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
	static public <V extends Serializable> void doOperation(Session sess, ObjectMap map, Object key, Operation op, V... values)
	{
		AgentMBeanImpl mbean = WXSUtils.getAgentMBeanManager().getBean(sess.getObjectGrid().getName(), SetAddRemoveAgent.class.getName());
		long startNS = System.nanoTime();
		try
		{
			map.getForUpdate(key); // token lock to prevent deadlocks
			// for each value, each value could hash to different bucket
			for(V value : values)
			{
				SetElement<V> wrapper = new SetElement<V>(value);
				String bucketKey = getBucketKeyForValue(key, wrapper);
				LinkedHashSet<SetElement<V>> s = (LinkedHashSet<SetElement<V>>)map.getForUpdate(bucketKey);
				if(s != null)
				{
					switch(op)
					{
					case ADD:
						if(!s.contains(wrapper))
						{
							s.add(wrapper);
							map.update(bucketKey, s);
						}
						break;
					case REMOVE:
						if(s.contains(wrapper))
						{
							s.remove(wrapper);
							// update the new set contents or remove bucket if empty
							if(s.size() > 0)
								map.update(bucketKey, s);
							else
								map.remove(bucketKey);
						}
						break;
					default:
						throw new ObjectGridRuntimeException("Unknown operation");
					}
				}
				else
				{
					s = new LinkedHashSet<SetElement<V>>();
					if(op == Operation.ADD)
					{
						s.add(wrapper);
						map.insert(bucketKey, s);
					}
				}
			}
			mbean.getKeysMetric().logTime(System.nanoTime() - startNS);
		}
		catch(Exception e)
		{
			mbean.getKeysMetric().logException(e);
			logger.log(Level.SEVERE, "Exception", e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	public Object process(Session sess, ObjectMap map, Object key) 
	{
		SetAddRemoveAgent.doOperation(sess, map, key, op, values);
		return Boolean.TRUE;
	}
	public Map processAllEntries(Session arg0, ObjectMap arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
