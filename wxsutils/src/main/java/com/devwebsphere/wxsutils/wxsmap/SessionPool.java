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
package com.devwebsphere.wxsutils.wxsmap;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.plugins.ObjectGridEventListener;

public class SessionPool {
	static Logger logger = Logger.getLogger(SessionPool.class.getName());

	static class ObjectGridStuff {
		public ObjectGrid grid;
		public ThreadLocal<Session> localSession = new ThreadLocal<Session>();
	}

	static class OGListener implements ObjectGridEventListener {
		ObjectGrid grid;

		public void transactionEnd(String paramString, boolean paramBoolean1, boolean paramBoolean2, Collection paramCollection) {
		}

		public void transactionBegin(String paramString, boolean paramBoolean) {
		}

		public void initialize(Session s) {
			grid = s.getObjectGrid();
		}

		public void destroy() {
			ogStuff.remove(grid);
		}
	}

	static ConcurrentHashMap<ObjectGrid, ObjectGridStuff> ogStuff = new ConcurrentHashMap<ObjectGrid, SessionPool.ObjectGridStuff>(5);

	private SessionPool() {
	}

	/**
	 * This will return the Session for this thread. If checks if the underlying utils instance has reconnected or not.
	 * If it has then it gets a 'new' session.
	 * 
	 * @return
	 */
	static public Session getSessionForThread(ObjectGrid grid) {
		ObjectGridStuff stuff = getObjectGridStuff(grid);
		Session s = stuff.localSession.get();
		if (s == null) {
			// no current session on the thread
			s = getPooledSession(stuff);
			stuff.localSession.set(s);
		}

		return s;

	}

	static public Session getPooledSession(ObjectGrid grid) {
		ObjectGridStuff stuff = getObjectGridStuff(grid);
		return getPooledSession(stuff);
	}

	static private Session getPooledSession(ObjectGridStuff stuff) {
		try {
			return stuff.grid.getSession();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception getting Session from grid", e);
			throw new ObjectGridRuntimeException("Exception getting session from grid", e);
		}
	}

	static ObjectGridStuff getObjectGridStuff(ObjectGrid grid) {
		ObjectGridStuff stuff = ogStuff.get(grid);
		if (stuff == null) {
			ObjectGridStuff previous;
			synchronized (ogStuff) {
				stuff = new ObjectGridStuff();
				stuff.grid = grid;
				previous = ogStuff.putIfAbsent(grid, stuff);
			}
			if (previous == null) {
				// register grid listener to clean up
				grid.addEventListener(new OGListener());
			} else {
				// someone beat us
				stuff = previous;
			}
		}
		return stuff;
	}

	static public void returnSession(Session s) {
		ObjectGridStuff stuff = ogStuff.get(s.getObjectGrid());
		if (stuff != null) {
			if (s == stuff.localSession.get()) {
				stuff.localSession.set(null);
			}

			if (s.isTransactionActive()) {
				logger.log(Level.WARNING, "Session has an active transaction.");
			}

			s.close();
		} else {
			logger.log(Level.WARNING, "Returning a session for an unknown grid.");
		}
	}
}
