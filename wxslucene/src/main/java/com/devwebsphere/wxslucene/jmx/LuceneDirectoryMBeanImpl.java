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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxslucene.GridDirectory;
import com.devwebsphere.wxslucene.MTLRUCache;
import com.devwebsphere.wxssearch.ByteArrayKey;
import com.devwebsphere.wxsutils.jmx.SummaryMBeanImpl;
import com.devwebsphere.wxsutils.jmx.TabularAttribute;
import com.devwebsphere.wxsutils.jmx.TabularKey;

public class LuceneDirectoryMBeanImpl implements LuceneDirectoryMBean 
{
	static Logger logger = Logger.getLogger(LuceneDirectoryMBeanImpl.class.getName());
	
	volatile MTLRUCache<ByteArrayKey, byte[]> blockLRUCache;
	
	boolean isAsyncEnabled = false;
	boolean isCompressionEnabled = true;
	int blockSize = 4096;
	// This is how many puts we will batch PER partition
	int partitionMaxBatchSize = 20;
	
	int block_cache_size;
	
	boolean verifyCopyMode = false;
	
	boolean isKeyAsDigestEnabled = true;
	
	public final boolean isKeyAsDigestEnabled() {
		return isKeyAsDigestEnabled;
	}

	public final boolean isVerifyCopyMode() {
		return verifyCopyMode;
	}

	AtomicLong numOpenInputs = new AtomicLong();
	AtomicLong numOpenOutputs = new AtomicLong();
	
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxslucene.jmx.LuceneDirectoryMBean#getBlock_cache_size()
	 */
	public final Integer getBlock_cache_size() {
		return new Integer(block_cache_size);
	}

	/* (non-Javadoc)
	 * @see com.devwebsphere.wxslucene.jmx.LuceneDirectoryMBean#setBlock_cache_size(int)
	 */
	public final void setBlock_cache_size(Integer blockCacheSize) {
		block_cache_size = blockCacheSize;
		if(block_cache_size > 0)
			blockLRUCache = new MTLRUCache<ByteArrayKey, byte[]>(blockCacheSize);
		else
			blockLRUCache = null;
				
		logger.log(Level.INFO, "Block LRU Cache size changed to " + blockCacheSize);
	}

	String gridName;
	String directoryName;	

	/**
	 * This looks for a property in the file. The property can be specified as:
	 * directoryName.propName=XXXX
	 * 
	 * or
	 * 
	 * propName=XXXX
	 * 
	 * @param props
	 * @param propSuffix
	 * @param dflt
	 * @return
	 */
	String getProperty(Properties props, String propSuffix, String dflt)
	{
		// try per directory property first
		String value = props.getProperty(directoryName + "." + propSuffix, dflt);
		if(value.equals(dflt))
		{
			// otherwise get default property
			value = props.getProperty(propSuffix, dflt);
		}
		return value;
	}
	
	public LuceneDirectoryMBeanImpl(String grid, String name)
	{
		gridName = grid;
		directoryName = name;
		Properties props = new Properties();
		boolean useDefaults = true;
		try
		{
			props.load(new FileInputStream(new File(GridDirectory.class.getResource("/wxslucene.properties").toURI())));
			
			// compression can be done per directory
			String value = getProperty(props, "compression", "true");
			setCompressionEnabled(value.equalsIgnoreCase("true"));
			value = props.getProperty("async_put", "true");
			setAsyncEnabled(value.equalsIgnoreCase("true"));
			
			// block_size can be done per directory
			value = getProperty(props, "block_size", "16384");
			setBlockSize(Integer.parseInt(value));
			value = props.getProperty("partition_max_batch_size", "20");
			setPartitionMaxBatchSize(Integer.parseInt(value));

			value = getProperty(props, "verify_copy", "");
			if(value.equalsIgnoreCase("true"))
			{
				logger.log(Level.INFO, "Verify copy mode is true for directory " + name);
				verifyCopyMode = true;
			}
			
			value = getProperty(props, "key_as_digest", "");
			if(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))
			{
				isKeyAsDigestEnabled = value.equalsIgnoreCase("true");
				logger.log(Level.INFO, "Key as Digest Mode is " + value + " for directory " + name);
			}
			
			// block_cache can be done per directory
			value = getProperty(props, "block_cache_size", "");
			if(value.length() > 0)
			{
				block_cache_size = Integer.parseInt(value);
				blockLRUCache = new MTLRUCache<ByteArrayKey, byte[]>(block_cache_size);
				logger.log(Level.INFO, "Local lru block cache set to " + block_cache_size + " blocks for directory " + name);
			}
			useDefaults = false;
		}
		catch(FileNotFoundException e)
		{
			logger.log(Level.INFO, "wxslucene.properties not found, using defaults");
		}
		catch(NumberFormatException e)
		{
			logger.log(Level.WARNING, "wxslucene.properties number format exception on property");
		}
		catch(Exception e)
		{
			logger.log(Level.WARNING, "Exception reading wxslucene.properties", e);
		}
		if(useDefaults)
		{
			// turn on compression by default
			setCompressionEnabled(true);
			// turn on async put by default
			setAsyncEnabled(true);
			setBlockSize(4096);
		}
	}

	/* (non-Javadoc)
	 * @see com.devwebsphere.wxslucene.jmx.LuceneDirectoryMBean#getPartitionMaxBatchSize()
	 */
	@TabularAttribute
	public final Integer getPartitionMaxBatchSize() {
		return new Integer(partitionMaxBatchSize);
	}

	/**
	 * When async puts are enabled then this specifies how many individual put operations for
	 * a single partition to batch together at a maximum.
	 * @param partitionMaxBatchSize
	 */
	public final void setPartitionMaxBatchSize(int partitionMaxBatchSize) {
		this.partitionMaxBatchSize = partitionMaxBatchSize;
		logger.log(Level.INFO, "Partition Max Batch Size  = " + partitionMaxBatchSize + " for directory " + directoryName);
	}

	public void incrementOpenInput()
	{
		numOpenInputs.incrementAndGet();
	}
	
	public void incrementOpenOutput()
	{
		numOpenOutputs.incrementAndGet();
	}
	
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxslucene.jmx.LuceneDirectoryMBean#getBlockSize()
	 */
	@TabularAttribute
	public final Integer getBlockSize() {
		return new Integer(blockSize);
	}

	/**
	 * This specifies the block size used for storing files in the directory. 4k looks a common number
	 * from researching.
	 * @param blockSize
	 */
	public final void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
		logger.log(Level.INFO, "Block Size  = " + blockSize + " for directory " + directoryName);
	}

	/* (non-Javadoc)
	 * @see com.devwebsphere.wxslucene.jmx.LuceneDirectoryMBean#isAsyncEnabled()
	 */
	@TabularAttribute
	public final Boolean isAsyncEnabled() {
		return new Boolean(isAsyncEnabled);
	}

	/**
	 * This greatly accelerates writing files to the grid as it parallelizes puts and batches them. Normally
	 * each flush results in an individual put. These puts are buffered and only written on an explicit flush or
	 * close in this mode. They are also written if the number of buffers puts reaches the #partitions times
	 * the PartitionMaxBatchSize
	 * @param isAsyncEnabled
	 */
	public final void setAsyncEnabled(Boolean isAsyncEnabled) {
		this.isAsyncEnabled = isAsyncEnabled;
		logger.log(Level.INFO, "Async enabled = " + isAsyncEnabled + " for directory " + directoryName);
	}

	/* (non-Javadoc)
	 * @see com.devwebsphere.wxslucene.jmx.LuceneDirectoryMBean#getGridName()
	 */
	@TabularKey
	public String getGridName()
	{
		return gridName;
	}
	
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxslucene.jmx.LuceneDirectoryMBean#getDirectoryName()
	 */
	@TabularKey
	public String getDirectoryName()
	{
		return directoryName;
	}
	
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxslucene.jmx.LuceneDirectoryMBean#isCompressionEnabled()
	 */
	@TabularAttribute
	public final Boolean isCompressionEnabled() {
		return new Boolean(isCompressionEnabled);
	}

	public final void setCompressionEnabled(Boolean isCompressionEnabled) {
		this.isCompressionEnabled = isCompressionEnabled;
		logger.log(Level.INFO, "Compression enabled = " + isCompressionEnabled + " for directory " + directoryName);
	}
	
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxslucene.jmx.LuceneDirectoryMBean#reset()
	 */
	public void reset()
	{
	}
	
	/* (non-Javadoc)
	 * @see com.devwebsphere.wxslucene.jmx.LuceneDirectoryMBean#getBlockHitRate()
	 */
	@TabularAttribute(mbean=SummaryMBeanImpl.MONITOR_MBEAN)
	public Double getBlockHitRate()
	{
		if(blockLRUCache != null)
			return blockLRUCache.getHitRate();
		else
			return 0.0;
	}
	
	public final MTLRUCache<ByteArrayKey, byte[]> getBlockLRUCache() {
		return blockLRUCache;
	}

	@TabularAttribute
	public Long getOpenInputCounter() 
	{
		return new Long(numOpenInputs.get());
	}

	@TabularAttribute
	public Long getOpenOutputCounter() 
	{
		return new Long(numOpenOutputs.get());
	}
}
