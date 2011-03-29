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
package com.devwebsphere.wxsutils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.devwebsphere.wxsutils.wxsmap.BigListPushAgent;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;

/**
 * This test connects to a grid running on the same box. Use the gettingstarted example
 * with the xml files in this folder. These xmls just add a third Map which doesn't
 * use client side caching.
 *
 */
public class BenchmarkListAPIs 
{
	static ObjectGrid ogclient;
	static WXSUtils utils;
	
	@BeforeClass
	public static void setupTest()
	{
		// do everything in one JVM for test
		ogclient = WXSUtils.startTestServer("Grid", "/objectgrid.xml", "/deployment.xml");
		// switch to this to connect to remote grid instead.
//		ogclient = WXSUtils.connectClient("localhost:2809", "Grid", "/objectgrid.xml");
		utils = new WXSUtils(ogclient);
	}

	/**
	 * This clears the FarMap3 in preparation for any tests
	 */
	public static void clearMap()
	{
		try
		{
			ogclient.getSession().getMap("FarMap3").clear();
		}
		catch(ObjectGridException e)
		{
			Assert.fail("Exception during clear");
		}
	}
	
	/**
	 * For benchmarking/profiling purposes only
	 */
//	@Test
	public void testListOperations()
	{
		WXSMapOfLists<String, String> map = utils.getMapOfLists("BigList");
		Assert.assertNotNull(map);
		
		String key = "BENCH";
		String data1K = "";
		for(int i = 0; i < 100; ++i) data1K += "0123456789";
		
		for(int i = 0; i < BigListPushAgent.BUCKET_SIZE + 5; ++i)
			map.lpush(key, data1K);

		long counter = 1;
		long startTime = System.nanoTime();
		while(true)
		{
			map.lpush(key, data1K);
			String rc = map.lpop(key);
			Assert.assertEquals(data1K, rc);
			if(counter++ % 10000 == 0)
			{
				double duration = (System.nanoTime() - startTime) / 1000000000.0;
				double rate = (double)counter / duration;
				System.out.println("Push/Pop rate is " + rate);
			}
		}
	}
	
	/**
	 * This does a simple stress test against the grid.
	 */
	@Test 
	public void testPutRate()
	{
		clearMap();
		int maxTests = 50;
		// run more than one time to allow JIT to settle
		// for unit test once is enough
		for(int loop = 0; loop < 1; ++loop)
		{
			for(int batchSize = 1000; batchSize <= 32000; batchSize *= 2 )
			{
				Map<String, String> batch = new HashMap<String, String>();
				for(int i = 0; i < batchSize; ++i)
					batch.put(Integer.toString(i), "V" + i);
				
				long start = System.nanoTime();
				for(int test = 0; test < maxTests; ++test)
				{
					utils.putAll(batch, ogclient.getMap("FarMap3"));
				}
				if(false)
				{
					ArrayList<String> keys = new ArrayList<String>();
					for(int i = 0; i < batchSize; ++i)
					{
						keys.add(Integer.toString(i));
					}
					Map<String, String> rc = utils.getAll(keys, ogclient.getMap("FarMap3"));
					
					for(Map.Entry<String, String> e : rc.entrySet())
					{
						Assert.assertEquals("V" + e.getKey(), e.getValue());
					}
				}
				double duration = (System.nanoTime() - start) / 1000000000.0;
				double rate = (double)batchSize * (double)maxTests / duration;
				System.out.println("Batch of " + batchSize + " rate is " + rate + " <" + (batch.size() * maxTests) + ":" + duration + ">");
			}
		}
	}
}
