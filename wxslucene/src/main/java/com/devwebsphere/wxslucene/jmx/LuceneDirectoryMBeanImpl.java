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

/**
 * There is one of these MBeans for each Directory. Multiple instance of
 * GridDirectory for the same actual grid index share this one instance. All
 * per directory metrics etc are kept in this MBean. 
 * @author bnewport
 *
 */
public class LuceneDirectoryMBeanImpl implements LuceneDirectoryMBean 
{
	static Logger logger = Logger.getLogger(LuceneDirectoryMBeanImpl.class.getName());

	/**
	 * This property enables the local block cache if specified. It
	 * can be also specified per directory.
	 */
	static String OPT_BLOCK_CACHE_SIZE = "block_cache_size";
	/**
	 * This property compresses the local block cache if enabled
	 */
	static String OPT_COMPRESS_LOCAL_CACHE = "compress_block_cache";
	/**
	 * This turns on compression for blocks stored in the grid
	 */
	static String OPT_COMPRESSION = "compression";
	/**
	 * This batches writes to the grid for improved performance.
	 */
	static String OPT_ASYNC_PUT = "async_put";
	/**
	 * Each file is broken in a equal sized blocks and each block is stored
	 * in the grid using the file#blocknum key.
	 */
	static String OPT_BLOCK_SIZE = "block_size";
	/**
	 * This checks if the copy in the grid is identical to the original copy
	 */
	static String OPT_VERIFY_COPY = "verify_copy";
	/**
	 * This shows infrequent metrics as INFO messages.
	 */
	static String OPT_LOG_HIT_RATE = "log_hit_rate";
	/**
	 * This stores the keys as a byte[16] instead of a string.
	 */
	static String OPT_KEY_AS_DIGEST = "key_as_digest";
	static String OPT_PARTITION_MAX_BATCH_SIZE = "partition_max_batch_size";
	
	// this is usually globalBlockLRUCache unless the user
	// overrides the cache size for a specific directory
	volatile MTLRUCache<ByteArrayKey, byte[]> blockLRUCache;
	// the block cache to share between directories
	static MTLRUCache<ByteArrayKey, byte[]> globalBlockLRUCache;
	
	boolean isAsyncEnabled = false;
	boolean isCompressionEnabled = true;
	public final boolean isCacheCompressionEnabled() {
		return isCacheCompressionEnabled;
	}

	int blockSize = 4096;
	// This is how many puts we will batch PER partition
	int partitionMaxBatchSize = 20;
	
	int block_cache_size;
	
	boolean verifyCopyMode = false;
	
	boolean isKeyAsDigestEnabled = true;
	
	boolean isHitRateLoggingEnabled = false;
	
	boolean isCacheCompressionEnabled = false;
	
	/**
	 * This tracks the number of block cache hits for any file for this directory
	 */
	AtomicLong hitCounter = new AtomicLong();
	/**
	 * This tracks the number of block cache misses for any file for this directory
	 */
	AtomicLong missCounter = new AtomicLong();

	/**
	 * WE count the number of bytes thrown in to the compressor
	 */
	AtomicLong unCompressedTotal = new AtomicLong();
	/**
	 * and how many bytes come out of the compressor for calculating
	 * the ratio.
	 */
	AtomicLong compressedTotal = new AtomicLong();
	
	public void recordCompressionRatio(long bytesIn, long bytesOut)
	{
		unCompressedTotal.addAndGet(bytesIn);
		compressedTotal.addAndGet(bytesOut);
	}
	
	/**
	 * This tracks block hit and misses against the cache for this
	 * Directory only
	 * @param isHit true if recording a hit otherwise it's a miss
	 */
	public void recordBlockCacheHit(boolean isHit)
	{
		if(isHit)
		{
			long hits = hitCounter.incrementAndGet();
			if(isHitRateLoggingEnabled() && hits % 5000 == 0)
			{
				long misses = missCounter.get();
				double hr = MTLRUCache.calculateHitRate(hits, misses);
				int hrRounded = (int)(hr * 100);
				boolean showHR = false;
				// if its poor, show frequently
				if(hr < 0.5)
					showHR = true;
				else
					// otherwise, show infrequently
					if(hits % 20000 == 0)
						showHR = true;
				if(showHR)
				{
					int compRate = 0;
					if(unCompressedTotal.get() != 0)
					{
						compRate = (int)(100.0 *(double)compressedTotal.get() / (double)unCompressedTotal.get());
					}
					logger.log(Level.INFO, "Hit rate for " + directoryName + " Directory is " + hrRounded + "%, total reads " + (hits + misses) + ", Compress Rate:" + compRate + "%");
				}
			}
		}
		else
			missCounter.incrementAndGet();
	}
	
	
	public final boolean isHitRateLoggingEnabled() {
		return isHitRateLoggingEnabled;
	}

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
			blockLRUCache = new MTLRUCache<ByteArrayKey, byte[]>(directoryName, blockCacheSize, true);
		else
			blockLRUCache = null;
				
		logger.log(Level.INFO, "Block LRU Cache size for " + directoryName + " changed to " + blockCacheSize);
		logger.log(Level.INFO, "Hitrate logging enabled for " + directoryName);
	}

	/**
	 * The name of the grid for this directory.
	 */
	String gridName;
	/**
	 * The name of the directory stored here.
	 */
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
	
	String getSpecificProperty(Properties props, String propSuffix, String dflt)
	{
		// try per directory property first
		String value = props.getProperty(directoryName + "." + propSuffix, dflt);
		return value;
	}
	
	/**
	 * This parses properties in wxslucene.properties related
	 * to the remote grid configuration.
	 * @param props
	 */
	private void processRemoteGridProperties(Properties props)
	{
		// compression can be done per directory
		String value = getProperty(props, OPT_COMPRESSION, "true");
		setCompressionEnabled(value.equalsIgnoreCase("true"));
		value = props.getProperty(OPT_ASYNC_PUT, "true");
		setAsyncEnabled(value.equalsIgnoreCase("true"));
		
		// block_size can be done per directory
		value = getProperty(props, OPT_BLOCK_SIZE, "16384");
		setBlockSize(Integer.parseInt(value));
		value = props.getProperty(OPT_PARTITION_MAX_BATCH_SIZE, "20");
		setPartitionMaxBatchSize(Integer.parseInt(value));
		
		value = getProperty(props, OPT_KEY_AS_DIGEST, "");
		if(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))
		{
			isKeyAsDigestEnabled = value.equalsIgnoreCase("true");
			logger.log(Level.INFO, "Key as Digest Mode is " + value + " for directory " + directoryName);
		}
		
	}
	
	/**
	 * This parses wxslucene.properties for properties related
	 * to the local block cache.
	 * @param props
	 */
	private void processLocalCacheProperties(Properties props)
	{
		String value = getProperty(props, OPT_LOG_HIT_RATE, "");
		if(value.equalsIgnoreCase("true"))
			isHitRateLoggingEnabled = true;
		
		// global compress local cache setting
		value = props.getProperty(OPT_COMPRESS_LOCAL_CACHE, "");
		if(value.equalsIgnoreCase("true"))
			isCacheCompressionEnabled = true;
		
		// if a global local cache is enabled then create it ONCE
		value = props.getProperty(OPT_BLOCK_CACHE_SIZE, "");
		if(value.length() > 0)
		{
			synchronized(LuceneDirectoryMBeanImpl.class)
			{
				block_cache_size = Integer.parseInt(value);
				globalBlockLRUCache = new MTLRUCache<ByteArrayKey, byte[]>("Global Directory Cache", block_cache_size, isHitRateLoggingEnabled);
				logger.log(Level.INFO, "Global lru block cache set to " + block_cache_size + " blocks");
			}
		}
		
		// if a specific cache size is set for this directory then
		// can only specific specific cache compression IFF there is a specific block cache too
		value = getSpecificProperty(props, OPT_BLOCK_CACHE_SIZE, "");
		if(value.length() > 0)
		{
			block_cache_size = Integer.parseInt(value);
			blockLRUCache = new MTLRUCache<ByteArrayKey, byte[]>(directoryName, block_cache_size, isHitRateLoggingEnabled);
			logger.log(Level.INFO, "Local lru block cache set to " + block_cache_size + " blocks for directory " + directoryName);
			value = getSpecificProperty(props, OPT_COMPRESS_LOCAL_CACHE, "");
			if(value.equalsIgnoreCase("true"))
				isCacheCompressionEnabled = true;
		}
		else
			// otherwise reuse the global one
			blockLRUCache = globalBlockLRUCache;
		
		if(isCacheCompressionEnabled)
		{
			logger.log(Level.INFO, "Local cache compression enabled for " + directoryName);
		}
		
	}
	
	public LuceneDirectoryMBeanImpl(String grid, String name)
	{
		gridName = grid;
		directoryName = name;
		Properties props = new Properties();
		boolean useDefaults = true;
		try
		{
			String value;
			props.load(new FileInputStream(new File(GridDirectory.class.getResource("/wxslucene.properties").toURI())));

			processRemoteGridProperties(props);
			
			value = getProperty(props, OPT_VERIFY_COPY, "");
			if(value.equalsIgnoreCase("true"))
			{
				logger.log(Level.INFO, "Verify copy mode is true for directory " + name);
				verifyCopyMode = true;
			}

			processLocalCacheProperties(props);
			
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
			setBlockSize(16384);
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
