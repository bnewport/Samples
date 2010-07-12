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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.WXSMap;
import com.devwebsphere.wxsutils.WXSUtils;

public class GridOutputStream 
{
	static Logger logger = Logger.getLogger(GridOutputStream.class.getName());
	final public static int BLOCK_SIZE = 10 * 1024;

	WXSUtils utils;
	WXSMap<String, byte[]> chunkMap;
	WXSMap<String, FileMetaData> mdMap;
	String fileName;
	long currentBucket;
	byte[] buffer = new byte[BLOCK_SIZE];
	int currentPosition;
	long absolutePosition;
	FileMetaData md;
	Map<String, byte[]> asyncBuffers;

	public FileMetaData getMetaData()
	{
		return md;
	}
	
	public String toString()
	{
		return "GridOutputStream(" + fileName + ", abs=" + absolutePosition + ", pos=" + currentPosition + ", max=" + md.getActualSize() + ")";
	}
	
	public GridOutputStream(WXSUtils utils, GridFile file) throws FileNotFoundException 
	{
		currentBucket = 0;
		currentPosition = 0;
		absolutePosition = 0;
		fileName = file.getName();
		this.utils = utils;
		chunkMap = utils.getCache(MapNames.CHUNK_MAP);
		mdMap = utils.getCache(MapNames.MD_MAP);
		md = mdMap.get(fileName);
		if(md == null)
		{
			md = new FileMetaData();
			mdMap.put(fileName, md);
			md.setActualSize(0);
		}
		if(logger.isLoggable(Level.FINE))
			logger.log(Level.FINE, "Opened output stream " + fileName + " size = " + md.getActualSize());
	}
	
	public void enableAsyncWrite()
	{
		asyncBuffers = new HashMap<String, byte[]>();
	}

	public void seek(long n)
	{
		if(logger.isLoggable(Level.FINE))
			logger.log(Level.FINE, "seek to " + n + " in " + fileName);
		flushCurrentBuffer();
		currentBucket = n / BLOCK_SIZE;
		buffer = chunkMap.get(GridInputStream.generateKey(fileName, currentBucket));
		if(buffer == null) // could be sparse
		{
			buffer = new byte[BLOCK_SIZE];
		}
		currentPosition = (int)(n % BLOCK_SIZE);
		absolutePosition = n;
	}
	
	public long getPosition()
	{
		if(logger.isLoggable(Level.FINE))
			logger.log(Level.FINE, "position=" + absolutePosition + " in " + fileName);
		return absolutePosition;
	}
	
	public void write(int b) throws IOException 
	{
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":write/1a:" + b);
		}
		byte[] v = new byte[1];
		v[0] = (byte)b;
		writeArray(v);
	}

	void writeArray(byte[] b)
	{
//		if(logger.isLoggable(Level.FINEST))
//		{
//			logger.log(Level.FINEST, this.toString() + ":write/1b:" + b.length);
//		}
		int size = b.length;
		int offset = 0;
		int toGo = size;
		while(toGo != 0)
		{
			int spaceAvailable = BLOCK_SIZE - currentPosition;
			if(spaceAvailable != 0)
			{
				int copySize = Math.min(toGo, spaceAvailable);
				System.arraycopy(b, offset, buffer, currentPosition, copySize);
				toGo -= copySize;
				offset += copySize;
				currentPosition += copySize;
			}
			else
			{
				flushCurrentBufferAndAdvance();
			}
		}
		absolutePosition += size;
		md.setActualSize(Math.max(md.getActualSize(), absolutePosition));
	}
	
	void flushCurrentBuffer()
	{
		String bucketKey = GridInputStream.generateKey(fileName, currentBucket);
		if(asyncBuffers == null)
			chunkMap.put(bucketKey, buffer);
		else
		{
			asyncBuffers.put(bucketKey, buffer.clone());
			if(asyncBuffers.size() > 40)
			{
				chunkMap.putAll(asyncBuffers);
				asyncBuffers.clear();
			}
		}
		md.setLastModifiedTime(System.currentTimeMillis());
	}
	
	void flushCurrentBufferAndAdvance()
	{
		flushCurrentBuffer();
		currentPosition = 0;
		currentBucket++;
	}
	
	public void write(byte[] b) throws IOException 
	{
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":write/1");
		}
		writeArray(b);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":write/3");
		}
		if(off == 0 && len == b.length)
			write(b);
		else
		{
			byte[] buffer = new byte[len];
			System.arraycopy(b, off, buffer, 0, len);
			write(buffer);
		}
	}

	public void close() throws IOException {
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":close");
		}
		flush();
	}

	public void flush() throws IOException 
	{
		if(logger.isLoggable(Level.FINEST))
		{
			logger.log(Level.FINEST, this.toString() + ":flush:Current size " + md.getActualSize());
		}
		flushCurrentBuffer();
		if(asyncBuffers != null)
		{
			chunkMap.putAll(asyncBuffers);
			asyncBuffers.clear();
		}
		mdMap.put(fileName, md);
	}

}
