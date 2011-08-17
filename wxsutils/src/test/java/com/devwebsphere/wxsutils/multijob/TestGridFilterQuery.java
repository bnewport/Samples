package com.devwebsphere.wxsutils.multijob;
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


import java.util.Date;
import java.util.Map;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.devwebsphere.wxsutils.WXSMap;
import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.filter.Filter;
import com.devwebsphere.wxsutils.filter.FilterBuilder;
import com.devwebsphere.wxsutils.filter.ValuePath;
import com.devwebsphere.wxsutils.filter.path.PojoPropertyPath;
import com.devwebsphere.wxsutils.filter.set.GridFilteredIndex;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;

/**
 * This test connects to a grid running on the same box. Use the gettingstarted example
 * with the xml files in this folder. These xmls just add a third Map which doesn't
 * use client side caching.
 *
 */
public class TestGridFilterQuery 
{
	static ObjectGrid ogclient;
	static WXSUtils utils;
	static BackingMap personMap;
	static String PERSON_MAP = "Person";
	
	@BeforeClass
	public static void setupTest()
	{
		// do everything in one JVM for test
		ogclient = WXSUtils.startTestServer("Grid", "/multijob/filterindex_objectgrid.xml", "/multijob/filterindex_deployment.xml");
		// switch to this to connect to remote grid instead.
//		ogclient = WXSUtils.connectClient("localhost:2809", "Grid", "/multijob/multijob_objectgrid.xml");
		utils = new WXSUtils(ogclient);
		personMap = ogclient.getMap(PERSON_MAP);
	}

	/**
	 * This clears the FarMap3 in preparation for any tests
	 */
	public static void clearMap()
	{
		try
		{
			ogclient.getSession().getMap(PERSON_MAP).clear();
		}
		catch(ObjectGridException e)
		{
			Assert.fail("Exception during clear");
		}
	}

	@Test
	public void testQuery()
		throws Exception
	{
		clearMap();
		WXSMap<String, Person> map = utils.getCache(PERSON_MAP);
		String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		for(int i = 0; i < 1000; ++i)
		{
			Person p = new Person();
			p.setFirstName("William the " + i);
			
			// surname is a single letter
			int im = i % letters.length();
			p.setSurname(letters.substring(im, im + 1));
			
			p.setCreditLimit(10000 * i);
			p.setDateOfBirth(new Date(System.currentTimeMillis()));
			p.setMiddleInitial("T");
			map.put(Integer.toString(i), p);
		}

		// keep only people with credit limit under a million;
		FilterBuilder fb = new FilterBuilder();
		ValuePath creditLimitPath = new PojoPropertyPath("CreditLimit");
		Double desiredMaxCreditLimit = 1000000.0;
		Filter f = fb.lt(creditLimitPath, desiredMaxCreditLimit);

		// find all surnames < "M" with credit limit < 1000000
		String value = "M";
		GridFilteredIndex<String, Person> q = map.lt("surname", value, f);
		Map<String, Person> block = q.getNextResult();

		// count iterations
		int blockCount = 0;
		while(block != null)
		{
			++blockCount;
			for(Map.Entry<String, Person> e : block.entrySet())
			{
				Person p = (Person)e.getValue();
				System.out.println(p.toString());
				
				// check p matches filter
				Assert.assertTrue(f.filter(p));
				if(p.getSurname().compareTo(value) >= 0)
					Assert.fail("Surname isn't smaller than " + value);
				Assert.assertTrue(p.getCreditLimit() < desiredMaxCreditLimit);
			}
			block = q.getNextResult();
		}
		// should always be a result for each partition
		Assert.assertEquals(ogclient.getMap(PERSON_MAP).getPartitionManager().getNumOfPartitions(), blockCount);
	}
}
