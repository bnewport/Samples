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

public interface LuceneDirectoryMBean {

	public Integer getBlock_cache_size();

	public void setBlock_cache_size(Integer blockCacheSize);

	public Integer getPartitionMaxBatchSize();

	public Integer getBlockSize();

	public Boolean isAsyncEnabled();

	public String getGridName();

	public String getDirectoryName();

	public Boolean isCompressionEnabled();

	public void reset();

	public Double getBlockHitRate();

	public Double getMDCacheHitRate();

	public Long getOpenInputCounter();
	
	public Long getOpenOutputCounter();
}