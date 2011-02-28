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
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import com.devwebsphere.purequery.loader.ScalarKey;
import com.devwebsphere.rediswxs.data.list.ListItem;
import com.devwebsphere.rediswxs.data.list.ListItemKey;
import com.devwebsphere.rediswxs.data.set.SetHead;
import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanImpl;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;


public class SRemove extends BaseAgent<Object> implements MapGridAgent 
{
	public Serializable value;
	/**
	 * 
	 */
	private static final long serialVersionUID = 8842082032401137638L;
	public Object process(Session sess, ObjectMap map, Object key) 
	{
		if(key instanceof ScalarKey)
		{
			ScalarKey sk = (ScalarKey)key;
			key = sk.getKey();
		}
		AgentMBeanImpl mbean = WXSUtils.getAgentMBeanManager().getBean(sess.getObjectGrid().getName(), this.getClass().getName());
		long startNS = System.nanoTime();
		try
		{
			ObjectMap headMap = sess.getMap("set-head-string-long");
			ObjectMap itemMap = sess.getMap("set-item-string-long");
			HashSet<Serializable> s = fetchSetEntry(sess, (String)key, map);
			if(s != null)
			{
				SetHead head = (SetHead)headMap.get(key);
				if(s.contains(value))
				{
					s.remove(value);
					
					// first the pos value for this set element
					SetLoaderOperations itemLoader = (SetLoaderOperations)sess.getObjectGrid().getMap("set-item-string-long").getLoader();
					ListItem item = itemLoader.getSetRecord(sess, (String)key, (Long)value);
					if(item != null)
					{
						// remove it from backing table
						ListItemKey itemKey = new ListItemKey((String)key, item.pos);
						itemMap.removeAll(Collections.singletonList(itemKey));
					}
					map.update(key, s);
					headMap.update(key, head);
					mbean.getKeysMetric().logTime(System.nanoTime() - startNS);
					return Boolean.TRUE;
				}
			}
			mbean.getKeysMetric().logTime(System.nanoTime() - startNS);
			return Boolean.FALSE;
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
