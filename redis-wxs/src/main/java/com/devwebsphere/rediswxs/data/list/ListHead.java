package com.devwebsphere.rediswxs.data.list;

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

import java.io.Serializable;
import java.util.ArrayList;


import com.devwebsphere.purequery.loader.BasePQLoader;
import com.devwebsphere.purequery.loader.GenericPQLoader;
import com.ibm.pdq.runtime.Data;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.projector.annotations.Id;


/**
 * This is a POJO persisted using the purequery loader. It is used for the list head record
 * for a list. It works in conjunction with the ListItem POJO. There is a WXS map for ListHead
 * and another for ListItem. These Maps have a Loader to persist and fetch records from the
 * database. The agents for the list verbs use these maps to persist the lists of values to
 * the database. The ListHead has the key, and a min and max. The min and max are keys for the
 * first record and the last record in the ListItem table. Pushing something on the left
 * creates a record with --min and pushing it on the right creates a record with ++max.
 * Popping deletes the corresponding ListItem records with the key of either min or max.
 * The items in a list are numbered with no gaps from head.min to head.max
 * @author bnewport
 *
 */
public class ListHead implements Serializable, Cloneable
{
	@Override
	public Object clone() throws CloneNotSupportedException 
	{
		ListHead copy = new ListHead(keyz, listid, min, max);
		return copy;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 2775504357920936856L;
	/**
	 * This is the key for this list
	 */
	@Id
	public String keyz;
	/**
	 * This is unused right now
	 */
	public long listid;
	/**
	 * This is the pos value for the left most ListItem
	 */
	public long min;
	/**
	 * This is the pos value for the right most ListItem
	 */
	public long max;
	
	public ListHead()
	{
		
	}
	
	public ListHead(String k, long lid, long mn, long mx)
	{
		keyz = k;
		listid = lid;
		min = mn;
		max = mx;
	}

	/**
	 * This inserts a new list item on the left or right of the list. The caller MUST either insert or update the ListHead (this)
	 * after this method returns to ensure the current fields in this object are written to the database.
	 * @param isLeft True if inserting on the left
	 * @param value The value to insert
	 * @param s The array is existing values. This is changed after this method completes
	 * @param headMap The ObjectMap for the ListHeads
	 * @param itemMap The ObjectMap for the ListItems
	 * @return The newly created ListItem if needed
	 * @throws ObjectGridException
	 */
	public ListItem insertItem(boolean isLeft, Serializable value, ObjectMap headMap, ObjectMap itemMap)
		throws ObjectGridException
	{
		ListItem item = null;
		if(!isLeft)
		{
			if(min == -1 && max == -1)
			{
				min = 0;
				max = 0;
			}
			else
			{
				max++;
			}
			item = new ListItem((String)keyz, max, (Long)value);
			ListItemKey iKey = new ListItemKey((String)keyz, max);
			itemMap.insert(iKey,item);
		}
		else
		{
			if(min == -1 && max == -1)
			{
				min = 0;
				max = 0;
			}
			else
			{
				min--;
			}
			item = new ListItem((String)keyz, min, (Long)value);
			ListItemKey iKey = new ListItemKey((String)keyz, min);
			itemMap.insert(iKey,item); // this triggers a database insert either on commit or later with write behind
		}
		return item;
	}
	
	/**
	 * This prefetches the list items for a specific list
	 * @param sess The session to use
	 * @return The fetched list
	 * @throws ObjectGridException
	 */
	public ArrayList<Serializable> fetchListItems(Session sess)
		throws ObjectGridException
	{
		Data data = BasePQLoader.getData(sess.getTxID());
		GenericPQLoader itemLoader = (GenericPQLoader)sess.getObjectGrid().getMap("list-item-string-long").getLoader();
		String sql = "SELECT * FROM " + itemLoader.getTableName() + " WHERE KEY=:key ORDER BY POS";
		ListItem[] list = data.queryArray(sql, ListItem.class, this);
		ArrayList<Serializable> value = new ArrayList<Serializable>();
		for(ListItem item : list)
		{
			value.add(item.value);
		}
		return value;
	}
	
	public long size()
	{
		return max - min + 1;
	}
	
	public String toString()
	{
		return "ListHead<" + keyz + ", " + Long.toString(listid) + ", " + Long.toString(min) + ", " + Long.toString(max) + ">";
	}
}
