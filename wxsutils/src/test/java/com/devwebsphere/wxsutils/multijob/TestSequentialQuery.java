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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.devwebsphere.wxsutils.WXSMap;
import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.multijob.ogql.GridQuery;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;

/**
 * This test connects to a grid running on the same box. Use the gettingstarted example with the xml files in this
 * folder. These xmls just add a third Map which doesn't use client side caching.
 * 
 */
public class TestSequentialQuery {
	static ObjectGrid ogclient;
	static WXSUtils utils;
	static BackingMap personMap;
	static String PERSON_MAP = "Person";

	@BeforeClass
	public static void setupTest() {
		// do everything in one JVM for test
		ogclient = WXSUtils.startTestServer("Grid", "/multijob/multijob_objectgrid.xml", "/multijob/multijob_deployment.xml");
		// switch to this to connect to remote grid instead.
		// ogclient = WXSUtils.connectClient("localhost:2809", "Grid", "/multijob/multijob_objectgrid.xml");
		utils = new WXSUtils(ogclient);
		personMap = ogclient.getMap(PERSON_MAP);
	}

	/**
	 * This clears the FarMap3 in preparation for any tests
	 */
	public static void clearMap() {
		try {
			ogclient.getSession().getMap(PERSON_MAP).clear();
		} catch (ObjectGridException e) {
			Assert.fail("Exception during clear");
		}
	}

	@Test
	public void testQuery() throws Exception {
		clearMap();
		WXSMap<String, Person> map = utils.getCache("Person");
		Set<String> personSet = new HashSet<String>();
		Map<String, Person> batch = new HashMap<String, Person>();
		for (int i = 0; i < 1000; ++i) {
			Person p = new Person();
			p.setFirstName("William the " + i);
			personSet.add(p.getFirstName());
			p.setSurname("Newport");
			p.setCreditLimit(10000 * i);
			p.setDateOfBirth(new Date(System.currentTimeMillis()));
			p.setMiddleInitial("T");
			batch.put(Integer.toString(i), p);
		}
		// insert all Persons in a batch
		map.putAll(batch);

		GridQuery q = new GridQuery(ogclient, "select p from Person p", 2);
		ArrayList<Serializable> block = q.getNextResult();
		while (block != null) {
			for (Object r : block) {
				Person p = (Person) r;
				// System.out.println(p.toString());
				Assert.assertEquals(true, personSet.remove(p.getFirstName()));
			}
			block = q.getNextResult();
		}
		Assert.assertEquals(0, personSet.size());
	}
}
