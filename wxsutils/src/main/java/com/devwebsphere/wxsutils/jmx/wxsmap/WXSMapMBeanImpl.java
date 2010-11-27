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
	MinMaxAvgMetric insertMetric = new MinMaxAvgMetric();
	MinMaxAvgMetric removeMetric = new MinMaxAvgMetric();
	MinMaxAvgMetric invalidateMetric = new MinMaxAvgMetric();
	MinMaxAvgMetric containsMetric = new MinMaxAvgMetric();
	MinMaxAvgMetric lockMetric = new MinMaxAvgMetric();
	MinMaxAvgMetric unlockMetric = new MinMaxAvgMetric();
	
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

	public MinMaxAvgMetric getInsertMetrics()
	{
		return insertMetric;
	}
	
	public MinMaxAvgMetric getLockMetrics()
	{
		return lockMetric;
	}

	public MinMaxAvgMetric getUnlockMetrics()
	{
		return unlockMetric;
	}
	
	public MinMaxAvgMetric getInvalidateMetrics()
	{
		return invalidateMetric;
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
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getGetExceptionCount()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Integer getInsertExceptionCount() 
	{
		return new Integer(insertMetric.getExceptionCount());
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getGetLastExceptionString()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public String getInsertLastExceptionString() 
	{
		Throwable t = insertMetric.getLastException();
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
	public Double getInsertTimeAvgMS() 
	{
		return new Double(insertMetric.getAvgTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getGetTimeMaxMS()
	 */
	@TabularAttribute
	public Double getInsertTimeMaxMS() 
	{
		return new Double(insertMetric.getMaxTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getGetTimeMinMS()
	 */
	@TabularAttribute
	public Double getInsertTimeMinMS() {
		return new Double(insertMetric.getMinTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getGetTotalTimeMS()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Double getInsertTotalTimeMS() {
		return new Double(insertMetric.getTotalTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
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
	
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getGetExceptionCount()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Integer getLockExceptionCount() 
	{
		return new Integer(getMetric.getExceptionCount());
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getLockLastExceptionString()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public String getLockLastExceptionString() 
	{
		Throwable t = lockMetric.getLastException();
		if(t != null)
		{
			return t.toString();
		}
		else
			return "<>";
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getLockTimeAvgMS()
	 */
	@TabularAttribute
	public Double getLockTimeAvgMS() 
	{
		return new Double(lockMetric.getAvgTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getLockTimeMaxMS()
	 */
	@TabularAttribute
	public Double getLockTimeMaxMS() 
	{
		return new Double(lockMetric.getMaxTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getLockTimeMinMS()
	 */
	@TabularAttribute
	public Double getLockTimeMinMS() {
		return new Double(lockMetric.getMinTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getLockTotalTimeMS()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Double getLockTotalTimeMS() {
		return new Double(lockMetric.getTotalTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}

	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getUnlockExceptionCount()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Integer getUnlockExceptionCount() 
	{
		return new Integer(unlockMetric.getExceptionCount());
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getUnlockLastExceptionString()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public String getUnlockLastExceptionString() 
	{
		Throwable t = unlockMetric.getLastException();
		if(t != null)
		{
			return t.toString();
		}
		else
			return "<>";
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getUnlockTimeAvgMS()
	 */
	@TabularAttribute
	public Double getUnlockTimeAvgMS() 
	{
		return new Double(unlockMetric.getAvgTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getUnlockTimeMaxMS()
	 */
	@TabularAttribute
	public Double getUnlockTimeMaxMS() 
	{
		return new Double(unlockMetric.getMaxTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getUnlockTimeMinMS()
	 */
	@TabularAttribute
	public Double getUnlockTimeMinMS() {
		return new Double(unlockMetric.getMinTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getUnlockTotalTimeMS()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Double getUnlockTotalTimeMS() {
		return new Double(unlockMetric.getTotalTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}

	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getGetExceptionCount()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Integer getInvalidateExceptionCount() 
	{
		return new Integer(invalidateMetric.getExceptionCount());
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getGetLastExceptionString()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public String getInvalidateLastExceptionString() 
	{
		Throwable t = invalidateMetric.getLastException();
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
	public Double getInvalidateTimeAvgMS() 
	{
		return new Double(invalidateMetric.getAvgTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getGetTimeMaxMS()
	 */
	@TabularAttribute
	public Double getInvalidateTimeMaxMS() 
	{
		return new Double(invalidateMetric.getMaxTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getGetTimeMinMS()
	 */
	@TabularAttribute
	public Double getInvalidateTimeMinMS() {
		return new Double(invalidateMetric.getMinTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxsutils.jmx.wxsmap.Aa#getGetTotalTimeMS()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Double getInvalidateTotalTimeMS() {
		return new Double(invalidateMetric.getTotalTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
}
