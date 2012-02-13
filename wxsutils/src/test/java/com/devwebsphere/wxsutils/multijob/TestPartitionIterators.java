package com.devwebsphere.wxsutils.multijob;

//
//This sample program is provided AS IS and may be used, executed, copied and
//modified without royalty payment by customer (a) for its own instruction and
//study, (b) in order to develop applications designed to run with an IBM
//WebSphere product, either for customer's own internal use or for redistribution
//by customer, as part of such an application, in customer's own products. "
//
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2012
//All Rights Reserved * Licensed Materials - Property of IBM
//

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.devwebsphere.wxsutils.WXSUtils;
import com.ibm.websphere.objectgrid.ObjectGrid;

/**
 * This test connects to a grid running on the same box. Use the gettingstarted example with the xml files in this
 * folder. These xmls just add a third Map which doesn't use client side caching.
 * 
 */
public class TestPartitionIterators {
	static ObjectGrid ogclient;

	@BeforeClass
	public static void setupTest() {
		// do everything in one JVM for test
		ogclient = WXSUtils.startTestServer("Grid", "/multijob/filterindex_objectgrid.xml", "/multijob/filterindex_deployment.xml");
	}

	@Test
	public void testAscending() throws Exception {
		PartitionIterator itr = PartitionIterators.ascending(ogclient);
		int matchingPartiton = 0;
		while (itr.hasNext()) {
			Assert.assertEquals("Wrong partition", matchingPartiton++, itr.next());
		}
		Assert.assertEquals("Wrong end", 13, matchingPartiton);
		itr.reset();
		Assert.assertEquals("Reset failed", 0, itr.next());
	}

	@Test
	public void testAscendingWithStart() throws Exception {
		PartitionIterator itr = PartitionIterators.ascending(ogclient, 5);
		int matchingPartiton = 5;
		while (itr.hasNext()) {
			Assert.assertEquals("Wrong partition", matchingPartiton++, itr.next());
		}
		Assert.assertEquals("Wrong end", 13, matchingPartiton);
		itr.reset();
		Assert.assertEquals("Reset failed", 5, itr.next());
	}

	@Test
	public void testDescending() throws Exception {
		PartitionIterator itr = PartitionIterators.descending(ogclient);
		int matchingPartiton = 12;
		while (itr.hasNext()) {
			Assert.assertEquals("Wrong partition", matchingPartiton--, itr.next());
		}
		Assert.assertEquals("Wrong end", -1, matchingPartiton);
		itr.reset();
		Assert.assertEquals("Reset failed", 12, itr.next());
	}

	@Test
	public void testDescendingWithEnd() throws Exception {
		PartitionIterator itr = PartitionIterators.descending(ogclient, 6);
		int matchingPartiton = 12;
		while (itr.hasNext()) {
			Assert.assertEquals("Wrong partition", matchingPartiton--, itr.next());
		}
		Assert.assertEquals("Wrong end", 5, matchingPartiton);
		itr.reset();
		Assert.assertEquals("Reset failed", 12, itr.next());
	}

}
