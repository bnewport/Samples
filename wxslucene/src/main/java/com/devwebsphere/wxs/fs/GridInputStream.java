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
import com.devwebsphere.wxslucene.LRUCache;
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
	long currentBucket;
	long currentAbsolutePosition;
	int currentPosition;
	byte[] currentValue;
	FileMetaData md;
	int blockSize;
	LuceneFileMBeanImpl mbean;
	LRUCache<String, byte[]> parentBlockCache;
	
	public FileMetaData getMetaData()
	{
		return md;
	}
	
	public String toString()
	{
		return "GridInputStream(" + fileName + " pos = " + currentAbsolutePosition + " max= " + md.getActualSize();
	}
	
	public GridInputStream(WXSUtils utils, GridFile file) throws FileNotFoundException, IOException 
	{
		mbean = GridDirectory.getLuceneFileMBeanManager().getBean(file.getParent().getName(), file.getName());
		parentBlockCache = file.getParent().getLRUBlockCache();
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
		currentAbsolutePosition = 0;
		currentBucket = 0;
		currentValue = getBlock(currentBucket);
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
		if(currentAbsolutePosition == md.getActualSize())
		{
//			if(logger.isLoggable(Level.FINEST))
//			{
//				logger.log(Level.FINEST, this.toString() + ":areBytesAvailable=FALSE");
//			}
			return false;
		}
		if(currentValue == null || currentPosition == currentValue.length)
		{
			currentBucket++;
			currentValue = getBlock(currentBucket);
			if(currentValue == null)
			{
				currentValue = new byte[blockSize];
			}
			currentPosition = 0;
		}
//		if(logger.isLoggable(Level.FINEST))
//		{
//			logger.log(Level.FINEST, this.toString() + ":areBytesAvailable=TRUE");
//		}
		return true;
	}
	
	public int read() throws IOException 
	{
		int rc = -1;
		if(areBytesAvailable())
		{
			rc = currentValue[currentPosition++];
			++currentAbsolutePosition;
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
		if(!areBytesAvailable())
			return -1;
		
		int toGo = b.length;
		int offset = 0;
		
		while(areBytesAvailable() && toGo > 0)
		{
			int bytesAvailable = currentValue.length - currentPosition;
			if(bytesAvailable < toGo)
			{
				System.arraycopy(currentValue, currentPosition, b, offset, bytesAvailable);
				offset += bytesAvailable;
				toGo -= bytesAvailable;
				currentPosition += bytesAvailable;
				currentAbsolutePosition += bytesAvailable;
			}
			else
			{
				System.arraycopy(currentValue, currentPosition, b, offset, toGo);
				offset += toGo;
				currentPosition += toGo;
				currentAbsolutePosition += toGo;
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
		if(logger.isLoggable(Level.FINER))
		{
			logger.log(Level.FINER, this.toString() + ":skip to " + n);
		}
		if(currentAbsolutePosition == md.getActualSize())
			return 0;
		long newPosition = currentAbsolutePosition + n;
		if(newPosition >= md.getActualSize())
		{
			newPosition = md.getActualSize();
		}
		long skipAmount = newPosition - currentAbsolutePosition;
		
		int newBucket = (int)(newPosition / blockSize);
		if(newBucket != currentBucket)
		{
			currentBucket = newBucket;
			currentValue = getBlock(currentBucket);
			if(currentValue == null)
				currentValue = new byte[blockSize];
		}
		currentPosition = (int)(newPosition % blockSize);
		currentAbsolutePosition = newPosition;
		
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
		if(parentBlockCache != null)
			data = parentBlockCache.get(blockKey);
		// if found in cache then nothing to do
		if(data == null)
		{
			// try fetch from grid
			data = streamMap.get(blockKey);
			// decompress if needed
			data = GridOutputStream.unZip(blockSize, md, data);
			// update LRU cache if enabled and found
			if(data != null && parentBlockCache != null)
				parentBlockCache.put(blockKey, data);
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
		if(logger.isLoggable(Level.FINE))
		{
			logger.log(Level.FINE, this.toString() + ":seek to " + n);
		}
		n = Math.min(n, md.getActualSize());
		currentAbsolutePosition = 0;
		skip(n);
	}
	
	public long getAbsolutePosition()
	{
		return currentAbsolutePosition;
	}
}
