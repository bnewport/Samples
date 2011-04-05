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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanImpl;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;

public class BigListIsEmptyAgent<V extends Serializable> implements MapGridAgent 
{
	static Logger logger = Logger.getLogger(BigListIsEmptyAgent.class.getName());
	/**
	 * 
	 */
	private static final long serialVersionUID = -3736978703392897531L;
	
	static public <V extends Serializable> boolean isEmpty(Session sess, ObjectMap map, Object key)
	{
		AgentMBeanImpl mbean = WXSUtils.getAgentMBeanManager().getBean(sess.getObjectGrid().getName(), BigListIsEmptyAgent.class.getName());
		long startNS = System.nanoTime();
		boolean rc = false;
		try
		{
			BigListHead<V> head = (BigListHead<V>)map.get(key);
			rc = (head == null);
			mbean.getKeysMetric().logTime(System.nanoTime() - startNS);
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception", e);
			mbean.getKeysMetric().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
		return rc;
	}
	/**
	 * 
	 */
	public Object process(Session sess, ObjectMap map, Object key) 
	{
		return new Boolean(isEmpty(sess, map, key));
	}
	
	public Map processAllEntries(Session arg0, ObjectMap arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
