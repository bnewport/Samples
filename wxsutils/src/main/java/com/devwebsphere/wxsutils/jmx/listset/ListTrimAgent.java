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
package com.devwebsphere.wxsutils.jmx.listset;

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


public class ListTrimAgent<V extends Serializable> implements MapGridAgent 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -984733774220708513L;
	public int newSize;
	public Object process(Session sess, ObjectMap map, Object key) 
	{
		AgentMBeanImpl mbean = WXSUtils.getAgentMBeanManager().getBean(sess.getObjectGrid().getName(), this.getClass().getName());
		long startNS = System.nanoTime();
		Boolean rc = Boolean.FALSE;
		try
		{
			ArrayList<V> list = (ArrayList<V>)map.getForUpdate(key);
			if(list != null)
			{
				ArrayList<V> copy = new ArrayList<V>(newSize);
				for(int i = 0; i < Math.min(newSize, list.size()); ++i)
				{
					copy.add(list.get(i));
				}
				map.update(key, copy);
				rc = Boolean.TRUE;
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
	
	public Map processAllEntries(Session arg0, ObjectMap arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
