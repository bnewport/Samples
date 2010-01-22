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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import com.devwebsphere.purequery.loader.ScalarKey;
import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanImpl;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;

public class SetMembers extends BaseAgent<String> implements MapGridAgent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1981074932433373689L;

	public Object process(Session sess, ObjectMap map, Object key) 
	{
		if(key instanceof ScalarKey)
		{
			ScalarKey sk = (ScalarKey)key;
			key = sk.getKey();
		}
		AgentMBeanImpl mbean = WXSUtils.getAgentMBeanManager().getBean(this.getClass().getName());
		long startNS = System.nanoTime();
		try
		{
			map.getForUpdate(key); // get a lock to make the following atomic
			ArrayList<Serializable> rc = new ArrayList<Serializable>();
			HashSet<Serializable> v = null;
			v = fetchSetEntry(sess, (String)key, map);
			if(v != null)
			{
				rc = new ArrayList<Serializable>(v);
			}
			mbean.getKeysMetric().logTime(System.nanoTime() - startNS);
			return rc;
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
