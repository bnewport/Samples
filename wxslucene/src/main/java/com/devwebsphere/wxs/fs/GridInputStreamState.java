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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.jmx.MinMaxAvgMetric;

/**
 * GridInputStream can be shared between threads but it needs to track certain state
 * for a specific thread. This class is stored in a ThreadLocal PER GridInputStream instance
 * to avoid threads stepping on each other reading in parallel from a single
 * GridInputStream instance.
 * @author bnewport
 *
 */
class GridInputStreamState
{
	static Logger logger = Logger.getLogger(GridInputStreamState.class.getName());
	long currentBucket;
	/**
	 * The current position within the file.
	 */
	long currentAbsolutePosition;
	/**
	 * The current position with the current bucket
	 */
	int currentPosition;
	
	/**
	 * The block read from the grid, usually block #currentBucket
	 */
	byte[] currentValue;
	
	/**
	 * This tracks the block number of the previous block used for this thread
	 */
	long lastBlock = -1;
	/**
	 * This is used to detect sequential block fetches.
	 */
	int currentRun = 1;
	
	public GridInputStreamState()
	{
		currentAbsolutePosition = 0;
		currentBucket = 0;
		currentValue = null;
	}

	boolean areBytesAvailable(GridInputStream master, Map<Long, byte[]> bMap)
	throws IOException
	{
		if(currentAbsolutePosition == master.md.getActualSize())
		{
	//		if(logger.isLoggable(Level.FINEST))
	//		{
	//			logger.log(Level.FINEST, this.toString() + ":areBytesAvailable=FALSE");
	//		}
			return false;
		}
		if(currentValue == null || currentPosition == currentValue.length)
		{
			// if not first block in file then advance otherwise fetch first block
			if(currentAbsolutePosition > 0)
				currentBucket++;
			
			// check prefetch cache
			if(bMap != null)
				currentValue = bMap.get(new Long(currentBucket));
			else
				currentValue = null;
			// fetch if not found in prefetch
			if(currentValue == null)
				currentValue = master.getBlock(currentBucket);
			if(currentValue == null)
			{
				currentValue = new byte[master.blockSize];
			}
			currentPosition = 0;
		}
	//	if(logger.isLoggable(Level.FINEST))
	//	{
	//		logger.log(Level.FINEST, this.toString() + ":areBytesAvailable=TRUE");
	//	}
		return true;
	}

	public int read(GridInputStream master) throws IOException 
	{
		int rc = -1;
		if(areBytesAvailable(master, null))
		{
			rc = currentValue[currentPosition++];
			++currentAbsolutePosition;
			master.mbean.getReadBytesMetric().logTime(1);
		}
		if(logger.isLoggable(Level.FINER))
		{
			logger.log(Level.FINER, this.toString() + ":read/0=" + rc);
		}
		return rc;
	}

	/**
	 * If more than a single block is required for a byte range then fetch the blocks in parallel
	 * and keep the blocks in a prefetch Map keyed by block number.
	 * @param master
	 * @param beginOffset
	 * @param endOffset
	 * @return
	 */
	Map<Long, byte[]> prefetchBlocks(final GridInputStream master, long beginOffset, long endOffset)
	{
		int beginBlock = (int)(beginOffset / (long)master.blockSize);
		int endBlock = (int)(endOffset / (long)master.blockSize);

		// if only one block don't bother doing it in parallel
		// this also only works for now if a local cache is enabled
		if(beginBlock == endBlock)
			return null;

		if(logger.isLoggable(Level.FINE))
		{
			logger.log(Level.FINE, "Prefetching " + (endBlock - beginBlock + 1) + " blocks");
		}
		ArrayList<Future<byte[]>> blocks = new ArrayList<Future<byte[]>>();
		for(int block = beginBlock; block <= endBlock; ++block)
		{
			final int blk = block;
			Callable<byte[]> c = new Callable<byte[]>() 
			{
				int theBlock = blk;
				public byte[] call()
				{
					try
					{
						byte[] rc = master.getBlock(theBlock);
						return rc;
					}
					catch(Exception e)
					{
						logger.log(Level.SEVERE, "Exception prefetching block", e);
						return null;
					}
				}
			};
			blocks.add(master.utils.getExecutorService().submit(c));
		}
		Map<Long, byte[]> bMap = new HashMap<Long, byte[]>();
		try
		{
			long blockNum = beginBlock;
			for(Future<byte[]> f : blocks)
			{
				byte[] waitBlock = f.get();
				bMap.put(new Long(blockNum++), waitBlock);
			}
			return bMap;
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Prefetch threads threw an exception", e);
			return null;
		}
	}
	
	public int privateRead(final GridInputStream master, byte[] b) throws IOException 
	{
		int toGo = b.length;
		int offset = 0;

		long beginOffset = currentAbsolutePosition;
		long endOffset = currentAbsolutePosition + toGo - 1;
		Map<Long, byte[]> prefetchMap = prefetchBlocks(master, beginOffset, endOffset);
		
		if(!areBytesAvailable(master, prefetchMap))
			return -1;
		
		while(areBytesAvailable(master, prefetchMap) && toGo > 0)
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

	public long skip(GridInputStream master, long n) throws IOException 
	{
		if(logger.isLoggable(Level.FINER))
		{
			logger.log(Level.FINER, this.toString() + ":skip to " + n);
		}
		if(currentAbsolutePosition == master.md.getActualSize())
			return 0;
		long newPosition = currentAbsolutePosition + n;
		if(newPosition >= master.md.getActualSize())
		{
			newPosition = master.md.getActualSize();
		}
		long skipAmount = newPosition - currentAbsolutePosition;
		
		int newBucket = (int)(newPosition / master.blockSize);
		if(newBucket != currentBucket)
		{
			currentBucket = newBucket;
			currentValue = master.getBlock(currentBucket);
			if(currentValue == null)
				currentValue = new byte[master.blockSize];
		}
		currentPosition = (int)(newPosition % master.blockSize);
		currentAbsolutePosition = newPosition;
		
		return skipAmount;
	}

	void noteNewBlock(GridInputStream master, long blockNum)
	{
		if(blockNum == lastBlock + 1)
			currentRun++;
		else
		{
			if(currentRun != 1)
			{
				MinMaxAvgMetric metric = master.mbean.getReadSequentialMetric();
				metric.logTime(currentRun);
				if(logger.isLoggable(Level.FINE))
				{
					logger.log(Level.FINE, "<Min: " + metric.getMinTimeNS() + ", Max:" + metric.getMaxTimeNS() + ", Avg: " + metric.getAvgTimeNS() + ">" + " from " + master.fileName);
				}
				currentRun = 1;
			}
		}
		lastBlock = blockNum;
	}
}