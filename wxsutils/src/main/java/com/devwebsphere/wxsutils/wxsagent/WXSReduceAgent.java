package com.devwebsphere.wxsutils.wxsagent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.Beta;
import com.devwebsphere.wxsutils.ConfigProperties;
import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.multijob.JobExecutor;
import com.devwebsphere.wxsutils.wxsmap.ReduceAgentExecutor;
import com.devwebsphere.wxsutils.wxsmap.ReduceAgentNoKeysExecutor;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.UndefinedMapException;
import com.ibm.websphere.objectgrid.datagrid.AgentManager;
import com.ibm.websphere.objectgrid.datagrid.ReduceGridAgent;

public class WXSReduceAgent<A extends ReduceGridAgent, X> {
	static Logger logger = Logger.getLogger(WXSReduceAgent.class.getName());

	public <K, V> X callReduceAgentAll(WXSUtils utils, ReduceAgentFactory<A> factory, Collection<K> keys, BackingMap bmap) {
		if (keys.isEmpty()) {
			return factory.emptyResult();
		}

		Map<Integer, List<K>> pmap = WXSAgent.convertToPartitionEntryMap(bmap, keys);

		Map<K, A> agents = new HashMap<K, A>(pmap.size());
		for (Map.Entry<Integer, List<K>> e : pmap.entrySet()) {
			A a = factory.newAgent(e.getValue());
			agents.put(factory.<K> getKey(a), a);
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
	public <K> X callReduceAgentAll(WXSUtils utils, Map<K, A> batch, BackingMap bmap) {
		if (batch.size() > 0) {
			try {
				Map<Integer, Map<K, A>> pmap = WXSAgent.convertToPartitionEntryMap(bmap, batch);
				Iterator<Map<K, A>> items = pmap.values().iterator();
				ArrayList<Future<X>> results = new ArrayList<Future<X>>(pmap.size());

				while (items.hasNext()) {
					Map<K, A> perPartitionEntries = items.next();
					// we need one key for partition routing
					// so get the first one
					K key = perPartitionEntries.keySet().iterator().next();

					// invoke the agent to add the batch of records to the grid
					ReduceAgentExecutor<K, A> ia = new ReduceAgentExecutor<K, A>();
					ia.batch = perPartitionEntries;
					// only call if work to go
					if (ia.batch.size() > 0) {
						Future<X> fv = utils.getExecutorService().submit(new WXSAgent.CallReduceAgentThread<K, X>(utils, bmap.getName(), key, ia));
						results.add(fv);
					}
				}

				List<X> r = WXSAgent.collectResultsAsList(results, ConfigProperties.getAgentTimeout(utils.getConfigProperties()));
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

	public <K, V> void callReduceAgentAll(WXSUtils utils, ReduceAgentFactory<A> factory, Map<K, V> batch, BackingMap bmap) {
		int sz = batch.size();
		A a;
		switch (sz) {
		case 0:
			return;
		case 1: // optimized version, generates a little less garbage.
			// invoke the agent to add the batch of records to the grid
			a = factory.newAgent(batch);
			// Insert all keys for one partition using the first key as a routing key
			AgentManager am = null;
			try {
				am = utils.getSessionForThread().getMap(bmap.getName()).getAgentManager();
			} catch (UndefinedMapException e) {
				throw new ObjectGridRuntimeException(e);
			}
			Object rc = am.callReduceAgent(a, Collections.singletonList(factory.getKey(a)));
			boolean result = WXSAgent.checkReturnValue(Boolean.TRUE, rc);
			if (!result) {
				logger.log(Level.SEVERE, "putAll failed because of a server side exception");
				throw new ObjectGridRuntimeException("putAll failed");
			}
			break;
		default:
			Map<Integer, Map<K, V>> pmap = WXSAgent.convertToPartitionEntryMap(bmap, batch);
			ArrayList<Future<Boolean>> results = new ArrayList<Future<Boolean>>(pmap.size());
			for (Map.Entry<Integer, Map<K, V>> e : pmap.entrySet()) {
				// we need one key for partition routing
				// so get the first one
				Map<K, V> perPartitionEntries = e.getValue();
				K key = perPartitionEntries.keySet().iterator().next();

				// invoke the agent to add the batch of records to the grid
				a = factory.newAgent(perPartitionEntries);
				// Insert all keys for one partition using the first key as a routing key
				Future<Boolean> fv = utils.getExecutorService().submit(new WXSAgent.CallReduceAgentThread<K, Boolean>(utils, bmap.getName(), key, a));
				results.add(fv);
			}

			if (!WXSAgent.areAllFutures(Boolean.TRUE, results, ConfigProperties.getAgentTimeout(utils.getConfigProperties()))) {
				logger.log(Level.SEVERE, "putAll failed because of a server side exception");
				throw new ObjectGridRuntimeException("putAll failed");
			}
		}
	}

	@Beta
	public <K> Map<K, X> callReduceAgentAll(WXSUtils utils, A reduceAgent, BackingMap bmap) {
		int numPartitions = bmap.getPartitionManager().getNumOfPartitions();
		ArrayList<Future<Map<K, X>>> results = new ArrayList<Future<Map<K, X>>>(numPartitions);
		for (int i = 0; i < numPartitions; ++i) {
			Integer key = i;
			ReduceAgentNoKeysExecutor<K, A, X> ia = new ReduceAgentNoKeysExecutor<K, A, X>();
			ia.agent = reduceAgent;
			ia.agentTargetMapName = bmap.getName();
			Future<Map<K, X>> fv = utils.getExecutorService().submit(
					new WXSAgent.CallReduceAgentThread<Integer, Map<K, X>>(utils, JobExecutor.routingMapName, key, ia));
			results.add(fv);
		}

		return WXSAgent.collectResultsAsMap(results, ConfigProperties.getAgentTimeout(utils.getConfigProperties()));
	}
}
