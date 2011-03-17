package com.devwebsphere.wxsutils.jmx.listset;

import com.devwebsphere.wxsutils.jmx.MinMaxAvgMetric;
import com.devwebsphere.wxsutils.jmx.SummaryMBeanImpl;
import com.devwebsphere.wxsutils.jmx.TabularAttribute;
import com.devwebsphere.wxsutils.jmx.TabularKey;

public class WXSMapOfListsMBeanImpl implements WXSMapOfListsMBean {
	String mapName;
	String gridName;
	MinMaxAvgMetric lenMetric = new MinMaxAvgMetric();
	MinMaxAvgMetric pushMetric = new MinMaxAvgMetric();
	MinMaxAvgMetric popMetric = new MinMaxAvgMetric();
	MinMaxAvgMetric rangeMetric = new MinMaxAvgMetric();
	MinMaxAvgMetric trimMetric = new MinMaxAvgMetric();
	MinMaxAvgMetric removeMetric = new MinMaxAvgMetric();

	public WXSMapOfListsMBeanImpl(String gridName, String mapName)
	{
		this.gridName = gridName;
		this.mapName = mapName;
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
	
	public MinMaxAvgMetric getLenMetrics()
	{
		return lenMetric;
	}

	public MinMaxAvgMetric getPopMetrics()
	{
		return popMetric;
	}
	public MinMaxAvgMetric getRemoveMetrics()
	{
		return removeMetric;
	}
	public MinMaxAvgMetric getPushMetrics()
	{
		return pushMetric;
	}
	public MinMaxAvgMetric getRangeMetrics()
	{
		return rangeMetric;
	}
	public MinMaxAvgMetric getTrimMetrics()
	{
		return trimMetric;
	}
	
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Integer getLenExceptionCount() 
	{
		return new Integer(lenMetric.getExceptionCount());
	}
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public String getLenLastExceptionString() 
	{
		Throwable t = lenMetric.getLastException();
		if(t != null)
		{
			return t.toString();
		}
		else
			return "<>";
	}
	@TabularAttribute
	public Double getLenTimeAvgMS() 
	{
		return new Double(lenMetric.getAvgTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute
	public Double getLenTimeMaxMS() 
	{
		return new Double(lenMetric.getMaxTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute
	public Double getLenTimeMinMS() {
		return new Double(lenMetric.getMinTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Double getLenTotalTimeMS() {
		return new Double(lenMetric.getTotalTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Integer getPopExceptionCount() 
	{
		return new Integer(popMetric.getExceptionCount());
	}
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public String getPopLastExceptionString() 
	{
		Throwable t = popMetric.getLastException();
		if(t != null)
		{
			return t.toString();
		}
		else
			return "<>";
	}
	@TabularAttribute
	public Double getPopTimeAvgMS() 
	{
		return new Double(popMetric.getAvgTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute
	public Double getPopTimeMaxMS() 
	{
		return new Double(popMetric.getMaxTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute
	public Double getPopTimeMinMS() {
		return new Double(popMetric.getMinTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Double getPopTotalTimeMS() {
		return new Double(popMetric.getTotalTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}

	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Integer getPushExceptionCount() 
	{
		return new Integer(pushMetric.getExceptionCount());
	}
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public String getPushLastExceptionString() 
	{
		Throwable t = pushMetric.getLastException();
		if(t != null)
		{
			return t.toString();
		}
		else
			return "<>";
	}
	@TabularAttribute
	public Double getPushTimeAvgMS() 
	{
		return new Double(pushMetric.getAvgTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute
	public Double getPushTimeMaxMS() 
	{
		return new Double(pushMetric.getMaxTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute
	public Double getPushTimeMinMS() {
		return new Double(pushMetric.getMinTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Double getPushTotalTimeMS() {
		return new Double(pushMetric.getTotalTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}

	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Integer getRangeExceptionCount() 
	{
		return new Integer(rangeMetric.getExceptionCount());
	}
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public String getRangeLastExceptionString() 
	{
		Throwable t = rangeMetric.getLastException();
		if(t != null)
		{
			return t.toString();
		}
		else
			return "<>";
	}
	@TabularAttribute
	public Double getRangeTimeAvgMS() 
	{
		return new Double(rangeMetric.getAvgTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute
	public Double getRangeTimeMaxMS() 
	{
		return new Double(rangeMetric.getMaxTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute
	public Double getRangeTimeMinMS() {
		return new Double(rangeMetric.getMinTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Double getRangeTotalTimeMS() {
		return new Double(rangeMetric.getTotalTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}

	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Integer getTrimExceptionCount() 
	{
		return new Integer(trimMetric.getExceptionCount());
	}
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public String getTrimLastExceptionString() 
	{
		Throwable t = trimMetric.getLastException();
		if(t != null)
		{
			return t.toString();
		}
		else
			return "<>";
	}
	@TabularAttribute
	public Double getTrimTimeAvgMS() 
	{
		return new Double(trimMetric.getAvgTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute
	public Double getTrimTimeMaxMS() 
	{
		return new Double(trimMetric.getMaxTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute
	public Double getTrimTimeMinMS() {
		return new Double(trimMetric.getMinTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Double getTrimTotalTimeMS() {
		return new Double(trimMetric.getTotalTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}

	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Integer getRemoveExceptionCount() 
	{
		return new Integer(removeMetric.getExceptionCount());
	}
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public String getRemoveLastExceptionString() 
	{
		Throwable t = removeMetric.getLastException();
		if(t != null)
		{
			return t.toString();
		}
		else
			return "<>";
	}
	@TabularAttribute
	public Double getRemoveTimeAvgMS() 
	{
		return new Double(removeMetric.getAvgTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute
	public Double getRemoveTimeMaxMS() 
	{
		return new Double(removeMetric.getMaxTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute
	public Double getRemoveTimeMinMS() {
		return new Double(removeMetric.getMinTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Double getRemoveTotalTimeMS() {
		return new Double(removeMetric.getTotalTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
}
