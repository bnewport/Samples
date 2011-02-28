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
import java.util.HashSet;
import java.util.Map;

import com.devwebsphere.purequery.loader.ScalarKey;
import com.devwebsphere.rediswxs.data.set.SetHead;
import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanImpl;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;


public class SetAdd implements MapGridAgent 
{
	public Serializable value;

	/**
	 * 
	 */
	private static final long serialVersionUID = 6253283139729226636L;

	public Object process(Session sess, ObjectMap map, Object key) {
		if(key instanceof ScalarKey)
		{
			ScalarKey sk = (ScalarKey)key;
			key = sk.getKey();
		}
		AgentMBeanImpl mbean = WXSUtils.getAgentMBeanManager().getBean(sess.getObjectGrid().getName(), this.getClass().getName());
		long startNS = System.nanoTime();
		try
		{
			Boolean rc = Boolean.FALSE;
			SetHead head = null;
			map.getForUpdate(key); // get a lock to make the following atomic
			ObjectMap headMap = sess.getMap("set-head-string-long");
			ObjectMap itemMap = sess.getMap("set-item-string-long");
			if(map.containsKey(key))
			{
				// list is already in the proxy map
				HashSet<Serializable> s = (HashSet<Serializable>) map.get(key);
				head = (SetHead)headMap.get(key);
				if(head.insertItem(value, s, headMap, itemMap) != null)
				{
					map.update(key, s);
					rc = Boolean.TRUE;
				}
				headMap.update(key, head);
			}
			else
			{
				head = (SetHead)headMap.get(key);
				HashSet<Serializable> s = null;
				if(head == null)
				{
					// list doesn't exist on disk
					s = new HashSet<Serializable>();
					head = new SetHead((String)key, Long.MIN_VALUE);
					headMap.insert(key, head);
					rc = Boolean.TRUE;
				}
				else
				{
					// list is on the disk, warm the proxy list
					s = head.fetchSetItems(sess);
				}
				if(head.insertItem(value, s, headMap, itemMap) != null)
				{
					rc = Boolean.TRUE;
				}
				map.insert(key, s);
				headMap.update(key, head);
			}
			mbean.getKeysMetric().logTime(System.nanoTime() - startNS);
			return new Boolean(rc);
		}
		catch(ObjectGridException e)
		{
			mbean.getKeysMetric().logException(e);
			e.printStackTrace();
			throw new ObjectGridRuntimeException(e);
		}
	}

	public Map processAllEntries(Session arg0, ObjectMap arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
