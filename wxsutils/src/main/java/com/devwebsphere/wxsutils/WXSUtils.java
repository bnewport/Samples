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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxssearch.jmx.TextIndexMBeanManager;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanManager;
import com.devwebsphere.wxsutils.jmx.loader.LoaderMBeanManager;
import com.devwebsphere.wxsutils.jmx.wxsmap.WXSMapMBeanManager;
import com.devwebsphere.wxsutils.utils.ClassSerializer;
import com.devwebsphere.wxsutils.wxsagent.WXSAgent;
import com.devwebsphere.wxsutils.wxsagent.WXSReduceAgent;
import com.devwebsphere.wxsutils.wxsmap.ConditionalPutAgent;
import com.devwebsphere.wxsutils.wxsmap.ContainsAllAgent;
import com.devwebsphere.wxsutils.wxsmap.GetAllAgent;
import com.devwebsphere.wxsutils.wxsmap.InsertAgent;
import com.devwebsphere.wxsutils.wxsmap.InvalidateAgent;
import com.devwebsphere.wxsutils.wxsmap.LazyMBeanManagerAtomicReference;
import com.devwebsphere.wxsutils.wxsmap.RemoveAgent;
import com.devwebsphere.wxsutils.wxsmap.ThreadLocalSession;
import com.devwebsphere.wxsutils.wxsmap.WXSBaseMap;
import com.devwebsphere.wxsutils.wxsmap.WXSMapImpl;
import com.devwebsphere.wxsutils.wxsmap.WXSMapOfBigListsImpl;
import com.devwebsphere.wxsutils.wxsmap.WXSMapOfSetsImpl;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ClientClusterContext;
import com.ibm.websphere.objectgrid.ClientServerLoaderException;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridManagerFactory;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.TransactionException;
import com.ibm.websphere.objectgrid.deployment.DeploymentPolicy;
import com.ibm.websphere.objectgrid.deployment.DeploymentPolicyFactory;
import com.ibm.websphere.objectgrid.server.Container;
import com.ibm.websphere.objectgrid.server.ServerFactory;
import com.ibm.ws.objectgrid.cluster.ServiceUnavailableException;

/**
 * This is a utility class. Each instance is associated with a thread pool and a specific client ObjectGrid connection.
 * It provides accelerated method for bulk operations and interacting with agents in general.
 * 
 * Normally, most applications using a single grid would create this and store an instance in a static variable and then
 * use in their client.
 * 
 */
public class WXSUtils {
	/**
	 * The number of threads by default WXSUtils uses for agent calls (used in putAll *All type methods)
	 */
	public static final int THREADPOOL_SIZE = 32;

	static Logger logger = Logger.getLogger(WXSUtils.class.getName());
	/**
	 * A client grid reference for this instance. All operations use this grid
	 */
	volatile ObjectGrid grid;

	/**
	 * A shared thread pool if the non thread pool constructor is used. All such instances will share this pool.
	 */
	static AtomicReference<ExecutorService> globalThreadPool = new AtomicReference<ExecutorService>();
	ExecutorService threadPool;

	/**
	 * The properties currently used to connect to the grid
	 */
	volatile ConfigProperties configProps;

	Map<String, WXSBaseMap> maps = new ConcurrentHashMap<String, WXSBaseMap>();

	static LazyMBeanManagerAtomicReference<AgentMBeanManager> agentMBeanManager = new LazyMBeanManagerAtomicReference<AgentMBeanManager>(
			AgentMBeanManager.class);
	static LazyMBeanManagerAtomicReference<LoaderMBeanManager> loaderMBeanManager = new LazyMBeanManagerAtomicReference<LoaderMBeanManager>(
			LoaderMBeanManager.class);
	static LazyMBeanManagerAtomicReference<WXSMapMBeanManager> wxsMapMBeanManager = new LazyMBeanManagerAtomicReference<WXSMapMBeanManager>(
			WXSMapMBeanManager.class);
	static LazyMBeanManagerAtomicReference<TextIndexMBeanManager> indexMBeanManager = new LazyMBeanManagerAtomicReference<TextIndexMBeanManager>(
			TextIndexMBeanManager.class);

	ThreadLocalSession tls;

	static ClassSerializer serializer = new ClassSerializer();

	public static ClassSerializer getSerializer() {
		return serializer;
	}

	/**
	 * Returns a static MBean Manager. Hack until I get DI using Aries
	 * 
	 * @return
	 */
	public static AgentMBeanManager getAgentMBeanManager() {
		return agentMBeanManager.getLazyRef();
	}

	/**
	 * Returns a static MBean Manager. Hack until I get DI using Aries
	 * 
	 * @return
	 */
	public static TextIndexMBeanManager getIndexMBeanManager() {
		return indexMBeanManager.getLazyRef();
	}

	/**
	 * Returns a static MBean Manager. Hack until I get DI using Aries
	 * 
	 * @return
	 */
	public static LoaderMBeanManager getLoaderMBeanManager() {
		return loaderMBeanManager.getLazyRef();
	}

	/**
	 * Returns a static MBean Manager. Hack until I get DI using Aries
	 * 
	 * @return
	 */
	public static WXSMapMBeanManager getWXSMapMBeanManager() {
		return wxsMapMBeanManager.getLazyRef();
	}

	/**
	 * Make a new WXSUtils that can is independant of the original from a threading point of view. This allows a thread
	 * using a WXSUtils to quickly make another independant one. DO NOT USE THIS TO RECONNECT TO A GRID! Reuse the
	 * existing utils instance and call reconnect
	 * 
	 * @param utils
	 *            The original WXSUtils instance in use
	 * @see WXSUtils#reconnectUsingGrid(ObjectGrid)
	 * @see WXSUtils#reconnectToDefaultUtilsGrid()
	 */
	public WXSUtils(WXSUtils utils) {
		this.grid = utils.getObjectGrid();
		threadPool = utils.threadPool;
		this.configProps = utils.configProps;
		tls = new ThreadLocalSession(this);
	}

	/**
	 * This constructs an instance of this helper class.
	 * 
	 * @param grid
	 *            The grid to use with this instance. It is usually a client connection
	 * @param pool
	 *            The thread pool to use to parallelize later operations.
	 */
	public WXSUtils(ObjectGrid grid, ExecutorService pool) {
		this.grid = grid;
		threadPool = pool;
		tls = new ThreadLocalSession(this);
	}

	/**
	 * This returns a named Map that can be used to hold lists for keys
	 * 
	 * @param <K>
	 * @param <V>
	 * @param listName
	 * @return
	 */
	public <K extends Serializable, V extends Serializable> WXSMapOfLists<K, V> getMapOfLists(String listName) {
		WXSBaseMap bmap = maps.get(listName);
		if (bmap != null && !(bmap instanceof WXSMapOfBigListsImpl)) {
			throw new ObjectGridRuntimeException(listName + " is not a list");
		}
		WXSMapOfBigListsImpl<K, V> rc = (WXSMapOfBigListsImpl<K, V>) bmap;
		if (rc == null) {
			try {
				Session sess = getSessionForThread();
				ObjectMap headMap = sess.getMap(WXSMapOfBigListsImpl.getListHeadMapName(listName));
				ObjectMap bucketMap = sess.getMap(WXSMapOfBigListsImpl.getListBucketMapName(listName));
				ObjectMap dirtySetMap = sess.getMap(WXSMapOfBigListsImpl.getListDirtySetMapName(listName));
				if (headMap != null && bucketMap != null && dirtySetMap != null) {
					rc = new WXSMapOfBigListsImpl<K, V>(this, listName);
					maps.put(listName, rc);
				} else {
					logger.log(Level.SEVERE, "Templates for LH_, LDIRTY_ and LHB_ missing in xml files [" + listName + "]");
					throw new ObjectGridRuntimeException("Cannot create list:" + listName);
				}
			} catch (Exception e) {
				logger.log(Level.SEVERE, "Templates for LH_, LDIRTY_ and LHB_ likely missing in xml files [" + listName + "]");
				logger.log(Level.SEVERE, "Exception", e);
				throw new ObjectGridRuntimeException(e);
			}
		}
		return rc;
	}

	/**
	 * This returns a Map that can be used to store Sets for a particular key
	 * 
	 * @param <K>
	 * @param <V>
	 * @param mapName
	 * @return
	 */
	public <K, V extends Serializable> WXSMapOfSets<K, V> getMapOfSets(String mapName) {
		WXSBaseMap bmap = maps.get(mapName);
		if (bmap != null && !(bmap instanceof WXSMapOfSetsImpl)) {
			throw new ObjectGridRuntimeException(mapName + " is not a set");
		}
		WXSMapOfSetsImpl<K, V> rc = (WXSMapOfSetsImpl<K, V>) bmap;
		if (rc == null) {
			try {
				if (getSessionForThread().getMap(mapName) != null) {
					rc = new WXSMapOfSetsImpl<K, V>(this, mapName);
					maps.put(mapName, rc);
				} else {
					logger.log(Level.SEVERE, "Unknown map of sets " + mapName);
					throw new ObjectGridRuntimeException("Unknown map of sets:" + mapName);
				}
			} catch (ObjectGridException e) {
				logger.log(Level.SEVERE, "Exception", e);
				throw new ObjectGridRuntimeException(e);
			}
		}
		return rc;
	}

	/**
	 * This returns a simplified Map object to interact with the data in a cache.
	 * 
	 * @param mapName
	 * @return
	 */
	public <K extends Serializable, V extends Serializable> WXSMap<K, V> getCache(String mapName) {
		WXSBaseMap bmap = maps.get(mapName);
		if (bmap != null && !(bmap instanceof WXSMapImpl)) {
			throw new ObjectGridRuntimeException(mapName + " is not a KV map");
		}
		WXSMapImpl<K, V> rc = (WXSMapImpl<K, V>) bmap;
		if (rc == null) {
			try {
				if (getSessionForThread().getMap(mapName) != null) {
					rc = new WXSMapImpl<K, V>(this, mapName);
					maps.put(mapName, rc);
				} else {
					logger.log(Level.SEVERE, "Unknown map " + mapName);
					throw new ObjectGridRuntimeException("Unknown map:" + mapName);
				}
			} catch (ObjectGridException e) {
				logger.log(Level.SEVERE, "Exception", e);
				throw new ObjectGridRuntimeException(e);
			}
		}
		return rc;
	}

	/**
	 * This constructs an instance of this helper class with a built in default
	 * 
	 * @param grid
	 */
	public WXSUtils(ObjectGrid grid) {
		this.grid = grid;
		if (globalThreadPool.get() == null) {
			ExecutorService p = createClientThreadPool(THREADPOOL_SIZE, 1);
			if (!globalThreadPool.compareAndSet(null, p)) {
				p.shutdown();
			}
		}
		threadPool = globalThreadPool.get();
		tls = new ThreadLocalSession(this);
	}

	/**
	 * Get all the values for keys in parallel from a map
	 * 
	 * @param <K>
	 *            The key of the Map
	 * @param <V>
	 *            The values of the Map
	 * @param keys
	 *            The keys to fetch in parallel
	 * @param bmap
	 *            The map from which to fetch the keys
	 * @return A Map of the key/value pairs
	 */
	public <K extends Serializable, V extends Serializable> Map<K, V> getAll(Collection<K> keys, BackingMap bmap) {
		return WXSReduceAgent.callReduceAgentAll(this, GetAllAgent.FACTORY, keys, bmap);
	}

	/**
	 * Checks contains for keys in parallel from a map
	 * 
	 * @param <K>
	 *            The key of the Map
	 * @param <V>
	 *            The values of the Map
	 * @param keys
	 *            The keys to fetch in parallel
	 * @param bmap
	 *            The map from which to fetch the keys
	 * @return A Map of the key/value pairs
	 */
	public <K extends Serializable> Map<K, Boolean> containsAll(Collection<K> keys, BackingMap bmap) {
		return WXSReduceAgent.callReduceAgentAll(this, ContainsAllAgent.FACTORY, keys, bmap);
	}

	/**
	 * This puts the K/V pairs in to the grid in parallel as efficiently as possible. Any existing values for any K are
	 * over written. This does a get before the put on the server side. Use insertAll for a preload type scenario. If a
	 * Loader is used with the Map then this will trigger Loader.get calls for each record.
	 * 
	 * @param <K>
	 *            The key type to use
	 * @param <V>
	 *            The value type to use
	 * @param batch
	 *            The KV pairs to put in the grid
	 * @param bmap
	 *            The map to store them in.
	 */
	public <K extends Serializable, V extends Serializable> void putAll(Map<K, V> batch, BackingMap bmap) {
		internalPutAll(batch, bmap, true, true);
	}

	/**
	 * This is a putAll but it does not write the data through the Loader. This is useful for preload type scenarios
	 * where data is being read from the backend and copied in to the grid but we don't want that data to be pushed back
	 * to the backend in a loop.
	 * 
	 * @param <K>
	 * @param <V>
	 * @param batch
	 * @param bmap
	 */
	public <K extends Serializable, V extends Serializable> void putAll_noLoader(Map<K, V> batch, BackingMap bmap) {
		// no doGet and no writethrough
		internalPutAll(batch, bmap, false, false);
	}

	/**
	 * This updates the current values for a set of keys only if the current value matches the original value passed OR
	 * the new value is inserted if the passed original value is NULL.
	 * 
	 * @param <K>
	 * @param <V>
	 * @param original
	 *            The list of original values to check for in order to update (NULL to insert)
	 * @param updated
	 *            The values to set the key entries to if the condition matches
	 * @param bmap
	 *            The map to use
	 * @return True for every key updated/inserted, false if not updated/inserted
	 */
	public <K extends Serializable, V extends Serializable> Map<K, Boolean> cond_putAll(Map<K, V> original, Map<K, V> updated, BackingMap bmap) {
		if (updated.size() != original.size())
			throw new ObjectGridRuntimeException("Maps are different sizes");
		Set<K> origKeys = original.keySet();
		Set<K> updatedKeys = updated.keySet();
		if (!origKeys.equals(updatedKeys)) {
			throw new ObjectGridRuntimeException("Maps have different keys");
		}
		if (updated.size() > 0) {
			Map<Integer, Map<K, V>> origPmap = WXSAgent.convertToPartitionEntryMap(bmap, original);
			Iterator<Map.Entry<Integer, Map<K, V>>> origItems = origPmap.entrySet().iterator();
			Map<Integer, Map<K, V>> updPmap = WXSAgent.convertToPartitionEntryMap(bmap, updated);

			ArrayList<Future<Map<K, Boolean>>> results = new ArrayList<Future<Map<K, Boolean>>>(origPmap.size());
			while (origItems.hasNext()) {
				Map.Entry<Integer, Map<K, V>> origPerPartitionEntries = origItems.next();
				Map<K, V> updPerPartitionEntries = updPmap.get(origPerPartitionEntries.getKey());
				// if there are items for this partition
				int updSize = updPerPartitionEntries.size();
				int origSize = origPerPartitionEntries.getValue().size();
				if (updSize != origSize) {
					throw new ObjectGridRuntimeException("Orig and new maps must have same keys");
				}
				// we need one key for partition routing
				// so get the first one
				K key = origPerPartitionEntries.getValue().keySet().iterator().next();

				// invoke the agent to add the batch of records to the grid
				ConditionalPutAgent<K, V> ia = new ConditionalPutAgent<K, V>();
				ia.batchBefore = origPerPartitionEntries.getValue();
				ia.newValues = updPerPartitionEntries;
				// Insert all keys for one partition using the first key as a routing key
				Future<Map<K, Boolean>> fv = threadPool.submit(new WXSAgent.CallReduceAgentThread<Map<K, Boolean>>(this, bmap.getName(), key, ia));
				results.add(fv);
			}

			return WXSAgent.collectResultsAsMap(results, ConfigProperties.getAgentTimeout(configProps));
		} else {
			return Collections.emptyMap();
		}
	}

	/**
	 * This inserts the entries in the map. If the entries exist already then this method will fail.
	 * 
	 * @param <K>
	 * @param <V>
	 * @param batch
	 * @param bmap
	 */
	public <K extends Serializable, V extends Serializable> void insertAll(Map<K, V> batch, BackingMap bmap) {
		internalPutAll(batch, bmap, false, true);
	}

	<K extends Serializable, V extends Serializable> void internalPutAll(Map<K, V> batch, BackingMap bmap, boolean doGet, boolean isWriteThrough) {
		InsertAgent.Factory f = new InsertAgent.Factory(doGet, isWriteThrough);
		WXSReduceAgent.callReduceAgentAll(this, f, batch, bmap);
	}

	/**
	 * This removes the Keys from the grid in parallel as efficiently as possible. The entries will also be
	 * <b>removed</b> from any backend if a Loader is used. This isn't an invalidate, it's a remove operation.
	 * 
	 * @param <K>
	 *            The key type to use
	 * @param batch
	 *            The keys to remove from the grid
	 * @param bmap
	 *            The map to store them in.
	 */
	public <K extends Serializable> void removeAll(Collection<K> batch, BackingMap bmap) {
		WXSReduceAgent.callReduceAgentAll(this, RemoveAgent.FACTORY, batch, bmap);
	}

	/**
	 * This invalidates the Keys from the grid in parallel as efficiently as possible. This has no impact on a backend
	 * plugged in using a Loader. It just removes entries from the cache only.
	 * 
	 * @param <K>
	 *            The key type to use
	 * @param batch
	 *            The keys to invalidate from the grid
	 * @param bmap
	 *            The map to store them in.
	 */
	public <K extends Serializable> void invalidateAll(Collection<K> batch, BackingMap bmap) {
		WXSReduceAgent.callReduceAgentAll(this, InvalidateAgent.FACTORY, batch, bmap);
	}

	static Container container;

	/**
	 * This starts a Catalog Server instance within this JVM. This is intended for development scenarios where the
	 * developer wants a catalog running within the JVM with the code being debugged. However, startTestServer is
	 * usually the recommended approach for this
	 * 
	 * @param cep
	 * @param catName
	 * @see WXSUtils#startTestServer(String, String, String)
	 */
	public static void startCatalogServer(String cep, String catName) {
		try {
			// start a collocated catalog server which makes developing
			// in an IDE much easier.
			ServerFactory.getCatalogProperties().setCatalogClusterEndpoints(cep);
			ServerFactory.getCatalogProperties().setCatalogServer(true);
			ServerFactory.getCatalogProperties().setQuorum(false);
			ServerFactory.getServerProperties().setServerName(catName);
			ServerFactory.getServerProperties().setSystemStreamsToFileEnabled(false); // output goes to console, not a
																						// file
			ServerFactory.getServerProperties().setMinimumThreadPoolSize(50);

			// this starts the server
			com.ibm.websphere.objectgrid.server.Server server = ServerFactory.getInstance();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
			throw new ObjectGridRuntimeException(e);
		}

	}

	/**
	 * This is a boiler plate method to create a 'grid' within a single JVM. Both xml files must be loadable from the
	 * class path. This will take about 20 seconds on a 2.4Ghz Core 2 Duo type processor. This makes it easy to do
	 * debugging of the server and client side code for a WXS project as it's a single JVM. This tries to load the xml
	 * files using the WXSUtils classloader. This may not work depending on the deployment environment. The URL version
	 * below allows the application to provide a URL to avoid any class loader issues when required.
	 * 
	 * @param og_xml_path
	 *            The name of the objectgrid.xml file
	 * @param dep_xml_path
	 *            The name of the deployment.xml file
	 * @return A 'client' reference to the created grid.
	 * @see WXSUtils#startTestServer(String, URL, URL)
	 */
	public static ObjectGrid startTestServer(String gridName, String og_xml_path, String dep_xml_path) {
		try {
			URL serverObjectgridXML = WXSUtils.class.getResource(og_xml_path);
			URL deployment = WXSUtils.class.getResource(dep_xml_path);

			return startTestServer(gridName, serverObjectgridXML, deployment);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
			throw new ObjectGridRuntimeException("Cannot start OG container", e);
		}
	}

	/**
	 * This is the same as startTestServer but takes a URL rather than a file name for the xml files.
	 * 
	 * @param gridName
	 * @param serverObjectgridXML
	 * @param deployment
	 * @return
	 */
	public static ObjectGrid startTestServer(String gridName, URL serverObjectgridXML, URL deployment) {
		try {
			startCatalogServer("cs1:localhost:6601:6602", "cs1");

			com.ibm.websphere.objectgrid.server.Server server = ServerFactory.getInstance();

			logger.log(Level.INFO, "Started catalog");
			logger.log(Level.INFO, "OG is " + serverObjectgridXML.toString() + " DP is " + deployment.toString());
			DeploymentPolicy policy = DeploymentPolicyFactory.createDeploymentPolicy(deployment, serverObjectgridXML);
			container = server.createContainer(policy);
			logger.log(Level.INFO, "Container started");
			ClientClusterContext ccc = ObjectGridManagerFactory.getObjectGridManager().connect(null, null);
			ObjectGrid client = ObjectGridManagerFactory.getObjectGridManager().getObjectGrid(ccc, gridName);
			logger.log(Level.INFO, "Got client");
			return client;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
			throw new ObjectGridRuntimeException("Cannot start OG container", e);
		}
	}

	/**
	 * This connects to a remote grid using the default objectgrid.xml in use on the server side.
	 * 
	 * @param cep
	 * @param gridName
	 * @return
	 */
	static public ObjectGrid connectClient(String cep, String gridName) {
		return connectClient(cep, gridName, (String) null);
	}

	/**
	 * This connects to a remote WXS grid.
	 * 
	 * @param cep
	 *            A comma seperated list of host:port pairs for the catalog service
	 * @param gridName
	 *            The name of the grid thats desired.
	 * @param ogXMLPath
	 *            The path to the objectgrid.xml file on the classpath or null
	 * @return A client connection to the grid
	 */
	static public ObjectGrid connectClient(String cep, String gridName, String ogXMLpath) {
		try {
			URL cog = (ogXMLpath != null) ? WXSUtils.class.getResource(ogXMLpath) : null;
			if (ogXMLpath != null && cog == null) {
				logger.log(Level.SEVERE, "Specified og xml path could not be found:" + ogXMLpath);
				throw new ObjectGridRuntimeException("Specified og xml path could not be found:" + ogXMLpath);
			}

			return connectClient(cep, gridName, cog);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
			throw new ObjectGridRuntimeException("Cannot start OG client", e);
		}
	}

	/**
	 * This is the same as connectClient except the objectgrid.xml client file is specified with a URL. This allows the
	 * file to be provided without any class loader dependency issues.
	 * 
	 * @param cep
	 * @param gridName
	 * @param ogXMLURL
	 * @return
	 */
	static public ObjectGrid connectClient(String cep, String gridName, URL ogXMLURL) {
		try {
			ClientClusterContext ccc = ObjectGridManagerFactory.getObjectGridManager().connect(cep, null, ogXMLURL);
			ObjectGrid grid = ObjectGridManagerFactory.getObjectGridManager().getObjectGrid(ccc, gridName);
			return grid;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
			throw new ObjectGridRuntimeException("Cannot start OG client", e);
		}
	}

	/**
	 * This stops the test server if started.
	 */
	static public void stopContainer() {
		if (container != null) {
			container.teardown();
			container = null;
		}
	}

	/**
	 * This returns the ObjectGrid instance associated with this class instance
	 * 
	 * @return
	 */
	public ObjectGrid getObjectGrid() {
		return grid;
	}

	/**
	 * This returns the thread pool in use by this instance.
	 * 
	 * @return
	 */
	public ExecutorService getExecutorService() {
		return threadPool;
	}

	/**
	 * This checks if a WXS exception is retryable, i.e. recoverable or the exception is fatal
	 * 
	 * @param e
	 * @return
	 */
	static public boolean isRetryable(Throwable e) {
		Throwable theCause = null;
		if (e != null) {
			theCause = e.getCause();
			logger.fine("isRetryable Main: " + e.getMessage());
			logger.fine("isRetryable cause: " + theCause.getMessage());
		} else {
			logger.fine("isRetryable Main: null");
			return false;
		}

		boolean firstAttempt = isRetryable2(e);
		boolean secondAttempt = false;

		if (firstAttempt) {
			return true;
		} else {
			// check cause
			secondAttempt = isRetryable2(theCause);

		}

		if (secondAttempt) {
			return true;
		} else {
			if (theCause == null) {
				return false;
			} else {
				return (isRetryable(theCause.getCause()));
			}
		}
	}

	public static boolean isRetryable2(Throwable e) {
		boolean rc = false;
		if (e == null) {
			if (logger.isLoggable(Level.FINE))
				logger.fine("isRetryable: e is null.");
			rc = false;
		} else {
			logger.fine("is Exeption Retryable: " + e);
			if ((e instanceof ObjectGridException)
					&& (e instanceof ClientServerLoaderException || e instanceof TransactionException
							|| e instanceof com.ibm.websphere.objectgrid.ReplicationVotedToRollbackTransactionException || e instanceof ServiceUnavailableException)) {
				rc = true;
			}
		}
		if (logger.isLoggable(Level.FINE))
			logger.fine("returning retryable code: " + rc);
		return rc;
	}

	/**
	 * This returns the next atomically incremented long at the entry for K. The VALUE type MUST be a Long for this to
	 * work.
	 * 
	 * @param k
	 * @return
	 */
	public <K> long atomic_increment(String mapName, K k) {
		try {
			Session sess = getSessionForThread();
			while (true) {
				try {
					sess.begin();
					ObjectMap map = sess.getMap(mapName);
					Long partitionIdValue = (Long) map.getForUpdate(k);
					if (partitionIdValue == null) {
						partitionIdValue = Long.MIN_VALUE;
						map.insert(k, partitionIdValue);
					} else {
						partitionIdValue = Long.valueOf(partitionIdValue.longValue() + 1);// rk changed
						map.update(k, partitionIdValue);
					}
					sess.commit();
					return partitionIdValue;
				} catch (Exception e) {
					if (sess.isTransactionActive()) {
						sess.rollback();
					}
					if (WXSUtils.isRetryable(e)) {
						continue;
					}
				}
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception", e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	static WXSUtils globalDefaultUtils;

	/**
	 * This creates the client side thread pool for WXS. This uses a CallerRunsPolicy so that when clients are
	 * preloading the grid, we don't end up queueing lots of preload agent calls. This leads to an out of memory
	 * situation quickly because the client keeps fetching from the backend, makings lots of objects for that state and
	 * sending it to the threadpool where it just builds up.
	 * 
	 * @param numThreads
	 *            The desired number of threads in the pool.
	 * @return
	 */
	private static ExecutorService createClientThreadPool(int numThreads, int queueSize) {
		// this means that once there are 3x numThreads jobs queued waiting for
		// a thread then it will start running jobs on the submitting thread.
		LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>(queueSize);

		// Once the queue reports that it is full, the CallerRunPolicy will run jobs
		// on the submitter thread.
		ExecutorService p = new ThreadPoolExecutor(numThreads, numThreads, 2L, TimeUnit.MINUTES, queue, new ThreadPoolExecutor.CallerRunsPolicy());
		return p;
	}

	/**
	 * This is a helper method to return a configured grid connection. The configuration is specified in the property
	 * file wxsutils.properties on the classpath. The grid name and path to objectgrid.xml file must always be
	 * specified. If a remote grid connection is required then a cep must be specified also. If a local intra JVM test
	 * grid should be started then omit the cep property and specify a deployment xml file path. Any unexpected
	 * exceptions are wrapped in an {@link ObjectGridRuntimeException}
	 * 
	 * This can be called multiple times and the same WXSUtils instance is returned. It's a JVM wide instance
	 * 
	 * cep=XXXXX grid=XXXX (default Grid) og_xml_path=XXXXX (default /objectgrid.xml) dp_xml_path=XXXXX (default
	 * /deployment.xml)
	 * 
	 * @return
	 * @throws FileNotFoundException
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws ObjectGridRuntimeException
	 */
	static synchronized public WXSUtils getDefaultUtils() throws FileNotFoundException, URISyntaxException, IOException {
		if (globalDefaultUtils == null) {
			ConfigProperties cprops = new ConfigProperties();

			try {
				ObjectGrid grid = cprops.connect();
				if (cprops.numThreads > 0) {
					ExecutorService p = createClientThreadPool(cprops.numThreads, cprops.queueLength);
					logger.log(Level.INFO, "WXSUtils thread pool is " + cprops.numThreads + " threads");
					globalDefaultUtils = new WXSUtils(grid, p);
				} else {
					String aMapName = (String) grid.getListOfMapNames().get(0);
					BackingMap aMap = grid.getMap(aMapName);
					int numPartitions = aMap.getPartitionManager().getNumOfPartitions();
					ExecutorService p = createClientThreadPool(numPartitions, cprops.queueLength);
					logger.log(Level.INFO, "WXSUtils thread pool is " + numPartitions + " threads");
					globalDefaultUtils = new WXSUtils(grid);
				}
				globalDefaultUtils.configProps = cprops;
			} catch (Throwable e) {
				logger.log(Level.SEVERE, "Cannot connect to grid ", e);
				throw new ObjectGridRuntimeException(e); // BN don't swallow these, rethrow them
			}
		}
		return globalDefaultUtils;
	}

	/**
	 * This will reconnect to the grid. All threads will then try to use the new grid when obtaining sessions. This will
	 * re-open the property file to read the current properties.
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void reconnectToDefaultUtilsGrid() throws IOException, FileNotFoundException {
		ConfigProperties cprops = new ConfigProperties();
		this.configProps = cprops;
		reconnectUsingGrid(cprops.connect());
	}

	/**
	 * This allows an application to reconnect manually to a WXS grid and then repurpose this utils to use the new
	 * connection. All thread locals etc will reset to this once this is done.
	 * 
	 * @param client
	 *            The new client connection
	 */
	public void reconnectUsingGrid(ObjectGrid client) {
		logger.log(Level.WARNING, "Switching to new client connection for wxsutils instance", client);
		grid = client;
	}

	/**
	 * This returns the WXS Session being used by this WXSUtils instance for this thread. If you create multiple
	 * WXSUtils instances, each one has a different ThreadLocal
	 * 
	 * @return The WXS Session in use for this thread with this WXSUtil instance
	 */
	public Session getSessionForThread() {
		return tls.getSession();
	}

	@Beta
	public static final Logger getLogger() {
		return logger;
	}

	@Beta
	public final Map<String, WXSBaseMap> getMaps() {
		return maps;
	}

	@Beta
	public final ThreadLocalSession getTls() {
		return tls;
	}

	@Beta
	public ConfigProperties getConfigProperties() {
		return configProps;
	}

}
