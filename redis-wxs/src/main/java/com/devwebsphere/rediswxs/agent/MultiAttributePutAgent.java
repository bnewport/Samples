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
package com.devwebsphere.rediswxs.agent;

import java.util.List;
import java.util.Map;

import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanImpl;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;

/**
 * This is used to fetch all the values for a set of keys within a given partition using a single hop.
 */
public class MultiAttributePutAgent implements MapGridAgent 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6568906743945108310L;
	
	public List<String> batchStringKeys;
	public List<String> batchStringValues;
	public List<String> batchLongKeys;
	public List<Long> batchLongValues;
	public String stringMapName;
	public String longMapName;

	public Object process(Session sess, ObjectMap map, Object key) 
	{
		AgentMBeanImpl bean = WXSUtils.getAgentMBeanManager().getBean(sess.getObjectGrid().getName(), this.getClass().getName());
		long startNS = System.nanoTime();
		try
		{
			ObjectMap m_str = sess.getMap(stringMapName);
			for(int i = 0; i < batchStringKeys.size(); ++i)
			{
				if(m_str.getForUpdate(batchStringKeys.get(i)) != null)
					m_str.update(batchStringKeys.get(i), batchStringValues.get(i));
				else
					m_str.insert(batchStringKeys.get(i), batchStringValues.get(i));
			}
			ObjectMap m_long = sess.getMap(longMapName);
			for(int i = 0; i < batchLongKeys.size(); ++i)
			{
				if(m_long.getForUpdate(batchLongKeys.get(i)) != null)
					m_long.update(batchLongKeys.get(i), batchLongValues.get(i));
				else
					m_long.insert(batchLongKeys.get(i), batchLongValues.get(i));
			}
			bean.getKeysMetric().logTime(System.nanoTime() - startNS);
			return Boolean.TRUE;
		}
		catch(Exception e)
		{
			bean.getKeysMetric().logException(e);
			return Boolean.FALSE;
		}
	}

	public MultiAttributePutAgent()
	{
 	}

	public Map processAllEntries(Session arg0, ObjectMap arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
