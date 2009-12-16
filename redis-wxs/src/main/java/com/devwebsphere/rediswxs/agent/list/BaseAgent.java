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

import com.devwebsphere.rediswxs.data.list.ListHead;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;


public class BaseAgent<K> 
{
	protected ArrayList<Serializable> fetchListEntry(Session sess, K key, ObjectMap map)
	{
		try
		{
			ObjectMap headMap = sess.getMap("list-head-string-long");
			ArrayList<Serializable> s = null;
			s = (ArrayList<Serializable>)map.getForUpdate(key);
			if(s == null)
			{
				ListHead head = (ListHead)headMap.get(key);
				if(head != null)
				{
					s = head.fetchListItems(sess);
					map.insert(key, s);
				}
			}
			return s;
		}
		catch(ObjectGridException e)
		{
			throw new ObjectGridRuntimeException(e);
		}
	}
}
