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
package com.devwebsphere.wxsutils.jmx;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This is a helper class to track timings. It tracks the minimum/avg and maximum
 * time. It's designed to be thread safe but I attempted to try
 * to avoid any synchronization to avoid it being a bottleneck. It also tracks the response times using
 * a graph to see how many timings were of x ms and so on.
 * 
 * This can also be used to put times in buckets. For example, if we want to measure response
 * times up to 10 seconds but want them in 1 second buckets then the parameterized constructor can
 * be used to do this with the parameters (10000, 1000).
 *
 */
public final class MinMaxAvgMetric 
{
	static public final double TIME_SCALE_NS_MS = 1000000.0;
	
	AtomicInteger count = new AtomicInteger();
	AtomicInteger exceptionCount = new AtomicInteger();
	volatile Throwable lastException;
	
	AtomicLong minTimeNS = new AtomicLong();
	AtomicLong maxTimeNS = new AtomicLong();
	AtomicLong lastTimeNS = new AtomicLong();
	
	AtomicLong totalTimeNS = new AtomicLong();
	AtomicIntegerArray responseTimeArray;
	int bucketSizeMS;
	

	/**
	 * Tracks response times < 1 second normally
	 */
	public MinMaxAvgMetric()
	{
		responseTimeArray = new AtomicIntegerArray(1000);
		reset();
		bucketSizeMS = 1;
	}

	/**
	 * Tracks times < maxTimeMS normally
	 * @param maxTimeMS
	 * @param bucketSizeMS For example, puts all times together with this many milliseconds
	 */
	public MinMaxAvgMetric(int maxTimeMS, int bucketSizeMS)
	{
		if(bucketSizeMS <= 0)
			throw new IllegalArgumentException("Bucket Size must be > 1");
		this.bucketSizeMS = bucketSizeMS;
		maxTimeMS = (maxTimeMS / bucketSizeMS);
		responseTimeArray = new AtomicIntegerArray(maxTimeMS);
		reset();
	}

	/**
	 * This resets all the statistics. This is deliberately not fully
	 * thread safe to avoid synchronization in other methods.
	 */
	public void reset()
	{
		count.set(0);
		minTimeNS.set(0);
		maxTimeNS.set(0);
		exceptionCount.set(0);
		lastTimeNS.set(0);
		
		totalTimeNS.set(0);
		lastException = null;
		for(int i = 0; i < responseTimeArray.length(); ++i)
		{
			responseTimeArray.set(i, 0);
		}
	}

	/**
	 * This should be called to record the timing for what ever is being measured
	 * @param durationNS The amount of time in nano seconds.
	 */
	public void logTime(long durationNS)
	{
		lastTimeNS.set(durationNS);
		int c = count.incrementAndGet();
		totalTimeNS.addAndGet(durationNS);
		int durationMS = (int)(durationNS / 1000000L);
		// round to bucket interval
		int bucket = durationMS / bucketSizeMS;
		
		int maxTime = responseTimeArray.length() - 1;
		responseTimeArray.incrementAndGet((bucket >= 0 && bucket < maxTime) ? bucket : maxTime);
		if(c == 1)
		{
			// make sure in the window between fetch count
			// and here, we don't overwrite any changes
			minTimeNS.compareAndSet(0L, durationNS);
			maxTimeNS.compareAndSet(0L, durationNS);
		}
		else
		{
			// No point in looping here, if we miss a sample then I don't think
			// it's the end of the world. The compareAndSet just makes sure
			// it either works correctly or not at all
			long min = minTimeNS.get();
			minTimeNS.compareAndSet(min, Math.min(minTimeNS.get(), durationNS));
			long max = maxTimeNS.get();
			maxTimeNS.compareAndSet(max, Math.max(maxTimeNS.get(), durationNS));
		}
	}

	/**
	 * This should be called when an exception occurs during the method being timed.
	 * @param t The exception
	 */
	public void logException(Throwable t)
	{
		lastException = t;
		exceptionCount.incrementAndGet();
	}

	/**
	 * This returns the number of times logTime has been called
	 * @return
	 */
	public int getCount() {
		return count.get();
	}

	/**
	 * This returns the minimum time seen while timing the method
	 * @return
	 */
	public long getMinTimeNS() {
		return minTimeNS.get();
	}

	/**
	 * This returns the maximum time seens while timing the method
	 * @return
	 */
	public long getMaxTimeNS() {
		return maxTimeNS.get();
	}

	/**
	 * This returns the time since the last reset operation
	 * @return
	 */
	public long getTotalTimeNS() {
		return totalTimeNS.get();
	}

	/**
	 * This returns the average time for the timings.
	 * @return
	 */
	public long getAvgTimeNS()
	{
		int c = count.get();
		if(c > 0)
		{
			return totalTimeNS.get() / c;
		}
		else
			return 0;
	}

	/**
	 * This returns how many times logException has been called since the
	 * last reset operation
	 * @return
	 */
	public int getExceptionCount() {
		return exceptionCount.get();
	}

	/**
	 * This returns the last exception recorded by logException
	 * @return
	 */
	public Throwable getLastException() {
		return lastException;
	}

	/**
	 * This returns the last timing recorded. This is expected to change
	 * frequently.
	 * @return
	 */
	public long getLastOperationTimeNS()
	{
		return lastTimeNS.get();
	}
	
	public String toString()
	{
		return "<Min: " + (getMinTimeNS() / TIME_SCALE_NS_MS) + "ms, Max:" + (getMaxTimeNS() / TIME_SCALE_NS_MS) + "ms, Avg: " + (getAvgTimeNS() / TIME_SCALE_NS_MS) + "ms>";
	}
	
	/**
	 * This prints to System.out a bar graph of the response times seen so far.
	 */
	public void dumpResponseTimes()
	{
		String bar = "**************************************************";
		int maxCount = 0;
		int total = 0;
		for(int i = 0; i < responseTimeArray.length(); ++i)
		{
			int c = responseTimeArray.get(i);
			maxCount = Math.max(c, maxCount);
			total += c;
		}
			
		for(int i = 0; i < responseTimeArray.length(); ++i)
		{
			int count = Math.min(maxCount, responseTimeArray.get(i));
			if(count > 0)
			{
				int numChars = bar.length() * count / maxCount;
				int percentageMax = (count * 100 / total);
				if(numChars >= 1)
					System.out.println("R " + (i * bucketSizeMS) + "ms : " + bar.substring(0, numChars) + " (" + percentageMax + "%)");
			}
		}
	}
}
