package com.devwebsphere.wxsutils.filter;

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
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.devwebsphere.wxsutils.WXSMap;
import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.filter.path.SerializedValuePath;
import com.devwebsphere.wxsutils.filter.set.GridFilteredIndex;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.plugins.index.MapIndexPlugin;
import com.ibm.ws.xs.jdk5.java.util.Arrays;

/**
 * This test connects to a grid running on the same box. Use the gettingstarted example with the xml files in this
 * folder. These xmls just add a third Map which doesn't use client side caching.
 * 
 */
public class TestSerializedFilter {
	static ObjectGrid ogclient;
	static WXSUtils utils;
	static BackingMap personMap;
	static String PERSON_MAP = "Person";
	static String PERSON2_MAP = "Person2";

	@BeforeClass
	public static void setupTest() {
		// do everything in one JVM for test
		ogclient = WXSUtils.startTestServer("Grid", "/filter/serialized_objectgrid.xml", "/filter/serialized_deployment.xml");
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
	public void serializedValues() throws Exception {
		clearMap();
		WXSMap<String, byte[]> map = utils.getCache(PERSON_MAP);
		String BW = "<person><firstName>Betty</firstName><lastName>White</lastName><age>29</age></person>";
		String JW = "<person><firstName>Joe</firstName><lastName>White</lastName><age>45</age></person>";
		map.put("BW", BW.getBytes());
		map.put("JW", JW.getBytes());
		map.put("WT", "<person><firstName>William</firstName><lastName>Tell</lastName><age>200</age></person>".getBytes());

		ValuePath agePath = new SerializedValuePath("age");
		Filter youngFilter = FilterBuilder.lt(agePath, 30.0);
		Filter oldFilter = FilterBuilder.gt(agePath, 40.0);

		Map<String, byte[]> results = collect(map.eq("lastName", "White", youngFilter));
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(BW, new String(results.get("BW")));

		results = collect(map.eq("lastName", "White", oldFilter));
		Assert.assertEquals(1, results.size());
		Assert.assertEquals(JW, new String(results.get("JW")));
	}

	@Test
	public void serializedKeysValues() throws Exception {
		clearMap();
		WXSMap<byte[], byte[]> map = utils.getCache(PERSON2_MAP);
		String BW = "<person><firstName>Betty</firstName><lastName>White</lastName><age>29</age></person>";
		String JW = "<person><firstName>Joe</firstName><lastName>White</lastName><age>45</age></person>";
		byte[] bwBytes = BW.getBytes();
		map.put(bwBytes, bwBytes);
		byte[] jwBytes = JW.getBytes();
		map.put(jwBytes, jwBytes);
		map.put("WT".getBytes(), "<person><firstName>William</firstName><lastName>Tell</lastName><age>200</age></person>".getBytes());

		ValuePath agePath = new SerializedValuePath("age");
		Filter youngFilter = FilterBuilder.lt(agePath, 30.0);
		Filter oldFilter = FilterBuilder.gt(agePath, 40.0);

		Map<byte[], byte[]> results = collect(map.eq(MapIndexPlugin.SYSTEM_KEY_INDEX_NAME, null, youngFilter));
		Assert.assertEquals(1, results.size());
		Assert.assertTrue(Arrays.equals(bwBytes, results.keySet().iterator().next()));

		results = collect(map.eq(MapIndexPlugin.SYSTEM_KEY_INDEX_NAME, null, oldFilter));
		Assert.assertEquals(2, results.size());
	}

	private <K extends Serializable, V extends Serializable> Map<K, V> collect(GridFilteredIndex<K, V> index) {
		Map<K, V> results = new HashMap<K, V>();
		Map<K, V> curR;
		while ((curR = index.getNextResult()) != null) {
			results.putAll(curR);
		}
		return results;
	}
}
