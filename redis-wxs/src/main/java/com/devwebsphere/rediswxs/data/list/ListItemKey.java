package com.devwebsphere.rediswxs.data.list;

import java.io.Serializable;

import com.ibm.websphere.projector.annotations.Id;

/**
 * This is the key for the ListItem maps.
 * @author bnewport
 *
 */
public class ListItemKey implements Serializable, Cloneable
{
	
	public Object clone()
	{
		ListItemKey copy = new ListItemKey(this.keyz, this.pos);
		return copy;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -4326494340167094518L;

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof ListItemKey == false)
			return false;
		ListItemKey other = (ListItemKey)obj;
		return keyz.equals(other.keyz) && pos == other.pos;
	}

	@Override
	public int hashCode() {
		return keyz.hashCode() ^ new Long(pos).hashCode();
	}

	/**
	 * The name of the list that owns this item
	 */
	@Id
	public String keyz;
	/**
	 * The position in the list of this item, lower numbers means 'left', higher means 'right'
	 */
	public long pos;

	public ListItemKey()
	{
	}
	
	public ListItemKey(String key, long pos)
	{
		this.keyz = key;
		this.pos = pos;
	}

	public String toString()
	{
		return "ListItemKey<" + keyz + ", " + Long.toString(pos) + ">";
	}
}
