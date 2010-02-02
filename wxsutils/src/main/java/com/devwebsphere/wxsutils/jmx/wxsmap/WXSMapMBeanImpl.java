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
package com.devwebsphere.wxsutils.jmx.wxsmap;

import com.devwebsphere.wxsutils.jmx.MinMaxAvgMetric;
import com.devwebsphere.wxsutils.jmx.SummaryMBeanImpl;
import com.devwebsphere.wxsutils.jmx.TabularAttribute;
import com.devwebsphere.wxsutils.jmx.TabularKey;

public class WXSMapMBeanImpl implements WXSMapMBean 
{
	String mapName;
	String gridName;
	MinMaxAvgMetric getMetric = new MinMaxAvgMetric();
	MinMaxAvgMetric putMetric = new MinMaxAvgMetric();
	MinMaxAvgMetric removeMetric = new MinMaxAvgMetric();
	MinMaxAvgMetric containsMetric = new MinMaxAvgMetric();
	
	@TabularKey
	public final String getMapName() {
		return mapName;
	}

	@TabularKey
	public String getGridName()
	{
		return gridName;
	}
	
	public WXSMapMBeanImpl(String gridName, String mapName)
	{
		this.gridName = gridName;
		this.mapName = mapName;
	}
	
	public MinMaxAvgMetric getGetMetrics()
	{
		return getMetric;
	}
	
	public MinMaxAvgMetric getPutMetrics()
	{
		return putMetric;
	}
	
	public MinMaxAvgMetric getRemoveMetrics()
	{
		return removeMetric;
	}

	public MinMaxAvgMetric getContainsMetrics()
	{
		return containsMetric;
	}
	
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getGetExceptionCount()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Integer getGetExceptionCount() 
	{
		return new Integer(getMetric.getExceptionCount());
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getGetLastExceptionString()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public String getGetLastExceptionString() 
	{
		Throwable t = getMetric.getLastException();
		if(t != null)
		{
			return t.toString();
		}
		else
			return "<>";
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getGetTimeAvgMS()
	 */
	@TabularAttribute
	public Double getGetTimeAvgMS() 
	{
		return new Double(getMetric.getAvgTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getGetTimeMaxMS()
	 */
	@TabularAttribute
	public Double getGetTimeMaxMS() 
	{
		return new Double(getMetric.getMaxTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getGetTimeMinMS()
	 */
	@TabularAttribute
	public Double getGetTimeMinMS() {
		return new Double(getMetric.getMinTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getGetTotalTimeMS()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Double getGetTotalTimeMS() {
		return new Double(getMetric.getTotalTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}

	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getPutExceptionCount()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Integer getPutExceptionCount() 
	{
		return new Integer(putMetric.getExceptionCount());
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getPutLastExceptionString()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public String getPutLastExceptionString() 
	{
		Throwable t = putMetric.getLastException();
		if(t != null)
		{
			return t.toString();
		}
		else
			return "<>";
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getPutTimeAvgMS()
	 */
	@TabularAttribute
	public Double getPutTimeAvgMS() 
	{
		return new Double(putMetric.getAvgTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getPutTimeMaxMS()
	 */
	@TabularAttribute
	public Double getPutTimeMaxMS() 
	{
		return new Double(putMetric.getMaxTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getPutTimeMinMS()
	 */
	@TabularAttribute
	public Double getPutTimeMinMS() {
		return new Double(putMetric.getMinTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getPutTotalTimeMS()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Double getPutTotalTimeMS() {
		return new Double(putMetric.getTotalTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}

	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getRemoveExceptionCount()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Integer getRemoveExceptionCount() 
	{
		return new Integer(removeMetric.getExceptionCount());
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getRemoveLastExceptionString()
	 */
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
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getRemoveTimeAvgMS()
	 */
	@TabularAttribute
	public Double getRemoveTimeAvgMS() 
	{
		return new Double(removeMetric.getAvgTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getRemoveTimeMaxMS()
	 */
	@TabularAttribute
	public Double getRemoveTimeMaxMS() 
	{
		return new Double(removeMetric.getMaxTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getRemoveTimeMinMS()
	 */
	@TabularAttribute
	public Double getRemoveTimeMinMS() {
		return new Double(removeMetric.getMinTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getRemoveTotalTimeMS()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Double getRemoveTotalTimeMS() {
		return new Double(removeMetric.getTotalTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}

	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getContainsExceptionCount()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Integer getContainsExceptionCount() 
	{
		return new Integer(containsMetric.getExceptionCount());
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getContainsLastExceptionString()
	 */
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
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getContainsTimeAvgMS()
	 */
	@TabularAttribute
	public Double getContainsTimeAvgMS() 
	{
		return new Double(containsMetric.getAvgTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getContainsTimeMaxMS()
	 */
	@TabularAttribute
	public Double getContainsTimeMaxMS() 
	{
		return new Double(containsMetric.getMaxTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getContainsTimeMinMS()
	 */
	@TabularAttribute
	public Double getContainsTimeMinMS() {
		return new Double(containsMetric.getMinTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getContainsTotalTimeMS()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Double getContainsTotalTimeMS() {
		return new Double(containsMetric.getTotalTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
}
