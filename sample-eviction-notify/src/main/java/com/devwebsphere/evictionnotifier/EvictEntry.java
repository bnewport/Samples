//
//This sample program is provided AS IS and may be used, executed, copied and
//modified without royalty payment by customer (a) for its own instruction and 
//study, (b) in order to develop applications designed to run with an IBM 
//WebSphere product, either for customer's own internal use or for redistribution 
//by customer, as part of such an application, in customer's own products. "
//
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2005
//All Rights Reserved * Licensed Materials - Property of IBM
//
package com.devwebsphere.evictionnotifier;

import java.io.Serializable;

/**
 * This is the value stored in an evict q map. The key for these
 * entries will be unique strings.
 *
 */
public class EvictEntry implements Serializable, Cloneable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7017864066295411660L;

	/**
	 * The map this entry was evicted from
	 */
	String mapName;
	/**
	 * The key of the evicted entry
	 */
	Serializable key;
	/**
	 * The value of the evicted entry
	 */
	Serializable value;
	
	/**
	 * Performance aid, avoids the need for Serialization to copy the object.
	 * Recommended
	 */
	public Object clone()
	{
		EvictEntry copy = new EvictEntry();
		copy.key = key;
		copy.value = value;
		copy.mapName = mapName;
		return copy;
	}

	public final String getMapName() {
		return mapName;
	}

	public final Serializable getKey() {
		return key;
	}

	public final Serializable getValue() {
		return value;
	}

	public String toString()
	{
		String valStr = (value != null) ? value.toString() : "";
		return "EvictEntry<" + key.toString() + ", " + valStr + ", " + mapName + ">";
	}
}
