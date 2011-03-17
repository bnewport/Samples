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
package com.devwebsphere.wxsutils.wxsmap;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.WXSUtils;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;

/**
 * Each WXSUtils instance uses a ThreadLocal to store a dedicated
 * session for each thread.
 *
 */
public class ThreadLocalSession extends ThreadLocal<ThreadStuff>
{
	WXSUtils utils;
	static Logger logger = Logger.getLogger(ThreadLocalSession.class.getName());
	
	public ThreadLocalSession(WXSUtils utils)
	{
		this.utils = utils;
	}
	
	protected ThreadStuff initialValue()
	{
		try
		{
			ThreadStuff v = new ThreadStuff();
			v.session = utils.getObjectGrid().getSession();
			return v;
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Cannot get Session", e);
			throw new IllegalStateException("Cannot get session", e);
		}
	}
	
	public Session getSession()
	{
		return get().session;
	}

	public ObjectGrid getObjectGrid()
	{
		return get().session.getObjectGrid();
	}
	
	public ObjectMap getMap(String name)
	{
		try
		{
			return get().session.getMap(name);
		}
		catch(ObjectGridException e)
		{
			logger.log(Level.SEVERE, "Cannot get Map", e);
			throw new ObjectGridRuntimeException(e);
		}
	}
}
