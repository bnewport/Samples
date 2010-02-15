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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.WXSUtils;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ClientClusterContext;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridManagerFactory;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.AgentManager;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;

/**
 * Still testing. Wrapper for application messages with multicast behavior
 * @author bnewport
 *
 * @param <V>
 */
public class MulticastMessage<V>
{
	static Logger logger = Logger.getLogger(MulticastMessage.class.getName());
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1594277498069390181L;
	
	Serializable message;
	
	public MulticastMessage()
	{
	}
	
	public MulticastMessage(Serializable message)
	{
		this.message = message;
	}
		
	public Map<Integer, V> onMessage(Session sess, String MsgId) 
	{
		final String historyId = MsgId + ".M";
		
		try
		{
			// need a client grid reference to invoke agents.
			ClientClusterContext ccc = ObjectGridManagerFactory.getObjectGridManager().connect(null, null);
			ObjectGrid clientGrid = ObjectGridManagerFactory.getObjectGridManager().getObjectGrid(ccc, sess.getObjectGrid().getName());
			ObjectMap clientQueueMap = clientGrid.getSession().getMap(MapNames.QUEUE_MAP);
			AgentManager am = clientQueueMap.getAgentManager();
			
			BackingMap bmap = clientGrid.getMap(MapNames.QUEUE_MAP);
			
			//ObjectMap queueMap = sess.getMap(MapNames.QUEUE_MAP);
			int numPartitions = bmap.getPartitionManager().getNumOfPartitions();
			
			if(logger.isLoggable(Level.FINE))
			{
				logger.log(Level.FINE, "processing " + MsgId + " for " + numPartitions + " partitions (incoming)");
			}
			Set<Integer> remainingPartitions = new HashSet<Integer>();
			for(int i = 0; i < numPartitions; ++i)
			{
				remainingPartitions.add(new Integer(i));
			}
			
			// get results for all partitions, failures are rare
			
			while(!remainingPartitions.isEmpty())
			{
				MultiSendAgent<V> agent = new MultiSendAgent<V>();
				agent.message = message;
				agent.neededPartitions = remainingPartitions;
				agent.msgId = historyId;

				// this asynchronously invokes each partition and synchronously
				// blocks for results.
				if(logger.isLoggable(Level.FINE))
				{
					logger.log(Level.FINE, "calling multi send agent.");
				}
				Map<Integer, Boolean> rc = am.callMapAgent(agent);
				
				if(logger.isLoggable(Level.FINE))
				{
					logger.log(Level.FINE, "multi send agent returns.");
				}
				
				Iterator<Map.Entry<Integer, Boolean>> iter = rc.entrySet().iterator();
				
				while(iter.hasNext())
				{
					Map.Entry<Integer, Boolean> e = iter.next();
					if(e.getValue())
					{
						if(logger.isLoggable(Level.FINE))
						{
							logger.log(Level.FINE, "Message " + historyId + " was sent to partition " + e.getKey());
						}
						remainingPartitions.remove(e.getKey());
					}
				}
			}

			if(logger.isLoggable(Level.FINE))
			{
				logger.log(Level.FINE, "all partitions notified");
			}
			Map<Integer, V> rc = null;
			while(true) // wait for all results
			{
				if(logger.isLoggable(Level.FINE))
				{
					logger.log(Level.FINE, "calling MultiGetResultAgent for msg "+historyId);
				}
				MultiGetResultAgent<V> agent = new MultiGetResultAgent<V>();
				agent.msgId = historyId;
				rc = am.callMapAgent(agent);
				// check we got answers for all partitions
				Iterator<Map.Entry<Integer, V>> iter = rc.entrySet().iterator();
				while(iter.hasNext())
				{
					Map.Entry<Integer, V> e = iter.next();
					if(logger.isLoggable(Level.FINE))
					{
						logger.log(Level.FINE, "MultiGetResultAgent result "+e.getValue());
					}
					if(e.getValue() == null)
					{
						// oops, retry
						continue;
					}
					
				}
				break;
			}
			return rc;
		}
		catch(ObjectGridException e)
		{
			logger.log(Level.SEVERE, "Exception processing multicast message", e);
		}
		return null;
	}

	static final class MultiGetResultAgent<V> implements MapGridAgent
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -7559456035193017319L;
		String msgId;
		
		public MultiGetResultAgent() {
			if (logger.isLoggable(Level.FINE)) {
				logger.log(Level.FINE, "init");
			}
		}

		public Object process(Session s, ObjectMap m, Object keys) 
		{
			// TODO Auto-generated method stub
			return null;
		}

		public Map<Integer, V> processAllEntries(Session s, ObjectMap m) 
		{
			Map<Integer, V> rc = new HashMap<Integer, V>();
			int partition = s.getObjectGrid().getMap(m.getName()).getPartitionId();
			Integer key = new Integer(partition);
			if(logger.isLoggable(Level.FINE))
			{
				logger.log(Level.FINE, "MultiGetResultAgent:processing receive for " + msgId + "[" + key + "]");
			}
			try
			{
				AsyncServiceFuture<V> fv = new AsyncServiceFuture<V>();
				fv.id = msgId;
				fv.setGrid(s.getObjectGrid());
				V v = fv.get();
				if(logger.isLoggable(Level.FINE))
				{
					logger.log(Level.FINE, "MultiGetResultAgent:processAllEntries "+v);
				}
				rc.put(key, v);
			}
			catch(Exception e)
			{
				logger.log(Level.SEVERE, "Exception getting ", e);
				rc.put(key, null);
			}
			return rc;
		}
	};
	
	static class MultiSendAgent<V> implements MapGridAgent
	{
		private static final long serialVersionUID = -3947730120296781257L;
		
		public MultiSendAgent() {
			if(logger.isLoggable(Level.FINE))	{
				logger.log(Level.FINE,"init");
			}
		}
		
		String msgId;
		Serializable message;
		Set<Integer> neededPartitions;

		public Object process(Session sess, ObjectMap m, Object key) {
			// TODO Auto-generated method stub
			return null;
		}

		public Map<Integer, Boolean> processAllEntries(Session sess, ObjectMap m) 
		{
			if(logger.isLoggable(Level.FINER))
			{
				logger.entering(this.toString(), "processAllEntries");
			}
			Map<Integer, Boolean> rc = new HashMap<Integer, Boolean>();
			Integer key = new Integer(sess.getObjectGrid().getMap(m.getName()).getPartitionId());
			if(logger.isLoggable(Level.FINE))
			{
				logger.log(Level.FINE, "MultiSendAgent:processing send for " + msgId + "[" + key + "]");
			}
			if(neededPartitions.contains(key))
			{
				try
				{
					if(logger.isLoggable(Level.FINE))
					{
						logger.log(Level.FINE, "MultiSendAgent:contains key "+key+" calling sendAsyncMessage2 ");
					}
					AsyncServiceManagerImpl as = new AsyncServiceManagerImpl(new WXSUtils(sess.getObjectGrid()));
	
					as.sendAsyncMessage2(null, message, msgId);
					rc.put(key, Boolean.TRUE);
				}
				catch(Exception e)
				{
					logger.log(Level.SEVERE, "MultisendAgent sending ", e);
					rc.put(key, Boolean.FALSE);
				}
			}
			else
			{
				if(logger.isLoggable(Level.FINE))
				{
					logger.log(Level.FINE, "MultiSendAgent:does not contain key "+key);
				}
				rc.put(key, Boolean.TRUE);
			}
			
			if(logger.isLoggable(Level.FINER))
			{
				logger.exiting(this.toString(), "processAllEntries");
			}
			
			return rc;
		}
	}
}
