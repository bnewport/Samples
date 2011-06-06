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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.WXSUtils;
import com.devwebsphere.wxsutils.jmx.agent.AgentMBeanImpl;
import com.devwebsphere.wxsutils.utils.ClassSerializer;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.EntryErrorValue;
import com.ibm.websphere.objectgrid.datagrid.ReduceGridAgent;

/**
 * This is used to insert a chunk of records for a given partition using a single hop.
 * It returns TRUE if the operation succeeded. It works two ways. An insert style 
 * and a put style. The insert style just does an insert. It doesn't check if the
 * record existed already. The put style always does a get first and if it exists then
 * do an update, if it didn't exist then do an insert. A Put is especially expensive when
 * a Loader is plugged in to the Map as the get may trigger a SQL SELECT for each key
 * potentially.
 * 
 * Applications preloading data from a database in to the grid should use insertAll. The grid
 * maps need to be empty in this case so the application should do a clear on the grid first
 * 
 * @see WXSUtils#insertAll(java.util.Map, com.ibm.websphere.objectgrid.BackingMap)
 * @see WXSUtils#putAll(java.util.Map, com.ibm.websphere.objectgrid.BackingMap)
 */
public class InsertAgent<K,V> implements ReduceGridAgent, Externalizable 
{
	static Logger logger = Logger.getLogger(InsertAgent.class.getName());
	/**
	 * 
	 */
	private static final long serialVersionUID = 6568906743945108310L;
	
	public java.util.Map<K,V> batch;
	public boolean isWriteThrough = true;
	
	/**
	 * For a preload especially with a Loader plugged in then we don't want to do a get
	 */
	public boolean doGet;

	public Object reduce(Session sess, ObjectMap map) 
	{
		return null;
	}

	public Object reduce(Session sess, ObjectMap map, Collection arg2) {
		AgentMBeanImpl agent = WXSUtils.getAgentMBeanManager().getBean(sess.getObjectGrid().getName(), this.getClass().getName());
		long startNS = System.nanoTime();
		try
		{
			Session s = sess.getObjectGrid().getSession();
			ObjectMap m = s.getMap(map.getName());
			if(!isWriteThrough)
				s.beginNoWriteThrough();
			else
				s.begin();
			ArrayList keys = new ArrayList(batch.keySet());
			
			// BN V2.3.1 If write through is disabled DONT DO A GET
			if(doGet && isWriteThrough)
			{
				// sort keys to get U locks in the same order each time
				// to avoid deadlock.
				// would be nice if WXS had a getAllForUpdate method 
				// when a Loader is plugged in.
				
				TreeSet sortedKeys = new TreeSet(keys);
				for(Object k : sortedKeys)
					m.getForUpdate(k);
			}
			// then do a put. Just a put won't work as it will treat
			// all entries as inserts.
			m.putAll(batch);
			s.commit();
			agent.getKeysMetric().logTime(System.nanoTime() - startNS);
			return Boolean.TRUE;
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Exception", e);
			agent.getKeysMetric().logException(e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	/**
	 * Combine the Boolean results of the process calls using
	 * AND
	 */
	public Object reduceResults(Collection arg0) 
	{
		boolean rc = true;
		for(Object o : arg0)
		{
			if(o instanceof EntryErrorValue)
			{
				EntryErrorValue ev = (EntryErrorValue)o;
				return o;
			}
			if(o instanceof Boolean)
			{
				Boolean b = (Boolean)o;
				rc = rc && b;
			}
			if(!rc) break;
		}
		return rc;
	}
	
	public InsertAgent()
	{
 	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException 
	{
		ClassSerializer serializer = WXSUtils.getSerializer();
		doGet = in.readBoolean();
		isWriteThrough = in.readBoolean();
		batch = serializer.readMap(in);
	}

	public void writeExternal(ObjectOutput out) throws IOException 
	{
		ClassSerializer serializer = WXSUtils.getSerializer();
		out.writeBoolean(doGet);
		out.writeBoolean(isWriteThrough);
		serializer.writeMap(out, batch);
	}
}
