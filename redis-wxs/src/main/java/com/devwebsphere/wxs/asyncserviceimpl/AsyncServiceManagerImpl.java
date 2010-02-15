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
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxs.asyncservice.AsyncServiceManager;
import com.devwebsphere.wxs.asyncservice.Job;
import com.devwebsphere.wxs.asyncservice.SerializedFuture;
import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.uuid.WXSUUID;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.DuplicateKeyException;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;

public class AsyncServiceManagerImpl implements AsyncServiceManager
{
	static Logger logger = Logger.getLogger(AsyncServiceManagerImpl.class.getName());
	ObjectGrid grid;
	WXSUUID uuidGenerator;
	//final boolean debug = false;

	public AsyncServiceManagerImpl(WXSUtils utils)
		throws ObjectGridException
	{
		grid = utils.getObjectGrid();
		uuidGenerator = new WXSUUID(utils, MapNames.SYSTEM_MAP);
 	}
	
	protected static final String timestampKey = AsyncServiceManagerImpl.class.getName() + "_timestamp";
	
	private static final long serialVersionUID = -8327540866404893880L;

	/**
	 * This is an internal method that sends a message using a specific id.
	 * @param message The message to send
	 * @param id The id to use
	 * @return The Future for the message
	 * @throws ObjectGridException
	 */
	public <V> Future<V> sendAsyncMessage2(Session userSession, Serializable message, Serializable id)
		throws ObjectGridException
	{
		Session sess = null;
		if(userSession != null)
			sess = userSession;
		else
			sess = grid.getSession();
		AsyncServiceFuture<V> future = new AsyncServiceFuture<V>(grid, id);
		future.id = id;
		boolean isSendable = true;
		ObjectMap q = sess.getMap(MapNames.QUEUE_MAP);
		ObjectMap history = sess.getMap(MapNames.HISTORY_MAP);
		while(isSendable)
		{
			if(userSession == null) sess.begin();
			try
			{
				if(!history.containsKey(id)) // not already processed
				{
					// acquires X lock whether id is present or not
					q.insert(id, message);
				}
				if(userSession == null) sess.commit();
				if(logger.isLoggable(Level.FINE))
				{
					logger.fine("successfully inserted a new message " + id);
				}
				updateCount();					
				return future;
			}
			catch(DuplicateKeyException e)
			{
				// it was already there.
				return future;
			}
			catch(Exception e)
			{
				if(sess.isTransactionActive()) {
					sess.rollback();
				}
				
				isSendable = WXSUtils.isRetryable(e);
				
				if(true && isSendable && logger.isLoggable(Level.FINE))
				{
					logger.fine( "Re-trying " + id);
				}
				if(!isSendable)
				{
					logger.log(Level.SEVERE, "Exception in sendAsyncMessage2", e);
					throw new ObjectGridRuntimeException("Cannot send message ", e);
				}
			}
		}
		return null;
	}
	
	public <V> Future<V> sendAsyncMessage(Serializable message)
		throws ObjectGridRuntimeException
	{
		try
		{
			String id = getNextClusterUUID();
			return sendAsyncMessage2(null, message, id);
		}
		catch(ObjectGridException e)
		{
			logger.log(Level.SEVERE, "Exception in sendAsyncMessage", e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	/**
	 * This sends a message to the partition holding the specified key for a Map.
	 * This happens immediately independant of any transaction. It's guaranteed
	 * to execute once remotely.
	 * @param <V>
	 * @param mapName The name of the Map for the key
	 * @param key The key of the entry
	 * @param message The message to send
	 * @return
	 */
	public <V> Future<V> sendAsyncRoutedJob(String mapName, Object key, Job<V> job)
	{
		try
		{
			BackingMap map = grid.getMap(mapName);
			int pid = map.getPartitionManager().getPartition(key);
			String id = getNextClusterUUID();
			RoutableKey r = new RoutableKey(pid, id);
			return sendAsyncMessage2(null, job, r);
		}
		catch(ObjectGridException e)
		{
			logger.log(Level.SEVERE, "Exception in sendAsyncRoutedJob", e);
			throw new ObjectGridRuntimeException(e);
		}
	}
	
	public void scheduleDurableLocalJob(Session sess, Job job)
		throws ObjectGridRuntimeException
	{
		try
		{
			String id = getNextClusterUUID();
			sendAsyncMessage2(sess, job, id);
		}
		catch(ObjectGridException e)
		{
			logger.log(Level.SEVERE, "Exception in scheduleDurableLocalJob", e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	public long successCount=0;

	/**
	 * This returns a grid wide UUID.
	 */
	public String getNextClusterUUID()
		throws ObjectGridRuntimeException
	{
		return uuidGenerator.getNextClusterUUID();
	}

	public <V> SerializedFuture<V> serialize(Future<V> f) 
	{
		AsyncServiceFuture<V> a = (AsyncServiceFuture<V>)f;
		return a;
	}

	/**
	 * Still testing
	 */
	public <V> Future<Map<Integer, V>> sendAsyncAllPartitions(Serializable multicastMessage) throws ObjectGridRuntimeException 
	{
		try
		{
			MulticastMessage<V> mm = new MulticastMessage<V>(multicastMessage);
			
			Session sess = grid.getSession();
			String id = getNextClusterUUID();
			AsyncServiceFuture<Map<Integer, V>> future = new AsyncServiceFuture<Map<Integer, V>>(grid, id);
			future.id = id;
			boolean isSendable = true;
			boolean messageInserted = false;
			while(isSendable)
			{
				messageInserted=false;
				sess.begin();
				try
				{
					ObjectMap q = sess.getMap(MapNames.QUEUE_MAP);
					ObjectMap history = sess.getMap(MapNames.HISTORY_MAP);
					if(!history.containsKey(id)) // not already processed
					{
						Object o = q.getForUpdate(id);
						if(o == null)
						{
							if( logger.isLoggable(Level.FINE))
							{
								logger.fine( "Inserting new message " + id);
							}
							q.insert(id, mm);
							messageInserted=true;
						}
					}
					sess.commit();
					if( logger.isLoggable(Level.FINE))
					{
						
						logger.fine( "successfully inserted a new message " + id);
					}
					if( messageInserted )
					{
						updateCount();					
					}
					
					return future;
				} 
				catch(Exception e)
				{
					logger.log(Level.FINE, "Exception in sendAsyncMessage for " + id, e);
					if(sess.isTransactionActive()) {
						sess.rollback();
					}
					isSendable = WXSUtils.isRetryable(e);
					if(!isSendable) {
						throw new ObjectGridRuntimeException("Cannot send message ", e);
					}
					logger.fine("Retrying "+isSendable);
				}
			}
		}
		catch(ObjectGridException e)
		{
			logger.log(Level.SEVERE, "Exception in sendAsyncAllPartitions", e);
			throw new ObjectGridRuntimeException(e);
		}
		return null;
	}
	
	private synchronized void updateCount() {
		successCount++;
		
	}
}
