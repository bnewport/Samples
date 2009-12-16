package com.devwebsphere.rediswxs.data.set;

import java.io.Serializable;
import java.util.HashSet;


import com.devwebsphere.purequery.loader.BasePQLoader;
import com.devwebsphere.purequery.loader.GenericPQLoader;
import com.devwebsphere.rediswxs.data.list.ListItem;
import com.devwebsphere.rediswxs.data.list.ListItemKey;
import com.ibm.pdq.runtime.Data;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.projector.annotations.Id;


/**
 * This is persisted to the set head table. It keeps a long for the id to use for the next item to store in the set. This
 * is just a number to give each element within a set a unique key.
 * @author bnewport
 *
 */
public class SetHead implements Serializable, Cloneable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6155936206345700605L;
	@Id
	public String keyz;
	public long pos;

	@Override
	public Object clone() throws CloneNotSupportedException 
	{
		SetHead copy = new SetHead();
		copy.keyz = keyz;
		copy.pos = pos;
		return copy;
	}
	
	public SetHead()
	{
		
	}
	
	public SetHead(String key, long pos)
	{
		this.keyz = key;
		this.pos = pos;
	}

	public ListItem insertItem(Serializable value, HashSet<Serializable> s, ObjectMap headMap, ObjectMap itemMap)
		throws ObjectGridException
	{
		if(!s.contains(value))
		{
			s.add(value);
			++pos;
			ListItem item = new ListItem((String)keyz, pos, (Long)value);
			ListItemKey iKey = new ListItemKey((String)keyz, item.pos);
			itemMap.insert(iKey,item);
			return item;
		}
		else
			return null;
	}
	
	public HashSet<Serializable> fetchSetItems(Session sess)
		throws ObjectGridException
	{
		Data data = BasePQLoader.getData(sess.getTxID());
		GenericPQLoader itemLoader = (GenericPQLoader)sess.getObjectGrid().getMap("set-item-string-long").getLoader();
		String sql = "SELECT * FROM " + itemLoader.getTableName() + " WHERE KEYZ=:key";
		ListItem[] list = data.queryArray(sql, ListItem.class, this);
		HashSet<Serializable> value = new HashSet<Serializable>();
		for(ListItem item : list)
		{
			value.add(item.value);
		}
		return value;
	}
}
