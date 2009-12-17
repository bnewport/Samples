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


import com.devwebsphere.purequery.loader.BasePQLoader;
import com.devwebsphere.purequery.loader.GenericPQLoader;
import com.devwebsphere.rediswxs.data.list.ListItem;
import com.devwebsphere.rediswxs.data.list.ListItemKey;
import com.devwebsphere.rediswxs.data.set.SetHead;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanImpl;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanManager;
import com.ibm.pdq.runtime.Data;
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
		AgentMBeanImpl mbean = AgentMBeanManager.getBean(this.getClass().getName());
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
					Data data = BasePQLoader.getData(sess.getTxID());
					GenericPQLoader itemLoader = (GenericPQLoader)sess.getObjectGrid().getMap("set-item-string-long").getLoader();
					String sql = "SELECT * FROM " + itemLoader.getTableName() + " WHERE KEYZ=:keyz AND VALUE=:value";
					ListItem item = new ListItem((String)key, 0L, (Long)value);
					item = data.queryFirst(sql, ListItem.class, item);
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