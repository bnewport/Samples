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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanImpl;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;


public class SetAddFlushAgent<V extends Serializable> implements MapGridAgent 
{
	public int maxSize;
	public V[] values;
	/**
	 * 
	 */
	private static final long serialVersionUID = 8842082032401137638L;
	public Object process(Session sess, ObjectMap map, Object key) 
	{
		AgentMBeanImpl mbean = WXSUtils.getAgentMBeanManager().getBean(sess.getObjectGrid().getName(), this.getClass().getName());
		long startNS = System.nanoTime();
		Set<V> rc = null;
		try
		{
			Set<V> s = (Set<V>)map.getForUpdate(key);
			if(s != null)
			{
				if(s.size() <= maxSize)
				{
					for(V v : values)
						s.add(v);
					rc = new HashSet<V>();
				}
				else
				{
					rc = s;
					s = new HashSet<V>();
					for(V v : values)
						s.add(v);
				}
				map.update(key, s);
			}
			else
			{
				s = new HashSet<V>();
				for(V v : values)
					s.add(v);
				map.insert(key, s);
				rc = new HashSet<V>();
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
