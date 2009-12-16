package com.devwebsphere.wxs.asyncservice;

import java.io.Serializable;
import java.util.concurrent.Future;

import com.devwebsphere.wxs.asyncserviceimpl.AsyncServiceManagerImpl;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.plugins.BeanFactory;
import com.ibm.websphere.objectgrid.spring.ObjectGridSpringFactory;

public class KeySpecificJob<V> implements Job<V> 
{
	private static final long serialVersionUID = -3383985114712633447L;
	
	public Serializable key;
	public String mapName;
	public Job<V> job;

	public V process(Session localSession, String MsgId) 
	{
		// Get the WXS BeanFactory being used with this grid. This is usually
		// an adapter for a Spring BeanFactory
		BeanFactory bf = ObjectGridSpringFactory.getBeanFactoryForObjectGrid(localSession.getObjectGrid().getName());
		
		WXSSessionFactory sessionPool = (WXSSessionFactory)bf.getBean("wxsSessionPool");
		
		// Get the Session Pool singleton from Spring
		Session clientSession = sessionPool.getSessionFor(localSession.getObjectGrid().getName());
		
		try
		{
			AsyncServiceManagerImpl mgr = new AsyncServiceManagerImpl(clientSession.getObjectGrid());
			Future<V> fv = mgr.sendAsyncRoutedJob(mapName, key, job);
			return null;
		}
		finally
		{
			sessionPool.returnSessionTo(clientSession);
		}
	}
}
