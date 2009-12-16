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

import com.ibm.websphere.projector.annotations.Id;

/**
 * This is used for both set and list items. It's persisted to different tables depending on whether its a set or a list. In a set, the pos attribute is
 * just a unique number for this entry. In the list, it's a relative position.
 * @author bnewport
 *
 */
public class ListItem implements Serializable, Cloneable
{
	@Override
	public Object clone() throws CloneNotSupportedException 
	{
		ListItem copy = new ListItem();
		copy.keyz = keyz;
		copy.pos = pos;
		copy.value = value;
		return copy;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -7083371445216277155L;
	@Id
	public String keyz;
	@Id
	public long pos;
	public long value;
	
	public ListItem()
	{
	}
	
	public ListItem(String k, long p, Long v)
	{
		keyz = k;
		pos = p;
		value = v;
	}
	
	public String toString()
	{
		return "ListItem<" + keyz + ", " + Long.toString(pos) + ", " + value + ">";
	}
}
