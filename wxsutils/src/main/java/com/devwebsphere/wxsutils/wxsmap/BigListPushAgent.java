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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanImpl;
import com.devwebsphere.wxsutils.wxsmap.BigListHead.LR;
import com.devwebsphere.wxsutils.wxsmap.SetAddRemoveAgent.Operation;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.UndefinedMapException;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;

public class BigListPushAgent <K extends Serializable, V extends Serializable> implements MapGridAgent 
{
	static Logger logger = Logger.getLogger(BigListPushAgent.class.getName());

	public static int BUCKET_SIZE = 20;
	
	public LR isLeft;
	public V value;
	public K dirtyKey;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5627208135087330201L;
	
	static public String getDirtySetMapNameForListMap(String mapName)
	{
		StringBuilder sb = new StringBuilder(mapName);
		sb.append("_dirty");
		return sb.toString();
	}
	
	static <K extends Serializable, V extends Serializable> void push(Session sess, ObjectMap map, Object key, LR isLeft, V value, K dirtyKey)
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
			
			BigListHead<V> head = (BigListHead<V>)map.getForUpdate(key);
			if(head == null)
			{
				// this inserts the head in the map also.
				head = new BigListHead<V>(sess, map, key, value, BUCKET_SIZE);
			}
			else
			{
				// this updates the head in the map also
				head.push(sess, map, key, isLeft, value);
			}
			// maintain a set of list keys in this partition when they have
			// a push
			if(dirtyKey != null)
			{
				SetAddRemoveAgent.doOperation(sess, dirtyMap, dirtyKey, Operation.ADD, (Serializable)key);
			}
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
			e.printStackTrace();
			throw new ObjectGridRuntimeException(e);
		}
	}

	public Object process(Session sess, ObjectMap map, Object key) 
	{
		push(sess, map, key, isLeft, value, dirtyKey);
		return Boolean.TRUE;
	}

	public Map processAllEntries(Session arg0, ObjectMap arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
