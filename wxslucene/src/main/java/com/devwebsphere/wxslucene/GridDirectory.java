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

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.index.IndexFileNameFilter;
import org.apache.lucene.store.ChecksumIndexInput;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.NIOFSDirectory;

import com.devwebsphere.wxs.fs.GridFile;
import com.devwebsphere.wxs.fs.GridInputStream;
import com.devwebsphere.wxs.fs.GridOutputStream;
import com.devwebsphere.wxs.fs.MapNames;
import com.devwebsphere.wxslucene.jmx.LuceneDirectoryMBeanImpl;
import com.devwebsphere.wxslucene.jmx.LuceneDirectoryMBeanManager;
import com.devwebsphere.wxslucene.jmx.LuceneFileMBeanManager;
import com.devwebsphere.wxssearch.ByteArrayKey;
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
	static LazyMBeanManagerAtomicReference<LuceneDirectoryMBeanManager> luceneDirectoryMBeanManager = new LazyMBeanManagerAtomicReference<LuceneDirectoryMBeanManager>(LuceneDirectoryMBeanManager.class);
	
	WXSUtils client;
	WXSMap<String, Set<String>> dirMap;
	String name;
	boolean verifyCopy = false;
	
	
	LuceneDirectoryMBeanImpl mbean;

	/**
	 * This tracks block hit and misses against the cache for this
	 * Directory only
	 * @param isHit true if recording a hit otherwise it's a miss
	 */
	public void recordBlockCacheHit(boolean isHit)
	{
		mbean.recordBlockCacheHit(isHit);
	}
	
	public boolean isCacheCompressionEnabled()
	{
		return mbean.isCacheCompressionEnabled();
	}
	
	public final LuceneDirectoryMBeanImpl getMbean() {
		return mbean;
	}

	public static LuceneFileMBeanManager getLuceneFileMBeanManager()
	{
		return luceneFileMBeanManager.getLazyRef();
	}
	
	public final int getPartitionMaxBatchSize() {
		return mbean.getPartitionMaxBatchSize();
	}

	/**
	 * When async puts are enabled then this specifies how many individual put operations for
	 * a single partition to batch together at a maximum.
	 * @param partitionMaxBatchSize
	 */
	public final void setPartitionMaxBatchSize(int partitionMaxBatchSize) {
		mbean.setPartitionMaxBatchSize(partitionMaxBatchSize);
	}

	public final int getBlockSize() {
		return mbean.getBlockSize();
	}

	/**
	 * This specifies the block size used for storing files in the directory. 4k looks a common number
	 * from researching.
	 * @param blockSize
	 */
	public final void setBlockSize(int blockSize) 
	{
		mbean.setBlockSize(blockSize);
	}

	public final boolean isAsyncEnabled() {
		return mbean.isAsyncEnabled();
	}

	/**
	 * This greatly accelerates writing files to the grid as it parallelizes puts and batches them. Normally
	 * each flush results in an individual put. These puts are buffered and only written on an explicit flush or
	 * close in this mode. They are also written if the number of buffers puts reaches the #partitions times
	 * the PartitionMaxBatchSize
	 * @param isAsyncEnabled
	 */
	public final void setAsyncEnabled(boolean isAsyncEnabled) 
	{
		mbean.setAsyncEnabled(isAsyncEnabled);
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
	 * the grid using a directory named after the file directory path. Once an index is
	 * loaded then the ClientGridDirectory class can be used to attach to the
	 * grid from that point onwards.
	 * @param fileDirectoryName The location of the file index to copy.
	 * @see WXSUtils#getDefaultUtils()
	 * @see ClientGridDirectory
	 */
	public GridDirectory(String fileDirectoryName)
	{
		try
		{
			WXSUtils c = WXSUtils.getDefaultUtils();
			init(c, fileDirectoryName);
			Directory diskDir = NIOFSDirectory.getDirectory(fileDirectoryName);
			if(mbean.isVerifyCopyMode())
				GridDirectory.copy(diskDir, this, true);
			else
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

	public MTLRUCache<ByteArrayKey, byte[]> getLRUBlockCache()
	{
		return mbean.getBlockLRUCache();
	}
	
	private void init(WXSUtils client, String directoryName)
	{
		this.client = client;
		mbean = luceneDirectoryMBeanManager.getLazyRef().getBean(client.getObjectGrid().getName(), directoryName);
		dirMap = client.getCache(MapNames.DIR_MAP);
		name = directoryName;
		setLockFactory(new WXSLockFactory(client));
		getLockFactory().setLockPrefix(directoryName);
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
		if(mbean.isAsyncEnabled())
			os.enableAsyncWrite();
		mbean.incrementOpenOutput();
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
		mbean.incrementOpenInput();
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
		return mbean.isCompressionEnabled();
	}

	public final void setCompressionEnabled(boolean isCompressionEnabled) 
	{
		mbean.setCompressionEnabled(isCompressionEnabled);
	}
	
	public String toString()
	{
		return "GridDirectory:<" + getName() + ">";
	}
	
	static final int COPY_BUFFER_SIZE = 16384;
	  
	public static void copy(Directory src, GridDirectory dest, boolean closeDirSrc) throws IOException 
	{
		final String[] files = src.listAll();
	    IndexFileNameFilter filter = IndexFileNameFilter.getFilter();

	    byte[] buf = new byte[COPY_BUFFER_SIZE];
	    for (int i = 0; i < files.length; i++) {

	      if (!filter.accept(null, files[i]))
	        continue;

	      IndexOutput os = null;
	      ChecksumIndexInput is = null;
	      try {
	        // create file in dest directory
	        os = dest.createOutput(files[i]);
	        // read current file
	        is = new ChecksumIndexInput(src.openInput(files[i]));
	        // and copy to dest directory
	        long len = is.length();
	        long readCount = 0;
	        while (readCount < len) {
	          int toRead = readCount + COPY_BUFFER_SIZE > len ? (int)(len - readCount) : COPY_BUFFER_SIZE;
	          is.readBytes(buf, 0, toRead);
	          os.writeBytes(buf, toRead);
	          readCount += toRead;
	        }
	        long src_sum = is.getChecksum();
	        os.flush();

	        // this code can just compare the new file with the old one
	        // to make sure it's copied correctly
	        ChecksumIndexInput dst_check_stream = new ChecksumIndexInput(dest.openInput(files[i]));

	        len = dst_check_stream.length();
	        readCount = 0;
	        while(readCount < len) {
	            int toRead = readCount + COPY_BUFFER_SIZE > len ? (int)(len - readCount) : COPY_BUFFER_SIZE;
	            dst_check_stream.readBytes(buf, 0, toRead);
	            readCount += toRead;
	        }
	        long dst_sum = dst_check_stream.getChecksum();
	        if(dst_sum == src_sum)
	        {
	        	logger.log(Level.INFO, "Verify " + files[i] + " was successful");
	        }
	        else
	        {
	        	logger.log(Level.INFO, "Verify " + files[i] + " failed");
	        	throw new IllegalStateException("File " + files[i] + " failed verification");
	        }
	      } finally {
	        // graceful cleanup
	        try {
	          if (os != null)
	            os.close();
	        } finally {
	          if (is != null)
	            is.close();
	        }
	      }
	    }
	    if(closeDirSrc)
	    	src.close();
	}
}
