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
import java.util.ArrayList;
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

public class BigListPopAllAgent<K extends Serializable, V extends Serializable> implements MapGridAgent 
{
	static Logger logger = Logger.getLogger(BigListPopAllAgent.class.getName());
	/**
	 * 
	 */
	private static final long serialVersionUID = -3820884829092397741L;

	public K dirtyKey;
	
	static public <K extends Serializable, V extends Serializable> ArrayList<V> popAll(Session sess, ObjectMap map, Object key, K dirtyKey)
	{
		AgentMBeanImpl mbean = WXSUtils.getAgentMBeanManager().getBean(sess.getObjectGrid().getName(), BigListPopAllAgent.class.getName());
		long startNS = System.nanoTime();
		try
		{
			ObjectMap dirtyMap = null;
			// lock dirtymap first to avoid dead locks
			if(dirtyKey != null)
			{
				dirtyMap = sess.getMap(BigListPushAgent.getDirtySetMapNameForListMap(map.getName()));
				dirtyMap.getForUpdate(dirtyKey);
			}
			ArrayList<V> rc = null;
			BigListHead<V> head = (BigListHead<V>)map.getForUpdate(key);
			if(head != null)
				rc = head.popAll(sess, map, key, dirtyKey);
			else
				rc = new ArrayList<V>();
			mbean.getKeysMetric().logTime(System.nanoTime() - startNS);
			return rc;
		}
		catch(ObjectGridException e)
		{
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getKeysMetric().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}
	/**
	 * 
	 */
	public Object process(Session sess, ObjectMap map, Object key) 
	{
		return popAll(sess, map, key, dirtyKey);
	}

	public Map processAllEntries(Session arg0, ObjectMap arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
