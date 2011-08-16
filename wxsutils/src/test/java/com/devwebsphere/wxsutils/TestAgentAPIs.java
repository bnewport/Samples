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
		List<List<String>> skeys = WXSReduceAgent.callReduceAgentAll(utils, FACTORY, keys, bmFarMap3);
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
		HashMap<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < partitions * 3 + 1; i++) {
			String s = String.valueOf(i);
			map.put(s, s);
		}

		List<Future<List<String>>> futures = WXSReduceAgent.callReduceAgentAll(utils, FACTORY, map, bmFarMap3);
		List<List<String>> skeys = WXSAgent.collectResultsAsList(futures, ConfigProperties.getAgentTimeout(utils.getConfigProperties()));
		for (List<String> s : skeys) {
			ArrayList<String> sorted = new ArrayList<String>(s);
			Collections.sort(sorted);
			Assert.assertEquals("Not sorted", sorted, s);
		}
	}

	static ReduceAgentFactory<TestReduceGridAgent> FACTORY = new ReduceAgentFactory<TestReduceGridAgent>() {

		public <K extends Serializable> TestReduceGridAgent newAgent(List<K> keys) {
			TestReduceGridAgent a = new TestReduceGridAgent();
			a.keys = (List<Serializable>) keys;
			return a;
		}

		public <K extends Serializable, V> TestReduceGridAgent newAgent(Map<K, V> map) {
			TestReduceGridAgent a = new TestReduceGridAgent();
			a.map = (Map<Serializable, Serializable>) map;
			return a;
		}

		public <K extends Serializable> K getKey(TestReduceGridAgent a) {
			if (a.keys != null) {
				return (K) a.keys.get(0);
			} else {
				return (K) a.map.keySet().iterator().next();
			}
		}

		public <X> X emptyResult() {
			return null;
		}
	};

	static class TestReduceGridAgent implements ReduceGridAgent {

		List<Serializable> keys;
		Map<Serializable, Serializable> map;

		public Object reduce(Session paramSession, ObjectMap paramObjectMap, Collection paramCollection) {
			if (keys != null) {
				return keys;
			} else {
				return new ArrayList<Serializable>(map.keySet());
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
