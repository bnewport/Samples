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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.WXSMapOfLists.BulkPushItem;
import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanImpl;
import com.devwebsphere.wxsutils.utils.ClassSerializer;
import com.devwebsphere.wxsutils.wxsmap.BigListHead.LR;
import com.devwebsphere.wxsutils.wxsmap.SetAddRemoveAgent.Operation;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.UndefinedMapException;
import com.ibm.websphere.objectgrid.datagrid.ReduceGridAgent;

public class BigListPushAgent <K extends Serializable, V extends Serializable> implements ReduceGridAgent, Externalizable 
{
	static Logger logger = Logger.getLogger(BigListPushAgent.class.getName());

	public static int BUCKET_SIZE = 5;
	
	public LR isLeft;
	// keys MUST be sorted on client
	public List<K> keys;
	// corresponding lists for each key
	public List<List<BulkPushItem<V>>> values;
	public K dirtyKey;

	public void setKeyValues(Map<K, List<BulkPushItem<V>>> batch)
	{
		TreeSet<K> sortedKeys = new TreeSet<K>(batch.keySet());
		keys = new ArrayList<K>(sortedKeys);
		values = new ArrayList<List<BulkPushItem<V>>>(keys.size());
		for(K k : keys)
			values.add(batch.get(k));
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -5627208135087330201L;
	
	static public String getDirtySetMapNameForListMap(String mapName)
	{
		StringBuilder sb = new StringBuilder("LDIRTY.");
		sb.append(BigListHead.getListNameFromHeadMapName(mapName));
		return sb.toString();
	}
	
	static public String getDirtySetLockMapNameForListMap(String mapName)
	{
		StringBuilder sb = new StringBuilder("LCKDIRTY.");
		sb.append(BigListHead.getListNameFromHeadMapName(mapName));
		return sb.toString();
	}
	
	static <K extends Serializable, V extends Serializable> void push(Session sess, ObjectMap map, LR isLeft, List<K> keys, List<List<BulkPushItem<V>>> values, K dirtyKey)
	{
		AgentMBeanImpl mbean = WXSUtils.getAgentMBeanManager().getBean(sess.getObjectGrid().getName(), BigListPushAgent.class.getName());
		long startNS = System.nanoTime();
		try
		{
			ObjectMap dirtyMap = null;
			// lock dirtymap first to avoid dead locks
			if(dirtyKey != null)
			{
				dirtyMap = sess.getMap(getDirtySetMapNameForListMap(map.getName()));
				dirtyMap.getForUpdate(dirtyKey);
			}
			
			for(int index = 0; index < keys.size(); ++index)
			{
				K key = keys.get(index);
				BigListHead<V> head = (BigListHead<V>)map.getForUpdate(key);
				List<BulkPushItem<V>> vl = values.get(index);
				for(BulkPushItem<V> v : vl)
				{
					if(head == null)
					{
						// this inserts the head in the map also.
						head = new BigListHead<V>(sess, map, key, v.getValue(), BUCKET_SIZE);
					}
					else
					{
						// this updates the head in the map also
						head.push(sess, map, key, isLeft, v.getValue(), v.getFilter());
					}
				}
				if(dirtyKey != null)
				{
					if(logger.isLoggable(Level.FINE))
					{
						logger.log(Level.FINE, "Adding key [" + key + "] to dirtySet [" + dirtyKey +"]");
					}
					SetAddRemoveAgent.doOperation(sess, dirtyMap, dirtyKey, Operation.ADD, (Serializable)key);
				}
			}
			// maintain a set of list keys in this partition when they have
			// a push
			mbean.getKeysMetric().logTime(System.nanoTime() - startNS);
		}
		catch(UndefinedMapException e)
		{
			logger.log(Level.SEVERE, "Undefined map", e);
			throw new ObjectGridRuntimeException(e);
		}
		catch(ObjectGridException e)
		{
			logger.log(Level.SEVERE, "Unexpected exception", e);
			mbean.getKeysMetric().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	public Object reduce(Session sess, ObjectMap map, Collection arg2)
	{
		push(sess, map, isLeft, keys, values, dirtyKey);
		return Boolean.TRUE;
	}

	/**
	 * Combine the Boolean results of the process calls using
	 * AND
	 */
	public Object reduceResults(Collection arg0) 
	{
		boolean rc = true;
		for(Object o : arg0)
		{
			if(o instanceof Boolean)
			{
				Boolean b = (Boolean)o;
				rc = rc && b;
			}
			else
			{
				rc = false;
			}
			if(!rc) break;
		}
		return rc;
	}

	public Object reduce(Session sess, ObjectMap map) 
	{
		return null;
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException 
	{
		ClassSerializer serializer = WXSUtils.getSerializer();
		dirtyKey = (K)serializer.readObject(in);
		isLeft = (in.readBoolean()) ? LR.LEFT : LR.RIGHT;
		keys = serializer.readList(in);
		values = serializer.readList(in);
	}

	public void writeExternal(ObjectOutput out) throws IOException 
	{
		ClassSerializer serializer = WXSUtils.getSerializer();
		serializer.writeObject(out, dirtyKey);
		out.writeBoolean(isLeft == LR.LEFT ? true : false);
		serializer.writeList(out, keys);
		serializer.writeList(out, values);
	}
}
