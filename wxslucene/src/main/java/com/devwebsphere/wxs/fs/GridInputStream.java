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
package com.devwebsphere.wxs.fs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxslucene.GridDirectory;
import com.devwebsphere.wxslucene.MTLRUCache;
import com.devwebsphere.wxslucene.jmx.LuceneFileMBeanImpl;
import com.devwebsphere.wxsutils.WXSMap;
import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.jmx.MinMaxAvgMetric;

public class GridInputStream 
{
	static Logger logger = Logger.getLogger(GridInputStream.class.getName());
	String fileName;
	WXSUtils utils;
	WXSMap<String, byte[]> streamMap;
	WXSMap<String, FileMetaData> mdMap;
	
	ThreadLocalInputStreamState tlsState = new ThreadLocalInputStreamState();
	
	FileMetaData md;
	int blockSize;
	LuceneFileMBeanImpl mbean;
	GridDirectory parentDirectory;
	
	public FileMetaData getMetaData()
	{
		return md;
	}
	
	public String toString()
	{
		GridInputStreamState state = tlsState.get();
		return "GridInputStream(" + fileName + " pos = " + state.currentAbsolutePosition + " max= " + md.getActualSize();
	}
	
	public GridInputStream(WXSUtils utils, GridFile file) throws FileNotFoundException, IOException 
	{
		this.parentDirectory = file.getParent();
		mbean = GridDirectory.getLuceneFileMBeanManager().getBean(file.getParent().getName(), file.getName());
		this.utils = utils;
		streamMap = utils.getCache(MapNames.CHUNK_MAP_PREFIX + file.getParent().getName());
		fileName= file.getName();
		mdMap = utils.getCache(MapNames.MD_MAP_PREFIX + file.getParent().getName());
		md = mdMap.get(fileName);
		blockSize = file.getParent().getBlockSize();
		if(md == null)
		{
			throw new FileNotFoundException(fileName);
		}
		if(logger.isLoggable(Level.FINE))
		{
			logger.log(Level.FINE, "Opening stream for " + fileName + " with " + md.getActualSize() + " bytes");
		}
	}

	static String generateKey(String fileName, long bucket)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(fileName);
		sb.append("#");
		sb.append(Long.toString(bucket));
		return sb.toString();
	}
	
	boolean areBytesAvailable()
		throws IOException
	{
		GridInputStreamState state = tlsState.get();
		if(state.currentAbsolutePosition == md.getActualSize())
		{
//			if(logger.isLoggable(Level.FINEST))
//			{
//				logger.log(Level.FINEST, this.toString() + ":areBytesAvailable=FALSE");
//			}
			return false;
		}
		if(state.currentValue == null || state.currentPosition == state.currentValue.length)
		{
			// if not first block in file then advance otherwise fetch first block
			if(state.currentAbsolutePosition > 0)
				state.currentBucket++;
			state.currentValue = getBlock(state.currentBucket);
			if(state.currentValue == null)
			{
				state.currentValue = new byte[blockSize];
			}
			state.currentPosition = 0;
		}
//		if(logger.isLoggable(Level.FINEST))
//		{
//			logger.log(Level.FINEST, this.toString() + ":areBytesAvailable=TRUE");
//		}
		return true;
	}
	
	public int read() throws IOException 
	{
		GridInputStreamState state = tlsState.get();
		int rc = -1;
		if(areBytesAvailable())
		{
			rc = state.currentValue[state.currentPosition++];
			++state.currentAbsolutePosition;
			mbean.getReadBytesMetric().logTime(1);
		}
		if(logger.isLoggable(Level.FINER))
		{
			logger.log(Level.FINER, this.toString() + ":read/0=" + rc);
		}
		return rc;
	}

	public int read(byte[] b) throws IOException
	{
		mbean.getReadBytesMetric().logTime(b.length);
		long now = System.nanoTime();
		if(logger.isLoggable(Level.FINER))
		{
			logger.log(Level.FINER, this.toString() + ":read/1");
		}
		int rc = privateRead(b);
		mbean.getReadTimeMetric().logTime(System.nanoTime() - now);
		return rc;
	}
	
	public int privateRead(byte[] b) throws IOException 
	{
		GridInputStreamState state = tlsState.get();
		if(!areBytesAvailable())
			return -1;
		
		int toGo = b.length;
		int offset = 0;
		
		while(areBytesAvailable() && toGo > 0)
		{
			int bytesAvailable = state.currentValue.length - state.currentPosition;
			if(bytesAvailable < toGo)
			{
				System.arraycopy(state.currentValue, state.currentPosition, b, offset, bytesAvailable);
				offset += bytesAvailable;
				toGo -= bytesAvailable;
				state.currentPosition += bytesAvailable;
				state.currentAbsolutePosition += bytesAvailable;
			}
			else
			{
				System.arraycopy(state.currentValue, state.currentPosition, b, offset, toGo);
				offset += toGo;
				state.currentPosition += toGo;
				state.currentAbsolutePosition += toGo;
				toGo = 0;
			}
		}
		return offset;
	}

	public int read(byte[] b, int off, int len) throws IOException 
	{
		mbean.getReadBytesMetric().logTime(len);
		long now = System.nanoTime();
		if(logger.isLoggable(Level.FINER))
		{
			logger.log(Level.FINER, this.toString() + ":read/3");
		}
		int maxToRead = Math.min(b.length - off, len);
		byte[] buffer = new byte[maxToRead];
		int bytesRead = privateRead(buffer);
		if(bytesRead > 0)
			System.arraycopy(buffer, 0, b, off, bytesRead);
		mbean.getReadTimeMetric().logTime(System.nanoTime() - now);
		return bytesRead;
	}

	public long skip(long n) throws IOException {
		GridInputStreamState state = tlsState.get();
		if(logger.isLoggable(Level.FINER))
		{
			logger.log(Level.FINER, this.toString() + ":skip to " + n);
		}
		if(state.currentAbsolutePosition == md.getActualSize())
			return 0;
		long newPosition = state.currentAbsolutePosition + n;
		if(newPosition >= md.getActualSize())
		{
			newPosition = md.getActualSize();
		}
		long skipAmount = newPosition - state.currentAbsolutePosition;
		
		int newBucket = (int)(newPosition / blockSize);
		if(newBucket != state.currentBucket)
		{
			state.currentBucket = newBucket;
			state.currentValue = getBlock(state.currentBucket);
			if(state.currentValue == null)
				state.currentValue = new byte[blockSize];
		}
		state.currentPosition = (int)(newPosition % blockSize);
		state.currentAbsolutePosition = newPosition;
		
		return skipAmount;
	}

	long lastBlock = -1;
	int currentRun = 1;
	private byte[] getBlock(long blockNum)
		throws IOException
	{
		if(blockNum == lastBlock + 1)
			currentRun++;
		else
		{
			if(currentRun != 1)
			{
				MinMaxAvgMetric metric = mbean.getReadSequentialMetric();
				metric.logTime(currentRun);
				if(logger.isLoggable(Level.FINE))
				{
					logger.log(Level.FINE, "<Min: " + metric.getMinTimeNS() + ", Max:" + metric.getMaxTimeNS() + ", Avg: " + metric.getAvgTimeNS() + ">" + " from " + fileName);
				}
				currentRun = 1;
			}
		}
		lastBlock = blockNum;
		String blockKey = generateKey(fileName, blockNum);
		
		// maintain an LRU cache of uncompressed blocks
		byte[] data = null;
		MTLRUCache<String, byte[]> cache = parentDirectory.getLRUBlockCache();
		if(cache != null)
			data = cache.get(blockKey);
		// if found in cache then nothing to do
		if(data == null)
		{
			// try fetch from grid
			data = streamMap.get(blockKey);
			// decompress if needed
			data = GridOutputStream.unZip(blockSize, md, data);
			// update LRU cache if enabled and found
			if(data != null && cache != null)
				cache.put(blockKey, data);
		}
		return data;
	}
	
	public void close() throws IOException {
		if(logger.isLoggable(Level.FINER))
		{
			logger.log(Level.FINER, this.toString() + ":close");
		}
	}

	public void seek(long n)
		throws IOException
	{
		GridInputStreamState state = tlsState.get();
		if(logger.isLoggable(Level.FINE))
		{
			logger.log(Level.FINE, this.toString() + ":seek to " + n);
		}
		n = Math.min(n, md.getActualSize());
		state.currentAbsolutePosition = 0;
		skip(n);
	}
	
	public long getAbsolutePosition()
	{
		GridInputStreamState state = tlsState.get();
		return state.currentAbsolutePosition;
	}
}
