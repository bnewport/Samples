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
package com.devwebsphere.rediswxs.agent.list;

import java.util.Collections;
import java.util.Map;

import com.devwebsphere.purequery.loader.ScalarKey;
import com.devwebsphere.rediswxs.data.list.ListHead;
import com.devwebsphere.rediswxs.data.list.ListItemKey;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanImpl;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanManager;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;


public class LTrim extends BaseAgent<Object> implements MapGridAgent 
{
	public int size;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4867167858846237996L;

	public Object process(Session sess, ObjectMap headMap, Object key) 
	{
		if(key instanceof ScalarKey)
		{
			ScalarKey sk = (ScalarKey)key;
			key = sk.getKey();
		}
		AgentMBeanImpl mbean = AgentMBeanManager.getBean(this.getClass().getName());
		long startNS = System.nanoTime();
		try
		{
			ObjectMap itemMap = sess.getMap("list-item-string-long");
			ListHead head = (ListHead)headMap.getForUpdate(key);
			if(head != null)
			{
				while(head.size() > size)
				{
					ListItemKey itemKey = new ListItemKey((String)key, head.max);
					head.max--;
					itemMap.removeAll(Collections.singletonList(itemKey));
				}
				if(head.min > head.max)
				{
					headMap.remove(key);
				}
				else
				{
					headMap.update(key, head);
				}
				mbean.getKeysMetric().logTime(System.nanoTime() - startNS);
				return Boolean.TRUE;
			}
			else
			{
				mbean.getKeysMetric().logTime(System.nanoTime() - startNS);
				return Boolean.FALSE;
			}
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
