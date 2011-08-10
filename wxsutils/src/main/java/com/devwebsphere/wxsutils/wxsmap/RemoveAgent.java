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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanImpl;
import com.devwebsphere.wxsutils.wxsagent.ReduceAgentFactory;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.ReduceGridAgent;

/**
 * This is used to remove a set of records for a given partition using a single hop.
 */

public class RemoveAgent<K> implements ReduceGridAgent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6568906743945108310L;
	static Logger logger = Logger.getLogger(RemoveAgent.class.getName());

	public List<K> batch;

	static public ReduceAgentFactory<RemoveAgent<?>> FACTORY = new ReduceAgentFactory<RemoveAgent<?>>() {

		public <K> RemoveAgent<?> newAgent(List<K> keys) {
			RemoveAgent<Serializable> a = new RemoveAgent<Serializable>();
			a.batch = (List<Serializable>) keys;
			return a;
		}

		public <K, V> RemoveAgent<?> newAgent(Map<K, V> map) {
			throw new ObjectGridRuntimeException("NOT SUPPORTED");
		}

		public <K> K getKey(RemoveAgent<?> a) {
			return (K) a.batch.get(0);
		}

		public <X> X emptyResult() {
			// TODO Auto-generated method stub
			return null;
		}
	};

	public Object reduce(Session sess, ObjectMap map) {
		return null;
	}

	public Object reduce(Session sess, ObjectMap map, Collection arg2) {
		AgentMBeanImpl agent = WXSUtils.getAgentMBeanManager().getBean(sess.getObjectGrid().getName(), this.getClass().getName());
		long startNS = System.nanoTime();
		Session s = null;
		try {
			s = SessionPool.getSessionForThread(sess.getObjectGrid());
			ObjectMap m = s.getMap(map.getName());
			s.begin();
			m.removeAll(batch);
			s.commit();
			agent.getKeysMetric().logTime(System.nanoTime() - startNS);
			return Boolean.TRUE;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
			agent.getKeysMetric().logException(e);
			return Boolean.FALSE;
		} finally {
			if (s != null && s.isTransactionActive()) {
				try {
					s.rollback();
				} catch (ObjectGridException e) {
					logger.log(Level.SEVERE, "Exception", e);
				}
			}
			SessionPool.returnSession(s);
		}

	}

	/**
	 * Combine the Boolean results of the process calls using AND
	 */
	public Object reduceResults(Collection arg0) {
		boolean rc = true;
		for (Object o : arg0) {
			if (o instanceof Boolean) {
				Boolean b = (Boolean) o;
				rc = rc && b;
			} else {
				rc = false;
			}
			if (!rc)
				break;
		}
		return rc;
	}

	public RemoveAgent() {
	}
}
