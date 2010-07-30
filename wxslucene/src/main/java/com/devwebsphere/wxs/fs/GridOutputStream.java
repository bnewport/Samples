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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import com.devwebsphere.wxslucene.GridDirectory;
import com.devwebsphere.wxslucene.jmx.LuceneFileMBeanImpl;
import com.devwebsphere.wxsutils.WXSMap;
import com.devwebsphere.wxsutils.WXSUtils;

public class GridOutputStream 
{
	static Logger logger = Logger.getLogger(GridOutputStream.class.getName());

	WXSUtils utils;
	WXSMap<String, byte[]> chunkMap;
	WXSMap<String, FileMetaData> mdMap;
	String fileName;
	long currentBucket;
	byte[] buffer;
	int currentPosition;
	long absolutePosition;
	FileMetaData md;
	Map<String, byte[]> asyncBuffers;
	int blockSize;
	int partitionMaxBatchSize;
	String chunkMapName;
	static ThreadLocalDeflator tlsDeflator = new ThreadLocalDeflator();
	LuceneFileMBeanImpl mbean;

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
		mbean = GridDirectory.getLuceneFileMBeanManager().getBean(file.getParent().getName(), file.getName());
		currentBucket = 0;
		currentPosition = 0;
		absolutePosition = 0;
		fileName = file.getName();
		this.utils = utils;
		chunkMapName = MapNames.CHUNK_MAP_PREFIX + file.getParent().getName();
		chunkMap = utils.getCache(chunkMapName);
		mdMap = utils.getCache(MapNames.MD_MAP_PREFIX + file.getParent().getName());
		md = mdMap.get(fileName);
		blockSize = file.getParent().getBlockSize();
		partitionMaxBatchSize = file.getParent().getPartitionMaxBatchSize();
		buffer = new byte[blockSize];
		if(md == null)
		{
			md = new FileMetaData();
			md.setActualSize(0);
			if(file.getParent().isCompressionEnabled())
				md.setCompressed();
			mdMap.put(fileName, md);
		}
		if(logger.isLoggable(Level.FINE))
			logger.log(Level.FINE, "Opened output stream " + fileName + " size = " + md.getActualSize());
	}
	
	public void enableAsyncWrite()
	{
		asyncBuffers = new HashMap<String, byte[]>();
	}

	static byte[] unZip(int blockSize, FileMetaData md, byte[] b)
		throws IOException
	{
		byte[] o = null;
		if(b != null && md.isCompressed())
		{
			ByteArrayInputStream bis = new ByteArrayInputStream(b);
			InflaterInputStream zis = new InflaterInputStream(bis);
			o = new byte[blockSize];
			int offset = 0;
			int toGo = blockSize;
			while(toGo > 0)
			{
				int c = zis.read(o, offset, toGo);
				offset += c;
				toGo -= c;
			}
			zis.close();
			bis.close();
		}
		else
			o = b;
		return o;
	}

	static byte[] zip(int blockSize, FileMetaData md, byte[] b)
		throws IOException
	{
		if(md.isCompressed())
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream(blockSize);
			Deflater deflater = tlsDeflator.get();
			DeflaterOutputStream zos = new DeflaterOutputStream(bos);
			zos.write(b);
			zos.close();
			bos.close();
			return bos.toByteArray();
		}
		else
			return b;
	}
	
	public void seek(long n)
		throws IOException
	{
		if(logger.isLoggable(Level.FINE))
			logger.log(Level.FINE, "seek to " + n + " in " + fileName);
		flushCurrentBuffer();
		currentBucket = n / blockSize;
		buffer = unZip(blockSize, md, chunkMap.get(GridInputStream.generateKey(fileName, currentBucket)));
		if(buffer == null) // could be sparse
		{
			buffer = new byte[blockSize];
		}
		currentPosition = (int)(n % blockSize);
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
		throws IOException
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
			int spaceAvailable = blockSize - currentPosition;
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
		throws IOException
	{
		String bucketKey = GridInputStream.generateKey(fileName, currentBucket);
		byte[] obuffer = zip(blockSize, md, buffer);
		if(asyncBuffers == null)
			chunkMap.put(bucketKey, obuffer);
		else
		{
			asyncBuffers.put(bucketKey, md.isCompressed() ? obuffer : buffer.clone());
			int numPartitions = utils.getObjectGrid().getMap(chunkMapName).getPartitionManager().getNumOfPartitions();
			if(asyncBuffers.size() > numPartitions * partitionMaxBatchSize)
			{
				chunkMap.putAll(asyncBuffers);
				asyncBuffers.clear();
			}
		}
		md.setLastModifiedTime(System.currentTimeMillis());
	}
	
	void flushCurrentBufferAndAdvance()
		throws IOException
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
