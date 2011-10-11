//
//This sample program is provided AS IS and may be used, executed, copied and
//modified without royalty payment by customer (a) for its own instruction and 
//study, (b) in order to develop applications designed to run with an IBM 
//WebSphere product, either for customer's own internal use or for redistribution 
//by customer, as part of such an application, in customer's own products. "
//
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2009
//All Rights Reserved * Licensed Materials - Property of IBM
//
package com.devwebsphere.wxs.asyncserviceimpl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxs.asyncservice.Job;
import com.devwebsphere.wxs.asyncservice.MessageProcessor;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.plugins.LogElement;
import com.ibm.websphere.objectgrid.plugins.LogSequence;
import com.ibm.websphere.objectgrid.plugins.ObjectGridEventGroup;
import com.ibm.websphere.objectgrid.plugins.ObjectGridEventListener;

public class AsyncServiceProcessor<T> implements ObjectGridEventListener, ObjectGridEventGroup.TransactionEvents, ObjectGridEventGroup.ShardEvents,
		ObjectGridEventGroup.ShardLifecycle {
	static Logger logger = Logger.getLogger(AsyncServiceProcessor.class.getName());
	static ScheduledExecutorService executor;

	Object[] listeners;
	PerfReporter reporter;

	int partitionId;
	volatile int numMessagesProcessed;

	// Number of alarms per partition.
	int numAlarms = 3;

	MessageProcessor<T> listenerInstance = null;
	ScheduledFuture<?> perfTask;
	ThreadLocalGridClient tlsOGClient;
	private int poolSize = 3;

	public void setThreadPoolSize(int tpSize) {
		synchronized (AsyncServiceProcessor.class) {
			if (executor == null) {
				poolSize = Math.max(1, tpSize);
				executor = new ScheduledThreadPoolExecutor(tpSize);
			}
		}
	}

	public void setWorkers(int workers) {
		numAlarms = Math.max(1, workers);
	}

	/**
	 * This is called whenever a primary shard is place in a JVM. It starts the dispatcher threads at that point.
	 */
	public void shardActivated(ObjectGrid shard) {
		try {
			tlsOGClient = new ThreadLocalGridClient(shard);
			BackingMap queueMap = shard.getMap(MapNames.QUEUE_MAP);
			setThreadPoolSize(poolSize);

			listeners = new Object[numAlarms];
			for (int i = 0; i < numAlarms; ++i) {
				listeners[i] = new Listener(shard, queueMap, i);
			}

			partitionId = queueMap.getPartitionId();
			logger.info("Activated " + partitionId);
			reporter = new PerfReporter();
			perfTask = executor.scheduleAtFixedRate(reporter, 10, 10, TimeUnit.SECONDS);
		} catch (ObjectGridException e) {
			logger.log(Level.SEVERE, "Exception activating Messaging shard", e);
			e.printStackTrace(System.out);
		}
	}

	/**
	 * This is called whenever a primary shard is about to be moved to another JVM. It stops the dispatchers.
	 */
	public void shardDeactivate(ObjectGrid shard) {
		for (Object l : listeners) {
			((Listener) l).stopListening();
		}
		listeners = null;

		perfTask.cancel(false);
	}

	class PerfReporter implements Runnable {
		volatile boolean isActive = true;

		public void run() {
			// System.out.println("P" + partitionId + " = " + (numMessagesProcessed / delay_sec));
			if (logger.isLoggable(Level.FINE)) {
				logger.log(Level.FINE, "MessagesProcessed for partition P" + partitionId + " = " + (numMessagesProcessed / 10));
			}
			numMessagesProcessed = 0;
		}

		public void stopWorking() {
			isActive = false;
		}
	}

	/**
	 * This dispatcher thread
	 * 
	 * @author bnewport
	 * 
	 * @param <T>
	 */
	class Listener implements Callable<Boolean> {
		static final int busySleepTime = 1;
		static final int maxCounter = 200;
		ObjectGrid grid;
		Session sess;
		BackingMap queue;
		ObjectMap map;
		ObjectMap processedIdMap;
		volatile boolean isActive;

		private ScheduledFuture<Boolean> future;
		boolean scheduled = false;

		boolean isActive() {
			return this.isActive;
		}

		Listener(ObjectGrid g, BackingMap q, int threadID) throws ObjectGridException {
			grid = g;
			queue = q;
			sess = grid.getSession();
			isActive = true;
			map = sess.getMap(queue.getName());
			processedIdMap = sess.getMap(MapNames.HISTORY_MAP);
		}

		void stopListening() {
			this.isActive = false;
		}

		public Boolean call() {
			if (isActive()) {
				boolean reschedule = true;
				try {
					sess.begin();
					// get next unlocked/unprocessed message from queue
					RoutableKey nextMsgKey = (RoutableKey) map.getNextKey(busySleepTime);
					if (nextMsgKey != null) {
						++numMessagesProcessed;
						// we have a message
						Serializable message = (Serializable) map.get(nextMsgKey);
						T result = AsyncServiceProcessor.this.processMessage(nextMsgKey, message, sess);
						// invoke application process logic on it
						// remove message indicating it's processed
						map.remove(nextMsgKey);
						// store result in history map for peekresult plus duplicate detection
						processedIdMap.insert(nextMsgKey, result);
						sess.commit();
					} else {
						reschedule = false;
					}
				} catch (Exception e) {
					logger.log(Level.SEVERE, "Exception processing message ", e);
				} finally {
					if (sess.isTransactionActive()) {
						try {
							sess.rollback();
						} catch (ObjectGridException se) {
						}
					}
				}

				synchronized (this) {
					future = null;
					schedule(reschedule);
				}
			}
			return Boolean.TRUE;
		}

		synchronized void schedule(boolean reschedule) {
			scheduled |= reschedule;
			if (future == null && scheduled) {
				future = executor.schedule(this, busySleepTime, TimeUnit.MILLISECONDS);
				scheduled = false;
			}
		}
	}

	public int getNumThreads() {
		return numAlarms;
	}

	public void setNumThreads(int numThreads) {
		this.numAlarms = numThreads;
	}

	public T processMessage(RoutableKey id, Serializable msg, Session sess) {
		// if (msg instanceof MulticastMessage)
		// {
		// return ((MulticastMessage)msg).onMessage(sess, id);
		// }
		if (msg instanceof Job) {
			Job<T> qmsg = (Job<T>) msg;
			return qmsg.process(sess, id, tlsOGClient.get());
		} else {
			return listenerInstance.onMessage(sess, id, msg);
		}
	}

	public void setListenerClass(String name) {
		try {
			Class<MessageProcessor<T>> l = (Class<MessageProcessor<T>>) Class.forName(name);
			listenerInstance = l.newInstance();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Cannot create listener class", e);
		}
	}

	public String getListenerClass() {
		if (listenerInstance != null) {
			return listenerInstance.getClass().getName();
		} else {
			return null;
		}
	}

	public void transactionBegin(String txId, boolean isWriteThroughEnabled) {
	}

	public void transactionEnd(String txId, boolean isWriteThroughEnabled, boolean committed, Collection changes) {
		if (committed) {
			for (Object o : changes) {
				LogSequence lseq = (LogSequence) o;
				if (lseq.getMapName().equals(MapNames.QUEUE_MAP)) {
					for (Iterator<LogElement> lEntryItr = lseq.getAllChanges(); lEntryItr.hasNext();) {
						LogElement lElem = lEntryItr.next();
						if (LogElement.INSERT.equals(lElem.getType())) {
							// make sure the workers/alarms are running with inserts
							for (Object l : listeners) {
								((Listener) l).schedule(true);
							}
						}
					}
				}
			}
		}
	}

	public void destroy() {
	}

	public void initialize(Session sess) {
		// disable on clients
		if (sess.getObjectGrid().getObjectGridType() != ObjectGrid.SERVER) {
			sess.getObjectGrid().removeEventListener(this);
		}
	}
}
