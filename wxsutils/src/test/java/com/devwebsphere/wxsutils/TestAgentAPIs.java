package com.devwebsphere.wxsutils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.devwebsphere.wxsutils.wxsagent.ReduceAgentFactory;
import com.devwebsphere.wxsutils.wxsagent.WXSAgent;
import com.devwebsphere.wxsutils.wxsagent.WXSReduceAgent;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.ReduceGridAgent;

public class TestAgentAPIs {

	static Logger logger = Logger.getLogger(TestAgentAPIs.class.getName());

	static ObjectGrid ogclient;
	static WXSUtils utils;
	static BackingMap bmFarMap3;

	@BeforeClass
	public static void setupTest() throws Exception {
		// do everything in one JVM for test
		// ogclient = WXSUtils.startTestServer("Grid", "/objectgrid.xml", "/deployment.xml");
		// switch to this to connect to remote grid instead.
		utils = WXSUtils.getDefaultUtils();
		ogclient = utils.getObjectGrid();
		bmFarMap3 = ogclient.getMap("FarMap3");
	}

	@Test
	public void testSortedKeys() {
		int partitions = bmFarMap3.getPartitionManager().getNumOfPartitions();
		ArrayList<String> keys = new ArrayList<String>();
		for (int i = 0; i < partitions * 3 + 1; i++) {
			keys.add(String.valueOf(i));
		}
		Collections.shuffle(keys);
		Factory<List<List<String>>> factory = new Factory<List<List<String>>>();
		List<List<String>> skeys = WXSReduceAgent.<TestReduceGridAgent<String, Serializable>, String, List<List<String>>> callReduceAgentAll(utils,
				factory, keys, bmFarMap3);
		System.out.println("skeys: " + skeys);
		for (List<String> s : skeys) {
			System.out.println("s: " + s);
			ArrayList<String> sorted = new ArrayList<String>(s);
			Collections.sort(sorted);
			Assert.assertEquals("Not sorted", sorted, s);
		}
	}

	@Test
	public void testSortedMap() {
		int partitions = bmFarMap3.getPartitionManager().getNumOfPartitions();
		Map<String, Serializable> map = new HashMap<String, Serializable>();
		for (int i = 0; i < partitions * 3 + 1; i++) {
			String s = String.valueOf(i);
			map.put(s, s);
		}

		Factory<List<String>> factory = new Factory<List<String>>();
		List<Future<List<String>>> futures = WXSReduceAgent.callReduceAgentAll(utils, factory, map, bmFarMap3);
		List<List<String>> skeys = WXSAgent.collectResultsAsList(futures, ConfigProperties.getAgentTimeout(utils.getConfigProperties()));
		for (List<String> s : skeys) {
			ArrayList<String> sorted = new ArrayList<String>(s);
			Collections.sort(sorted);
			Assert.assertEquals("Not sorted", sorted, s);
		}
	}

	static class Factory<X> implements ReduceAgentFactory<TestReduceGridAgent<String, Serializable>, String, Serializable, X> {

		public TestReduceGridAgent<String, Serializable> newAgent(List<String> keys) {
			TestReduceGridAgent<String, Serializable> a = new TestReduceGridAgent<String, Serializable>();
			a.keys = keys;
			return a;
		}

		public TestReduceGridAgent<String, Serializable> newAgent(Map<String, Serializable> map) {
			TestReduceGridAgent<String, Serializable> a = new TestReduceGridAgent<String, Serializable>();
			a.map = map;
			return a;
		}

		public String getKey(TestReduceGridAgent<String, Serializable> a) {
			if (a.keys != null) {
				return a.keys.get(0);
			} else {
				return a.map.keySet().iterator().next();
			}
		}

		public X emptyResult() {
			return null;
		}
	};

	static class TestReduceGridAgent<K, V> implements ReduceGridAgent {
		private static final long serialVersionUID = 2235930254411812853L;
		List<K> keys;
		Map<K, V> map;

		public Object reduce(Session paramSession, ObjectMap paramObjectMap, Collection paramCollection) {
			if (keys != null) {
				return keys;
			} else {
				return new ArrayList<K>(map.keySet());
			}
		}

		public Object reduce(Session paramSession, ObjectMap paramObjectMap) {
			return null;
		}

		public Object reduceResults(Collection c) {
			if (c.size() == 1) {
				return c.iterator().next();
			}

			return c;
		}

	}
}
