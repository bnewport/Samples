package com.devwebsphere.wxsutils.jmx.listset;

import com.devwebsphere.wxsutils.jmx.MinMaxAvgMetric;
import com.devwebsphere.wxsutils.jmx.SummaryMBeanImpl;
import com.devwebsphere.wxsutils.jmx.TabularAttribute;
import com.devwebsphere.wxsutils.jmx.TabularKey;

public class WXSMapOfSetsMBeanImpl implements WXSMapOfSetsMBean {
	String gridName;
	String mapName;
	
	MinMaxAvgMetric addRemoveMetric = new MinMaxAvgMetric();
	MinMaxAvgMetric sizeMetric = new MinMaxAvgMetric();
	MinMaxAvgMetric containsMetric = new MinMaxAvgMetric();
	
	public WXSMapOfSetsMBeanImpl(String g, String m)
	{
		gridName = g;
		mapName = m;
	}
	@TabularKey
	public final String getMapName() {
		return mapName;
	}

	@TabularKey
	public String getGridName()
	{
		return gridName;
	}
	
	public MinMaxAvgMetric getAddRemoveMetrics()
	{
		return addRemoveMetric;
	}

	public MinMaxAvgMetric getSizeMetrics()
	{
		return sizeMetric;
	}

	public MinMaxAvgMetric getContainsMetrics()
	{
		return containsMetric;
	}
	
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Integer getAddRemoveExceptionCount() 
	{
		return new Integer(addRemoveMetric.getExceptionCount());
	}
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public String getAddRemoveLastExceptionString() 
	{
		Throwable t = addRemoveMetric.getLastException();
		if(t != null)
		{
			return t.toString();
		}
		else
			return "<>";
	}
	@TabularAttribute
	public Double getAddRemoveTimeAvgMS() 
	{
		return new Double(addRemoveMetric.getAvgTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute
	public Double getAddRemoveTimeMaxMS() 
	{
		return new Double(addRemoveMetric.getMaxTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute
	public Double getAddRemoveTimeMinMS() {
		return new Double(addRemoveMetric.getMinTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Double getAddRemoveTotalTimeMS() {
		return new Double(addRemoveMetric.getTotalTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Integer getSizeExceptionCount() 
	{
		return new Integer(sizeMetric.getExceptionCount());
	}
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public String getSizeLastExceptionString() 
	{
		Throwable t = sizeMetric.getLastException();
		if(t != null)
		{
			return t.toString();
		}
		else
			return "<>";
	}
	@TabularAttribute
	public Double getSizeTimeAvgMS() 
	{
		return new Double(sizeMetric.getAvgTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute
	public Double getSizeTimeMaxMS() 
	{
		return new Double(sizeMetric.getMaxTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute
	public Double getSizeTimeMinMS() {
		return new Double(sizeMetric.getMinTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Double getSizeTotalTimeMS() {
		return new Double(sizeMetric.getTotalTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}

	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Integer getContainsExceptionCount() 
	{
		return new Integer(containsMetric.getExceptionCount());
	}
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public String getContainsLastExceptionString() 
	{
		Throwable t = containsMetric.getLastException();
		if(t != null)
		{
			return t.toString();
		}
		else
			return "<>";
	}
	@TabularAttribute
	public Double getContainsTimeAvgMS() 
	{
		return new Double(containsMetric.getAvgTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute
	public Double getContainsTimeMaxMS() 
	{
		return new Double(containsMetric.getMaxTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute
	public Double getContainsTimeMinMS() {
		return new Double(containsMetric.getMinTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Double getContainsTotalTimeMS() {
		return new Double(containsMetric.getTotalTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
}
