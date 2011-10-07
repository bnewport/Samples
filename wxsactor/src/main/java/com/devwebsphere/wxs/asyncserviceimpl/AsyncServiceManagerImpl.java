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
package com.devwebsphere.wxs.asyncserviceimpl;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxs.asyncservice.AsyncServiceManager;
import com.devwebsphere.wxs.asyncservice.Job;
import com.devwebsphere.wxs.asyncservice.KeyOperator;
import com.devwebsphere.wxs.asyncservice.KeyOperatorResult;
import com.devwebsphere.wxs.asyncservice.SerializedFuture;
import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.uuid.WXSUUID;
import com.devwebsphere.wxsutils.wxsmap.SessionPool;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.DuplicateKeyException;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;

public class AsyncServiceManagerImpl implements AsyncServiceManager {
	static Logger logger = Logger.getLogger(AsyncServiceManagerImpl.class.getName());
	private static final long serialVersionUID = -8327540866404893880L;

	protected static final String timestampKey = AsyncServiceManagerImpl.class.getName() + "_timestamp";

	ObjectGrid grid;
	ObjectGrid clientGrid;
	WXSUUID uuidGenerator;
	WXSUtils utils;

	// final boolean debug = false;

	public AsyncServiceManagerImpl(WXSUtils utils) throws ObjectGridException {
		this.utils = utils;
		grid = utils.getObjectGrid();
		uuidGenerator = new WXSUUID(utils, MapNames.SYSTEM_MAP);
		if (grid.getObjectGridType() == ObjectGrid.CLIENT)
			clientGrid = grid;
		else {
			clientGrid = WXSUtils.connectClient(null, grid.getName());
		}
	}

	/**
	 * This is an internal method that sends a message using a specific id.
	 * 
	 * @param message
	 *            The message to send
	 * @param id
	 *            The id to use
	 * @return The Future for the message
	 * @throws ObjectGridException
	 */
	protected <V> AsyncServiceFuture<V> sendAsyncMessage2(Session userSession, Serializable message, RoutableKey id) throws ObjectGridException {
		Session sess = null;
		if (userSession != null) {
			sess = userSession;
		} else {
			sess = SessionPool.getPooledSession(grid);
		}

		AsyncServiceFuture<V> future = new AsyncServiceFuture<V>(grid, id);
		future.id = id;
		boolean isSendable = true;
		ObjectMap q = sess.getMap(MapNames.QUEUE_MAP);
		ObjectMap history = sess.getMap(MapNames.HISTORY_MAP);
		try {
			while (isSendable) {
				if (userSession == null) {
					sess.begin();
				}
				try {
					if (!history.containsKey(id)) { // not already processed
						// acquires X lock whether id is present or not
						q.insert(id, message);
						if (userSession == null) {
							sess.commit();
						}
					}

					if (logger.isLoggable(Level.FINE)) {
						logger.fine("successfully inserted a new message " + id);
					}
					updateCount();
					return future;
				} catch (DuplicateKeyException e) {
					// it was already there.
					return future;
				} catch (Exception e) {
					isSendable = WXSUtils.isRetryable(e) && userSession == null;
					if (true && isSendable && logger.isLoggable(Level.FINE)) {
						logger.fine("Re-trying " + id);
					}
					if (!isSendable) {
						logger.log(Level.SEVERE, "Exception in sendAsyncMessage2", e);
						throw new ObjectGridRuntimeException("Cannot send message ", e);
					}
				} finally {
					if (sess.isTransactionActive()) {
						sess.rollback();
					}
				}
			}
		} finally {
			if (userSession == null) {
				SessionPool.returnSession(sess);
			}
		}
		return null;
	}

	public <V> AsyncServiceFuture<V> sendAsyncMessage(Serializable message) throws ObjectGridRuntimeException {
		try {
			String id = getNextClusterUUID();
			BackingMap map = grid.getMap(MapNames.QUEUE_MAP);
			int pid = map.getPartitionManager().getPartition(id);
			RoutableKey r = new RoutableKey(pid, id);
			return sendAsyncMessage2(null, message, r);
		} catch (ObjectGridException e) {
			logger.log(Level.SEVERE, "Exception in sendAsyncMessage", e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	/**
	 * This sends a message to the partition holding the specified key for a Map. This happens immediately independent
	 * of any transaction. It's guaranteed to execute once remotely.
	 * 
	 * @param <V>
	 * @param mapName
	 *            The name of the Map for the key
	 * @param key
	 *            The key of the entry
	 * @param message
	 *            The message to send
	 * @return
	 */
	public <V> Future<V> sendAsyncRoutedJob(String mapName, Object key, Job<V> job, String id) {
		try {
			BackingMap map = grid.getMap(mapName);
			int pid = map.getPartitionManager().getPartition(key);
			RoutableKey r = new RoutableKey(pid, id);
			return sendAsyncMessage2(null, job, r);
		} catch (ObjectGridException e) {
			logger.log(Level.SEVERE, "Exception in sendAsyncRoutedJob", e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	public <R> AsyncServiceFuture<R> scheduleDurableLocalJob(Session sess, Job<R> job) throws ObjectGridRuntimeException {
		try {
			BackingMap map = grid.getMap(MapNames.QUEUE_MAP);
			String id = getNextClusterUUID();
			int pid = map.getPartitionManager().getPartition(id);
			RoutableKey r = new RoutableKey(pid, id);
			AsyncServiceFuture<R> f = sendAsyncMessage2(sess, job, r);
			return f;
		} catch (ObjectGridException e) {
			logger.log(Level.SEVERE, "Exception in scheduleDurableLocalJob", e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	public AtomicLong successCount = new AtomicLong();

	/**
	 * This returns a grid wide UUID.
	 */
	public String getNextClusterUUID() throws ObjectGridRuntimeException {
		return uuidGenerator.getNextClusterUUID();
	}

	public <V> SerializedFuture<V> serialize(Future<V> f) {
		AsyncServiceFuture<V> a = (AsyncServiceFuture<V>) f;
		return a;
	}

	private void updateCount() {
		successCount.incrementAndGet();
	}

	public <K extends Serializable, D extends KeyOperator<K>> Future<Map<K, KeyOperatorResult<K>>> doChainedTransaction(Session session,
			String mapName, Map<K, D> operators) {
		long now = System.currentTimeMillis();
		ChainedTransactionJob<K, D> job = new ChainedTransactionJob<K, D>(mapName, operators);
		AsyncServiceFuture<Boolean> rc = this.scheduleDurableLocalJob(session, job);
		RoutableKey id = rc.getId();
		return new ChainedTransactionFuture<K>(clientGrid, id.getUUID(), now);
	}
}
