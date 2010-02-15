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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import com.devwebsphere.purequery.loader.ScalarKey;
import com.devwebsphere.rediswxs.data.list.ListHead;
import com.devwebsphere.rediswxs.data.list.ListItem;
import com.devwebsphere.rediswxs.data.list.ListItemKey;
import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanImpl;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;


public class LRange extends BaseAgent<Object> implements MapGridAgent 
{
	public long low, high;

	/**
	 * 
	 */
	private static final long serialVersionUID = -1357106004117434142L;

	public Object process(Session sess, ObjectMap headMap, Object key) 
	{
		if(key instanceof ScalarKey)
		{
			ScalarKey sk = (ScalarKey)key;
			key = sk.getKey();
		}
		AgentMBeanImpl mbean = WXSUtils.getAgentMBeanManager().getBean(sess.getObjectGrid(), this.getClass().getName());
		long startNS = System.nanoTime();
		try
		{
			ObjectMap itemMap = sess.getMap("list-item-string-long");
			ListHead head = (ListHead)headMap.getForUpdate(key);
			ArrayList<Serializable> rc = new ArrayList<Serializable>((int)(high - low + 1L));
			if(head != null)
			{
				
				if(low < 0L) low = 0L;
				if(low < head.size())
				{
					if(high >= head.size()) high = head.size() - 1;
					for(long i = low; i <= high; ++i)
					{
						ListItemKey itemKey = new ListItemKey((String)key, head.min + i);
						ListItem item = (ListItem)itemMap.get(itemKey);
						if(item != null)
							rc.add(new Long(item.value));
					}
				}
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
