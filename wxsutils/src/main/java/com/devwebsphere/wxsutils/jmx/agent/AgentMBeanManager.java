package com.devwebsphere.wxsutils.jmx.agent;

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

/**
 * This tracks all AgentMBeans within a JMX and creates the SummaryMBean for those agents.
 *
 */
public final class AgentMBeanManager
{
	static public ConcurrentMap<String, AgentMBeanImpl> beans = new ConcurrentHashMap<String, AgentMBeanImpl>();

	static volatile MBeanServer mbeanServer;
	
	static ObjectName makeObjectName(String grid, String agentClass)
		throws MalformedObjectNameException
	{
		Hashtable<String, String> props = new Hashtable<String, String>();
		props.put("grid", grid);
		props.put("agentClass", agentClass);
		props.put("type", "Agent");
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
	        	SummaryMBeanSource<AgentMBeanImpl> beanSource = new SummaryMBeanSource<AgentMBeanImpl>() 
	        	{
	        		public AgentMBeanImpl getBean(String mapName)
	        		{
	        			return AgentMBeanManager.getBean(mapName);
	        		}
	        		public List<String> getCurrentBeanNames()
	        		{
	        			return AgentMBeanManager.getCurrentBeanNames();
	        		}
				};
		        SummaryMBeanImpl<AgentMBeanImpl> mb = new SummaryMBeanImpl<AgentMBeanImpl>(beanSource, AgentMBeanImpl.class, "AgentClass", "Agent");
		        
				Hashtable<String, String> props = new Hashtable<String, String>();
				props.put("grid", "Grid");
				props.put("type", "AgentSummary");
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
	
	static public AgentMBeanImpl getBean(String agentClass)
	{
		AgentMBeanImpl bean = beans.get(agentClass);
		if(bean == null)
		{
			synchronized(AgentMBeanManager.class)
			{
				bean = beans.get(agentClass);
				if(bean == null)
				{
					bean = new AgentMBeanImpl();
					try
					{
						MBeanServer server = getServer();
						if(server != null);
						{
							StandardMBean mbean = new StandardMBean(bean, AgentMBean.class);
							MBeanInfo info = mbean.getMBeanInfo();
							ObjectName on = makeObjectName("Grid", agentClass);
							server.registerMBean(mbean, on);
						}
					}
					catch(Exception e)
					{
						throw new ObjectGridRuntimeException(e);
					}
					beans.put(agentClass, bean);
				}
			}
		}
		return bean;
	}
}
