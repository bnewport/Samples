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
import com.devwebsphere.wxsutils.WXSMapOfSets.Contains;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanImpl;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;


public class SetIsMemberAgent<V extends Serializable> implements MapGridAgent 
{
	static Logger logger = Logger.getLogger(SetIsMemberAgent.class.getName());
	
	public Contains op;
	public V[] values;
	/**
	 * 
	 */
	private static final long serialVersionUID = 8842082032401137638L;
	public Object process(Session sess, ObjectMap map, Object key) 
	{
		AgentMBeanImpl mbean = WXSUtils.getAgentMBeanManager().getBean(sess.getObjectGrid().getName(), this.getClass().getName());
		long startNS = System.nanoTime();
		boolean rc = false;
		boolean allFirst = true;
		try
		{
			map.get(key); // token lock to prevent deadlocks
			for(V v : values)
			{
				SetElement<V> wrapper = new SetElement<V>(v);
				String bucketKey = SetAddRemoveAgent.getBucketKeyForValue(key, v);
				LinkedHashSet<SetElement<V>> s = (LinkedHashSet<SetElement<V>>)map.get(bucketKey);
				if(s != null)
				{
					if(op == Contains.ALL)
					{
						if(!s.contains(wrapper))
						{
							rc = false;
							break;
						}
						else if(allFirst)
						{
							rc = true;
							allFirst = false;
						}
					}
					else
					{
						if(s.contains(wrapper))
						{
							rc = true;
							break;
						}
					}
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
	public Map processAllEntries(Session arg0, ObjectMap arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
