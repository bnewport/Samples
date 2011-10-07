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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.devwebsphere.wxs.asyncservice.KeyOperator;
import com.devwebsphere.wxs.asyncservice.KeyOperatorResult;
import com.devwebsphere.wxs.asyncserviceimpl.AsyncServiceManagerImpl;
import com.devwebsphere.wxs.asyncserviceimpl.RoutableKey;
import com.devwebsphere.wxsutils.WXSMap;
import com.devwebsphere.wxsutils.WXSUtils;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.Session;

/**
 * Make sure when running this as a WXS client to start a JMX service using the command line
 * -Dcom.sun.management.jmxremote
 * 
 * @author bnewport
 * 
 */
public class BasicTest {
	static ObjectGrid client;
	static WXSUtils utils;
	static WXSMap<String, InventoryLevel> skuMap;
	static AsyncServiceManagerImpl asManager;

	@BeforeClass
	public static void startTestServer() throws ObjectGridException, FileNotFoundException, IOException, URISyntaxException {
		utils = WXSUtils.getDefaultUtils();
		client = utils.getObjectGrid();
		skuMap = utils.getCache(TestMapNames.skuMap);
		asManager = new AsyncServiceManagerImpl(utils);
	}

	private static void initializeTest() {
		skuMap.clear();

		// sku 0 is out of stock, the skus from 1 to 99 are in stock
		for (int sku = 0; sku < 100; ++sku) {
			InventoryLevel level = new InventoryLevel();
			level.availableInTransit = 0;
			if (sku != 0)
				level.availableToSell = 1000000;
			else
				level.availableToSell = 0;
			level.demand = 0;
			level.reserved = 0;
			String skuString = Integer.toString(sku);
			int actualPartitionId = client.getMap(TestMapNames.skuMap).getPartitionManager().getPartition(skuString);
			RoutableKey rKey = new RoutableKey(actualPartitionId, skuString);
			int fakePartitionId = client.getMap(com.devwebsphere.wxs.asyncserviceimpl.MapNames.QUEUE_MAP).getPartitionManager().getPartition(rKey);
			Assert.assertEquals(actualPartitionId, fakePartitionId);
			skuMap.put(skuString, level);
		}
	}

	@Test
	public void testShoppingCartOneItem() throws Exception {
		initializeTest();

		ShoppingCartWorker worker1 = new ShoppingCartWorker();
		worker1.quantity = 1;

		Map<String, KeyOperator<String>> cartCommand = new HashMap<String, KeyOperator<String>>();
		cartCommand.put("1", worker1);
		Session session = utils.getObjectGrid().getSession();
		Future<Map<String, KeyOperatorResult<String>>> rc = asManager.doChainedTransaction(session, TestMapNames.skuMap, cartCommand);

		while (!rc.isDone()) {
			Thread.sleep(1000);
		}
	}

	@Test
	public void testShoppingCartFiveItems() throws Exception {
		initializeTest();

		Map<String, KeyOperator<String>> cartCommand = new HashMap<String, KeyOperator<String>>();
		for (int sku = 1; sku <= 5; ++sku) {
			ShoppingCartWorker worker1 = new ShoppingCartWorker();
			worker1.quantity = 1;
			cartCommand.put(Integer.toString(sku), worker1);
		}
		Session session = utils.getObjectGrid().getSession();
		Future<Map<String, KeyOperatorResult<String>>> rc = asManager.doChainedTransaction(session, TestMapNames.skuMap, cartCommand);
		while (!rc.isDone()) {
			Thread.sleep(1000);
		}
	}

	@Test
	public void testShoppingCartOneItemOutOfStock() throws Exception {
		initializeTest();
		ShoppingCartWorker worker1 = new ShoppingCartWorker();
		worker1.quantity = 1;

		Map<String, KeyOperator<String>> cartCommand = new HashMap<String, KeyOperator<String>>();
		cartCommand.put("0", worker1);
		Session session = utils.getObjectGrid().getSession();
		Future<Map<String, KeyOperatorResult<String>>> rc = asManager.doChainedTransaction(session, TestMapNames.skuMap, cartCommand);

		while (!rc.isDone()) {
			Thread.sleep(1000);
		}
	}

	@Test
	public void testShoppingCartFiveItemsOutOfStock() throws Exception {
		initializeTest();

		Map<String, KeyOperator<String>> cartCommand = new HashMap<String, KeyOperator<String>>();
		for (int sku = 0; sku < 5; ++sku) {
			ShoppingCartWorker worker1 = new ShoppingCartWorker();
			worker1.quantity = 1;
			cartCommand.put(Integer.toString(sku), worker1);
		}
		Session session = utils.getObjectGrid().getSession();
		Future<Map<String, KeyOperatorResult<String>>> rc = asManager.doChainedTransaction(session, TestMapNames.skuMap, cartCommand);
		while (!rc.isDone()) {
			Thread.sleep(1000);
		}
	}

	public void testPerformanceSingleThread() throws ObjectGridException, InterruptedException {
		initializeTest();
		Map<String, KeyOperator<String>> cartCommand = new HashMap<String, KeyOperator<String>>();
		for (int sku = 1; sku <= 5; ++sku) {
			ShoppingCartWorker worker1 = new ShoppingCartWorker();
			worker1.quantity = 1;
			cartCommand.put(Integer.toString(sku), worker1);
		}
		Session session = utils.getObjectGrid().getSession();
		long start = System.currentTimeMillis();
		ArrayList<Future<Map<String, KeyOperatorResult<String>>>> futures = new ArrayList<Future<Map<String, KeyOperatorResult<String>>>>();
		int maxChains = 1;
		for (int i = 0; i < maxChains; ++i) {
			Future<Map<String, KeyOperatorResult<String>>> rc = asManager.doChainedTransaction(session, TestMapNames.skuMap, cartCommand);
			futures.add(rc);
		}
		long phase1 = System.currentTimeMillis();
		System.out.println("Waiting");
		for (int i = 0; i < maxChains; ++i) {
			Future<Map<String, KeyOperatorResult<String>>> rc = futures.get(i);
			while (!rc.isDone()) {
				Thread.sleep(1);
			}
		}
		long phase2 = System.currentTimeMillis();
		double rate = maxChains / ((phase1 - start) / 1000.0);
		System.out.println("Phase 1 is " + rate);
		rate = maxChains / ((phase2 - start) / 1000.0);
		System.out.println("Phase 2 is " + rate);
	}
}
