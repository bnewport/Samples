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
import java.util.concurrent.atomic.AtomicLong;

/**
 * This is a helper class to track timings. It tracks the minimum/avg and maximum
 * time. It's designed to be thread safe but I attempted to try
 * to avoid any synchronization to avoid it being a bottleneck.
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

	public MinMaxAvgMetric()
	{
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
}
