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


public class Benchmark {

	private static final class BenchSetThread implements Runnable {
		private final Long orig_value;

		private BenchSetThread(Long orig_value) {
			this.orig_value = orig_value;
		}

		public void run()
		{
			int counter = 0;
			long start = System.currentTimeMillis();
			int[] responseTimes = new int[500];
			while(true)
			{
				long before = System.currentTimeMillis();
				String key = Integer.toString(counter % 100000);
				R.str_long.set(Thread.currentThread().toString() + key, orig_value);
				long now = System.currentTimeMillis();
				int latency = (int)(now - before);
				if(latency >= responseTimes.length)
					latency = responseTimes.length - 1;
				responseTimes[latency]++;
				if(counter++ == 2000)
				{
					int rate = (int)(counter / ((now - start) / 1000.0));
					System.out.println("***********************************");
					System.out.println("Rate is " + rate);
					for(int i = 0; i < responseTimes.length;++i)
					{
						if(responseTimes[i] > 0)
						{
							double scale = 10000;
							double p = (((double)responseTimes[i]) / counter) * 100 * scale;
							p = ((int)p) / scale;
							if(p >= 1.0)
								System.out.println(Integer.toString(i) + "ms " + p + "%");
							responseTimes[i] = 0;
						}
					}
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
		R.initialize(null);
		final Long orig_value = new Long(0);
		
		Runnable t = new BenchSetThread(orig_value);
		int numThreads = 1;

		ScheduledExecutorService ses = new ScheduledThreadPoolExecutor(numThreads);
		for(int i = 0; i < numThreads; ++i)
		{
			ses.execute(t);
		}
	}

}
