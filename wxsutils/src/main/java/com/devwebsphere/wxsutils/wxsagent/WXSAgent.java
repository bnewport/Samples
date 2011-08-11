package com.devwebsphere.wxsutils.wxsagent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.WXSUtils;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.UndefinedMapException;
import com.ibm.websphere.objectgrid.datagrid.AgentManager;
import com.ibm.websphere.objectgrid.datagrid.EntryErrorValue;
import com.ibm.websphere.objectgrid.datagrid.ReduceGridAgent;

public class WXSAgent {
	static Logger logger = Logger.getLogger(WXSAgent.class.getName());

	static public class CallReduceAgentThread<K, X> implements Callable<X> {
		K key;
		String mapName;
		ReduceGridAgent agent;
		WXSUtils wxsutils;

		public CallReduceAgentThread(WXSUtils wxsutils, String mapName, K key, ReduceGridAgent agent) {
			this.wxsutils = wxsutils;
			this.key = key;
			this.mapName = mapName;
			this.agent = agent;
		}

		public X call() {
			try {
				Session sess = wxsutils.getSessionForThread();
				if (sess.isTransactionActive()) {
					logger.log(Level.WARNING, "Session has active transaction, create a new one");
					sess = wxsutils.getObjectGrid().getSession();
				}
				AgentManager agentMgr = sess.getMap(mapName).getAgentManager();
				X x = (X) agentMgr.callReduceAgent(agent, Collections.singleton(key));
				return x;
			} catch (UndefinedMapException e) {
				logger.log(Level.SEVERE, "Undefined Map using in Agent call " + mapName);
			} catch (Throwable e) {
				logger.log(Level.SEVERE, "Exception in CallReduceAgentThread.call", e);
			}
			return null;
		}
	}

	/**
	 * This takes a Map of K/V pairs and sorts them in to buckets per partition. Partitions with no pairs are not in the
	 * returned Map.
	 * 
	 * @param <K1>
	 * @param <V1>
	 * @param baseMap
	 * @param items
	 * @return A Map with entries for each partition with pairs
	 */
	static public <K1, V1> Map<Integer, Map<K1, V1>> convertToPartitionEntryMap(BackingMap baseMap, Map<K1, V1> items) {
		Map<Integer, Map<K1, V1>> entriesForPartition = new HashMap<Integer, Map<K1, V1>>();
		for (Map.Entry<K1, V1> e : items.entrySet()) {
			Integer partitionId = baseMap.getPartitionManager().getPartition(e.getKey());
			Map<K1, V1> listEntries = (Map<K1, V1>) entriesForPartition.get(partitionId);
			if (listEntries == null) {
				listEntries = new HashMap<K1, V1>();
				entriesForPartition.put(partitionId, listEntries);
			}
			listEntries.put(e.getKey(), e.getValue());
		}
		return entriesForPartition;
	}

	/**
	 * This takes a list of keys and places them in partition aligned buckets. Partitions with no keys have no entries
	 * in the returned Map.
	 * 
	 * @param <K>
	 * @param baseMap
	 * @param keys
	 * @return
	 */
	static public <K> Map<Integer, List<K>> convertToPartitionEntryMap(BackingMap baseMap, Collection<K> keys) {
		Map<Integer, List<K>> entriesForPartition = new HashMap<Integer, List<K>>();
		for (K k : keys) {
			Integer partitionId = baseMap.getPartitionManager().getPartition(k);
			List<K> listEntries = (List<K>) entriesForPartition.get(partitionId);
			if (listEntries == null) {
				listEntries = new LinkedList<K>();
				entriesForPartition.put(partitionId, listEntries);
			}
			listEntries.add(k);
		}
		return entriesForPartition;
	}

	static public <K, V> Map<K, V> collectResultsAsMap(List<Future<Map<K, V>>> futures, long timeout) {
		Map<K, V> result = new HashMap<K, V>(futures.size());
		for (Future<Map<K, V>> f : futures) {
			long start = System.nanoTime();
			try {
				result.putAll(f.get(timeout, TimeUnit.NANOSECONDS));
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Exception", e);
				throw new ObjectGridRuntimeException(e);
			}
			long end = System.nanoTime();
			timeout -= (end - start);
		}

		return result;
	}

	static public <V> List<V> collectResultsAsList(List<Future<V>> futures, long timeout) {
		List<V> result = new ArrayList<V>(futures.size());
		for (Future<V> f : futures) {
			long start = System.nanoTime();
			try {
				result.add(f.get(timeout, TimeUnit.NANOSECONDS));
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Exception", e);
				throw new ObjectGridRuntimeException(e);
			}
			long end = System.nanoTime();
			timeout -= (end - start);
		}

		return result;
	}

	/**
	 * This is used to check all Agents returned TRUE which indicates no problems.
	 * 
	 * @param results
	 * @return true if all Agents returned true
	 */
	static public <V> boolean areAllFutures(V expected, List<Future<V>> results, long timeout) {
		try {
			for (Future<V> f : results) {
				long start = System.nanoTime();
				if (!checkReturnValue(expected, f.get(timeout, TimeUnit.NANOSECONDS))) {
					return false;
				}
				long end = System.nanoTime();
				timeout -= (end - start);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
			throw new ObjectGridRuntimeException(e);
		}

		return true;
	}

	static public <V> boolean checkReturnValue(V expected, Object rc) {
		if (rc != null && rc instanceof EntryErrorValue) {
			EntryErrorValue ev = (EntryErrorValue) rc;
			logger.log(Level.SEVERE, "Remote exception: " + ev.toString());
			throw new ObjectGridRuntimeException(ev.toString());
		}

		return expected.equals(rc);
	}
}