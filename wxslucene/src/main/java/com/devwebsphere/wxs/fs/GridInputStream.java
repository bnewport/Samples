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

import com.devwebsphere.wxsutils.WXSMap;
import com.devwebsphere.wxsutils.WXSUtils;

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
		this.utils = utils;
		streamMap = utils.getCache(MapNames.CHUNK_MAP);
		fileName= file.getName();
		mdMap = utils.getCache(MapNames.MD_MAP);
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
		currentValue = GridOutputStream.unZip(blockSize, md, streamMap.get(generateKey(fileName, currentBucket)));
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
			currentValue = GridOutputStream.unZip(blockSize, md, streamMap.get(generateKey(fileName, currentBucket)));
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
		}
		if(logger.isLoggable(Level.FINER))
		{
			logger.log(Level.FINER, this.toString() + ":read/0=" + rc);
		}
		return rc;
	}

	public int read(byte[] b) throws IOException 
	{
		if(logger.isLoggable(Level.FINER))
		{
			logger.log(Level.FINER, this.toString() + ":read/1");
		}
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
		if(logger.isLoggable(Level.FINER))
		{
			logger.log(Level.FINER, this.toString() + ":read/3");
		}
		int maxToRead = Math.min(b.length - off, len);
		byte[] buffer = new byte[maxToRead];
		int bytesRead = read(buffer);
		if(bytesRead > 0)
			System.arraycopy(buffer, 0, b, off, bytesRead);
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
			currentValue = GridOutputStream.unZip(blockSize, md, streamMap.get(generateKey(fileName, currentBucket)));
			if(currentValue == null)
				currentValue = new byte[blockSize];
		}
		currentPosition = (int)(newPosition % blockSize);
		currentAbsolutePosition = newPosition;
		
		return skipAmount;
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
