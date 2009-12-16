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
