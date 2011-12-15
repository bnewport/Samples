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
package com.devwebsphere.wxsutils.continuousfilter;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;

import javax.jms.JMSException;

import junit.framework.Assert;

import org.apache.activemq.broker.BrokerService;
import org.junit.BeforeClass;
import org.junit.Test;

import com.devwebsphere.wxsutils.WXSMap;
import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.continuousfilter.CFMessage.Operation;
import com.devwebsphere.wxsutils.filter.Filter;
import com.devwebsphere.wxsutils.notification.Connection;
import com.devwebsphere.wxsutils.notification.Producer;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;

/**
 * This test connects to a grid running on the same box. Use the gettingstarted example with the xml files in this
 * folder. These xmls just add a third Map which doesn't use client side caching.
 * 
 */
public class TestContFilter {
	private static final String FILTERED_MAP = "FilteredMap";

	static Logger logger = Logger.getLogger(TestContFilter.class.getName());

	static ObjectGrid ogclient;
	static WXSUtils utils;

	static class StringFilter extends Filter {

		public StringFilter() {

		}

		@Override
		public boolean filter(Object o) {
			return o instanceof String;
		}
	};

	static class NumberFilter extends Filter {

		public NumberFilter() {

		}

		@Override
		public boolean filter(Object o) {
			return o instanceof Number;
		}
	};

	@BeforeClass
	public static void setupTest() throws Exception {
		ogclient = WXSUtils.startTestServer("Grid", "/contfilter/objectgrid.xml", "/contfilter/deployment.xml");
		// switch to this to connect to remote grid instead.
		// ogclient = WXSUtils.connectClient("localhost:2809", "Grid",
		// "/multijob/multijob_objectgrid.xml");
		utils = new WXSUtils(ogclient);

		startBroker();
	}

	private static void startBroker() throws Exception {
		BrokerService broker = new BrokerService();

		// configure the broker
		broker.addConnector("tcp://localhost:61616");
		broker.start();
	}

	@Test
	public void setFilter() throws JMSException {
		ActiveMQTransport t = new ActiveMQTransport();
		WXSMap<Producer, LinkedList<?>> registrationsMap = utils.getCache(CFAgent.getRegistrationMap(FILTERED_MAP));
		Producer p = t.createProducer();
		Object regs = registrationsMap.get(p);
		Assert.assertNull(regs);

		ContinuousFilter<Serializable, Serializable> cq = new ContinuousFilter<Serializable, Serializable>(utils, FILTERED_MAP, t);
		boolean b = cq.setFilter(new StringFilter());
		Assert.assertTrue(b);
		regs = registrationsMap.get(p);
		Assert.assertNotNull(regs);
		cq.close();
		regs = registrationsMap.get(p);
		Assert.assertNull(regs);
	}

	@Test(expected = ObjectGridRuntimeException.class)
	public void missingMap() throws JMSException {
		new ContinuousFilter<Serializable, Serializable>(utils, "bogus", new ActiveMQTransport());
	}

	static class RecordingListener implements CFSet.Listener {
		LinkedList<Operation> operations = new LinkedList<CFMessage.Operation>();
		LinkedList<Object[]> keys = new LinkedList<Object[]>();

		public void reset() {
			operations.clear();
			keys.clear();
		}

		public void keysAdded(Object[] keys) {
			operations.add(Operation.ADD);
			this.keys.add(keys);

			synchronized (this) {
				notify();
			}
		}

		public void keysRemoved(Object[] keys) {
			operations.add(Operation.REMOVE);
			this.keys.add(keys);
			synchronized (this) {
				notify();
			}
		}

	}

	@Test
	public void earlyFilter() throws JMSException {
		Connection t = new ActiveMQTransport();

		ContinuousFilter<Serializable, Serializable> cf = new ContinuousFilter<Serializable, Serializable>(utils, FILTERED_MAP, t);
		cf.setFilter(new StringFilter());
		CFSet<Serializable> set = cf.asSet();
		RecordingListener l = new RecordingListener();
		set.setListener(l);

		WXSMap<String, Serializable> fMap = utils.getCache(FILTERED_MAP);
		fMap.put("String", "String");
		equals(new Operation[] { Operation.ADD }, new Object[][] { { "String" } }, l);
		fMap.put("Integer", 1);
		fMap.put("Foo", "Bar");
		fMap.remove("String");
		equals(new Operation[] { Operation.ADD, Operation.REMOVE }, new Object[][] { { "Foo" }, { "String" } }, l);
		fMap.clear();
		equals(new Operation[] { Operation.REMOVE }, new Object[][] { { "Foo" } }, l);
		cf.close();

		Assert.assertEquals(0, l.operations.size());
	}

	@Test
	public void lateFilter() throws JMSException {
		Connection t = new ActiveMQTransport();
		WXSMap<String, Serializable> fMap = utils.getCache(FILTERED_MAP);
		fMap.put("Integer", 1);
		ContinuousFilter<Serializable, Serializable> cfInt = new ContinuousFilter<Serializable, Serializable>(utils, FILTERED_MAP, t);
		cfInt.setFilter(new NumberFilter());

		RecordingListener lInt = new RecordingListener();
		CFSet<Serializable> set = cfInt.asSet();
		set.setListener(lInt);
		equals(new Operation[] { Operation.ADD }, new Object[][] { { "Integer" } }, lInt);
		cfInt.close();
	}

	void equals(Operation[] expectedOps, Object[][] expectedKeys, RecordingListener l) {
		try {
			synchronized (l) {
				for (int i = 0; i < expectedOps.length; ++i) {
					Operation lOp = l.operations.peek();
					if (lOp == null) {
						l.wait(1000);
					}
					lOp = l.operations.poll();
					Assert.assertEquals("index " + i, expectedOps[i], lOp);
					Assert.assertTrue("index " + i, Arrays.equals(expectedKeys[i], l.keys.poll()));
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void setOperations() throws JMSException, InterruptedException {
		Connection t = new ActiveMQTransport();
		WXSMap<Integer, Serializable> fMap = utils.getCache(FILTERED_MAP);
		fMap.clear();
		fMap.put(1, 1);
		fMap.put(2, 1);
		fMap.put(3, "String");
		ContinuousFilter<Integer, Serializable> cfInt = new ContinuousFilter<Integer, Serializable>(utils, FILTERED_MAP, t);
		cfInt.setFilter(new NumberFilter());

		CFSet<Integer> set = cfInt.asSet();
		Thread.sleep(1000);
		Assert.assertEquals(2, set.size());
		Assert.assertTrue(set.contains(1));
		Assert.assertTrue(set.contains(2));

		fMap.removeAll(Arrays.asList(1, 3));
		Thread.sleep(1000);
		Assert.assertEquals(1, set.size());
		Iterator<Integer> itr = set.iterator();
		Assert.assertTrue(itr.hasNext());
		Assert.assertEquals(Integer.valueOf(2), itr.next());
		Assert.assertFalse(itr.hasNext());

		cfInt.close();
	}

}
