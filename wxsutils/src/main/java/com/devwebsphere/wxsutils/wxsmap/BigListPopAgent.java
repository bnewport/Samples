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
import java.util.Map;

import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanImpl;
import com.devwebsphere.wxsutils.wxsmap.BigListHead.LR;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;

public class BigListPopAgent<K extends Serializable, V extends Serializable> implements MapGridAgent 
{

	static public class EmptyMarker implements Serializable
	{

		/**
		 * 
		 */
		private static final long serialVersionUID = 5608399827890355903L;
		
	};
	
	public LR isLeft;
	public K dirtyKey;
	
	static EmptyMarker emptyMarker = new EmptyMarker();
	
	static public <K extends Serializable, V extends Serializable> Object pop(Session sess, ObjectMap map, Object key, LR isLeft, K dirtyKey)
	{
		AgentMBeanImpl mbean = WXSUtils.getAgentMBeanManager().getBean(sess.getObjectGrid().getName(), BigListPopAgent.class.getName());
		long startNS = System.nanoTime();
		Object rc = null;
		try
		{
			ObjectMap dirtyMap = null;
			// lock dirtymap first to avoid dead locks
			if(dirtyKey != null)
			{
				dirtyMap = sess.getMap(BigListPushAgent.getDirtySetMapNameForListMap(map.getName()));
				dirtyMap.getForUpdate(dirtyKey);
			}
			
			BigListHead<V> head = (BigListHead<V>)map.getForUpdate(key);
			if(head == null)
				rc = emptyMarker;
			else
			{
				// this updates the map head also
				rc = head.pop(sess, map, key, isLeft, dirtyKey);
			}
			mbean.getKeysMetric().logTime(System.nanoTime() - startNS);
		}
		catch(ObjectGridException e)
		{
			mbean.getKeysMetric().logException(e);
			e.printStackTrace();
			throw new ObjectGridRuntimeException(e);
		}
		return rc;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 8842082032401137638L;
	public Object process(Session sess, ObjectMap map, Object key) 
	{
		return pop(sess, map, key, isLeft, dirtyKey);
	}
	
	public Map processAllEntries(Session arg0, ObjectMap arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
