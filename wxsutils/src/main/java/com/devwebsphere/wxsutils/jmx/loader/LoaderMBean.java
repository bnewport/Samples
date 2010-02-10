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

public interface LoaderMBean 
{
	String getMapName();
	
	Integer getGetCount();
	
	Double getGetTimeMinMS();
	Double getGetTimeMaxMS();
	Double getGetTimeAvgMS();
	Double getGetTotalTimeMS();
	Integer getGetExceptionCount();
	String getGetLastExceptionString();
	
	Long getGetSizeMinRecords();
	Long getGetSizeMaxRecords();
	Long getGetSizeAvgRecords();
	
	Integer getBatchUpdateCount();
	Double getBatchUpdateTimeMinMS();
	Double getBatchUpdateTimeMaxMS();
	Double getBatchUpdateTimeAvgMS();
	Double getBatchUpdateTimeTotalMS();
	
	Integer getBatchUpdateListMinSize();
	Integer getBatchUpdateListMaxSize();
	Integer getBatchUpdateListAvgSize();

	Integer getBatchUpdateExceptionCount();
	String getBatchUpdateLastExceptionString();
	
	Long getRowInsertCounter();
	Long getRowUpdateCounter();
	Long getRowDeleteCounter();
	
	void resetStatistics();

	Long getQueryTimeMS();
}
