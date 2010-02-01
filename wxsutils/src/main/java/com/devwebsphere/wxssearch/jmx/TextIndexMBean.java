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
package com.devwebsphere.wxssearch.jmx;


public interface TextIndexMBean {

	public void reset();

	public String getIndexName();

	public Integer getInsertExceptionCount();

	public String getInsertLastExceptionString();

	public Double getInsertTimeAvgMS();

	public Double getInsertTimeMaxMS();

	public Double getInsertTimeMinMS();

	public Double getInsertTotalTimeMS();

	public Integer getContainsExceptionCount();

	public String getContainsLastExceptionString();

	public Double getContainsTimeAvgMS();

	public Double getContainsTimeMaxMS();

	public Double getContainsTimeMinMS();

	public Double getContainsTotalTimeMS();

	public Integer getRemoveExceptionCount();

	public String getRemoveLastExceptionString();

	public Double getRemoveTimeAvgMS();

	public Double getRemoveTimeMaxMS();

	public Double getRemoveTimeMinMS();

	public Double getRemoveTotalTimeMS();

	public Long getQueryTimeMS();

}