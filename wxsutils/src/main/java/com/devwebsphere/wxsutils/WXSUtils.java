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
package com.devwebsphere.wxsutils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import com.devwebsphere.wxssearch.jmx.TextIndexMBeanManager;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanManager;
import com.devwebsphere.wxsutils.jmx.loader.LoaderMBeanManager;
import com.devwebsphere.wxsutils.jmx.wxsmap.WXSMapMBeanManager;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ClientClusterContext;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridManagerFactory;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.AgentManager;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;
import com.ibm.websphere.objectgrid.datagrid.ReduceGridAgent;
import com.ibm.websphere.objectgrid.deployment.DeploymentPolicy;
import com.ibm.websphere.objectgrid.deployment.DeploymentPolicyFactory;
import com.ibm.websphere.objectgrid.server.Container;
import com.ibm.websphere.objectgrid.server.ServerFactory;

/**
 * This is a utility class. Each instance is associated with a thread pool
 * and a specific client ObjectGrid connection. It provides accelerated method for bulk operations
 * and interacting with agents in general.
 * 
 * Normally, most applications using a single grid would create this and store an instance
 * in a static variable and then use in their client.
 *
 */
public class WXSUtils
{
	/**
	 * A client grid reference for this instance. All operations use this grid
	 */
	ObjectGrid grid;
	
	/**
	 * A shared thread pool if the non thread pool constructor is used. All such instances
	 * will share this pool.
	 */
	static AtomicReference<ExecutorService> globalThreadPool = new AtomicReference<ExecutorService>();
	ExecutorService threadPool;
	
	Map<String, WXSMap> maps = new ConcurrentHashMap<String, WXSMap>();

	static AtomicReference<AgentMBeanManager> agentMBeanManager = new AtomicReference<AgentMBeanManager>();
	static AtomicReference<LoaderMBeanManager> loaderMBeanManager = new AtomicReference<LoaderMBeanManager>();
	static AtomicReference<WXSMapMBeanManager> wxsMapMBeanManager = new AtomicReference<WXSMapMBeanManager>();
	static AtomicReference<TextIndexMBeanManager> indexMBeanManager = new AtomicReference<TextIndexMBeanManager>();

	/**
	 * Returns a static MBean Manager. Hack until I get DI using Aries
	 * @return
	 */
	public static AgentMBeanManager getAgentMBeanManager()
	{
		if(agentMBeanManager.get() == null)
		{
			AgentMBeanManager m = new AgentMBeanManager("Grid");
			agentMBeanManager.compareAndSet(null, m);
		}
		return agentMBeanManager.get();
	}
	
	/**
	 * Returns a static MBean Manager. Hack until I get DI using Aries
	 * @return
	 */
	public static TextIndexMBeanManager getIndexMBeanManager()
	{
		if(indexMBeanManager.get() == null)
		{
			TextIndexMBeanManager m = new TextIndexMBeanManager("Grid");
			indexMBeanManager.compareAndSet(null, m);
		}
		return indexMBeanManager.get();
	}
	
	/**
	 * Returns a static MBean Manager. Hack until I get DI using Aries
	 * @return
	 */
	public static LoaderMBeanManager getLoaderMBeanManager()
	{
		if(loaderMBeanManager.get() == null)
		{
			LoaderMBeanManager m = new LoaderMBeanManager("Grid");
			loaderMBeanManager.compareAndSet(null, m);
		}
		return loaderMBeanManager.get();
	}
	
	/**
	 * Returns a static MBean Manager. Hack until I get DI using Aries
	 * @return
	 */
	public static WXSMapMBeanManager getWXSMapMBeanManager()
	{
		if(wxsMapMBeanManager.get() == null)
		{
			WXSMapMBeanManager m = new WXSMapMBeanManager("Grid");
			wxsMapMBeanManager.compareAndSet(null, m);
		}
		return wxsMapMBeanManager.get();
	}
	
	/**
	 * This constructs an instance of this helper class.
	 * @param grid The grid to use with this instance. It is usually a client connection
	 * @param pool The thread pool to use to parallelize later operations.
	 */
	public WXSUtils(ObjectGrid grid, ExecutorService pool)
	{
		this.grid = grid;
		threadPool = pool;
	}

	/**
	 * This returns a simplified Map object to interact with the
	 * data in a cache.
	 * @param mapName
	 * @return
	 */
	public WXSMap getCache(String mapName)
	{
		WXSMap rc = maps.get(mapName);
		if(rc == null)
		{
			rc = new WXSMap(this, mapName);
			maps.put(mapName, rc);
		}
		return rc;
	}
	/**
	 * This constructs an instance of this helper class with a
	 * built in default
	 * @param grid
	 */
	public WXSUtils(ObjectGrid grid)
	{
		this.grid = grid;
		if(globalThreadPool.get() == null)
		{
			ExecutorService p = Executors.newFixedThreadPool(32);
			if(!globalThreadPool.compareAndSet(null, p))
			{
				p.shutdown();
			}
		}
		threadPool = globalThreadPool.get();
	}
	
	/**
	 * Get all the values for keys in parallel from a map
	 * @param <K> The key of the Map
	 * @param <V> The values of the Map
	 * @param keys The keys to fetch in parallel
	 * @param bmap The map from which to fetch the keys
	 * @return A Map of the key/value pairs
	 */
	public <K,V> Map<K,V> getAll(Collection<K> keys, BackingMap bmap)
	{
		Map<Integer, List<K>> pmap = convertToPartitionEntryMap(bmap, keys);
		
		Map<K, GetAllAgent<K, V>> agents = new HashMap<K, GetAllAgent<K,V>>();
		for(Map.Entry<Integer, List<K>> e : pmap.entrySet())
		{
			GetAllAgent<K, V> a = new GetAllAgent<K, V>();
			a.batch = e.getValue();
			agents.put(a.batch.get(0), a);
		}
		
		Map<K,V> r = callReduceAgentAll(agents, bmap);
		return r;
	}
	
	/**
	 * Checks contains for keys in parallel from a map
	 * @param <K> The key of the Map
	 * @param <V> The values of the Map
	 * @param keys The keys to fetch in parallel
	 * @param bmap The map from which to fetch the keys
	 * @return A Map of the key/value pairs
	 */
	public <K> Map<K,Boolean> containsAll(Collection<K> keys, BackingMap bmap)
	{
		Map<Integer, List<K>> pmap = convertToPartitionEntryMap(bmap, keys);
		
		Map<K, ContainsAllAgent<K>> agents = new HashMap<K, ContainsAllAgent<K>>();
		for(Map.Entry<Integer, List<K>> e : pmap.entrySet())
		{
			ContainsAllAgent<K> a = new ContainsAllAgent<K>();
			a.batch = e.getValue();
			agents.put(a.batch.get(0), a);
		}
		
		Map<K,Boolean> r = callReduceAgentAll(agents, bmap);
		return r;
	}
	
	/**
	 * This puts the K/V pairs in to the grid in parallel as efficiently as possible. Any existing values
	 * for any K are over written.
	 * @param <K> The key type to use
	 * @param <V> The value type to use
	 * @param batch The KV pairs to put in the grid
	 * @param bmap The map to store them in.
	 */
	public <K,V> void putAll(Map<K,V> batch, BackingMap bmap)
	{
		Map<Integer, Map<K,V>> pmap = convertToPartitionEntryMap(bmap, batch);
		Iterator<Map<K,V>> items = pmap.values().iterator();
		ArrayList<Future<?>> results = new ArrayList<Future<?>>();
		while(items.hasNext())
		{
			Map<K,V> perPartitionEntries = items.next();
			// we need one key for partition routing
			// so get the first one
			K key = perPartitionEntries.keySet().iterator().next();
			
			// invoke the agent to add the batch of records to the grid
			InsertAgent<K,V> ia = new InsertAgent<K,V>();
			ia.batch = perPartitionEntries;
			// Insert all keys for one partition using the first key as a routing key
			Future<?> fv = threadPool.submit(new CallReduceAgentThread(bmap.getName(), key, ia));
			results.add(fv);
		}

		blockForAllFuturesToFinish(results);
		if(!areAllFuturesTRUE(results))
			throw new ObjectGridRuntimeException("putAll failed");
	}

	/**
	 * This is used to check all Agents returned TRUE which indicates
	 * no problems.
	 * @param results
	 * @return true if all Agents returned true
	 */
	boolean areAllFuturesTRUE(List<Future<?>> results)
	{
		try
		{
			for(Future<?> f : results)
			{
				Boolean b = (Boolean)f.get();
				if(!b)
					return false;
			}
		}
		catch(ExecutionException e)
		{
			throw new ObjectGridRuntimeException(e);
		}
		catch(InterruptedException e)
		{
			throw new ObjectGridRuntimeException(e);
		}
		return true;
	}

	/**
	 * This removes the Keys from the grid in parallel as efficiently as possible. The entries will
	 * also be <b>removed</b> from any backend if a Loader is used. This isn't an invalidate, it's
	 * a remove operation.
	 * @param <K> The key type to use
	 * @param batch The keys to remove from the grid
	 * @param bmap The map to store them in.
	 */
	public <K> void removeAll(Collection<K> batch, BackingMap bmap)
	{
		Map<Integer, List<K>> pmap = convertToPartitionEntryMap(bmap, batch);
		Iterator<List<K>> items = pmap.values().iterator();
		ArrayList<Future<?>> results = new ArrayList<Future<?>>();
		while(items.hasNext())
		{
			List<K> perPartitionEntries = items.next();
			// we need one key for partition routing
			// so get the first one
			K key = perPartitionEntries.iterator().next();
			
			// invoke the agent to add the batch of records to the grid
			RemoveAgent<K> ra = new RemoveAgent<K>();
			ra.batch = perPartitionEntries;
			Future<?> fv = threadPool.submit(new CallReduceAgentThread(bmap.getName(), key, ra));
			results.add(fv);
		}

		blockForAllFuturesToFinish(results);
		if(!areAllFuturesTRUE(results))
			throw new ObjectGridRuntimeException("removeAll failed");
	}
	/**
	 * This takes a Map of key/MapAgent pairs and then invokes the agent for each key as efficiently
	 * as possible
	 * @param <K> The type of the keys
	 * @param <A> The agent type
	 * @param <X> The result type of the agent process method
	 * @param batch The K/Agent map to execute
	 * @param bmap The map containing the keys
	 * @return A Map with the agent result for each key
	 */
	public <K, A extends MapGridAgent, X> Map<K,X> callMapAgentAll(Map<K,A> batch, BackingMap bmap)
	{
		try
		{
			Map<Integer, Map<K,A>> pmap = convertToPartitionEntryMap(bmap, batch);
			Iterator<Map<K,A>> items = pmap.values().iterator();
			ArrayList<Future<Map<K,X>>> results = new ArrayList<Future<Map<K,X>>>();
			while(items.hasNext())
			{
				Map<K,A> perPartitionEntries = items.next();
				// we need one key for partition routing
				// so get the first one
				K key = perPartitionEntries.keySet().iterator().next();
				
				// invoke the agent to add the batch of records to the grid
				MapAgentExecutor<K,A,X> ia = new MapAgentExecutor<K,A,X>();
				ia.batch = perPartitionEntries;
				Future<Map<K,X>> fv = threadPool.submit(new CallReduceAgentThread<K,Map<K,X>>(bmap.getName(), key, ia));
				results.add(fv);
			}
			Map<K,X> result = new HashMap<K, X>();
			Iterator<Future<Map<K,X>>> iter = results.iterator();
			while(iter.hasNext())
			{
				Future<Map<K,X>> fv = iter.next();
				result.putAll(fv.get());
			}
			return result;
		}
		catch(Exception e)
		{
			throw new ObjectGridRuntimeException(e);
		}
	}
	
	/**
	 * This invokes the ReduceAgent for each key as efficiently as possible and reduces the results.
	 * @param <K> The key type
	 * @param <A> The agent type
	 * @param <X> The result of the ReduceGridAgent
	 * @param batch The key/agent map
	 * @param bmap The map containing the keys
	 * @return The reduced value for all agents
	 */
	public <K, A extends ReduceGridAgent,X> X callReduceAgentAll(Map<K,A> batch, BackingMap bmap)
	{
		try
		{
			Map<Integer, Map<K,A>> pmap = convertToPartitionEntryMap(bmap, batch);
			Iterator<Map<K,A>> items = pmap.values().iterator();
			ArrayList<Future<X>> results = new ArrayList<Future<X>>();
			if(batch.size() > 0)
			{
				while(items.hasNext())
				{
					Map<K,A> perPartitionEntries = items.next();
					// we need one key for partition routing
					// so get the first one
					K key = perPartitionEntries.keySet().iterator().next();
					
					// invoke the agent to add the batch of records to the grid
					ReduceAgentExecutor<K,A> ia = new ReduceAgentExecutor<K,A>();
					ia.batch = perPartitionEntries;
					Future<X> fv = threadPool.submit(new CallReduceAgentThread<K,X>(bmap.getName(), key, ia));
					results.add(fv);
				}
				Iterator<Future<X>> iter = results.iterator();
				A agent = batch.get(batch.keySet().iterator().next());
				ArrayList<X> tempList = new ArrayList<X>();
				while(iter.hasNext())
					tempList.add(iter.next().get());
				X r = (X)agent.reduceResults(tempList);
				return r;
			}
			else
				return null;
		}
		catch(Exception e)
		{
			throw new ObjectGridRuntimeException(e);
		}
	}
	
	/**
	 * This takes a Map of K/V pairs and sorts them in to buckets per partition.
	 * @param <K1>
	 * @param <V1>
	 * @param baseMap
	 * @param items
	 * @return
	 */
	protected <K1, V1> Map<Integer, Map<K1,V1>> convertToPartitionEntryMap(BackingMap baseMap, Map<K1,V1> items)
	{
		Iterator<Map.Entry<K1, V1>> iter = items.entrySet().iterator();
		Map<Integer,Map<K1,V1>> entriesForPartition = new HashMap<Integer, Map<K1,V1>>();
        while (iter.hasNext()) 
        {
            Map.Entry<K1,V1> e = iter.next();
            int partitionId = baseMap.getPartitionManager().getPartition(e.getKey());
            Map<K1,V1> listEntries = (Map<K1,V1>) entriesForPartition.get(new Integer(partitionId));
            if (listEntries == null) 
            {
                listEntries = new HashMap<K1,V1>();
                entriesForPartition.put(new Integer(partitionId), listEntries);
            }
            listEntries.put(e.getKey(), e.getValue());
        }
        return entriesForPartition;
	}
	
	/**
	 * This takes a list of keys and places them in partition aligned buckets
	 * @param <K>
	 * @param baseMap
	 * @param keys
	 * @return
	 */
	protected <K> Map<Integer, List<K>> convertToPartitionEntryMap(BackingMap baseMap, Collection<K> keys)
	{
		Map<Integer,List<K>> entriesForPartition = new HashMap<Integer, List<K>>();
		for(K k : keys)
        {
            int partitionId = baseMap.getPartitionManager().getPartition(k);
            List<K> listEntries = (List<K>) entriesForPartition.get(new Integer(partitionId));
            if (listEntries == null) 
            {
                listEntries = new LinkedList<K>();
                entriesForPartition.put(new Integer(partitionId), listEntries);
            }
            listEntries.add(k);
        }
        return entriesForPartition;
	}
	
	static Container container;

	public static void startCatalogServer(String cep, String catName)
	{
		try
		{
			// start a collocated catalog server which makes developing
			// in an IDE much easier.
			ServerFactory.getCatalogProperties().setCatalogClusterEndpoints(cep);
			ServerFactory.getCatalogProperties().setCatalogServer(true);
			ServerFactory.getCatalogProperties().setQuorum(false);
			ServerFactory.getServerProperties().setServerName(catName);
			ServerFactory.getServerProperties().setSystemStreamsToFileEnabled(false); // output goes to console, not a file
			
			// this starts the server
			com.ibm.websphere.objectgrid.server.Server server = ServerFactory.getInstance();
		}
		catch(Exception e)
		{
			throw new ObjectGridRuntimeException(e);
		}
		
	}
	/**
	 * This is a boiler plate method to create a 'grid' within a single JVM. Both xml files must be loadable from the
	 * class path. This will take about 20 seconds on a 2.4Ghz Core 2 Duo type processor. This makes it easy to do debugging of
	 * the server and client side code for a WXS project as it's a single JVM.
	 * @param og_xml_path The name of the objectgrid.xml file
	 * @param dep_xml_path The name of the deployment.xml file
	 * @return A 'client' reference to the created grid.
	 */
	public static ObjectGrid startTestServer(String gridName, String og_xml_path, String dep_xml_path) 
	{
		try
		{
			startCatalogServer("cs1:localhost:6601:6602", "cs1");
			
			com.ibm.websphere.objectgrid.server.Server server = ServerFactory.getInstance();
			
			System.out.println("Started catalog");
			URL serverObjectgridXML =  WXSUtils.class.getResource(og_xml_path);
			URL deployment =  WXSUtils.class.getResource(dep_xml_path);
			if(serverObjectgridXML == null)
				throw new ObjectGridRuntimeException("ObjectGrid xml file not found: " + og_xml_path);
			if(deployment == null)
				throw new ObjectGridRuntimeException("Deployment xml file not found: " + dep_xml_path);
			System.out.println("OG is " + serverObjectgridXML.toString() + " DP is " + deployment.toString());
			DeploymentPolicy policy = DeploymentPolicyFactory.createDeploymentPolicy(deployment, serverObjectgridXML);
			container = server.createContainer(policy);
			System.out.println("Container started");
			ClientClusterContext ccc = ObjectGridManagerFactory.getObjectGridManager().connect(null, null);
			ObjectGrid client = ObjectGridManagerFactory.getObjectGridManager().getObjectGrid(ccc, gridName);
			System.out.println("Got client");
			return client;
		}
		catch(Exception e)
		{
			throw new ObjectGridRuntimeException("Cannot start OG container", e);
		}
	}
	
	/**
	 * This connects to a remote WXS grid.
	 * @param cep A comma seperated list of host:port pairs for the catalog service
	 * @param gridName The name of the grid thats desired.
	 * @return A client connection to the grid
	 */
	static public ObjectGrid connectClient(String cep, String gridName, String ogXMLpath) 
	{
		try
		{
			URL cog = WXSUtils.class.getClassLoader().getResource(ogXMLpath);
			
			ClientClusterContext ccc = ObjectGridManagerFactory.getObjectGridManager().connect(cep, null, cog);
			ObjectGrid grid = ObjectGridManagerFactory.getObjectGridManager().getObjectGrid(ccc, gridName);
			return grid;
		}
		catch(Exception e)
		{
			throw new ObjectGridRuntimeException("Cannot start OG client", e);
		}
	}
	
	static public void stopContainer()
	{
		if(container != null)
			container.teardown();
	}

	class CallReduceAgentThread<K,X> implements Callable<X>
	{
		K key;
		String mapName;
		ReduceGridAgent agent;
		
		public CallReduceAgentThread(String mapName, K key, ReduceGridAgent agent)
		{
			this.key = key;
			this.mapName = mapName;
			this.agent = agent;
		}
		
		public X call()
		{
			try
			{
				Session sess = grid.getSession();
				AgentManager agentMgr = sess.getMap(mapName).getAgentManager();
				X x = (X) agentMgr.callReduceAgent(agent, Collections.singleton(key));
				return x;
			}
			catch(Exception e)
			{
				System.out.println("Exception " + e.toString());
				e.printStackTrace();
			}
			return null;
		}
	}
	
	/**
	 * This waits for all the Futures to be complete
	 * @param results The list of Futures to wait on.
	 */
	void blockForAllFuturesToFinish(List<Future<?>> futures)
	{
		// wait for all threads to finish or cancel
		ArrayList<Future<?>> copy = new ArrayList<Future<?>>(futures);
		while(!copy.isEmpty())
		{
			int last = copy.size() - 1;
			if(copy.get(last).isDone() || copy.get(last).isCancelled())
			{
				copy.remove(last);
			}
		}
	}

	/**
	 * This returns the ObjectGrid instance associated with this class instance
	 * @return
	 */
	public ObjectGrid getObjectGrid()
	{
		return grid;
	}
}
