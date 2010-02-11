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
package com.devwebsphere;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import com.devwebsphere.evictionnotifier.EvictEntry;
import com.devwebsphere.evictionnotifier.EvictionEventListener;
import com.devwebsphere.wxsutils.WXSUtils;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;

/**
 * This is a basic test case which starts a local grid within this JVM and then tests the function
 * by inserting records in to a watched Map and then watches that they are all evicted.
 *
 */
public class TestEvictionNotificationsSingleJVM 
{
	static ObjectGrid clientOG;
	static WXSUtils utils;
	static int numKeys = 10;
	
	/**
	 * This initializes a grid running within this JVM with a single catalog and a single
	 * container using these xml files.
	 */
	@BeforeClass
	static public void initGrid()
	{
		// creates an in JVM complete grid using these xml files for testing and returns
		// a client reference to it.
		clientOG = WXSUtils.startTestServer("Grid", "/objectgrid.xml", "/deployment.xml");
		utils = new WXSUtils(clientOG);
	}

	/**
	 * This inserts some records which will get evicted
	 * @throws ObjectGridException
	 */
	@Test
	public void testInsertRecords()
		throws ObjectGridException
	{
		Map<Integer, SampleValue> entries = new HashMap<Integer, SampleValue>();
		
		for(int i = 0; i < numKeys; ++i)
		{
			SampleValue v = new SampleValue();
			v.time = System.nanoTime();
			entries.put(new Integer(i), v);
		}
		utils.putAll(entries, clientOG.getMap("Map"));
	}

	/**
	 * This blocks until all inserted records have been evicted.
	 * @throws ObjectGridException
	 */
	@Test
	public void testNotificationEvents()
		throws ObjectGridException
	{
		EvictionEventListener listener = new EvictionEventListener(clientOG, "EvictQ");

		Set<Integer> seenKeys = new HashSet<Integer>();
		while(seenKeys.size() != numKeys)
		{
			EvictEntry ee = listener.getNextEntry();
			if(ee != null)
			{
				Integer key = (Integer)ee.getKey();
				if(!seenKeys.contains(key))
				{
					seenKeys.add(key);
					System.out.println("Found " + key);
				}
			}
			else
			{
				try { Thread.sleep(2000); } catch(InterruptedException e) {}
			}
		}
	}
}
