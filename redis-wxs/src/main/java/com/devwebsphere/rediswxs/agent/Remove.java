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

import java.util.Map;

import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanImpl;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;

/**
 * This removes an entry if it exists.
 *
 */
public class Remove implements MapGridAgent 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4291305437621932998L;

	public Object process(Session sess, ObjectMap map, Object key) 
	{
		AgentMBeanImpl mbean = WXSUtils.getAgentMBeanManager().getBean(sess.getObjectGrid().getName(), this.getClass().getName());
		long startNS = System.nanoTime();
		try
		{
			boolean rc = false;
			if(map.containsKey(key))
			{
				map.remove(key);
				rc = true;
			}
			mbean.getKeysMetric().logTime(System.nanoTime() - startNS);
			return new Boolean(rc);
		}
		catch(ObjectGridException e)
		{
			mbean.getKeysMetric().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	public Map processAllEntries(Session arg0, ObjectMap arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
