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

/**
 * There is one MBean per JVM for each directory used.
 * @author bnewport
 *
 */
public interface LuceneDirectoryMBean {

	/**
	 * This is the size of the current local block cache or 0.
	 * @return
	 */
	public Integer getBlock_cache_size();

	/**
	 * This allows the local block cache to be specified for
	 * a directory. It can be turned off with a value of 0. Setting
	 * the size clears the current cache.
	 * @param blockCacheSize
	 */
	public void setBlock_cache_size(Integer blockCacheSize);

	public Integer getPartitionMaxBatchSize();

	public Integer getBlockSize();

	public Boolean isAsyncEnabled();

	public String getGridName();

	public String getDirectoryName();

	public Boolean isCompressionEnabled();

	public void reset();

	/**
	 * Returns the local cache hit rate for this directory. This
	 * works whether it's a global or directory specific block cache.
	 * @return
	 */
	public Double getBlockHitRate();

	/**
	 * Counts how many OpenInputs have been done.
	 * @return
	 */
	public Long getOpenInputCounter();

	/**
	 * Counts how many OpenOutputs have been done.
	 * @return
	 */
	public Long getOpenOutputCounter();
}