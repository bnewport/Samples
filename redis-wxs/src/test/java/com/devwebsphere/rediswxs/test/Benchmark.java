//
//This sample program is provided AS IS and may be used, executed, copied and
//modified without royalty payment by customer (a) for its own instruction and
//study, (b) in order to develop applications designed to run with an IBM
//WebSphere product, either for customer's own internal use or for redistribution
//by customer, as part of such an application, in customer's own products. "
//
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2009
//All Rights Reserved * Licensed Materials - Property of IBM
//

package com.devwebsphere.rediswxs.test;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import com.devwebsphere.rediswxs.R;
import com.devwebsphere.wxsutils.jmx.MinMaxAvgMetric;


public class Benchmark {

	private static final class BenchSetThread implements Runnable {
		private final Long orig_value;

		private BenchSetThread(Long orig_value) {
			this.orig_value = orig_value;
		}

		public void run()
		{
			int counter = 0;
			long start = System.nanoTime();
			MinMaxAvgMetric metric = new MinMaxAvgMetric();
			while(true)
			{
				long before = System.nanoTime();
				String key = Integer.toString(counter % 100000);
				R.str_long.set(Thread.currentThread().toString() + key, orig_value);
				long now = System.nanoTime();
				metric.logTime(now - before);
				if(counter++ == 2000)
				{
					long rate = (long)(counter / ((now - start) / 1000000000.0));
					System.out.println("***********************************");
					System.out.println("Rate is " + rate);
					counter = 0;
					start = now;
				}
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		R.initialize();
		final Long orig_value = new Long(0);
		
		Runnable t = new BenchSetThread(orig_value);
		int numThreads = 10;

		ScheduledExecutorService ses = new ScheduledThreadPoolExecutor(numThreads);
		for(int i = 0; i < numThreads; ++i)
		{
			ses.execute(t);
		}
	}

}
