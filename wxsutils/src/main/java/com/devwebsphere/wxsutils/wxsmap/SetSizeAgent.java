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
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;

public class SetSizeAgent<V extends Serializable> implements MapGridAgent 
{
	static Logger logger = Logger.getLogger(SetSizeAgent.class.getName());
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3736978703392897531L;
	/**
	 * 
	 */
	
	static public <V extends Serializable> int size(Session sess, ObjectMap map, Object key)
	{
		AgentMBeanImpl mbean = WXSUtils.getAgentMBeanManager().getBean(sess.getObjectGrid().getName(), SetSizeAgent.class.getName());
		long startNS = System.nanoTime();
		int rc = 0;
		try
		{
 			map.get(key);
			for(int  b = 0; b < SetAddRemoveAgent.NUM_BUCKETS; ++b)
			{
				LinkedHashSet<SetElement<V>> d = (LinkedHashSet<SetElement<V>>)map.get(SetAddRemoveAgent.getBucketKeyForBucket(key, b));
				if(d != null)
				{
					rc += d.size();
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
		return rc;
	}
	
	public Object process(Session sess, ObjectMap map, Object key) 
	{
		return new Integer(size(sess, map, key));
	}
	
	public Map processAllEntries(Session arg0, ObjectMap arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
