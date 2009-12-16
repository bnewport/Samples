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
package com.devwebsphere.rediswxs.agent.set;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Map;

import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanImpl;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanManager;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;

public class SetCard extends BaseAgent<String> implements MapGridAgent 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4867167858846237996L;

	public Object process(Session sess, ObjectMap map, Object key) 
	{
		AgentMBeanImpl mbean = AgentMBeanManager.getBean(this.getClass().getName());
		long startNS = System.nanoTime();
		HashSet<Serializable> s = fetchSetEntry(sess, (String)key, map);
		if(s != null)
		{
			mbean.getKeysMetric().logTime(System.nanoTime() - startNS);
			return new Integer(s.size());
		}
		else
		{
			mbean.getKeysMetric().logTime(System.nanoTime() - startNS);
			return new Integer(0);
		}
	}

	public Map processAllEntries(Session arg0, ObjectMap arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
