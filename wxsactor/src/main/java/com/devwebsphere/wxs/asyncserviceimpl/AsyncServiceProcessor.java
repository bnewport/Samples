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
import java.util.ArrayList;
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
import com.ibm.websphere.objectgrid.plugins.ObjectGridEventGroup;

public class AsyncServiceProcessor<T> implements ObjectGridEventGroup.ShardEvents {
	static ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(3);

	ArrayList<Listener> listeners;
	PerfReporter reporter;
	static Logger logger = Logger.getLogger(AsyncServiceProcessor.class.getName());
	int partitionId;
	volatile int numMessagesProcessed;

	// Number of alarms per partition.
	int numAlarms = 3;

	MessageProcessor<T> listenerInstance = null;
	ScheduledFuture<?> perfTask;
	ThreadLocalGridClient tlsOGClient;

	/**
	 * This is called whenever a primary shard is place in a JVM. It starts the dispatcher threads at that point.
	 */
	public void shardActivated(ObjectGrid shard) {
		try {
			tlsOGClient = new ThreadLocalGridClient(shard);
			BackingMap queueMap = shard.getMap(MapNames.QUEUE_MAP);
			partitionId = queueMap.getPartitionId();
			logger.info("Activated " + partitionId);
			listeners = new ArrayList<Listener>(numAlarms);
			for (int i = 0; i < numAlarms; ++i) {
				Listener lis = new Listener(shard, queueMap, i);
				executor.schedule(lis, Listener.idleSleepTime, TimeUnit.MILLISECONDS);
				listeners.add(lis);
			}

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
		for (Listener l : listeners) {
			l.stopListening();
		}

		listeners.clear();
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
		static final int idleSleepTime = 250;
		static final int maxCounter = 200;
		ObjectGrid grid;
		Session sess;
		BackingMap queue;
		ObjectMap map;
		ObjectMap processedIdMap;
		volatile boolean isActive;
		int counter;
		int sleepTime;

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
			counter = maxCounter;
			sleepTime = idleSleepTime;
		}

		void stopListening() {
			this.isActive = false;
		}

		public Boolean call() {
			if (isActive()) {
				try {
					sess.begin();
					// get next unlocked/unprocessed message from queue
					RoutableKey nextMsgKey = (RoutableKey) map.getNextKey(sleepTime);
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
						counter = maxCounter;
						// short timeouts when we're busy
						sleepTime = busySleepTime;
					} else {
						// if no work then reset transaction every maxCounter tries
						if (--counter == 0) {
							counter = maxCounter;
							// we're idle to switch to long sleep times
							sleepTime = idleSleepTime;
						}
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
				executor.schedule(this, sleepTime, TimeUnit.MILLISECONDS);
			}
			return Boolean.TRUE;
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

}
