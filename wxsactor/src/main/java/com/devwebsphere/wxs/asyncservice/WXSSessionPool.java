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
package com.devwebsphere.wxs.asyncservice;

import java.util.AbstractQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.websphere.objectgrid.NoActiveTransactionException;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.SessionHandle;
import com.ibm.websphere.objectgrid.TargetNotAvailableException;
import com.ibm.websphere.objectgrid.TransactionAlreadyActiveException;
import com.ibm.websphere.objectgrid.TransactionException;
import com.ibm.websphere.objectgrid.TxID;
import com.ibm.websphere.objectgrid.UndefinedMapException;
import com.ibm.websphere.objectgrid.em.EntityManager;
import com.ibm.websphere.objectgrid.plugins.LogSequence;
import com.ibm.websphere.objectgrid.query.ObjectQuery;
import com.ibm.websphere.objectgrid.query.ObjectQueryException;

/**
 * This will preallocate a number of sessions and then use a blocking pool to serve them.
 * A Session proxy is returned which allows the Session to be isolated on returning it to the
 * pool, this makes debugging easier.
 * @author bnewport
 *
 */
final public class WXSSessionPool 
{
	static final Logger logger = Logger.getLogger(WXSSessionPool.class.getName());
	ObjectGrid clientGrid;
	AbstractQueue<SProxy> sessionPool;

	/**
	 * Creates a session pool to serve Sessions. WXS Sessions are relatively
	 * expensive to create so pooling them makes sense.
	 * @param grid The grid from which to allocate Sessions
	 * @param maxSessions The maximum number of Sessions to pool
	 * @throws ObjectGridException
	 */
	public WXSSessionPool(ObjectGrid grid, int maxSessions)
		throws ObjectGridException
	{
		// true means use FIFO on waiting threads. This makes it fairer
		sessionPool = new ArrayBlockingQueue<SProxy>(maxSessions, true);
		// allocate the sessions and add to the pool
		for(int i = 0; i < maxSessions; ++i)
		{
			Session s = grid.getSession();
			sessionPool.add(new SProxy(s));
		}
	}

	/**
	 * This waits for a Session to become available and returns it
	 * @return
	 */
	public Session getSession()
	{
		SProxy p = sessionPool.remove();
		p.initialize();
		return p;
	}

	/**
	 * This returns the Session to the pool
	 * @param s
	 */
	public void returnSession(Session s)
	{
		SProxy p = (SProxy)s;
		// clean it up if needed
		if(p.isTransactionActive())
		{
			try
			{
				p.rollback();
			}
			catch(Exception e)
			{
				logger.log(Level.SEVERE, "Exception returning session", e);
			}
		}
		p.destroy();
		sessionPool.add(p);
	}

	/**
	 * Proxy Session to allow us to disconnect a Session once
	 * it is returned to the pool.
	 * @author bnewport
	 *
	 */
	private final static class SProxy implements Session
	{
		Session realSession;
		Session s;
		SProxy(Session s)
		{
			this.s = s;
		}

		void initialize()
		{
			s = realSession;
		}
		
		void destroy()
		{
			s = null;
		}
		
		public void begin() throws TransactionAlreadyActiveException,
				TransactionException 
		{
			s.begin();
		}

		public void beginNoWriteThrough()
				throws TransactionAlreadyActiveException, TransactionException {
			s.beginNoWriteThrough();
		}

		public void commit() throws NoActiveTransactionException,
				TransactionException {
			s.commit();
		}

		public ObjectQuery createObjectQuery(String arg0)
				throws ObjectQueryException {
			return s.createObjectQuery(arg0);
		}

		public void flush() throws NoActiveTransactionException,
				TransactionException {
			s.flush();
		}

		public EntityManager getEntityManager() {
			return s.getEntityManager();
		}

		public ObjectMap getMap(String arg0) throws UndefinedMapException {
			return getMap(arg0);
		}

		public ObjectGrid getObjectGrid() 
		{
			return s.getObjectGrid();
		}

		public SessionHandle getSessionHandle() {
			// TODO Auto-generated method stub
			return null;
		}

		public int getTransactionIsolation() {
			return s.getTransactionIsolation();
		}

		public int getTransactionTimeout() {
			return s.getTransactionTimeout();
		}

		public TxID getTxID() throws NoActiveTransactionException {
			return s.getTxID();
		}

		public ObjectMap getViewMap(String arg0) throws UndefinedMapException,
				ObjectGridRuntimeException {
			return s.getViewMap(arg0);
		}

		public boolean isCommitting() {
			return s.isCommitting();
		}

		public boolean isFlushing() {
			return s.isFlushing();
		}

		public boolean isMarkedRollbackOnly() {
			return s.isMarkedRollbackOnly();
		}

		public boolean isTransactionActive() {
			return s.isTransactionActive();
		}

		public boolean isWriteThroughEnabled() {
			return s.isWriteThroughEnabled();
		}

		public void markRollbackOnly(Throwable arg0)
				throws NoActiveTransactionException {
			s.markRollbackOnly(arg0);
		}

		public void processLogSequence(LogSequence arg0)
				throws NoActiveTransactionException, UndefinedMapException,
				ObjectGridException {
			s.processLogSequence(arg0);
		}

		public void rollback() throws NoActiveTransactionException,
				TransactionException {
			s.rollback();
		}

		public void setSessionHandle(SessionHandle arg0)
				throws TargetNotAvailableException {
			s.setSessionHandle(arg0);
		}

		public void setTransactionIsolation(int arg0) {
			s.setTransactionIsolation(arg0);
		}

		public void setTransactionTimeout(int arg0) {
			s.setTransactionTimeout(arg0);
		}

		public void setTransactionType(String arg0) {
			s.setTransactionType(arg0);
		}

		public boolean transactionTimedOut() {
			return s.transactionTimedOut();
		}

		public long getRequestRetryTimeout() {
			return s.getRequestRetryTimeout();
		}

		public void setRequestRetryTimeout(long arg0) {
			s.setRequestRetryTimeout(arg0);
		}

		public boolean isSessionHandleSet() {
			return s.isSessionHandleSet();
		}
	}
}
