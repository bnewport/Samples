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

package com.devwebsphere.wxssearch;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import com.ibm.websphere.objectgrid.ClientClusterContext;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridManagerFactory;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.AgentManager;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;

public class IndexEntryUpdateAgent implements MapGridAgent, Runnable {
	long[] nameKeys;
	boolean isAddOperation;
    String indexName;
    String gridName;

    transient static AtomicReference<ObjectGrid> _clientGrid = new AtomicReference<ObjectGrid>();
    
    /**
     * This is the maximum number of entries that we track for a given symbol. If
     * its more than this then the symbol is added to the bad symbol map and
     * no entries are tracked for it.
     */
	int maxMatches;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3889707424317468360L;

	static ObjectGrid getClientGrid(String gridName)
		throws ObjectGridException
	{
		ObjectGrid rc = _clientGrid.get();
		if(rc == null)
		{
			ClientClusterContext cc = ObjectGridManagerFactory.getObjectGridManager().connect(null, null);
			rc = ObjectGridManagerFactory.getObjectGridManager().getObjectGrid(cc, gridName);
			_clientGrid.compareAndSet(null, rc);
			rc = _clientGrid.get();
		}
		return rc;
	}

    /** 
     * Add an symbol/indexKey with it's associated name key longs. 
     * If the  keys length is great than MAXMATCHES, then we remove it from the index map,
     * and insert the symbo/indexKey into the bad map.
     * Otherwise, we iterate the current name longs see if the new name is included in it. 
     * If not, we store it in delta; otherwise it is ignored.
     * Finally, we combine the delta with the current name key longs.
     */
	void add(Session sess, ObjectMap indexMap, Object indexKey, String indexName)
		throws ObjectGridException
	{
		long[] keys = (long[])indexMap.getForUpdate(indexKey);
		if(keys != null && keys.length > maxMatches)
		{
			// too many records to be useful for this symbol
			// remove and add key to not useful symbol map
			Session clientSession = getClientGrid(gridName).getSession();
			ObjectMap badMap = clientSession.getMap(indexName + "_" + Index.DYN_BAD_SYMBOL_MAP_SUFFIX);
			// remote tx
			clientSession.begin();
			if(badMap.getForUpdate(indexKey) == null)
			{
				badMap.insert(indexKey, Boolean.TRUE);
			}
			clientSession.commit();
			// end remove tx
			// local tx again
			indexMap.remove(indexKey);
		}
		else
		{
			// keep list of keys not already in the list
			LinkedList<Long> delta = new LinkedList<Long>();
			for(long nameKey : nameKeys)
			{
				keys = (long[])indexMap.getForUpdate(indexKey);
				boolean found = false;
				for(int i = 0; !found && keys != null && i < keys.length; ++i)
				{
					if(keys[i] == nameKey)
						found = true;
				}
				if(!found)
				{
					delta.add(nameKey);
				}
			}
			// add non dup keys to end of list
			long[] newKeys = null;
			if(keys == null)
			{
				newKeys = new long[delta.size()];
				int c = 0;
				for(Long l : delta)
					newKeys[c++] = l;
				indexMap.insert(indexKey, newKeys);
			}
			else
			{
				newKeys = new long[keys.length + delta.size()];
				System.arraycopy(keys, 0, newKeys, 0, keys.length);
				int c = keys.length;
				for(Long l : delta)
					newKeys[c++] = l;
				indexMap.update(indexKey, newKeys);
			}
		}
	}
	
	void remove(Session sess, ObjectMap indexMap, Object indexKey)
		throws ObjectGridException
	{
		long[] keys = (long[])indexMap.getForUpdate(indexKey);
		for(long nameKey: nameKeys)
		{
			boolean found = false;
			int pos;
			for(pos = 0; !found && keys != null && pos < keys.length; ++pos)
			{
				if(keys[pos] == nameKey)
					found = true;
			}
			if(found)
			{
				// if only one entry, delete whole key and stop
				if(keys.length == 1)
				{
					indexMap.remove(indexKey);
					break;
				}
				else
				{
					// copy array excluding the key
					long[] newKeys = new long[keys.length - 1];
					int c = 0;
					for(int i =0; i < keys.length; ++i)
					{
						if(i != pos)
							newKeys[c++] = keys[i];
						
					}
					// mark as updated
					indexMap.update(indexKey, newKeys);
					keys = newKeys;
				}
			}
		}
	}
	
	public Object process(Session sess, ObjectMap indexMap, Object indexKey) 
	{
		try
		{
			if(isAddOperation)
			{
				add(sess, indexMap, indexKey, indexName);
			}
			else // is a delete operation
			{
				remove(sess, indexMap, indexKey);
			}
			return Boolean.TRUE;
		}
		catch(ObjectGridException e)
		{
			throw new ObjectGridRuntimeException(e);
		}
	}

	public Map processAllEntries(Session arg0, ObjectMap arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	transient AgentManager am;
	transient String agentKey;
	
	public void schedule(ScheduledExecutorService pool, AgentManager am, String key)
	{
		this.am = am;
		this.agentKey = key;
		pool.execute(this);
	}
	
	public void run() 
	{
		am.callMapAgent(this, Collections.singleton(agentKey));
	}

}
