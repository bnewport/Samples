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
package com.devwebsphere.wxs.asyncservice;

import java.util.concurrent.ConcurrentHashMap;

import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.Session;

public class WXSSessionFactory 
{
	ConcurrentHashMap<String, WXSSessionPool> grids;
	
	public WXSSessionFactory()
	{
		grids = new ConcurrentHashMap<String, WXSSessionPool>();
	}

	public void registerGrid(ObjectGrid g, int maxSessions)
		throws ObjectGridException
	{
		WXSSessionPool pool = new WXSSessionPool(g, maxSessions);
		grids.put(g.getName(), pool);
	}
	
	public Session getSessionFor(String gridName)
	{
		WXSSessionPool pool = grids.get(gridName);
		return pool.getSession();
	}
	
	public void returnSessionTo(Session s)
	{
		WXSSessionPool pool = grids.get(s.getObjectGrid().getName());
		pool.returnSession(s);
	}
}
