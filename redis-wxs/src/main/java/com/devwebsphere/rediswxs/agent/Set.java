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
package com.devwebsphere.rediswxs.agent;

import java.io.Serializable;
import java.util.Map;

import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanImpl;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanManager;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;

/**
 * This implements a set verb. This will either update an existing entry or insert it.
 *
 */
public class Set implements MapGridAgent
{
	public Serializable value;

	/**
	 * 
	 */
	private static final long serialVersionUID = -3097068112344385146L;

	public Object process(Session sess, ObjectMap map, Object key) 
	{
		AgentMBeanImpl mbean = AgentMBeanManager.getBean(this.getClass().getName());
		long startNS = System.nanoTime();
		try
		{
			map.getForUpdate(key); // get a lock to make the following atomic
			if(map.containsKey(key))
			{
				map.update(key, value);
			}
			else
				map.insert(key, value);
		}
		catch(ObjectGridException e)
		{
			mbean.getKeysMetric().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
		mbean.getKeysMetric().logTime(System.nanoTime() - startNS);
		return null;
	}

	public Map processAllEntries(Session arg0, ObjectMap arg1) {
		// TODO Auto-generated method stub
		return null;
	}
}
