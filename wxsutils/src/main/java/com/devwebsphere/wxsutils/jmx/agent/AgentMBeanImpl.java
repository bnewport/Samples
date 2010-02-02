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
package com.devwebsphere.wxsutils.jmx.agent;

import com.devwebsphere.wxsutils.jmx.MinMaxAvgMetric;
import com.devwebsphere.wxsutils.jmx.SummaryMBeanImpl;
import com.devwebsphere.wxsutils.jmx.TabularAttribute;
import com.devwebsphere.wxsutils.jmx.TabularKey;

/**
 * This implements the AgentMBean. This is once instance of this MBean PER JVM for a given Agent. All statistics
 * for all primaries within that partition are aggregated here.
 * You can see all attributes for summary MBeans are annotated using TabularAttribute and ones for the commercial
 * tools only are tagged with the mbean name monitor.
 */
public class AgentMBeanImpl implements AgentMBean 
{
	
	String className;
	String gridName;
	MinMaxAvgMetric partitionMetric = new MinMaxAvgMetric();
	MinMaxAvgMetric keysMetric = new MinMaxAvgMetric();
	
	public AgentMBeanImpl(String gridName, String className)
	{
		this.gridName = gridName;
		this.className = className;
	}

	@TabularKey
	public String getGridName()
	{
		return gridName;
	}
	
	@TabularKey
	public String getClassName() 
	{
		return className;
	}
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Integer getKeysExceptionCount() 
	{
		return new Integer(keysMetric.getExceptionCount());
	}
	
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public String getKeysLastExceptionString() 
	{
		Throwable t = keysMetric.getLastException();
		if(t != null)
		{
			return t.toString();
		}
		else
			return "<>";
	}
	
	@TabularAttribute
	public Double getKeysTimeAvgMS() 
	{
		return new Double(keysMetric.getAvgTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	
	@TabularAttribute
	public Double getKeysTimeMaxMS() 
	{
		return new Double(keysMetric.getMaxTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute
	public Double getKeysTimeMinMS() {
		return new Double(keysMetric.getMinTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Double getKeysTotalTimeMS() {
		return new Double(keysMetric.getTotalTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Integer getPartitionExceptionCount() 
	{
		return new Integer(partitionMetric.getExceptionCount());
	}
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public String getPartitionLastExceptionString() 
	{
		Throwable t = partitionMetric.getLastException();
		if(t != null)
		{
			return t.toString();
		}
		else
			return "<>";
	}
	@TabularAttribute
	public Double getPartitionTimeAvgMS() {
		return new Double(partitionMetric.getAvgTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute
	public Double getPartitionTimeMaxMS() {
		return new Double(partitionMetric.getMaxTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute
	public Double getPartitionTimeMinMS() {
		return new Double(partitionMetric.getMinTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Double getPartitionTotalTimeMS() {
		return new Double(partitionMetric.getTotalTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}

	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Long getQueryTimeMS() 
	{
		return new Long(System.currentTimeMillis());
	}
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Integer getKeysCount() 
	{
		return new Integer(keysMetric.getCount());
	}
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Integer getPartitionCount() 
	{
		return new Integer(partitionMetric.getCount());
	}
	public final MinMaxAvgMetric getPartitionMetric() {
		return partitionMetric;
	}
	public final MinMaxAvgMetric getKeysMetric() {
		return keysMetric;
	}
}
