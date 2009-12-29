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

import com.devwebsphere.purequery.loader.BasePQLoader;
import com.devwebsphere.purequery.loader.GenericPQLoader;
import com.devwebsphere.rediswxs.data.list.ListItem;
import com.devwebsphere.rediswxs.data.set.SetHead;
import com.ibm.pdq.runtime.Data;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.Session;

/**
 * This extends the GenericPQLoader and adds two helper methods
 * used by the set server code
 * 
 * @see SetHead#fetchSetItems(Session)
 * @see SRemove#process(Session, com.ibm.websphere.objectgrid.ObjectMap, Object)
 *
 */
public class SetItemPureQueryLoader extends GenericPQLoader implements SetLoaderOperations 
{
	public ListItem getSetRecord(Session sess, String key, Long value)
	{
		try
		{
			// first the pos value for this set element
			Data data = BasePQLoader.getData(sess.getTxID());
			String sql = "SELECT * FROM " + getTableName() + " WHERE KEYZ=:keyz AND VALUE=:value";
			ListItem item = new ListItem(key, 0L, value);
			item = data.queryFirst(sql, ListItem.class, item);
			return item;
		}
		catch(Exception e)
		{
			throw new ObjectGridRuntimeException(e);
		}
	}

	public HashSet<Serializable> getAllMembers(Session sess, String key) 
	{
		try
		{
			Data data = BasePQLoader.getData(sess.getTxID());
			String sql = "SELECT * FROM " + getTableName() + " WHERE KEYZ=:keyz";
			ListItem itemKey = new ListItem();
			itemKey.keyz = key;
			ListItem[] list = data.queryArray(sql, ListItem.class, itemKey);
			HashSet<Serializable> value = new HashSet<Serializable>();
			for(ListItem item : list)
			{
				value.add(item.value);
			}
			return value;
		}
		catch(Exception e)
		{
			throw new ObjectGridRuntimeException(e);
		}
	}

}
