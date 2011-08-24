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
package com.devwebsphere.wxsutils.wxsmap;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanImpl;
import com.devwebsphere.wxsutils.wxsagent.ReduceAgentFactory;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.ReduceGridAgent;

/**
 * This is used to check if a set of keys within a given partition is present using a single hop.
 */
public class ContainsAllAgent<K extends Serializable> implements ReduceGridAgent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6568906743945108310L;
	static Logger logger = Logger.getLogger(ContainsAllAgent.class.getName());

	public List<K> batch;

	public static class Factory<K extends Serializable> implements ReduceAgentFactory<ContainsAllAgent<K>, K, Object, Map<K, Boolean>> {

		public ContainsAllAgent<K> newAgent(List<K> keys) {
			ContainsAllAgent<K> a = new ContainsAllAgent<K>();
			a.batch = keys;
			return a;
		}

		public ContainsAllAgent<K> newAgent(Map<K, Object> map) {
			throw new UnsupportedOperationException();
		}

		public K getKey(ContainsAllAgent<K> a) {
			return a.batch.get(0);
		}

		public Map<K, Boolean> emptyResult() {
			return Collections.emptyMap();
		}

	};

	public Object reduce(Session sess, ObjectMap map) {
		return null;
	}

	public Object reduce(Session sess, ObjectMap map, Collection arg2) {
		AgentMBeanImpl bean = WXSUtils.getAgentMBeanManager().getBean(sess.getObjectGrid().getName(), this.getClass().getName());
		long startNS = System.nanoTime();
		try {
			ObjectMap m = sess.getMap(map.getName());
			Map<K, Boolean> results = new HashMap<K, Boolean>();
			for (K k : batch) {
				results.put(k, m.containsKey(k));
			}
			bean.getKeysMetric().logTime(System.nanoTime() - startNS);
			return results;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
			bean.getKeysMetric().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	public Object reduceResults(Collection maps) {
		Collection<Map<K, Boolean>> list = maps;
		Map<K, Boolean> union = new HashMap<K, Boolean>();
		for (Map<K, Boolean> m : list) {
			union.putAll(m);
		}
		return union;
	}

	public ContainsAllAgent() {
	}
}
