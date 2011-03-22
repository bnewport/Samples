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
import com.devwebsphere.wxsutils.filter.Filter;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanImpl;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;

public class SetGetAgent<V extends Serializable> implements MapGridAgent 
{
	Filter filter;
	/**
	 * 
	 */
	private static final long serialVersionUID = -3736978703392897531L;
	/**
	 * 
	 */
	
	static public <V extends Serializable> Set<V> get(Session sess, ObjectMap map, Object key, Filter filter)
	{
		AgentMBeanImpl mbean = WXSUtils.getAgentMBeanManager().getBean(sess.getObjectGrid().getName(), SetGetAgent.class.getName());
		long startNS = System.nanoTime();
		Set<V> rc = new HashSet<V>();
		try
		{
			map.get(key);
			for(int  b = 0; b < SetAddRemoveAgent.NUM_BUCKETS; ++b)
			{
				Set<V> d = (Set<V>)map.get(SetAddRemoveAgent.getBucketKeyForBucket(key, b));
				if(d != null)
				{
					if(filter == null)
						rc.addAll(d);
					else
					{
						for(V v : d)
						{
							if(filter.filter(v))
								rc.add(v);
						}
					}
				}
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
	
	public Object process(Session sess, ObjectMap map, Object key) 
	{
		return get(sess, map, key, filter);
	}
	
	public Map processAllEntries(Session arg0, ObjectMap arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
