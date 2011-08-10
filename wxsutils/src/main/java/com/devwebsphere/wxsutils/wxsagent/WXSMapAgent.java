package com.devwebsphere.wxsutils.wxsagent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.Beta;
import com.devwebsphere.wxsutils.ConfigProperties;
import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.multijob.JobExecutor;
import com.devwebsphere.wxsutils.wxsmap.MapAgentExecutor;
import com.devwebsphere.wxsutils.wxsmap.MapAgentNoKeysExecutor;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;

public class WXSMapAgent {
	static Logger logger = Logger.getLogger(WXSMapAgent.class.getName());

	/**
	 * This takes a Map of key/MapAgent pairs and then invokes the agent for each key as efficiently as possible
	 * 
	 * @param <K>
	 *            The type of the keys
	 * @param <A>
	 *            The agent type
	 * @param <X>
	 *            The result type of the agent process method
	 * @param batch
	 *            The K/Agent map to execute
	 * @param bmap
	 *            The map containing the keys
	 * @return A Map with the agent result for each key
	 */
	static public <A extends MapGridAgent, K, X> Map<K, X> callMapAgentAll(WXSUtils utils, Map<K, A> batch, BackingMap bmap) {
		if (batch.size() > 0) {
			Map<Integer, Map<K, A>> pmap = WXSAgent.convertToPartitionEntryMap(bmap, batch);
			Iterator<Map<K, A>> items = pmap.values().iterator();
			ArrayList<Future<Map<K, X>>> results = new ArrayList<Future<Map<K, X>>>(pmap.size());
			while (items.hasNext()) {
				Map<K, A> perPartitionEntries = items.next();
				// we need one key for partition routing
				// so get the first one
				K key = perPartitionEntries.keySet().iterator().next();

				// invoke the agent to add the batch of records to the grid
				// but if no work for this partition then skip it
				if (perPartitionEntries.size() > 0) {
					MapAgentExecutor<K, A, X> ia = new MapAgentExecutor<K, A, X>();
					ia.batch = perPartitionEntries;
					Future<Map<K, X>> fv = utils.getExecutorService().submit(
							new WXSAgent.CallReduceAgentThread<K, Map<K, X>>(utils, bmap.getName(), key, ia));
					results.add(fv);
				}
			}

			return WXSAgent.collectResultsAsMap(results, ConfigProperties.getAgentTimeout(utils.getConfigProperties()));
		} else {
			return Collections.emptyMap();
		}
	}

	@Beta
	static public <A extends MapGridAgent, K, X> Map<K, X> callMapAgentAll(WXSUtils utils, A mapAgent, BackingMap bmap) {
		int numPartitions = bmap.getPartitionManager().getNumOfPartitions();
		ArrayList<Future<Map<K, X>>> results = new ArrayList<Future<Map<K, X>>>(numPartitions);
		for (int i = 0; i < numPartitions; ++i) {
			Integer key = i;
			MapAgentNoKeysExecutor<K, A, X> ia = new MapAgentNoKeysExecutor<K, A, X>();
			ia.agent = mapAgent;
			ia.agentTargetMapName = bmap.getName();
			Future<Map<K, X>> fv = utils.getExecutorService().submit(
					new WXSAgent.CallReduceAgentThread<Integer, Map<K, X>>(utils, JobExecutor.routingMapName, key, ia));
			results.add(fv);
		}

		return WXSAgent.collectResultsAsMap(results, ConfigProperties.getAgentTimeout(utils.getConfigProperties()));
	}
}
