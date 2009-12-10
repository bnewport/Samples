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

import java.lang.reflect.InvocationTargetException;

import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.TabularData;

/**
 * This is an MBean with two methods. One for fetch a table of all statistics and the
 * other for metrics for commercial monitor products only. This usually excludes metrics
 * such as min/max/avg and so on.
 *
 */
public interface SummaryMBean 
{
	/**
	 * This returns a Table of data with all metrics included.
	 * @return
	 * @throws OpenDataException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	TabularData getAllData() throws OpenDataException, IllegalAccessException, InvocationTargetException;
	/**
	 * This returns a table with much fewer columns that getAllData. Only metrics for commercial monitors will be
	 * here. Typically only MBean attributes annotated with TabularAttribute specifying monitor will be here.
	 * @return
	 * @throws OpenDataException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	TabularData getAllMonitorData() throws OpenDataException, IllegalAccessException, InvocationTargetException;
}
