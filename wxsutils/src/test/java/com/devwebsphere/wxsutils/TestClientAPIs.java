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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.devwebsphere.wxsutils.WXSMapOfLists.BulkPushItem;
import com.devwebsphere.wxsutils.WXSMapOfSets.Contains;
import com.devwebsphere.wxsutils.filter.FalseFilter;
import com.devwebsphere.wxsutils.filter.Filter;
import com.devwebsphere.wxsutils.filter.FilterBuilder;
import com.devwebsphere.wxsutils.filter.ValuePath;
import com.devwebsphere.wxsutils.filter.path.PojoPropertyPath;
import com.devwebsphere.wxsutils.multijob.Person;
import com.devwebsphere.wxsutils.multijob.pingall.PingAllPartitionsJob;
import com.devwebsphere.wxsutils.wxsagent.WXSMapAgent;
import com.devwebsphere.wxsutils.wxsmap.BigListPushAgent;
import com.devwebsphere.wxsutils.wxsmap.WXSMapOfBigListsImpl;
import com.devwebsphere.wxsutils.wxsmap.dirtyset.DirtyKey;
import com.devwebsphere.wxsutils.wxsmap.dirtyset.FetchJobsFromAllDirtyListsJob;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;

/**
 * This test connects to a grid running on the same box. Use the gettingstarted example with the xml files in this
 * folder. These xmls just add a third Map which doesn't use client side caching.
 * 
 */
public class TestClientAPIs {
	static Logger logger = Logger.getLogger(TestClientAPIs.class.getName());

	static ObjectGrid ogclient;
	static WXSUtils utils;
	static BackingMap bmFarMap3;

	@BeforeClass
	public static void setupTest() throws Exception {
		// do everything in one JVM for test
		// ogclient = WXSUtils.startTestServer("Grid", "/objectgrid.xml",
		// "/deployment.xml");
		// switch to this to connect to remote grid instead.
		utils = WXSUtils.getDefaultUtils();
		ogclient = utils.getObjectGrid();
		bmFarMap3 = ogclient.getMap("FarMap3");
	}

	/**
	 * This clears the FarMap3 in preparation for any tests
	 */
	public static void clearMap() {
		try {
			ogclient.getSession().getMap("FarMap3").clear();
		} catch (ObjectGridException e) {
			e.printStackTrace();
			Assert.fail("Exception during clear");
		}
	}

	@Test
	public void testPut() {
		clearMap();
		WXSMap<String, String> map = utils.getCache(bmFarMap3.getName());
		String value = "B";
		map.put("A", value);
		Assert.assertTrue(map.contains("A"));
		String v = map.get("A");
		Assert.assertEquals(value, v);
	}

	@Test
	public void testLock() throws InterruptedException {
		clearMap();
		WXSMap<String, String> lockMap = utils.getCache("Locks");
		Assert.assertNotNull(lockMap);

		Assert.assertTrue(lockMap.lock("1", "Billy", 1000));
		lockMap.unlock("1");
		Assert.assertTrue(lockMap.lock("1", "Billy", 1000));
		Assert.assertFalse(lockMap.lock("1", "Billy", 1000));
		// Thread.currentThread().sleep(60 * 1000L);
		// Assert.assertTrue(lockMap.lock("1", "Billy", 1000));

	}

	@Test
	public void testCallMapAgentAll() {
		clearMap();
		WXSMap<String, String> map = utils.getCache(bmFarMap3.getName());

		int numKeys = 1000;
		for (int i = 0; i < numKeys; ++i) {
			map.put(Integer.toString(i), Integer.toString(i));
		}

		TestMapGridAgent agent = new TestMapGridAgent();

		Map<String, Object> rc = WXSMapAgent.callMapAgentAll(utils, agent, bmFarMap3);
		Assert.assertEquals(numKeys, rc.size());
		HashSet<String> keysFound = new HashSet<String>();
		for (Map.Entry<String, Object> e : rc.entrySet()) {
			String value = (String) e.getValue();
			Assert.assertEquals(e.getKey(), value);
			keysFound.add(e.getKey());
		}
		Assert.assertEquals(numKeys, keysFound.size());

		System.out.println(rc.toString());
	}

	/**
	 * This tests the basic putAll/getAll/removeAll capabilities
	 */
	@Test
	public void testSingletonPutAll() {
		clearMap();
		Map<String, String> batch = new HashMap<String, String>();
		batch.put("A", "B");
		utils.putAll(batch, bmFarMap3);

		ArrayList<String> keys = new ArrayList<String>();
		keys.add("A");

		Map<String, String> rc = utils.getAll(keys, bmFarMap3);

		Assert.assertEquals("B", rc.get("A"));

		utils.removeAll(keys, bmFarMap3);
		rc = utils.getAll(keys, bmFarMap3);

		Assert.assertNull(rc.get("A"));
	}

	@Test
	public void testPutAll() {
		clearMap();
		for (int k = 0; k < 10; ++k) {
			int base = k * 1000;
			Map<String, String> batch = new HashMap<String, String>();
			for (int i = base; i < base + 1000; ++i) {
				batch.put("" + i, "V" + i);
			}
			utils.putAll(batch, bmFarMap3);
		}

		for (int k = 0; k < 10; ++k) {
			int base = k * 1000;
			ArrayList<String> keys = new ArrayList<String>();
			for (int i = base; i < base + 1000; ++i) {
				keys.add("" + i);
			}
			Map<String, String> rc = utils.getAll(keys, bmFarMap3);

			for (Map.Entry<String, String> e : rc.entrySet()) {
				Assert.assertEquals("V" + e.getKey(), e.getValue());
			}

			utils.removeAll(keys, bmFarMap3);
			rc = utils.getAll(keys, bmFarMap3);

			for (Map.Entry<String, String> e : rc.entrySet()) {
				Assert.assertNull(e.getValue());
			}
		}
	}

	@Test
	public void testPutAllSingleItem() {
		clearMap();
		Map<String, String> batch = new HashMap<String, String>();
		batch.put("1", "V1");
		utils.putAll_noLoader(batch, bmFarMap3);

		ArrayList<String> keys = new ArrayList<String>();

		keys.add("1");
		Map<String, String> rc = utils.getAll(keys, bmFarMap3);

		for (Map.Entry<String, String> e : rc.entrySet()) {
			Assert.assertEquals("V" + e.getKey(), e.getValue());
		}

		utils.removeAll(keys, bmFarMap3);
		rc = utils.getAll(keys, bmFarMap3);

		for (Map.Entry<String, String> e : rc.entrySet()) {
			Assert.assertNull(e.getValue());
		}
	}

	@Test
	public void testPutAllNotComparable() {
		clearMap();
		for (int k = 0; k < 10; ++k) {
			int base = k * 1000;
			Map<SerializedKey, String> batch = new TreeMap<SerializedKey, String>(SerializedKey.COMPARATOR);
			for (int i = base; i < base + 1000; ++i) {
				batch.put(new SerializedKey("" + i), "V" + i);
			}
			utils.putAll(batch, bmFarMap3);
		}

		for (int k = 0; k < 10; ++k) {
			int base = k * 1000;
			TreeSet<SerializedKey> keys = new TreeSet<SerializedKey>(SerializedKey.COMPARATOR);
			for (int i = base; i < base + 1000; ++i) {
				keys.add(new SerializedKey("" + i));
			}
			Map<SerializedKey, String> rc = utils.getAll(keys, bmFarMap3);

			for (Map.Entry<SerializedKey, String> e : rc.entrySet()) {
				Assert.assertEquals("V" + e.getKey().id, e.getValue());
			}

			utils.removeAll(keys, bmFarMap3);
			rc = utils.getAll(keys, bmFarMap3);

			for (Map.Entry<SerializedKey, String> e : rc.entrySet()) {
				Assert.assertNull(e.getValue());
			}
		}
	}

	@Test
	public void testCond_PutAll() {
		clearMap();
		Map<String, String> original = new HashMap<String, String>();
		for (int i = 0; i < 10; ++i) {
			original.put("" + i, "V" + i);
		}
		utils.putAll(original, bmFarMap3);

		Map<String, String> newValues = new HashMap<String, String>();
		for (int i = 0; i < 11; ++i) {
			newValues.put("" + i, "N" + i);
		}
		WXSMap<String, String> map = utils.getCache(bmFarMap3.getName());
		map.put("4", "DIFFERENT");

		// try with maps different size, orig = 10, new = 11
		try {
			utils.cond_putAll(original, newValues, bmFarMap3);
			Assert.fail("Should have thrown exception");
		} catch (ObjectGridRuntimeException e) {
			// this is expected
		}
		// now make maps same size
		original.put("10", null); // 10 doesn't exist so only update 10 if 10
									// doesn't already exist
		Map<String, Boolean> rc = utils.cond_putAll(original, newValues, bmFarMap3);
		Assert.assertNotNull(rc);
		for (Map.Entry<String, Boolean> e : rc.entrySet()) {
			Boolean b = rc.get(e.getKey());
			Assert.assertNotNull(b);
			if (e.getKey().equals("4"))
				Assert.assertFalse(b);
			else
				Assert.assertTrue(b);
		}
		for (int i = 0; i < 11; ++i) {
			String v = map.get("" + i);
			if (i != 4) {
				Assert.assertEquals("N" + i, v);
			} else {
				Assert.assertEquals("DIFFERENT", v);
			}
		}
	}

	@Test
	public void testFailedInsertAll() {
		clearMap();
		Map<String, String> batch = new HashMap<String, String>();
		for (int k = 0; k < 10; ++k) {
			batch.put("" + k, "V" + k);
		}
		utils.insertAll(batch, bmFarMap3);
		try {
			utils.insertAll(batch, bmFarMap3);
			Assert.fail("Should have thrown exception");
		} catch (ObjectGridRuntimeException e) {
			Throwable cause = e.getCause();
			Assert.assertSame("Wrong nested exception", FailedKeysException.class, cause.getClass());
			FailedKeysException fke = (FailedKeysException) cause;
			Set<String> keys = fke.getKeys();
			Assert.assertEquals("Wrong size", batch.size(), keys.size());
		}
	}

	@Test
	public void testEmptyBulkOperations() {
		ArrayList<String> emptyList = new ArrayList<String>();
		utils.getAll(emptyList, bmFarMap3);
		utils.removeAll(emptyList, bmFarMap3);
		Map<String, String> emptyMap = new HashMap<String, String>();
		utils.putAll(emptyMap, bmFarMap3);
	}

	@Test
	public void testLeftListOperations() {
		WXSMapOfLists<String, String> map = utils.getMapOfLists("BigList");
		Assert.assertNotNull(map);
		String key = "TESTLEFT";
		int numItems = BigListPushAgent.BUCKET_SIZE * 5;

		for (int i = 0; i < numItems; ++i) {
			map.lpush(key, Integer.toString(i));
			Assert.assertEquals(i + 1, map.llen(key));

			ArrayList<String> list = map.lrange(key, 0, i);
			Assert.assertEquals(i + 1, list.size());
			int c = i;
			for (String s : list) {
				Assert.assertEquals(Integer.toString(c), s);
				c--;
			}
		}

	}

	@Test
	public void testBigListOperations() {
		WXSMapOfLists<String, String> map = utils.getMapOfLists("BigList");
		Assert.assertNotNull(map);
		String key = "TEST";
		int numItems = BigListPushAgent.BUCKET_SIZE * 5;
		long startTime = System.nanoTime();

		// check popping an empty list returns the right value
		String rc = map.lpop(key);
		Assert.assertNull(rc);
		Assert.assertTrue(map.isEmpty(key));
		rc = map.rpop(key);
		Assert.assertNull(rc);

		for (int i = 0; i < numItems; ++i) {
			map.rpush(key, "" + i);
			Assert.assertEquals(i + 1, map.llen(key));
			Assert.assertFalse(map.isEmpty(key));
		}

		for (int i = numItems; i >= 0; --i) {
			map.rtrim(key, i);
			Assert.assertEquals(i, map.llen(key));
			if (i != 0) {
				ArrayList<String> list = map.lrange(key, i - 1, i - 1);
				Assert.assertNotNull(list);
				Assert.assertEquals(1, list.size());
				list = map.lrange(key, 0, i - 1);
				Assert.assertNotNull(list);
				Assert.assertEquals(i, list.size());
				for (int j = 0; j < i; ++j)
					Assert.assertEquals("" + j, list.get(j));
				Assert.assertFalse(map.isEmpty(key));
			} else {
				Assert.assertTrue(map.isEmpty(key));
			}
		}

		for (int i = 0; i < numItems; ++i) {
			map.rpush(key, "" + i);
			Assert.assertEquals(i + 1, map.llen(key));
			Assert.assertFalse(map.isEmpty(key));
		}

		for (int j = 0; j < numItems; ++j) {
			ArrayList<String> result = map.lrange(key, j, numItems - 1);
			Assert.assertNotNull(result);
			Assert.assertEquals(numItems - j, result.size());
			for (int i = j; i < numItems; ++i) {
				Assert.assertEquals("" + i, result.get(i - j));
			}
		}

		for (int j = 0; j < numItems - 2; ++j) {
			ArrayList<String> result = map.lrange(key, j, j + 1);
			Assert.assertNotNull(result);
			Assert.assertEquals(2, result.size());
			for (int i = 0; i < 2; ++i) {
				Assert.assertEquals("" + (j + i), result.get(i));
			}
		}
		for (int i = 0; i < numItems; ++i) {
			String v = map.lpop(key);
			Assert.assertEquals("" + i, v);
			Assert.assertEquals(numItems - (i + 1), map.llen(key));
		}

		for (int i = 0; i < numItems; ++i) {
			map.lpush(key, "" + i);
			Assert.assertEquals(i + 1, map.llen(key));
		}

		for (int i = 0; i < numItems; ++i) {
			String v = map.rpop(key);
			Assert.assertEquals("" + i, v);
			Assert.assertEquals(numItems - (i + 1), map.llen(key));
		}

		for (int i = 0; i < numItems; ++i) {
			map.rpush(key, "" + i);
			Assert.assertEquals(i + 1, map.llen(key));
		}

		map.remove(key);
		Assert.assertEquals(0, map.llen(key));

		Assert.assertTrue(map.popAll(key).isEmpty());

		{
			for (int i = 0; i < 10; ++i) {
				map.rpush(key, Integer.toString(i));
			}
			Assert.assertEquals(10, map.llen(key));
			List<String> partial = map.lpop(key, 5);
			Assert.assertEquals(5, partial.size());
			int counter = 0;
			for (String s : partial) {
				Assert.assertEquals(Integer.toString(counter++), s);
			}
			partial = map.lpop(key, 10);
			Assert.assertEquals(0, map.llen(key));
			Assert.assertEquals(5, partial.size());
			for (String s : partial) {
				Assert.assertEquals(Integer.toString(counter++), s);
			}
		}

		{
			for (int i = 0; i < 10; ++i) {
				map.rpush(key, Integer.toString(i));
			}
			Assert.assertEquals(10, map.llen(key));
			int numRemoved = map.lremove(key, 5);
			List<String> leftMost = map.lrange(key, 0, 0);
			Assert.assertEquals(1, leftMost.size());
			Assert.assertEquals("5", leftMost.get(0));
			Assert.assertEquals(5, numRemoved);
			numRemoved = map.lremove(key, 10);
			Assert.assertEquals(0, map.llen(key));
			Assert.assertEquals(5, numRemoved);
		}

		{
			for (int i = 0; i < 10; ++i) {
				map.lpush(key, Integer.toString(i));
			}
			Assert.assertEquals(10, map.llen(key));
			List<String> partial = map.rpop(key, 5);
			Assert.assertEquals(5, partial.size());
			int counter = 0;
			for (String s : partial) {
				Assert.assertEquals(Integer.toString(counter++), s);
			}
			partial = map.rpop(key, 10);
			Assert.assertEquals(0, map.llen(key));
			Assert.assertEquals(5, partial.size());
			for (String s : partial) {
				Assert.assertEquals(Integer.toString(counter++), s);
			}
		}
		long endTime = System.nanoTime();
		double duration = (endTime - startTime) / 1000000.0;
		logger.log(Level.INFO, "Took " + duration);
	}

	@Test
	public void testConditionalPush() {
		WXSMapOfLists<String, Person> map = utils.getMapOfLists("PersonList");
		String key = "key";

		map.remove(key);
		ValuePath firstName = new PojoPropertyPath("FirstName");
		ValuePath surname = new PojoPropertyPath("Surname");

		FilterBuilder fb = new FilterBuilder();
		Filter falseFilter = new FalseFilter();
		Filter isBillyFilter = fb.and(fb.eq(firstName, "Billy"), fb.eq(surname, "Newport"));

		Person billy = new Person();
		billy.setFirstName("Billy");
		billy.setSurname("Newport");
		billy.setCreditLimit(1);
		billy.setMiddleInitial("A");
		Assert.assertTrue(isBillyFilter.filter(billy));

		Person bobby = new Person();
		bobby.setFirstName("Bobby");
		bobby.setSurname("Newport");
		bobby.setCreditLimit(1);
		bobby.setMiddleInitial("I");
		Assert.assertFalse(isBillyFilter.filter(bobby));

		Assert.assertTrue(map.isEmpty(key));
		map.lcpush(key, bobby, isBillyFilter);
		Assert.assertFalse(map.isEmpty(key));
		map.lcpush(key, billy, isBillyFilter);
		Assert.assertEquals(2, map.llen(key));

		// just keep pushing some values and adding them
		int numExtra = 50;
		for (int i = 0; i < numExtra; ++i) {
			if (map.llen(key) == 21) {
				System.out.println("Len is " + map.llen(key));
			}
			map.lcpush(key, billy, falseFilter);
			Assert.assertEquals(3 + i, map.llen(key));
		}

		map.lcpush(key, billy, isBillyFilter);
		Assert.assertEquals(2 + numExtra, map.llen(key)); // no push this time
	}

	@Test
	public void testListOperations() {
		WXSMapOfLists<String, String> map = utils.getMapOfLists("BigList");
		Assert.assertNotNull(map);
		String key = "TEST";
		map.remove(key);
		Assert.assertTrue(map.isEmpty(key));
		int numItems = 10;
		for (int i = 0; i < numItems; ++i) {
			map.rpush(key, "" + i);
			Assert.assertEquals(i + 1, map.llen(key));
			Assert.assertFalse(map.isEmpty(key));
		}
		map.rtrim(key, numItems - 2);
		Assert.assertEquals(numItems - 2, map.llen(key));
		String item2 = map.rpop(key);
		Assert.assertEquals("" + (numItems - 3), item2);
		map.rpush(key, "" + (numItems - 3));
		map.rpush(key, "" + (numItems - 2));
		map.rpush(key, "" + (numItems - 1));
		Assert.assertEquals(numItems, map.llen(key));

		ArrayList<String> subList = map.lrange(key, 2, 4);
		Assert.assertEquals(3, subList.size());
		for (int i = 0; i < 3; ++i) {
			Assert.assertEquals("" + (i + 2), subList.get(i));
		}
		for (int i = 0; i < numItems; ++i) {
			String v = map.lpop(key);
			Assert.assertEquals("" + i, v);
			Assert.assertEquals(numItems - (i + 1), map.llen(key));
		}
		map.remove(key);
		Assert.assertEquals(0, map.llen(key));
		Assert.assertTrue(map.isEmpty(key));

		for (int i = 0; i < numItems; ++i) {
			map.lpush(key, "" + i);
			Assert.assertEquals(i + 1, map.llen(key));
		}
		for (int i = 0; i < numItems; ++i) {
			String v = map.rpop(key);
			Assert.assertEquals("" + i, v);
			Assert.assertEquals(numItems - (i + 1), map.llen(key));
		}
	}

	@Test
	public void testBulkPushOperations() {
		WXSMapOfLists<String, String> map = utils.getMapOfLists("BigList");
		String key = "BULK_LIST";

		Assert.assertEquals(0, map.llen(key));
		List<String> items = new ArrayList<String>();
		items.add("0");
		items.add("1");
		items.add("2");

		map.lpush(key, items); // list is now 2, 1, 0

		Assert.assertEquals(3, map.llen(key));
		items = map.lrange(key, 0, 2);
		Assert.assertEquals(3, items.size());
		Assert.assertEquals("2", items.get(0));
		Assert.assertEquals("1", items.get(1));
		Assert.assertEquals("0", items.get(2));

		map.lpush(key, "4");
		items = map.lrange(key, 0, 0);
		Assert.assertEquals(1, items.size());
		Assert.assertEquals("4", items.get(0));

		map.remove(key);

		// now do right push
		Assert.assertEquals(0, map.llen(key));
		items = new ArrayList<String>();
		items.add("0");
		items.add("1");
		items.add("2");

		map.rpush(key, items); // list is now 0, 1, 2

		Assert.assertEquals(3, map.llen(key));
		items = map.lrange(key, 0, 2);
		Assert.assertEquals(3, items.size());
		Assert.assertEquals("0", items.get(0));
		Assert.assertEquals("1", items.get(1));
		Assert.assertEquals("2", items.get(2));

		map.rpush(key, "4");
		items = map.lrange(key, 3, 3);
		Assert.assertEquals(1, items.size());
		Assert.assertEquals("4", items.get(0));

		{
			Map<String, List<BulkPushItem<String>>> bulkItems = new HashMap<String, List<BulkPushItem<String>>>();

			int numItemsToPush = 3;
			int numListsToTest = 10;
			for (int i = 0; i < numListsToTest; ++i) {
				ArrayList<BulkPushItem<String>> itemsToPush = new ArrayList<BulkPushItem<String>>();
				for (int j = 0; j < numItemsToPush; ++j)
					itemsToPush.add(new BulkPushItem<String>("ITEM #" + j + " for " + i, null));
				bulkItems.put(Integer.toString(i), itemsToPush);
			}
			map.rpush(bulkItems, "BULK_DIRTY");

			List<String> dirtySet = FetchJobsFromAllDirtyListsJob.getAllDirtyKeysInGrid(ogclient, "BigList", "BULK_DIRTY");
			Assert.assertEquals(numListsToTest, dirtySet.size());

			for (String dirtyKey : dirtySet) {
				Assert.assertEquals(numItemsToPush, map.llen(dirtyKey));
				List<String> v = map.popAll(dirtyKey);
				Assert.assertEquals(numItemsToPush, v.size());
				Set<String> vset = new HashSet<String>();
				vset.addAll(v);
				for (int j = 0; j < numItemsToPush; ++j) {
					Assert.assertTrue(vset.contains("ITEM #" + j + " for " + dirtyKey));
				}
			}
		}

		{
			// test big pushes
			int numItems = 25000;

		}
	}

	@Test
	public void testBulkPushLimit() {
		WXSMapOfLists<String, String> map = utils.getMapOfLists("BigListLimit");
		String key = "BULK_LIST";

		Assert.assertEquals(0, map.llen(key));
		List<String> items = new ArrayList<String>();
		items.add("0");
		items.add("1");
		items.add("2");

		int previousLimit = BigListPushAgent.LIMIT;
		try {
			BigListPushAgent.LIMIT = 1;
			map.lpush(key, items); // list is now 2, 1, 0
			Assert.fail("Limit not engaged");
		} catch (ObjectGridRuntimeException e) {
		} finally {
			BigListPushAgent.LIMIT = previousLimit;
		}
	}

	@Test
	public void testSetOperations() {
		WXSMapOfSets<String, String> map = utils.getMapOfSets("Set");
		Assert.assertNotNull(map);
		String key = "TEST";

		Assert.assertNull(map.get(key));

		int numKeys = 10;
		for (int i = 0; i < numKeys; ++i) {
			map.add(key, "" + i);
			Assert.assertEquals(i + 1, map.size(key));
			Assert.assertTrue(map.contains(key, Contains.ALL, "" + i));
		}
		map.add(key, "" + 0);
		Assert.assertEquals(numKeys, map.size(key));
		Assert.assertFalse(map.isEmpty(key));
		map.remove(key, "" + 0);
		Assert.assertFalse(map.contains(key, Contains.ALL, "" + 0));
		Assert.assertEquals(numKeys - 1, map.size(key));
		Set<String> values = map.get(key);
		Assert.assertNotNull(values);
		for (int i = 1; i < numKeys; ++i) {
			Assert.assertTrue(values.contains("" + i));
		}
		map.remove(key);
		Assert.assertNull(map.get(key));
		Assert.assertEquals(0, map.size(key));
		Assert.assertTrue(map.isEmpty(key));

		int maxSize = 3;
		for (int i = 0; i <= maxSize; ++i) {
			Set<String> ss = map.addAndFlush(key, maxSize, Integer.toString(i));
			Assert.assertNotNull(ss);
			Assert.assertEquals(0, ss.size());
		}
		Set<String> ss = map.addAndFlush(key, maxSize, "4");
		Assert.assertNotNull(ss);
		Assert.assertEquals(maxSize + 1, ss.size());
		Assert.assertEquals(1, map.size(key));
	}

	public boolean isDirty(String mapName, String dirtyKey, String listKey) {
		List<String> set = FetchJobsFromAllDirtyListsJob.getAllDirtyKeysInGrid(ogclient, mapName, dirtyKey);
		return (set.contains(listKey));
	}

	@Test
	public void testDirtySet() {
		String dirtyKey = "DIRTY";
		WXSMapOfSets<String, String> dirtySetMap = utils.getMapOfSets(WXSMapOfBigListsImpl.getListDirtySetMapName("BigList"));
		WXSMapOfLists<String, String> listMap = utils.getMapOfLists("BigList");

		// check set is empty
		Assert.assertEquals(0, dirtySetMap.size(dirtyKey));

		// check key L isn't in dirtySet
		Assert.assertFalse(isDirty("BigList", dirtyKey, "L"));

		// check with no dirty set key specified, the dirtySet doesn't change
		listMap.lpush("L", "HELLO");
		Assert.assertFalse(isDirty("BigList", dirtyKey, "L"));
		String rc = listMap.lpop("L");
		Assert.assertEquals("HELLO", rc);

		// now check it changes
		listMap.lpush("L", "HELLO", dirtyKey);

		// is L in the dirtySet?
		Assert.assertTrue(isDirty("BigList", dirtyKey, "L"));
		listMap.lpush("L", "BYE", dirtyKey);

		// check it's still there
		Assert.assertTrue(isDirty("BigList", dirtyKey, "L"));

		// one pop should leave it in there
		Assert.assertEquals("BYE", listMap.lpop("L", dirtyKey));
		Assert.assertTrue(isDirty("BigList", dirtyKey, "L"));

		// another pop should empty the list and remove it
		Assert.assertEquals("HELLO", listMap.lpop("L", dirtyKey));
		Assert.assertFalse(isDirty("BigList", dirtyKey, "L"));

		listMap.lpush("L", "0", dirtyKey);
		Assert.assertTrue(isDirty("BigList", dirtyKey, "L"));
		listMap.lpush("L", "1", dirtyKey);
		Assert.assertTrue(isDirty("BigList", dirtyKey, "L"));

		Assert.assertEquals(2, listMap.llen("L"));

		ArrayList<String> allValues = listMap.popAll("L", dirtyKey);
		Assert.assertFalse(isDirty("BigList", dirtyKey, "L")); // not dirty any
																// more
		Assert.assertEquals(2, allValues.size());
		Assert.assertTrue(allValues.contains("0"));
		Assert.assertTrue(allValues.contains("1"));
	}

	@Test
	public void testDirtySetSize() {
		String dirtyKey = "DIRTY2";
		WXSMapOfLists<String, String> listMap = utils.getMapOfLists("BigList");

		List<String> set = FetchJobsFromAllDirtyListsJob.getAllDirtyKeysInGrid(ogclient, "BigList", dirtyKey);
		Assert.assertEquals(0, set.size());

		int numKeys = 10;
		Set<String> keys = new HashSet<String>();
		for (int i = 0; i < numKeys; i++) {
			String key = UUID.randomUUID().toString();
			keys.add(key);
			listMap.lpush(key, "HELLO" + i, dirtyKey);
		}

		set = FetchJobsFromAllDirtyListsJob.getAllDirtyKeysInGrid(ogclient, "BigList", dirtyKey);
		Assert.assertEquals(keys.size(), set.size());

		int jobCounter = numKeys;
		while (true) {
			set = FetchJobsFromAllDirtyListsJob.getAllDirtyKeysInGrid(ogclient, "BigList", dirtyKey);

			if (set != null) {
				if (set.size() == 0)
					break;
			}

			for (String key : set) {
				String value = listMap.lpop(key, dirtyKey);
				if (value != null)
					--jobCounter;
			}

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, "Exception", e);
				Assert.fail();
			}
		}
		Assert.assertEquals(0, jobCounter);
	}

	@Test
	public void testDirtySetSizeLease() throws InterruptedException {
		String dirtyKey = "DIRTY3";
		WXSMapOfLists<String, String> listMap = utils.getMapOfLists("BigList");

		List<String> set = FetchJobsFromAllDirtyListsJob.getAllDirtyKeysInGrid(ogclient, "BigList", dirtyKey);
		Assert.assertEquals(0, set.size());

		int numKeys = 10;
		Set<String> keys = new HashSet<String>();
		for (int i = 0; i < numKeys; i++) {
			String key = UUID.randomUUID().toString();
			keys.add(key);
			listMap.lpush(key, "HELLO" + i, dirtyKey);
			listMap.lpush(key, "HELLO" + i, dirtyKey); // push twice for rremove
														// test below
		}

		// get keys with lock for 10 seconds
		set = FetchJobsFromAllDirtyListsJob.getAllDirtyKeysInGrid(ogclient, "BigList", dirtyKey, 10000L); // lease
																											// is
																											// 10
																											// seconds
		Assert.assertEquals(keys.size(), set.size());

		// get them again during lock period, should get none
		List<String> set2 = FetchJobsFromAllDirtyListsJob.getAllDirtyKeysInGrid(ogclient, "BigList", dirtyKey, 10000L); // lease
																														// is
																														// 10
																														// seconds
		Assert.assertEquals(0, set2.size());

		System.out.println("Waiting 20 seconds for lock to expire, don't panic...");
		// wait for lock to expire
		Thread.sleep(20000L);

		// fetch again and they should be there
		set2 = FetchJobsFromAllDirtyListsJob.getAllDirtyKeysInGrid(ogclient, "BigList", dirtyKey, 10000L); // lease
																											// is
																											// 10
																											// seconds
		Assert.assertEquals(keys.size(), set2.size());
		// unlock first key
		String firstKey = set2.get(0);
		int numItems = listMap.lremove(firstKey, 1, dirtyKey, true);
		// cannot be zero as this removes lease breaking what we're testing for.
		Assert.assertFalse(listMap.isEmpty(firstKey));
		Assert.assertEquals(1, numItems);
		// fetch again and only that key should be there
		set2 = FetchJobsFromAllDirtyListsJob.getAllDirtyKeysInGrid(ogclient, "BigList", dirtyKey, 10000L); // lease
																											// is
																											// 10
																											// seconds
		Assert.assertEquals(1, set2.size());
		Assert.assertEquals(firstKey, set2.get(0));
	}

	@Test
	public void testPingAllPartitions() {
		int partitionCount = PingAllPartitionsJob.visitAllPartitions(ogclient);
		Assert.assertEquals(ogclient.getMap("Set").getPartitionManager().getNumOfPartitions(), partitionCount);
	}

	@Test
	public void testListEviction() {
		WXSMapOfLists<String, String> list = utils.getMapOfLists("EvictionList");

		list.lpush("1", "12");
		list.evict("1", EvictionType.FIXED, 30);
	}

	@Test
	public void testMultiThread() throws InterruptedException {
		final String dirtyKey = "DIRTY_MULTI_THREAD";
		final String key = "M";
		final String listName = "BigList";
		final WXSMapOfLists<String, String> listMap = utils.getMapOfLists(listName);
		final String listHeadMapName = WXSMapOfBigListsImpl.getListHeadMapName(listName);
		int numPushers = 200;
		final int numKeys = 1000;
		final int numPushesPerPusher = 500;
		final CountDownLatch counter = new CountDownLatch(numPushers * numPushesPerPusher);

		final Map<String, AtomicLong> keyPushCounters = new HashMap<String, AtomicLong>();
		for (int i = 0; i < numKeys; ++i) {
			keyPushCounters.put(key + "#" + i, new AtomicLong());
		}

		Runnable puller = new Runnable() {
			public void run() {
				try {
					while (counter.getCount() > 0) {
						// make a FetchJob to scan grid for dirty keys
						FetchJobsFromAllDirtyListsJob<String, String> job = new FetchJobsFromAllDirtyListsJob<String, String>(ogclient,
								listHeadMapName, dirtyKey);

						ArrayList<DirtyKey<String>> r = null;
						// get the next block of dirty list keys
						// this array r is sorted in first dirtied order
						// when r is null then the whole grid has been checked
						while ((r = job.getNextResult()) != null) {
							for (DirtyKey<String> dk : r) {
								String aKey = dk.getValue();
								ArrayList<String> pList = listMap.popAll(aKey, dirtyKey);
								if (pList != null && pList.size() > 0) {
									System.out.println("Pulled " + pList.size() + " from " + aKey + ": Remaining = " + counter.getCount());
									// count down for each job retrieved
									for (String s : pList) {
										// value MUST be same as key
										Assert.assertEquals(aKey, s);
										// one less value for this list and
										// sanity check
										Assert.assertTrue(keyPushCounters.get(aKey).decrementAndGet() >= 0);
										counter.countDown();
									}
								}
							}
						}
					}
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Exception", e);
					Assert.fail();
				}
			}
		};

		Thread pullerThread = new Thread(puller);
		pullerThread.start();

		ArrayList<Thread> allPushers = new ArrayList<Thread>();

		for (int i = 0; i < numPushers; ++i) {
			Runnable pusher = new Runnable() {
				int pushesToGo = numPushesPerPusher;

				public void run() {
					try {
						while (pushesToGo > 0) {
							String aKey = key + "#" + (pushesToGo % numKeys);
							// twenty keys
							// added one more for aKey
							keyPushCounters.get(aKey).incrementAndGet();
							// push list key as value
							listMap.lpush(aKey, aKey, dirtyKey);
							pushesToGo--;
							try {
								// Thread.currentThread().wait(10);
							} catch (Exception e) {
								logger.log(Level.SEVERE, "Exception", e);
								Assert.fail();
							}
						}
					} catch (Exception e) {
						logger.log(Level.SEVERE, "Exception", e);
						Assert.fail();
					}
				}
			};

			Thread pusherThread = new Thread(pusher);
			allPushers.add(pusherThread);
			pusherThread.start();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, "Exception", e);
				Assert.fail();
			}
		}

		for (Thread t : allPushers)
			t.join();

		pullerThread.join();
		Assert.assertEquals(0, counter.getCount());
		// check all keys had the exact number removed as were pushed
		for (AtomicLong a : keyPushCounters.values()) {
			Assert.assertEquals(0, a.get());
		}
	}

	@Test
	public void testMultiThreadPushPop() throws InterruptedException {
		final String dirtyKey = "DIRTY4";
		final String key = "N";
		final String listName = "BigList";
		final WXSMapOfLists<String, String> listMap = utils.getMapOfLists(listName);
		int numPushers = 50;
		final int numKeys = 20;
		final int numPushesPerPusher = 100;
		final CountDownLatch counter = new CountDownLatch(numPushers * numPushesPerPusher);
		final String listHeadMapName = WXSMapOfBigListsImpl.getListHeadMapName(listName);

		final Map<String, AtomicLong> keyPushCounters = new HashMap<String, AtomicLong>();
		for (int i = 0; i < numKeys; ++i) {
			keyPushCounters.put(key + "#" + i, new AtomicLong());
		}

		Runnable puller = new Runnable() {
			public void run() {
				try {
					// while there are list elements remaining
					while (counter.getCount() > 0) {
						// make a FetchJob to scan grid for dirty keys
						FetchJobsFromAllDirtyListsJob<String, String> job = new FetchJobsFromAllDirtyListsJob<String, String>(ogclient,
								listHeadMapName, dirtyKey);
						job.setLeaseTimeMS(100L /* ms */);

						ArrayList<DirtyKey<String>> r = null;
						// get the next block of dirty list keys
						// this array r is sorted in first dirtied order
						// when r is null then the whole grid has been checked
						while ((r = job.getNextResult()) != null) {
							for (DirtyKey<String> dk : r) {
								String aKey = dk.getValue();
								String aValue = listMap.rpop(aKey, dirtyKey);
								if (aValue != null) {
									System.out.println("Pulled from " + aKey + ": Remaining = " + counter.getCount());
									// value MUST be same as key
									Assert.assertEquals(aKey, aValue);
									// one less value for this list and sanity
									// check
									Assert.assertTrue(keyPushCounters.get(aKey).decrementAndGet() >= 0);
									counter.countDown();
								}
							}
						}
					}
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Exception", e);
					Assert.fail();
				}
			}
		};

		Thread pullerThread = new Thread(puller);
		pullerThread.start();

		ArrayList<Thread> allPushers = new ArrayList<Thread>();

		for (int i = 0; i < numPushers; ++i) {
			Runnable pusher = new Runnable() {
				int pushesToGo = numPushesPerPusher;

				public void run() {
					try {
						while (pushesToGo > 0) {
							String aKey = key + "#" + (pushesToGo % numKeys);
							// twenty keys
							// added one more for aKey
							keyPushCounters.get(aKey).incrementAndGet();
							// push list key as value
							listMap.lpush(aKey, aKey, dirtyKey);
							pushesToGo--;
							try {
								// Thread.currentThread().wait(10);
							} catch (Exception e) {
								logger.log(Level.SEVERE, "Exception", e);
								Assert.fail();
							}
						}
					} catch (Exception e) {
						logger.log(Level.SEVERE, "Exception", e);
						Assert.fail();
					}
				}
			};

			Thread pusherThread = new Thread(pusher);
			allPushers.add(pusherThread);
			pusherThread.start();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				logger.log(Level.SEVERE, "Exception", e);
				Assert.fail();
			}
		}

		for (Thread t : allPushers)
			t.join();

		pullerThread.join();
		Assert.assertEquals(0, counter.getCount());
		// check all keys had the exact number removed as were pushed
		for (AtomicLong a : keyPushCounters.values()) {
			Assert.assertEquals(0, a.get());
		}
	}

	@Test
	public void testMaxKeysPerTrip() {
		final String dirtyKey = "DIRTY_MAXKEYS";
		final String key = "N";
		final String listName = "BigList";
		final WXSMapOfLists<String, String> listMap = utils.getMapOfLists(listName);
		final String listHeadMapName = WXSMapOfBigListsImpl.getListHeadMapName(listName);

		HashSet<String> allKeys = new HashSet<String>();
		final int totalKeys = 5000;
		for (int i = 0; i < totalKeys; ++i) {
			String aKey = key + i;
			listMap.lpush(aKey, aKey, dirtyKey);
			allKeys.add(aKey);
		}

		FetchJobsFromAllDirtyListsJob<String, String> job = new FetchJobsFromAllDirtyListsJob<String, String>(ogclient, listHeadMapName, dirtyKey);
		job.setLeaseTimeMS(100L /* ms */);

		ArrayList<DirtyKey<String>> r = null;
		// get the next block of dirty list keys
		// this array r is sorted in first dirtied order
		// when r is null then the whole grid has been checked
		int numDesiredKeys = 2;
		int keyIdx = 0;
		int loopCnt = 0;
		while ((r = job.getNextResult(numDesiredKeys)) != null) {
			loopCnt++;
			Assert.assertTrue("Wrong size", r.size() <= numDesiredKeys);
			for (DirtyKey<String> dk : r) {
				String aKey = dk.getValue();
				String aValue = listMap.rpop(aKey, dirtyKey);
				if (aValue != null) {
					// value MUST be same as key
					Assert.assertEquals(aKey, aValue);
					allKeys.remove(aKey);
					keyIdx++;
				}
			}
		}
		// System.out.println("loopCount = " + loopCnt);
		Assert.assertEquals(totalKeys, keyIdx);
		Assert.assertEquals(0, allKeys.size());
	}

	@Test
	public void testReleaseLease() {
		final String dirtyKey = "DIRTY_LEASEDKEYS";
		final String key = "N";
		final String listName = "BigList";
		final WXSMapOfLists<String, String> listMap = utils.getMapOfLists(listName);
		final String listHeadMapName = WXSMapOfBigListsImpl.getListHeadMapName(listName);

		HashSet<String> allKeys = new HashSet<String>();
		final int totalKeys = 5;
		for (int i = 0; i < totalKeys; ++i) {
			String aKey = key + i;
			// listMap.lpush(aKey, aKey, dirtyKey);
			listMap.lpush(key, aKey, dirtyKey);
			allKeys.add(aKey);
		}

		FetchJobsFromAllDirtyListsJob<String, String> job = new FetchJobsFromAllDirtyListsJob<String, String>(ogclient, listHeadMapName, dirtyKey);
		job.setLeaseTimeMS(5000L /* ms */);
		List<DirtyKey<String>> r1 = getOneKey(job);
		Assert.assertNotNull(r1);
		Assert.assertEquals(1, r1.size());
		List<DirtyKey<String>> r2 = getOneKey(job);
		Assert.assertNull(r2);

		listMap.lpop(r1.get(0).getValue(), 1, dirtyKey, true);
		List<DirtyKey<String>> r3 = getOneKey(job);
		// lease obtained
		Assert.assertNotNull(r3);
		Assert.assertEquals(1, r3.size());
		Assert.assertEquals(r1, r3);

		List<DirtyKey<String>> r4 = getOneKey(job);
		Assert.assertNull(r4);
		int blen = listMap.llen(key);
		listMap.rremove(key, 0, dirtyKey, true);
		int alen = listMap.llen(key);
		r4 = getOneKey(job);
		Assert.assertNotNull(r4);
		Assert.assertEquals(blen, alen);
	}

	private List<DirtyKey<String>> getOneKey(FetchJobsFromAllDirtyListsJob<String, String> job) {
		List<DirtyKey<String>> r = Collections.emptyList();
		while (r != null) {
			r = job.getNextResult(1);
			if (r != null && r.size() > 0) {
				return r;
			}
		}

		return null;
	}
}
