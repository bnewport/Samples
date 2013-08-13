//
//This sample program is provided AS IS and may be used, executed, copied and
//modified without royalty payment by customer (a) for its own instruction and 
//study, (b) in order to develop applications designed to run with an IBM 
//WebSphere product, either for customer's own internal use or for redistribution 
//by customer, as part of such an application, in customer's own products. "
//
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2013
//All Rights Reserved * Licensed Materials - Property of IBM
//
package com.devwebsphere.wxsutils.wxsmap;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
 * This is used to load all the values for a set of keys within a given partition using a single hop.
 */
public class LoadAllAgent<K extends Serializable> implements ReduceGridAgent {
	private static final long serialVersionUID = 6568906743945108310L;
	static Logger logger = Logger.getLogger(LoadAllAgent.class.getName());

	public List<K> batch;

	public static class Factory<K extends Serializable> implements ReduceAgentFactory<LoadAllAgent<K>, K, Object, Set<K>> {

		public LoadAllAgent<K> newAgent(List<K> keys) {
			LoadAllAgent<K> a = new LoadAllAgent<K>();
			a.batch = keys;
			return a;
		}

		public LoadAllAgent<K> newAgent(Map<K, Object> map) {
			throw new UnsupportedOperationException();
		}

		public K getKey(LoadAllAgent<K> a) {
			return a.batch.get(0);
		}

		public Set<K> emptyResult() {
			return Collections.emptySet();
		}

	};

	@Override
	public Object reduce(Session sess, ObjectMap map) {
		return null;
	}

	@Override
	public Object reduce(Session sess, ObjectMap map, Collection arg2) {
		AgentMBeanImpl bean = WXSUtils.getAgentMBeanManager().getBean(sess.getObjectGrid().getName(), this.getClass().getName());
		long startNS = System.nanoTime();
		try {
			ObjectMap m = sess.getMap(map.getName());
			Set<K> results = new HashSet<K>();
			// go over all the keys in the batch and return only the ones that there value is null
			for (K k : batch) {
				Object value = m.get(k);
				if (value == null) {
					results.add(k);
				}
			}
			bean.getKeysMetric().logTime(System.nanoTime() - startNS);
			return results;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
			bean.getKeysMetric().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	@Override
	public Object reduceResults(Collection sets) {
		Collection<Set<K>> list = sets;
		Set<K> union = new HashSet<K>();
		for (Collection<K> s : list) {
			union.addAll(s);
		}
		return union;
	}

	public LoadAllAgent() {
	}
}