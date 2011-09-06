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

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.devwebsphere.wxsutils.jmx.MBeanGroupManager;

/**
 * You should register this with your web application when using WXSUtils in a WAR. It cleans up the JMX MBeans so that
 * you can restart a web app. Add this fragment to your web.xml file
 * 
 * <listener> <listener-class>com.devwebsphere.wxsutils.WXSUtilsServletListener</listener-class> </listener>
 * 
 * @author mfraenkel
 * 
 */
public class WXSUtilsServletListener implements ServletContextListener {
	static Logger logger = Logger.getLogger(WXSUtilsServletListener.class.getName());

	/**
	 * Application Lifecycle Listener implementation class WXSUtilListener
	 * 
	 */

	/**
	 * Default constructor.
	 */
	public WXSUtilsServletListener() {
	}

	public void contextDestroyed(ServletContextEvent arg0) {
		cleanup(WXSUtils.wxsMapMBeanManager.get());
		cleanup(WXSUtils.agentMBeanManager.get());
		cleanup(WXSUtils.loaderMBeanManager.get());
		cleanup(WXSUtils.indexMBeanManager.get());
	}

	private void cleanup(MBeanGroupManager<?> mbeanGroupManager) {
		if (mbeanGroupManager == null) {
			// no group manager to cleanup
			return;
		}
		MBeanServer mbeanServer = mbeanGroupManager.getServer();
		try {
			ObjectName domain = new ObjectName(mbeanGroupManager.getDomainName() + ":*");
			Set<ObjectName> names = mbeanServer.queryNames(domain, null);
			for (ObjectName name : names) {
				mbeanServer.unregisterMBean(name);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception destroying MBeans", e);
		}
	}

	public void contextInitialized(ServletContextEvent arg0) {
		MBeanGroupManager.domainNameSuffix = arg0.getServletContext().getServletContextName();
	}

}