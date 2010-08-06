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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.jmx.MinMaxAvgMetric;

class GridInputStreamState
{
	static Logger logger = Logger.getLogger(GridInputStreamState.class.getName());
	long currentBucket;
	long currentAbsolutePosition;
	int currentPosition;
	byte[] currentValue;
	long lastBlock = -1;
	int currentRun = 1;
	
	public GridInputStreamState()
	{
		currentAbsolutePosition = 0;
		currentBucket = 0;
		currentValue = null;
	}

	boolean areBytesAvailable(GridInputStream master)
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
		if(areBytesAvailable(master))
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

	public int privateRead(GridInputStream master, byte[] b) throws IOException 
	{
		if(!areBytesAvailable(master))
			return -1;
		
		int toGo = b.length;
		int offset = 0;
		
		while(areBytesAvailable(master) && toGo > 0)
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