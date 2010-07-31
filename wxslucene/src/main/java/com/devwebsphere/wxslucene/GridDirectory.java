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
package com.devwebsphere.wxslucene;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.NIOFSDirectory;

import com.devwebsphere.wxs.fs.GridFile;
import com.devwebsphere.wxs.fs.GridInputStream;
import com.devwebsphere.wxs.fs.GridOutputStream;
import com.devwebsphere.wxs.fs.MapNames;
import com.devwebsphere.wxslucene.jmx.LuceneFileMBeanManager;
import com.devwebsphere.wxsutils.LazyMBeanManagerAtomicReference;
import com.devwebsphere.wxsutils.WXSMap;
import com.devwebsphere.wxsutils.WXSUtils;

/**
 * This is a Lucene Directory implementation to store the directory and its files in an IBM WebSphere
 * eXtreme Scale grid. The class can use an optional property file to configure it which must
 * be called wxslucene.properties and must be in the root class path. This property file allows
 * the performance of the Directory to be tuned.
 * 
 * These are the properties that can be in the file
 * compression=false
 * async_put=true
 * block_size=4096
 * partition_max_batch_size=40
 * 
 * @author bnewport
 *
 */
public class GridDirectory extends Directory 
{
	static Logger logger = Logger.getLogger(GridDirectory.class.getName());
	
	static LazyMBeanManagerAtomicReference<LuceneFileMBeanManager> luceneFileMBeanManager = new LazyMBeanManagerAtomicReference<LuceneFileMBeanManager>(LuceneFileMBeanManager.class);
	
	WXSUtils client;
	WXSMap<String, Set<String>> dirMap;
	String name;
	boolean isAsyncEnabled;
	boolean isCompressionEnabled = true;
	int blockSize = 4096;
	// This is how many puts we will batch PER partition
	int partitionMaxBatchSize = 20;
	
	MTLRUCache<String, byte[]> blockCache;

	public static LuceneFileMBeanManager getLuceneFileMBeanManager()
	{
		return luceneFileMBeanManager.getLazyRef();
	}
	
	public final int getPartitionMaxBatchSize() {
		return partitionMaxBatchSize;
	}

	/**
	 * When async puts are enabled then this specifies how many individual put operations for
	 * a single partition to batch together at a maximum.
	 * @param partitionMaxBatchSize
	 */
	public final void setPartitionMaxBatchSize(int partitionMaxBatchSize) {
		this.partitionMaxBatchSize = partitionMaxBatchSize;
		logger.log(Level.INFO, "Partition Max Batch Size  = " + partitionMaxBatchSize + " for directory " + name);
	}

	public final int getBlockSize() {
		return blockSize;
	}

	/**
	 * This specifies the block size used for storing files in the directory. 4k looks a common number
	 * from researching.
	 * @param blockSize
	 */
	public final void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
		logger.log(Level.INFO, "Block Size  = " + blockSize + " for directory " + name);
	}

	public final boolean isAsyncEnabled() {
		return isAsyncEnabled;
	}

	/**
	 * This greatly accelerates writing files to the grid as it parallelizes puts and batches them. Normally
	 * each flush results in an individual put. These puts are buffered and only written on an explicit flush or
	 * close in this mode. They are also written if the number of buffers puts reaches the #partitions times
	 * the PartitionMaxBatchSize
	 * @param isAsyncEnabled
	 */
	public final void setAsyncEnabled(boolean isAsyncEnabled) {
		this.isAsyncEnabled = isAsyncEnabled;
		logger.log(Level.INFO, "Async enabled = " + isAsyncEnabled + " for directory " + name);
	}

	public String getName()
	{
		return name;
	}
	
	public WXSUtils getWXSUtils()
	{
		return client;
	}
	
	/**
	 * This constructor obtains a grid connection using the wxsutils.properties file
	 * loads a File based directory at the specified path and then copies it in to
	 * the grid using a directory named after the file directory path.
	 * @param fileDirectoryName The location of the file index to copy.
	 * @see WXSUtils#getDefaultUtils()
	 */
	public GridDirectory(String fileDirectoryName)
	{
		try
		{
			WXSUtils c = WXSUtils.getDefaultUtils();
			init(c, fileDirectoryName);
			Directory diskDir = NIOFSDirectory.getDirectory(fileDirectoryName);
			Directory.copy(diskDir, this, false);
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception creating GridDirectory from file ", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * This creates a named Grid Directory thats stored in the grid represented by
	 * the client parameter
	 * @param client
	 * @param directoryName
	 */
	public GridDirectory(WXSUtils client, String directoryName)
	{
		if(logger.isLoggable(Level.INFO))
		{
			logger.log(Level.INFO, "Creating GridDirectory: " + directoryName);
		}
		init(client, directoryName);
	}

	public MTLRUCache<String, byte[]> getLRUBlockCache()
	{
		return blockCache;
	}
	
	private void init(WXSUtils client, String directoryName)
	{
		this.client = client;
		dirMap = client.getCache(MapNames.DIR_MAP);
		name = directoryName;
		setLockFactory(new WXSLockFactory(client));
		getLockFactory().setLockPrefix(directoryName);
		
		Properties props = new Properties();
		boolean useDefaults = true;
		try
		{
			props.load(new FileInputStream(new File(GridDirectory.class.getResource("/wxslucene.properties").toURI())));
			String value = props.getProperty("compression", "true");
			setCompressionEnabled(value.equalsIgnoreCase("true"));
			value = props.getProperty("async_put", "true");
			setAsyncEnabled(value.equalsIgnoreCase("true"));
			value = props.getProperty("block_size", "4096");
			setBlockSize(Integer.parseInt(value));
			value = props.getProperty("partition_max_batch_size", "20");
			setPartitionMaxBatchSize(Integer.parseInt(value));
			useDefaults = false;
			value = props.getProperty("block_cache_size", "");
			if(value.length() > 0)
			{
				int size = Integer.parseInt(value);
				blockCache = new MTLRUCache<String, byte[]>(size);
				logger.log(Level.INFO, "Local lru block cache set to " + size + " blocks for directory " + name);
			}
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
	
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public IndexOutput createOutput(String pathname) throws IOException 
	{
		if(logger.isLoggable(Level.FINE))
		{
			logger.log(Level.FINE, "Creating IndexOutput: " + pathname + " in directory " + name);
		}
		Set<String> files = getFiles();
		if(!files.contains(pathname))
		{
			if(logger.isLoggable(Level.FINE))
				logger.log(Level.FINE, "Adding " + pathname + " to directory " + name);
			files.add(pathname);
			dirMap.put(name, files);
		}
		GridFile file = new GridFile(this, pathname);
		GridOutputStream os = new GridOutputStream(client, file);
		if(isAsyncEnabled)
			os.enableAsyncWrite();
		return new GridIndexOutput(os);
	}

	@Override
	public void deleteFile(String pathname) throws IOException 
	{
		if(logger.isLoggable(Level.INFO))
		{
			logger.log(Level.INFO, "GridDirectory#delete: " + pathname);
		}
		Set<String> files = getFiles();
		if(files.contains(pathname))
		{
			files.remove(pathname);
			GridFile file = new GridFile(this, pathname);
			file.delete();
			dirMap.put(name, files);
		}
		else
		{
			logger.log(Level.SEVERE, "Attempt to delete unknown file " + pathname + " from directory " + name);
		}
	}

	@Override
	public boolean fileExists(String pathname) throws IOException 
	{
		GridFile file = new GridFile(this, pathname);
		return file.exists();
	}

	@Override
	public long fileLength(String pathname) throws IOException 
	{
		GridFile file = new GridFile(this, pathname);
		return file.length();
	}

	@Override
	public long fileModified(String pathname) throws IOException {
		GridFile file = new GridFile(this, pathname);
		return file.lastModified();
	}

	Set<String> getFiles()
	{
		Set<String> rc = dirMap.get(name);
		if(rc == null)
		{
			rc = new HashSet<String>();
		}
		return rc;
	}
	
	@Override
	public String[] list() throws IOException 
	{
		if(logger.isLoggable(Level.FINE))
		{
			logger.log(Level.FINE, "GridDirectory#list: " + name);
		}
		Set<String> files = getFiles();

		String[] rc = new String[files.size()];
		files.toArray(rc);
		if(logger.isLoggable(Level.FINER))
			logger.log(Level.FINER, "GridDirectory#list: (" + name +")" + " = " + files.toString());
		return rc;
	}

	@Override
	public IndexInput openInput(String pathname) throws IOException 
	{
		if(logger.isLoggable(Level.FINE))
		{
			logger.log(Level.FINE, "GridDirectory#openInput: " + pathname);
		}
		GridFile file = new GridFile(this, pathname);
		GridInputStream is = new GridInputStream(client, file);
		return new GridIndexInput(is);
	}

	@Override
	public void renameFile(String arg0, String arg1) throws IOException 
	{
		throw new RuntimeException("rename not implemented");
	}

	@Override
	public void touchFile(String pathname) throws IOException 
	{
		GridFile file = new GridFile(this, pathname);
		file.setLastModified(System.currentTimeMillis());
	}

	public final boolean isCompressionEnabled() {
		return isCompressionEnabled;
	}

	public final void setCompressionEnabled(boolean isCompressionEnabled) {
		this.isCompressionEnabled = isCompressionEnabled;
		logger.log(Level.INFO, "Compression enabled = " + isCompressionEnabled + " for directory " + name);
	}

}
