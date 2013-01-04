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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.FailedKeysException;
import com.devwebsphere.wxsutils.wxsagent.ReduceAgentFactory;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.EntryErrorValue;
import com.ibm.websphere.objectgrid.datagrid.ReduceGridAgent;

/**
 * This is used to upsert a chunk of records for a given partition using a single hop. It returns TRUE if the operation
 * succeeded.
 * 
 */
public class UpsertAgent<K extends Serializable, V extends Serializable> implements ReduceGridAgent {
	static Logger logger = Logger.getLogger(UpsertAgent.class.getName());
	/**
	 * 
	 */
	private static final long serialVersionUID = 6568906743945108310L;

	public LinkedHashMap<K, V> batch;
	public boolean isWriteThrough = true;

	static public class Factory<K extends Serializable, V extends Serializable> implements ReduceAgentFactory<UpsertAgent<K, V>, K, V, Boolean> {
		boolean isWriteThrough;

		public Factory(boolean isWriteThrough) {
			this.isWriteThrough = isWriteThrough;
		}

		public UpsertAgent<K, V> newAgent(List<K> keys) {
			throw new ObjectGridRuntimeException("NOT SUPPORTED");
		}

		public UpsertAgent<K, V> newAgent(Map<K, V> map) {
			UpsertAgent<K, V> a = newAgent();
			if (!(map instanceof LinkedHashMap)) {
				map = new LinkedHashMap<K, V>(map);
			}
			a.batch = (LinkedHashMap<K, V>) map;
			a.isWriteThrough = isWriteThrough;
			return a;
		}

		public K getKey(UpsertAgent<K, V> a) {
			return a.batch.entrySet().iterator().next().getKey();
		}

		public Boolean emptyResult() {
			return Boolean.FALSE;
		}

		protected UpsertAgent<K, V> newAgent() {
			return new UpsertAgent<K, V>();
		}

	}

	public Object reduce(Session sess, ObjectMap map) {
		return null;
	}

	public Object reduce(Session s, ObjectMap map, Collection arg2) {
		try {
			if (!isWriteThrough) {
				s.beginNoWriteThrough();
			} else {
				s.begin();
			}

			map.upsertAll(batch);
			s.commit();
			return Boolean.TRUE;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
			throw new ObjectGridRuntimeException(e);
		} finally {
			if (s != null && s.isTransactionActive()) {
				try {
					s.rollback();
				} catch (ObjectGridException e) {
					logger.log(Level.SEVERE, "Exception", e);
				}
			}
		}

	}

	/**
	 * Combine the Boolean results of the process calls using AND
	 */
	public Object reduceResults(Collection arg0) {
		boolean rc = true;
		for (Object o : arg0) {
			if (o instanceof EntryErrorValue) {
				return new FailedKeysException((EntryErrorValue) o, batch.keySet());
			}
			if (o instanceof Boolean) {
				Boolean b = (Boolean) o;
				rc = rc && b;
			}
			if (!rc)
				break;
		}
		return rc;
	}

	public UpsertAgent() {
	}
}
