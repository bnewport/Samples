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
package com.devwebsphere.wxsutils.jmx.loader;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import com.devwebsphere.wxsutils.jmx.SummaryMBean;
import com.devwebsphere.wxsutils.jmx.SummaryMBeanImpl;
import com.devwebsphere.wxsutils.jmx.SummaryMBeanSource;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;

public final class LoaderMBeanManager 
{
	static public ConcurrentMap<String, LoaderMBeanImpl> beans = new ConcurrentHashMap<String, LoaderMBeanImpl>();

	final static public String UNKNOWN_MAP = "_UNDEFINED";
	
	static volatile MBeanServer mbeanServer;
	
	static ObjectName makeObjectName(String grid, String map)
		throws MalformedObjectNameException
	{
		Hashtable<String, String> props = new Hashtable<String, String>();
		props.put("grid", grid);
		props.put("map", map);
		props.put("type", "Loader");
		return new ObjectName("com.ibm.websphere.objectgrid", props);
	}

	static public List<String> getCurrentBeanNames()
	{
		return new ArrayList<String>(beans.keySet());
	}
	
	static public MBeanServer getServer()
	{
		if(mbeanServer == null)
		{
			ArrayList<MBeanServer> mBeanServers = MBeanServerFactory.findMBeanServer(null);
	        mbeanServer = (MBeanServer) mBeanServers.get(0);
	        try
	        {
	        	SummaryMBeanSource<LoaderMBeanImpl> beanSource = new SummaryMBeanSource<LoaderMBeanImpl>() 
	        	{
	        		public LoaderMBeanImpl getBean(String mapName)
	        		{
	        			return LoaderMBeanManager.getBean(mapName);
	        		}
	        		public List<String> getCurrentBeanNames()
	        		{
	        			return LoaderMBeanManager.getCurrentBeanNames();
	        		}
				};
		        SummaryMBeanImpl<LoaderMBeanImpl> mb = new SummaryMBeanImpl<LoaderMBeanImpl>(beanSource, LoaderMBeanImpl.class, "MapName", "Loader");
		        
				Hashtable<String, String> props = new Hashtable<String, String>();
				props.put("grid", "Grid");
				props.put("type", "LoaderSummary");
				ObjectName on = new ObjectName("com.ibm.websphere.objectgrid", props);
				StandardMBean realMBean = new StandardMBean(mb, SummaryMBean.class);
				mbeanServer.registerMBean(realMBean, on);
	        }
	        catch(Exception e)
	        {
	        	System.out.println(e.toString());
	        	e.printStackTrace();
	        }
		}
		return mbeanServer;
	}
	
	static public LoaderMBeanImpl getBean(String mapName)
	{
		LoaderMBeanImpl bean = beans.get(mapName);
		if(bean == null)
		{
			synchronized(LoaderMBeanManager.class)
			{
				bean = beans.get(mapName);
				if(bean == null)
				{
					bean = new LoaderMBeanImpl(mapName);
					try
					{
						MBeanServer server = getServer();
						if(server != null && !mapName.equals(UNKNOWN_MAP));
						{
							StandardMBean mbean = new StandardMBean(bean, LoaderMBean.class);
							MBeanInfo info = mbean.getMBeanInfo();
							ObjectName on = makeObjectName("Grid", mapName);
							server.registerMBean(mbean, on);
						}
					}
					catch(Exception e)
					{
						throw new ObjectGridRuntimeException(e);
					}
					beans.put(mapName, bean);
				}
			}
		}
		return bean;
	}
}
