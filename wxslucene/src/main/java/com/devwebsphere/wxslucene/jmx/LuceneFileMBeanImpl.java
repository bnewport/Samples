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
package com.devwebsphere.wxslucene.jmx;

import com.devwebsphere.wxsutils.jmx.MinMaxAvgMetric;
import com.devwebsphere.wxsutils.jmx.SummaryMBeanImpl;
import com.devwebsphere.wxsutils.jmx.TabularAttribute;
import com.devwebsphere.wxsutils.jmx.TabularKey;

public class LuceneFileMBeanImpl implements LuceneFileMBean {
	
	String directoryName;
	String fileName;
	
	MinMaxAvgMetric readBytes = new MinMaxAvgMetric();
	MinMaxAvgMetric writeBytes = new MinMaxAvgMetric();
	MinMaxAvgMetric readSequencial = new MinMaxAvgMetric();
	
	MinMaxAvgMetric readTime = new MinMaxAvgMetric();
	MinMaxAvgMetric writeTime = new MinMaxAvgMetric();

	public MinMaxAvgMetric getReadBytesMetric()
	{
		return readBytes;
	}
	
	public MinMaxAvgMetric getWriteBytesMetric()
	{
		return writeBytes;
	}
	
	public MinMaxAvgMetric getReadTimeMetric()
	{
		return readTime;
	}
	
	public MinMaxAvgMetric getWriteTimeMetric()
	{
		return writeTime;
	}
	
	public LuceneFileMBeanImpl(String dName, String fName)
	{
		directoryName = dName;
		fileName = fName;
	}
	
	public void reset()
	{
		readBytes.reset();
		writeBytes.reset();
		readSequencial.reset();
		readTime.reset();
		writeTime.reset();
	}

	@TabularKey
	public final String getDirectoryName() {
		return directoryName;
	}

	public final void setDirectoryName(String directoryName) {
		this.directoryName = directoryName;
	}

	@TabularKey
	public final String getFileName() {
		return fileName;
	}

	public final void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public MinMaxAvgMetric getReadSequentialMetric()
	{
		return readSequencial;
	}
	
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Double getSequentialBlockAvg() 
	{
		return new Double(readSequencial.getAvgTimeNS());
	}
	
	@TabularAttribute
	public Double getSequentialBlockMax() 
	{
		return new Double(readSequencial.getMaxTimeNS());
	}
	@TabularAttribute
	public Double getSequentialBlockMin() {
		return new Double(readSequencial.getMinTimeNS());
	}

	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Double getReadBytesAvg() 
	{
		return new Double(readBytes.getAvgTimeNS());
	}
	
	@TabularAttribute
	public Double getReadBytesMax() 
	{
		return new Double(readBytes.getMaxTimeNS());
	}
	@TabularAttribute
	public Double getReadBytesMin() {
		return new Double(readBytes.getMinTimeNS());
	}
	
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxssearch.jmx.IndexMBean#getInsertTimeAvgMS()
	 */
	@TabularAttribute
	public Double getReadTimeAvgMS() 
	{
		return new Double(readTime.getAvgTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxssearch.jmx.IndexMBean#getInsertTimeMaxMS()
	 */
	@TabularAttribute
	public Double getReadTimeMaxMS() 
	{
		return new Double(readTime.getMaxTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxssearch.jmx.IndexMBean#getInsertTimeMinMS()
	 */
	@TabularAttribute
	public Double getReadTimeMinMS() {
		return new Double(readTime.getMinTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxssearch.jmx.IndexMBean#getInsertTotalTimeMS()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Double getReadTimeTotalTimeMS() {
		return new Double(readTime.getTotalTimeNS() / MinMaxAvgMetric.TIME_SCALE_NS_MS);
	}
	
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Double getReadTimeCount() {
		return new Double(readTime.getCount());
	}
}
