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

import java.util.List;

/**
 * This is used by the SummaryMBeanImpl class to provide access to an MBean store for the MBeans to be summarized.
 *
 * @param <T> The MBeanImpl type
 */
public interface SummaryMBeanSource <T>
{
	/**
	 * This returns the MBeanImpl for a specific name or null
	 * @param mapName
	 * @return The MBeanImpl or null
	 */
	T getBean(String mapName);
	
	/**
	 * This returns a list of MBeanImpl names
	 * @return
	 */
	List<String> getCurrentBeanNames();
}
