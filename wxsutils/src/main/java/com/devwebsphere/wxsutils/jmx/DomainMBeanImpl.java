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

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.StandardMBean;

public class DomainMBeanImpl implements DomainMBean {
	static Logger logger = Logger.getLogger(DomainMBeanImpl.class.getName());

	static String defaultDomainName = "com.devwebsphere.wxs";
	private MBeanServer server;
	private String name;

	private DomainMBeanImpl(MBeanServer server, String name) {
		this.server = server;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public MBeanServer getMBeanServer() {
		return server;
	}

	public void destroy() {
		try {
			ObjectName domainON = new ObjectName(name + ":*");
			Set<ObjectName> names = server.queryNames(domainON, null);
			for (ObjectName name : names) {
				server.unregisterMBean(name);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception destroying MBeans", e);
		}
	}

	static void setDefaultDomainName(String defaultName) {
		defaultDomainName = defaultName;
	}

	static String getDefaultDomainName() {
		return defaultDomainName;
	}

	static DomainMBeanImpl create() {
		// create a uniquely named domain mbean
		MBeanServer mbeanServer = findMBeanServer();
		String defaultDomain = getDefaultDomainName();
		String domainName = defaultDomain;

		DomainMBeanImpl dMbean = new DomainMBeanImpl(mbeanServer, domainName);
		Hashtable<String, String> props = new Hashtable<String, String>(1);
		props.put("type", "Domain");

		try {
			StandardMBean realMBean = new StandardMBean(dMbean, DomainMBean.class);

			int cnt = 0;
			for (;;) {
				ObjectName on = new ObjectName(domainName, props);
				try {
					mbeanServer.registerMBean(realMBean, on);
					break;
				} catch (InstanceAlreadyExistsException e) {
					domainName = defaultDomainName + '-' + String.valueOf(cnt++);
					dMbean.name = domainName;
				}
			}
		} catch (Exception e) {
			dMbean = null;
			logger.log(Level.SEVERE, "Exception initializing DomainMBean: " + e.toString(), e);
		}

		return dMbean;
	}

	static private MBeanServer findMBeanServer() {
		ArrayList<MBeanServer> mBeanServers = MBeanServerFactory.findMBeanServer(null);
		MBeanServer mbeanServer;
		if (mBeanServers.size() == 0) {
			logger.log(Level.FINE, "No MBeanServer found, creating a new one");
			mbeanServer = ManagementFactory.getPlatformMBeanServer();
		} else {
			logger.log(Level.FINE, "Reusing existing MBeanServer");
			mbeanServer = (MBeanServer) mBeanServers.get(0);
		}

		return mbeanServer;
	}

}
