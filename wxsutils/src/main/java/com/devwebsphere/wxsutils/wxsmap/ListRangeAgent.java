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

import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanImpl;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;

@Deprecated
public class ListRangeAgent<V extends Serializable> implements MapGridAgent 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3820884829092397741L;
	public int low;
	public int high;
	
	static public <V extends Serializable> ArrayList<V> range(Session sess, ObjectMap map, Object key, int low, int high)
	{
		AgentMBeanImpl mbean = WXSUtils.getAgentMBeanManager().getBean(sess.getObjectGrid().getName(), ListRangeAgent.class.getName());
		long startNS = System.nanoTime();
		try
		{
			ArrayList<V> list = (ArrayList<V>)map.get(key);
			if(list != null)
			{
				list = new ArrayList<V>(list.subList(low, high + 1));
			}
			else
				list = new ArrayList<V>();
			mbean.getKeysMetric().logTime(System.nanoTime() - startNS);
			return list;
		}
		catch(ObjectGridException e)
		{
			mbean.getKeysMetric().logException(e);
			e.printStackTrace();
			throw new ObjectGridRuntimeException(e);
		}
	}
	/**
	 * 
	 */
	public Object process(Session sess, ObjectMap map, Object key) 
	{
		return range(sess, map, key, low, high);
	}
	
	public Map processAllEntries(Session arg0, ObjectMap arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
