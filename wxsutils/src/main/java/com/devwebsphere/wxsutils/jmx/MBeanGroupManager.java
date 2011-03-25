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
package com.devwebsphere.wxsutils.jmx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;

/**
 * This is an abstract base class to create a SummaryMBean for a collection of homogenous MBeans. It has
 * two methods to retrieve a TabularData of EVERY attribute of all the mbeans as well as one with only
 * the attributes for an external monitor. Typically, the monitor only attributes exclude things like
 * calculated statistics which a 'real' JMX monitor will calculate it self.
 *
 * @param <M> The MBean Impl class
 */
public abstract class MBeanGroupManager <M>
{
	public ConcurrentMap<String, M> beans = new ConcurrentHashMap<String, M>();
	static Logger logger = Logger.getLogger(MBeanGroupManager.class.getName());

	final static public String UNKNOWN_MAP = "_UNDEFINED";
	
	/**
	 * The MBeanImpl class to use
	 */
	Class<M> mbeanClass;
	/**
	 * The JMX MBean interface to use, M must implement this
	 */
	Class mbeanInterface;
	/**
	 * The type of the MBeans grouped here.
	 */
	String typeName;
	/**
	 * The name of the MBean attribute thats the unique key for this group.
	 * Example: Map MBeans then MapName would be the key
	 */
	String keyAttributeName;
	
	/**
	 * The domain name to register MBeans under.
	 */
	String domainName;
	/**
	 * The main property these MBeans are about, i.e. a grid or a directory or a container of things with mbeans
	 */
	String groupingAttributeName;
	SummaryMBeanImpl<M> summaryMBean;
	
	volatile MBeanServer mbeanServer;
	
	public String getDomainName()
	{
		return domainName;
	}
	
	public ObjectName makeObjectName(String groupName, String keyValue)
		throws MalformedObjectNameException
	{
		Hashtable<String, String> props = new Hashtable<String, String>();
		props.put(groupingAttributeName, groupName);
		props.put(keyAttributeName, keyValue);
		props.put("type", typeName);
		return new ObjectName(domainName, props);
	}

	/**
	 * Construct a manager.
	 * @param clazz The MBeanImpl class file
	 * @param clazzI The JMX interface implemented by clazz
	 * @param domainName The domain to register MBeans for
	 * @param groupName The name of the container of these MBeans
	 * @param typeName The name of the attribute (agentClass or mapName...)
	 * @param keyName The attribute value attribute
	 */
	public MBeanGroupManager(Class<M> clazz, Class clazzI, String domainName, String groupName, String typeName, String keyName)
		throws InstanceAlreadyExistsException
	{
		this.domainName = domainName;
		groupingAttributeName = groupName;
		mbeanClass = clazz;
		mbeanInterface = clazzI;
		this.typeName = typeName;
		this.keyAttributeName = keyName;
		init();
	}
	
	public Collection<M> getAllBeans()
	{
		return beans.values();
	}
	
	void init()
		throws InstanceAlreadyExistsException
	{
		ArrayList<MBeanServer> mBeanServers = MBeanServerFactory.findMBeanServer(null);
		if(mBeanServers.size() == 0)
		{
			logger.log(Level.FINE, "No MBeanServer found, creating a new one");
			mbeanServer = MBeanServerFactory.createMBeanServer(domainName);
		}
		else
		{
			logger.log(Level.FINE, "Reusing existing MBeanServer");
			mbeanServer = (MBeanServer)mBeanServers.get(0);
		}
		
        try
        {
	        summaryMBean = new SummaryMBeanImpl<M>(this, mbeanClass, typeName);
	        
			Hashtable<String, String> props = new Hashtable<String, String>();
			props.put("type", typeName + "Summary");
			ObjectName on = new ObjectName(domainName, props);
			StandardMBean realMBean = new StandardMBean(summaryMBean, SummaryMBean.class);
			mbeanServer.registerMBean(realMBean, on);
        }
        catch(InstanceAlreadyExistsException e)
        {
//	        	logger.log(Level.INFO, "Summary MBean already exists, reuse old one");
        	throw e;
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        	logger.log(Level.SEVERE, "Exception initializing MBeanGroupManager: " + e.toString(), e);
		}
	}
	
	/**
	 * This returns the JMXServer to use for registering mbeans. The first time that it's called
	 * it also registers the Summary MBean for this group
	 * @return
	 */
	public MBeanServer getServer()
	{
		// if MBeanServer isn't available then return a fake one to allow everything to pretend everything is ok
		return mbeanServer;
	}
	
	/**
	 * This must be implemented and return an MBeanImpl for the parameters.
	 * @param gridName
	 * @param mapName
	 * @return
	 */
	abstract public M createMBean(String gridName, String keyName);

	/**
	 * This is called to fetch the MBeanImpl for a given key. The bean
	 * is created if it didn't already exist.
	 */
	public M getBean(String groupName, String keyValue)
	{
		M bean = beans.get(keyValue);
		if(bean == null)
		{
			synchronized(MBeanGroupManager.class)
			{
				bean = beans.get(keyValue);
				if(bean == null)
				{
					bean = createMBean(groupName, keyValue);
					try
					{
						MBeanServer server = getServer();
						if(server != null && !keyValue.equals(UNKNOWN_MAP));
						{
							StandardMBean mbean = new StandardMBean(bean, mbeanInterface);
							MBeanInfo info = mbean.getMBeanInfo();
							if(summaryMBean != null)
							{
								ObjectName on = summaryMBean.makeObjectName(bean, typeName);
								server.registerMBean(mbean, on);
							}
						}
						beans.put(keyValue, bean);
					}
					catch(Exception e)
					{
						logger.log(Level.SEVERE, "Exception registering MBean for " + groupName + ":" + keyValue, e);
						throw new ObjectGridRuntimeException(e);
					}
				}
			}
		}
		return bean;
	}
}
