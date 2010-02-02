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
package com.devwebsphere.wxsutils.jmx.loader;

import java.util.concurrent.atomic.AtomicLong;

import com.devwebsphere.wxsutils.jmx.MinMaxAvgMetric;
import com.devwebsphere.wxsutils.jmx.SummaryMBeanImpl;
import com.devwebsphere.wxsutils.jmx.TabularAttribute;
import com.devwebsphere.wxsutils.jmx.TabularKey;



public final class LoaderMBeanImpl implements LoaderMBean
{
	int avgBatchUpdateListSize;
	String mapName;
	String gridName;
	
	MinMaxAvgMetric getMetrics = new MinMaxAvgMetric();
	MinMaxAvgMetric batchUpdateMetrics = new MinMaxAvgMetric();
	MinMaxAvgMetric batchSizeMetrics = new MinMaxAvgMetric();
	MinMaxAvgMetric getSizeMetrics = new MinMaxAvgMetric();
	AtomicLong insertCounter = new AtomicLong();
	AtomicLong updateCounter = new AtomicLong();
	AtomicLong deleteCounter = new AtomicLong();

	public LoaderMBeanImpl(String gridName, String name)
	{
		this.gridName = gridName;
		mapName = name;
	}
	
	@TabularAttribute
	public Integer getBatchUpdateListAvgSize() 
	{
		return new Integer((int)batchSizeMetrics.getAvgTimeNS());
	}

	@TabularAttribute
	public Double getBatchUpdateTimeAvgMS() {
		return new Double(batchUpdateMetrics.getAvgTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}

	@TabularAttribute
	public Double getGetTimeAvgMS() {
		return new Double(getMetrics.getAvgTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}

	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Integer getBatchUpdateCount() {
		return new Integer((int)batchUpdateMetrics.getCount());
	}

	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Integer getBatchUpdateExceptionCount() 
	{
		return new Integer(batchUpdateMetrics.getExceptionCount());
	}

	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public String getBatchUpdateLastExceptionString()
	{
		Throwable t = batchUpdateMetrics.getLastException();
		if(t != null)
		{
			return t.toString();
		}
		else
			return "";
	}
	
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Integer getGetCount() {
		return new Integer((int)getMetrics.getCount());
	}

	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Integer getGetExceptionCount() 
	{
		return new Integer(getMetrics.getExceptionCount());
	}

	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public String getGetLastExceptionString()
	{
		Throwable t = getMetrics.getLastException();
		if(t != null)
		{
			return t.toString();
		}
		else
			return "<>";
	}
	
	@TabularAttribute
	public Integer getBatchUpdateListMaxSize() {
		return new Integer((int)batchSizeMetrics.getMaxTimeNS());
	}

	@TabularAttribute
	public Double getBatchUpdateTimeMaxMS() 
	{
		return new Double(batchUpdateMetrics.getMaxTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}

	@TabularAttribute
	public Double getGetTimeMaxMS() {
		return new Double(getMetrics.getMaxTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}

	@TabularAttribute
	public Integer getBatchUpdateListMinSize() {
		return new Integer((int)batchSizeMetrics.getMinTimeNS());
	}

	@TabularAttribute
	public Double getBatchUpdateTimeMinMS() {
		return new Double(batchUpdateMetrics.getMinTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}

	@TabularAttribute
	public Double getGetTimeMinMS() {
		return new Double(getMetrics.getAvgTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}

	public void resetStatistics() 
	{
		getMetrics.reset();
		batchUpdateMetrics.reset();
		batchSizeMetrics.reset();
		getSizeMetrics.reset();
		insertCounter.set(0);
		updateCounter.set(0);
		deleteCounter.set(0);
	}

	public MinMaxAvgMetric getGetMetrics() {
		return getMetrics;
	}

	public MinMaxAvgMetric getBatchUpdateMetrics() {
		return batchUpdateMetrics;
	}

	public MinMaxAvgMetric getBatchSizeMetrics() {
		return batchSizeMetrics;
	}

	public MinMaxAvgMetric getGetSizeMetrics() {
		return getSizeMetrics;
	}

	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Long getRowDeleteCounter() 
	{
		return new Long(deleteCounter.get());
	}

	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Long getRowInsertCounter() {
		return new Long(insertCounter.get());
	}

	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Long getRowUpdateCounter() {
		return new Long(updateCounter.get());
	}
	
	public void recordOperationRows(int numInserts, int numUpdates, int numDeletes)
	{
		insertCounter.addAndGet(numInserts);
		updateCounter.addAndGet(numUpdates);
		deleteCounter.addAndGet(numDeletes);
	}

	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Long getQueryTimeMS() 
	{
		return new Long(System.currentTimeMillis());
	}

	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Double getBatchUpdateTimeTotalMS() {
		return new Double(batchUpdateMetrics.getTotalTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}

	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Double getGetTotalTimeMS() 
	{
		return new Double(getMetrics.getTotalTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}

	@TabularKey
	public String getMapName() 
	{
		return mapName;
	}
	
	@TabularKey
	public String getGridName()
	{
		return gridName;
	}
}
