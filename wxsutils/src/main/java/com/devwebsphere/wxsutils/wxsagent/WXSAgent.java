package com.devwebsphere.wxsutils.wxsagent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.FailedKeysException;
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

	static public class CallReduceAgentThread<X> implements Callable<X> {
		Serializable key;
		String mapName;
		ReduceGridAgent agent;
		WXSUtils wxsutils;

		public CallReduceAgentThread(WXSUtils wxsutils, String mapName, Serializable key, ReduceGridAgent agent) {
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
	 * @return A Map with entries for each partition with sorted pairs
	 */
	static public <K1, V1> Map<Integer, SortedMap<K1, V1>> convertToPartitionEntryMap(BackingMap baseMap, Map<K1, V1> items) {
		// get the comparator if it exists
		Comparator<K1> comparator = null;
		if (items instanceof SortedMap) {
			comparator = ((SortedMap) items).comparator();
		}

		Map<Integer, SortedMap<K1, V1>> entriesForPartition = new HashMap<Integer, SortedMap<K1, V1>>();
		for (Map.Entry<K1, V1> e : items.entrySet()) {
			Integer partitionId = baseMap.getPartitionManager().getPartition(e.getKey());
			SortedMap<K1, V1> listEntries = entriesForPartition.get(partitionId);
			if (listEntries == null) {
				listEntries = new TreeMap<K1, V1>(comparator);
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
	 * @return a map of partition to sorted keys
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

		// get the comparator if it exists
		Comparator<K> comparator = null;
		if (keys instanceof SortedSet) {
			comparator = ((SortedSet) keys).comparator();
		}

		// sort all the keys per partition
		for (Map.Entry<Integer, List<K>> entry : entriesForPartition.entrySet()) {
			K[] o = (K[]) entry.getValue().toArray();
			Arrays.sort(o, comparator);
			entry.setValue(Arrays.asList(o));
		}
		return entriesForPartition;
	}

	static public <K, V> Map<K, V> collectResultsAsMap(Collection<Future<Map<K, V>>> futures, long timeout) {
		Map<K, V> result = new HashMap<K, V>(futures.size());
		for (Future<Map<K, V>> f : futures) {
			long start = System.nanoTime();
			try {
				Map<K, V> r = f.get(timeout, TimeUnit.NANOSECONDS);
				if (r != null) {
					result.putAll(r);
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Exception", e);
				throw new ObjectGridRuntimeException(e);
			}
			long end = System.nanoTime();
			timeout -= (end - start);
		}

		return result;
	}

	static public <V> List<V> collectResultsAsList(Collection<Future<V>> futures, long timeout) {
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
	static public <V> boolean areAllFutures(V expected, Collection<Future<V>> results, long timeout) {
		FailedKeysException failedKeys = null;
		try {
			for (Future<V> f : results) {
				long start = System.nanoTime();
				try {
					if (!checkReturnValue(expected, f.get(timeout, TimeUnit.NANOSECONDS))) {
						return false;
					}
				} catch (FailedKeysException fe) {
					if (failedKeys == null) {
						failedKeys = fe;
					} else {
						failedKeys.combine(fe);
					}
				}
				long end = System.nanoTime();
				timeout -= (end - start);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
			throw new ObjectGridRuntimeException(e);
		}

		if (failedKeys != null) {
			logger.log(Level.SEVERE, "Remote exception: " + failedKeys);
			throw new ObjectGridRuntimeException("Multiple agent failures", failedKeys);
		}

		return true;
	}

	static public <V> boolean checkReturnValue(V expected, Object rc) throws FailedKeysException {
		if (rc != null) {
			if (rc instanceof EntryErrorValue) {
				EntryErrorValue ev = (EntryErrorValue) rc;
				logger.log(Level.SEVERE, "Remote exception: " + ev.toString());
				throw new ObjectGridRuntimeException(ev.toString());
			}
			if (rc instanceof FailedKeysException) {
				throw (FailedKeysException) rc;
			}
		}

		return expected.equals(rc);
	}
}
