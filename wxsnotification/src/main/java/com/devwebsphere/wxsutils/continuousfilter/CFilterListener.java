package com.devwebsphere.wxsutils.continuousfilter;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.continuousfilter.CFMessage.Operation;
import com.devwebsphere.wxsutils.filter.Filter;
import com.devwebsphere.wxsutils.multijob.JobExecutor;
import com.devwebsphere.wxsutils.notification.Producer;
import com.devwebsphere.wxsutils.wxsmap.SessionPool;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.plugins.LogElement;
import com.ibm.websphere.objectgrid.plugins.LogElement.Type;
import com.ibm.websphere.objectgrid.plugins.LogSequence;
import com.ibm.websphere.objectgrid.plugins.ObjectGridEventGroup;
import com.ibm.websphere.objectgrid.plugins.ObjectGridEventListener;
import com.ibm.websphere.objectgrid.plugins.index.MapIndex;
import com.ibm.websphere.objectgrid.plugins.index.MapIndexPlugin;

public class CFilterListener implements ObjectGridEventListener, ObjectGridEventGroup.ShardEvents, ObjectGridEventGroup.TransactionEvents {
	static String CLASS_NAME = CFilterListener.class.getName();
	static Logger logger = Logger.getLogger(CLASS_NAME);

	static ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(5);

	class Notification implements Callable<Void> {
		Producer producer;
		Filter filter;
		Queue<CFMessage> messages = new LinkedBlockingQueue<CFMessage>();
		Future<Void> future = null;
		String mapName;

		public Notification(String mapName, Producer p, Filter f) {
			this.mapName = mapName;
			producer = p;
			filter = f;
		}

		public boolean add(CFMessage m) {
			boolean b = messages.offer(m);
			reschedule();

			return b;
		}

		private synchronized void reschedule() {
			if (future == null && messages.size() > 0) {
				future = executor.schedule(this, 100, TimeUnit.MILLISECONDS);
			}
		}

		public Void call() throws Exception {
			int partitionId = -1;
			try {
				CFMessage m;
				while ((m = messages.poll()) != null) {
					partitionId = m.partitionId;

					if (m.operation == Operation.SET) {
						// eat any unnecessary messages before we send everything
						CFMessage next;
						while ((next = messages.peek()) != null) {
							Operation nextO = next.operation;
							if (nextO == Operation.CLEAR) {
								// skip the SET since a CLEAR is coming
								m = messages.poll();
								break;
							} else if (next.operation == Operation.ADD || next.operation == Operation.REMOVE) {
								// eat the operation since the SET will include it
								messages.poll();
							}
						}

						if (m.operation == Operation.SET) {
							// we are still doing a SET
							m.keys = getFilteredKeys();
						}
					}

					producer.send(m);
				}
			} catch (Exception e) {
				logger.logp(Level.SEVERE, CLASS_NAME, "setNotification", "Exception", e);
				setNotification(partitionId, mapName, producer, null);
			}

			synchronized (this) {
				future = null;
				reschedule();
			}

			return null;
		}

		private Object[] getFilteredKeys() {
			Session s = SessionPool.getPooledSession(objectGrid);
			LinkedList<Object> keys = new LinkedList<Object>();
			try {
				ObjectMap oMap = s.getMap(mapName);
				MapIndex allKeysIndex = (MapIndex) oMap.getIndex(MapIndexPlugin.SYSTEM_KEY_INDEX_NAME);
				for (Iterator<Object> iterator = allKeysIndex.findAll(); iterator.hasNext();) {
					Object key = iterator.next();
					Object o = oMap.get(key);
					if (filter.filter(o)) {
						keys.add(key);
					}
				}
				return keys.toArray(new Object[keys.size()]);
			} catch (ObjectGridException e) {
				logger.logp(Level.SEVERE, CLASS_NAME, "getFilteredKeys", "Error", e);
			} finally {
				if (s != null) {
					SessionPool.returnSession(s);
				}
			}

			return null;
		}
	}

	static ConcurrentHashMap<Integer, CFilterListener> cfListeners = new ConcurrentHashMap<Integer, CFilterListener>();
	private ConcurrentHashMap<String, Vector<Notification>> notifiers = new ConcurrentHashMap<String, Vector<Notification>>();
	private volatile boolean active = false;

	ObjectGrid objectGrid;

	int partitionId = -1;

	public void destroy() {

	}

	public void initialize(Session s) {
	}

	public void transactionBegin(String txid, boolean isWriteThroughEnabled) {
	}

	public void transactionEnd(String txid, boolean isWriteThroughEnabled, boolean isCommitted, Collection changes) {
		if (!active || !isCommitted) {
			return;
		}

		for (Iterator<LogSequence> iterator = changes.iterator(); iterator.hasNext();) {
			LogSequence seq = iterator.next();
			String mapName = seq.getMapName();
			Vector<Notification> notifications = notifiers.get(mapName);
			if (notifications != null) {
				Notification[] toNotify = notifications.toArray(new Notification[notifications.size()]);

				LinkedList<Object> keysAdded = new LinkedList<Object>();
				LinkedList<Object> keysRemoved = new LinkedList<Object>();
				for (Notification n : toNotify) {
					for (Iterator<LogElement> iPending = seq.getPendingChanges(); iPending.hasNext();) {
						LogElement e = iPending.next();

						Object o;
						Type type = e.getType();
						if (type == LogElement.DELETE || type == LogElement.EVICT) {
							o = e.getBeforeImage();
						} else {
							o = e.getAfterImage();
						}
						boolean notify = (o != null) ? n.filter.filter(o) : false;

						switch (type.getCode()) {
						case LogElement.CODE_CLEAR:
							CFMessage m = new CFMessage(partitionId, Operation.CLEAR, null);
							n.add(m);
							break;
						case LogElement.CODE_INSERT:
							if (notify) {
								keysAdded.add(e.getKey());
							}
							break;
						case LogElement.CODE_UPDATE:
							if (notify) {
								keysAdded.add(e.getKey());
							} else {
								keysRemoved.add(e.getKey());
							}
							break;
						case LogElement.CODE_DELETE:
						case LogElement.CODE_EVICT:
							if (notify) {
								keysRemoved.add(e.getKey());
							}
							break;
						}
					}
					if (keysRemoved.size() > 0) {
						CFMessage m = new CFMessage(partitionId, Operation.REMOVE, keysRemoved);
						n.add(m);

					}
					if (keysAdded.size() > 0) {
						CFMessage m = new CFMessage(partitionId, Operation.ADD, keysAdded);
						n.add(m);
					}

				}
			}
		}
	}

	public void shardActivated(ObjectGrid og) {
		objectGrid = og;
		List<String> mapNames = og.getListOfMapNames();
		Session s = SessionPool.getPooledSession(og);
		partitionId = og.getMap(JobExecutor.routingMapName).getPartitionId();
		try {
			for (String mapName : mapNames) {
				if (mapName.startsWith(CFAgent.CONTINUOUS_FILTER_PREFIX)) {
					try {
						CFMessage setMsg = new CFMessage(partitionId, Operation.SET, null);
						ObjectMap m = s.getMap(mapName);
						Vector<Notification> notifications = new Vector<Notification>();
						MapIndex allKeysIndex = (MapIndex) m.getIndex(MapIndexPlugin.SYSTEM_KEY_INDEX_NAME);
						for (Iterator<Producer> iterator = allKeysIndex.findAll(); iterator.hasNext();) {
							Producer p = iterator.next();
							Filter filter = (Filter) m.get(p);
							Notification n = new Notification(mapName, p, filter);
							notifications.add(n);
							n.add(setMsg);
						}
						if (notifications.size() > 0) {
							notifiers.put(mapName, notifications);
						}
					} catch (Exception e) {
						logger.logp(Level.SEVERE, CLASS_NAME, "shardActivated", "Error", e);
					}
				}
			}

			if (notifiers.size() > 0) {
				active = true;
			}

			// register ourselves
			cfListeners.put(partitionId, this);
		} finally {
			SessionPool.returnSession(s);
		}

	}

	public void shardDeactivate(ObjectGrid og) {
		active = false;
		cfListeners.remove(partitionId);
	}

	static public boolean setNotification(int partitionId, String mapName, Producer p, Filter f) {
		CFilterListener listener = cfListeners.get(partitionId);
		if (listener != null) {
			return listener.setNotification(mapName, p, f);
		}
		return false;
	}

	public boolean setNotification(String mapName, Producer p, Filter f) {
		Vector<Notification> notifications = notifiers.get(mapName);
		if (notifications == null) {
			notifications = new Vector<Notification>();
			Vector<Notification> oldNotif = notifiers.putIfAbsent(mapName, notifications);
			if (oldNotif != null) {
				notifications = oldNotif;
			}
		}

		synchronized (notifications) {
			for (Iterator<Notification> iterN = notifications.iterator(); iterN.hasNext();) {
				Notification n = iterN.next();
				if (n.producer.equals(p)) {
					if (f == null) {
						// remove producer
						iterN.remove();
						if (notifications.isEmpty()) {
							notifiers.remove(mapName);
						}

						if (notifiers.isEmpty()) {
							active = false;
						}

						n.producer.stop();
					} else {
						n.filter = f;
						active = true;
					}
					return true;
				}
			}

			if (f != null) {
				try {
					p.start();

					Notification n = new Notification(mapName, p, f);
					notifications.add(n);
					n.add(new CFMessage(partitionId, Operation.SET, null));
					active = true;
					return true;
				} catch (Exception e) {
					logger.logp(Level.SEVERE, CLASS_NAME, "setNotification", "Exception", e);
				}

			}
		}

		return false;
	}
}
