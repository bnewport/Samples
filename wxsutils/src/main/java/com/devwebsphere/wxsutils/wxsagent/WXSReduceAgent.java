package com.devwebsphere.wxsutils.wxsagent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.Beta;
import com.devwebsphere.wxsutils.ConfigProperties;
import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.wxsmap.ReduceAgentExecutor;
import com.devwebsphere.wxsutils.wxsmap.ReduceAgentNoKeysExecutor;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.UndefinedMapException;
import com.ibm.websphere.objectgrid.datagrid.AgentManager;
import com.ibm.websphere.objectgrid.datagrid.ReduceGridAgent;

public class WXSReduceAgent extends WXSAgent {
	static Logger logger = Logger.getLogger(WXSReduceAgent.class.getName());

	static public <A extends ReduceGridAgent, K extends Serializable, X> X callReduceAgentAll(WXSUtils utils, ReduceAgentFactory<A, K, ?, X> factory,
			Collection<K> keys, BackingMap bmap) {
		if (keys.isEmpty()) {
			return factory.emptyResult();
		}

		Map<Integer, List<K>> pmap = convertToPartitionEntryMap(bmap, keys);

		Comparator<? super K> comparator = null;
		if (keys instanceof SortedSet) {
			comparator = ((SortedSet<K>) keys).comparator();
		}
		Map<K, A> agents = new TreeMap<K, A>(comparator);
		
		for (Map.Entry<Integer, List<K>> e : pmap.entrySet()) {
			A a = factory.newAgent(e.getValue());
			agents.put(factory.getKey(a), a);
		}

		X r = callReduceAgentAll(utils, agents, bmap);
		return r;
	}

	/**
	 * This invokes the ReduceAgent for each key as efficiently as possible and reduces the results.
	 * 
	 * @param <K>
	 *            The key type
	 * @param <A>
	 *            The agent type
	 * @param <X>
	 *            The result of the ReduceGridAgent
	 * @param batch
	 *            The key/agent map
	 * @param bmap
	 *            The map containing the keys
	 * @return The reduced value for all agents
	 */
	static public <A extends ReduceGridAgent, K extends Serializable, X> X callReduceAgentAll(WXSUtils utils, Map<K, A> batch, BackingMap bmap) {
		if (batch.size() > 0) {
			try {
				Map<Integer, SortedMap<K, A>> pmap = convertToPartitionEntryMap(bmap, batch);
				ArrayList<Future<X>> results = new ArrayList<Future<X>>(pmap.size());

				for (SortedMap<K, A> perPartitionEntries : pmap.values()) {
					// we need one key for partition routing
					// so get the first one
					K key = perPartitionEntries.keySet().iterator().next();

					// invoke the agent to add the batch of records to the grid
					ReduceAgentExecutor<A, K> ia = new ReduceAgentExecutor<A, K>();
					ia.batch = perPartitionEntries;
					// only call if work to go
					if (ia.batch.size() > 0) {
						Future<X> fv = utils.getExecutorService().submit(new CallReduceAgentThread<X>(utils, bmap.getName(), key, ia));
						results.add(fv);
					}
				}

				List<X> r = collectResultsAsList(results, ConfigProperties.getAgentTimeout(utils.getConfigProperties()));
				A agent = batch.values().iterator().next();
				X retVal = (X) agent.reduceResults(r);

				return retVal;
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Exception", e);
				throw new ObjectGridRuntimeException(e);
			}
		}

		return null;
	}

	static public <A extends ReduceGridAgent, K extends Serializable, V, X> List<Future<X>> callReduceAgentAll(WXSUtils utils,
			ReduceAgentFactory<A, K, V, X> factory, Map<K, V> batch, BackingMap bmap) {
		int sz = batch.size();
		A a;

		if (sz == 0) {
			return Collections.emptyList();
		} else if (sz == 1) {
			// optimized version, generates a little less garbage.
			// invoke the agent to add the batch of records to the grid
			a = factory.newAgent(batch);
			// Insert all keys for one partition using the first key as a routing key
			AgentManager am = null;
			try {
				am = utils.getSessionForThread().getMap(bmap.getName()).getAgentManager();
			} catch (UndefinedMapException e) {
				throw new ObjectGridRuntimeException(e);
			}
			X rc = (X) am.callReduceAgent(a, Collections.singletonList(factory.getKey(a)));
			Future<X> future = new DoneFuture<X>(rc);
			return Arrays.asList(future);
		} else {

			Map<Integer, SortedMap<K, V>> pmap = convertToPartitionEntryMap(bmap, batch);
			ArrayList<Future<X>> results = new ArrayList<Future<X>>(pmap.size());
			for (Map.Entry<Integer, SortedMap<K, V>> e : pmap.entrySet()) {
				// we need one key for partition routing
				// so get the first one
				SortedMap<K, V> perPartitionEntries = e.getValue();
				K key = perPartitionEntries.keySet().iterator().next();

				// invoke the agent to add the batch of records to the grid
				a = factory.newAgent(perPartitionEntries);
				// Insert all keys for one partition using the first key as a routing key
				Future<X> fv = utils.getExecutorService().submit(new CallReduceAgentThread<X>(utils, bmap.getName(), key, a));
				results.add(fv);

			}
			return results;
		}

	}

	static public <A extends ReduceGridAgent, K extends Serializable, V, X> void callReduceAgentAll(WXSUtils utils,
			ReduceAgentFactory<A, K, V, X> factory, Map<K, V> batch, BackingMap bmap, X result) {
		List<Future<X>> r = callReduceAgentAll(utils, factory, batch, bmap);

		if (!r.isEmpty()) {
			if (!areAllFutures(result, r, ConfigProperties.getAgentTimeout(utils.getConfigProperties()))) {
				logger.log(Level.SEVERE, "Agent failed because of a server side exception");
				throw new ObjectGridRuntimeException("Agent failed");
			}
		}
	}

	@Beta
	static public <A extends ReduceGridAgent, K extends Serializable, X extends Serializable> List<Future<X>> callReduceAgentAll(WXSUtils utils,
			A reduceAgent, BackingMap bmap) {
		int numPartitions = bmap.getPartitionManager().getNumOfPartitions();
		ArrayList<Future<X>> results = new ArrayList<Future<X>>(numPartitions);
		for (int i = 0; i < numPartitions; ++i) {
			Integer key = i;
			ReduceAgentNoKeysExecutor<A, X> ia = new ReduceAgentNoKeysExecutor<A, X>();
			ia.agent = reduceAgent;
			ia.agentTargetMapName = bmap.getName();
			Future<X> fv = utils.getExecutorService().submit(new CallReduceAgentThread<X>(utils, WXSUtils.routingMapName, key, ia));
			results.add(fv);
		}

		return results;
	}

	static class DoneFuture<X> implements Future<X> {
		X result;

		DoneFuture(X r) {
			result = r;
		}

		public boolean cancel(boolean mayInterruptIfRunning) {
			return false;
		}

		public boolean isCancelled() {
			return false;
		}

		public boolean isDone() {
			return true;
		}

		public X get() throws InterruptedException, ExecutionException {
			return result;
		}

		public X get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
			return result;
		}

	}
}
