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
package com.devwebsphere.manualpartition.data.keyutils;

import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;

/**
 * This is a ThreadLocal to store an ObjectMap on each Thread. This is for performance
 * reasons. This is faster than getting a per thread Session and then obtaining
 * an ObjectMap every time. It's probably overkill but it's worth doing
 * just as a sample
 * @author bnewport
 *
 */
public class ThreadLocalObjectMap extends ThreadLocal<ObjectMap> 
{
	/**
	 * The ObjectMap to use.
	 */
	String mapName;
	
	/**
	 * This is a reference to the ObjectGrid client reference.
	 */
	static ObjectGrid client;
	
	/**
	 * This MUST be called by the client main once it obtains an ObjectGrid client
	 * reference.
	 * @param c
	 */
	static public void setObjectGridClient(ObjectGrid c) { client = c; }

	/**
	 * Constructor, the map Name must be provided
	 * @param mn
	 */
	public ThreadLocalObjectMap(String mn) { mapName = mn; }

	/**
	 * This is called the first time the get method is called on a thread. It creates
	 * a Session for this Thread and then returns an ObjectMap using that Session. This
	 * ensures the ObjectMap is PER THREAD. Session and ObjectMap instances cannot be
	 * shared across threads.
	 */
	protected ObjectMap initialValue()
	{
		try
		{
			return client.getSession().getMap(mapName);
		}
		catch(ObjectGridException e)
		{
			throw new ObjectGridRuntimeException(e);
		}
	}
}