package utils;
//
//This sample program is provided AS IS and may be used, executed, copied and
//modified without royalty payment by customer (a) for its own instruction and
//study, (b) in order to develop applications designed to run with an IBM
//WebSphere product, either for customer's own internal use or for redistribution
//by customer, as part of such an application, in customer's own products. "
//
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2009
//All Rights Reserved * Licensed Materials - Property of IBM
//



import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.devwebsphere.rediswxs.R;

/**
 * This class will automatically initialize and shutdown the Redis client when
 * the WEBAPP starts and stops. This class should be registered as a listener
 * for the web app. This expects a grid-catalog-endpoints property to be
 * defined in the web.xml
 * @author bnewport
 *
 *  <context-param>
 *   <param-name>grid-catalog-endpoints</param-name>
 *   <param-value>localhost:2809</param-value>
 * </context-param>
 * 
 * <listener>
 *   <listener-class>utils.InitializeGridListener</listener-class>
 * </listener>
 */

public class InitializeGridListener implements ServletContextListener 
{
	public void contextDestroyed(ServletContextEvent arg0) 
	{
	}

	public void contextInitialized(ServletContextEvent c) 
	{
		R.initialize();
	}

}
