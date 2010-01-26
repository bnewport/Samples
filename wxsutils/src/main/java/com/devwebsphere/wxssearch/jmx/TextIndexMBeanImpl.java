package com.devwebsphere.wxssearch.jmx;

import com.devwebsphere.wxsutils.jmx.MinMaxAvgMetric;
import com.devwebsphere.wxsutils.jmx.SummaryMBeanImpl;
import com.devwebsphere.wxsutils.jmx.TabularAttribute;
import com.devwebsphere.wxsutils.jmx.TabularKey;

public class TextIndexMBeanImpl implements TextIndexMBean 
{
	MinMaxAvgMetric insertMetrics = new MinMaxAvgMetric();
	MinMaxAvgMetric containsMetrics = new MinMaxAvgMetric();
	MinMaxAvgMetric removeMetrics = new MinMaxAvgMetric();
	String indexName;
	
	public MinMaxAvgMetric getInsertMetrics() { return insertMetrics; }
	public MinMaxAvgMetric getContainsMetrics() { return containsMetrics; }
	public MinMaxAvgMetric getRemoveMetrics() { return removeMetrics; }

	public TextIndexMBeanImpl(String indexName)
	{
		this.indexName = indexName;
	}
	
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxssearch.jmx.IndexMBean#reset()
	 */
	public void reset()
	{
		insertMetrics.reset();
		containsMetrics.reset();
		removeMetrics.reset();
	}

	/* (non-Javadoc)
	 * @see com.devwebsphere.wxssearch.jmx.IndexMBean#getIndexName()
	 */
	@TabularKey
	public String getIndexName() 
	{
		return indexName;
	}

	/* (non-Javadoc)
	 * @see com.devwebsphere.wxssearch.jmx.IndexMBean#getInsertExceptionCount()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Integer getInsertExceptionCount() 
	{
		return new Integer(insertMetrics.getExceptionCount());
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxssearch.jmx.IndexMBean#getInsertLastExceptionString()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public String getInsertLastExceptionString() 
	{
		Throwable t = insertMetrics.getLastException();
		if(t != null)
		{
			return t.toString();
		}
		else
			return "<>";
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxssearch.jmx.IndexMBean#getInsertTimeAvgMS()
	 */
	@TabularAttribute
	public Double getInsertTimeAvgMS() 
	{
		return new Double(insertMetrics.getAvgTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxssearch.jmx.IndexMBean#getInsertTimeMaxMS()
	 */
	@TabularAttribute
	public Double getInsertTimeMaxMS() 
	{
		return new Double(insertMetrics.getMaxTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxssearch.jmx.IndexMBean#getInsertTimeMinMS()
	 */
	@TabularAttribute
	public Double getInsertTimeMinMS() {
		return new Double(insertMetrics.getMinTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxssearch.jmx.IndexMBean#getInsertTotalTimeMS()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Double getInsertTotalTimeMS() {
		return new Double(insertMetrics.getTotalTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxssearch.jmx.IndexMBean#getContainsExceptionCount()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Integer getContainsExceptionCount() 
	{
		return new Integer(containsMetrics.getExceptionCount());
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxssearch.jmx.IndexMBean#getContainsLastExceptionString()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public String getContainsLastExceptionString() 
	{
		Throwable t = containsMetrics.getLastException();
		if(t != null)
		{
			return t.toString();
		}
		else
			return "<>";
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxssearch.jmx.IndexMBean#getContainsTimeAvgMS()
	 */
	@TabularAttribute
	public Double getContainsTimeAvgMS() 
	{
		return new Double(containsMetrics.getAvgTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxssearch.jmx.IndexMBean#getContainsTimeMaxMS()
	 */
	@TabularAttribute
	public Double getContainsTimeMaxMS() 
	{
		return new Double(containsMetrics.getMaxTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxssearch.jmx.IndexMBean#getContainsTimeMinMS()
	 */
	@TabularAttribute
	public Double getContainsTimeMinMS() {
		return new Double(containsMetrics.getMinTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxssearch.jmx.IndexMBean#getContainsTotalTimeMS()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Double getContainsTotalTimeMS() {
		return new Double(containsMetrics.getTotalTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxssearch.jmx.IndexMBean#getRemoveExceptionCount()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Integer getRemoveExceptionCount() 
	{
		return new Integer(removeMetrics.getExceptionCount());
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxssearch.jmx.IndexMBean#getRemoveLastExceptionString()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public String getRemoveLastExceptionString() 
	{
		Throwable t = removeMetrics.getLastException();
		if(t != null)
		{
			return t.toString();
		}
		else
			return "<>";
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxssearch.jmx.IndexMBean#getRemoveTimeAvgMS()
	 */
	@TabularAttribute
	public Double getRemoveTimeAvgMS() 
	{
		return new Double(removeMetrics.getAvgTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxssearch.jmx.IndexMBean#getRemoveTimeMaxMS()
	 */
	@TabularAttribute
	public Double getRemoveTimeMaxMS() 
	{
		return new Double(removeMetrics.getMaxTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxssearch.jmx.IndexMBean#getRemoveTimeMinMS()
	 */
	@TabularAttribute
	public Double getRemoveTimeMinMS() {
		return new Double(removeMetrics.getMinTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxssearch.jmx.IndexMBean#getRemoveTotalTimeMS()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Double getRemoveTotalTimeMS() {
		return new Double(removeMetrics.getTotalTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Long getQueryTimeMS() 
	{
		return new Long(System.currentTimeMillis());
	}
}
