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


public class SummaryMBeanImpl<T> implements SummaryMBean 
{
	TabularDataMetaData<T> general;
	TabularDataMetaData<T> monitor;
	String typePrefix;
	/**
	 * Attributes annotated with this mbean name are included in the more concise
	 * summary mbeans.
	 */
	final public static String MONITOR_MBEAN = "monitor";
	
	public SummaryMBeanImpl(MBeanGroupManager<T> beanSource, Class<T> sourceClass, String indexName, String typePrefix)
		throws OpenDataException
	{
		this.typePrefix = typePrefix;
		general = new TabularDataMetaData<T>(beanSource, indexName, sourceClass, TabularAttribute.defaultMBean, typePrefix + "Detail", typePrefix+"DetailItemNames");
		monitor = new TabularDataMetaData<T>(beanSource, indexName, sourceClass, MONITOR_MBEAN, typePrefix+"MonitorDetail", typePrefix+" Statistics for monitors");
	}
	
	public TabularData getAllData()
		throws OpenDataException, IllegalAccessException, InvocationTargetException
	{
		TabularData rc = general.getData("all" + typePrefix + "Details", typePrefix + " details");
		return rc;
	}

	public TabularData getAllMonitorData() 
		throws OpenDataException, IllegalAccessException, InvocationTargetException
	{
		TabularData rc = monitor.getData("all" + typePrefix + "MonitorDetails", typePrefix + " details for Monitors");
		return rc;
	}
}
