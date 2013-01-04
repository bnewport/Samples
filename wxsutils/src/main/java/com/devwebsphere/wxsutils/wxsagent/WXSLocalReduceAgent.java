package com.devwebsphere.wxsutils.wxsagent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.ConfigProperties;
import com.devwebsphere.wxsutils.WXSUtils;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.UndefinedMapException;
import com.ibm.websphere.objectgrid.datagrid.AgentManager;
import com.ibm.websphere.objectgrid.datagrid.EntryErrorValue;
import com.ibm.websphere.objectgrid.datagrid.ReduceGridAgent;

public class WXSLocalReduceAgent extends WXSAgent {
	static Logger logger = Logger.getLogger(WXSLocalReduceAgent.class.getName());

	static class LocalErrorEntryValue implements EntryErrorValue {
		Throwable t;
		int partition;

		public LocalErrorEntryValue(Throwable t, int partition) {
			this.t = t;
			this.partition = partition;
		}

		@Override
		public String getErrorExceptionString() {
			return t.getMessage();
		}

		@Override
		public Throwable getException() {
			return t;
		}

		@Override
		public String getExceptionClassName() {
			return t.getClass().getName();
		}

		@Override
		public int getPartition() {
			return partition;
		}

		@Override
		public String getServerName() {
			return null;
		}

	}

	static public class CallLocalReduceAgentThread<X> implements Callable<X> {
		int partition;
		String mapName;
		ReduceGridAgent agent;
		WXSUtils wxsutils;

		public CallLocalReduceAgentThread(WXSUtils wxsutils, String mapName, int partition, ReduceGridAgent agent) {
			this.wxsutils = wxsutils;
			this.partition = partition;
			this.mapName = mapName;
			this.agent = agent;
		}

		public X call() {
			Session s = wxsutils.getSessionForThread();
			X rc;
			try {
				if (s.isTransactionActive()) {
					logger.log(Level.WARNING, "Session has active transaction, create a new one");
					s = wxsutils.getObjectGrid().getSession();
				}

				ObjectMap map = s.getMap(mapName);

				rc = (X) agent.reduce(s, map, null);
			} catch (Throwable t) {
				rc = (X) new LocalErrorEntryValue(t, partition);
			}
			return rc;
		}
	}

	static private <A extends ReduceGridAgent, K extends Serializable, V, X> List<Future<X>> callReduceAgentAll(WXSUtils utils,
			ReduceAgentFactory<A, K, V, X> factory, Map<K, V> batch, BackingMap bmap) {
		int sz = batch.size();
		A a;

		if (sz == 0) {
			return Collections.emptyList();
		} else if (sz == 1) {
			// optimized version, generates a little less garbage.
			// invoke the agent to add the batch of records to the grid
			a = factory.newAgent(batch);
			int partition = bmap.getPartitionManager().getPartition(batch.keySet().iterator().next());

			X rc;
			try {
				rc = new CallLocalReduceAgentThread<X>(utils, bmap.getName(), partition, a).call();
			} catch (ObjectGridRuntimeException ogre) {
				rc = (X) new LocalErrorEntryValue(ogre, partition);
			}
			Future<X> future = new DoneFuture<X>(rc);
			return Arrays.asList(future);
		} else {
			Map<Integer, SortedMap<K, V>> pmap = convertToPartitionEntryMap(bmap, batch);
			ArrayList<Future<X>> results = new ArrayList<Future<X>>(pmap.size());
			for (Map.Entry<Integer, SortedMap<K, V>> e : pmap.entrySet()) {
				// we need one key for partition routing
				// so get the first one
				SortedMap<K, V> perPartitionEntries = e.getValue();
				int partition = e.getKey();

				// invoke the agent to add the batch of records to the grid
				a = factory.newAgent(perPartitionEntries);
				// Insert all keys for one partition using the first key as a routing key
				Future<X> fv = utils.getExecutorService().submit(new CallLocalReduceAgentThread<X>(utils, bmap.getName(), partition, a));
				results.add(fv);

			}
			return results;
		}

	}

	public static <A extends ReduceGridAgent, K extends Serializable, V, X> void callReduceAgentAll(WXSUtils utils,
			ReduceAgentFactory<A, K, V, X> factory, Map<K, V> batch, BackingMap bmap, X result) {
		List<Future<X>> r = callReduceAgentAll(utils, factory, batch, bmap);

		if (!r.isEmpty()) {
			if (!areAllFutures(result, r, ConfigProperties.getAgentTimeout(utils.getConfigProperties()))) {
				logger.log(Level.SEVERE, "Agent failed because of a server side exception");
				throw new ObjectGridRuntimeException("Agent failed");
			}
		}
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
