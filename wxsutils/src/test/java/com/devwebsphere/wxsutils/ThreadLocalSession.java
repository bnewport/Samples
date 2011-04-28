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
package com.devwebsphere.wxsutils;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.Session;

/**
 * Each WXSUtils instance uses a ThreadLocal to store a dedicated
 * session for each thread.
 *
 */
public class ThreadLocalSession extends ThreadLocal<Session>
{
	ObjectGrid grid;
	static Logger logger = Logger.getLogger(ThreadLocalSession.class.getName());
	
	public ThreadLocalSession(ObjectGrid grid)
	{
		this.grid = grid;
	}
	
	protected Session initialValue()
	{
		try
		{
			return grid.getSession();
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Cannot get Session", e);
			throw new IllegalStateException("Cannot get session", e);
		}
	}
	
	public ObjectGrid getObjectGrid()
	{
		return grid;
	}
	
	static void v()
	{
		ObjectGrid grid = null;
		ThreadLocalSession tls = new ThreadLocalSession(grid);
		
		
		Session sess = tls.get();
	}
	
}
