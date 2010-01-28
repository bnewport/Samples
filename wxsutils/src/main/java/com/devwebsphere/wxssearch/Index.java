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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxssearch.jmx.TextIndexMBeanImpl;
import com.devwebsphere.wxsutils.WXSUtils;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.AgentManager;

/**
 * Each index uses 4 dynamic maps named after the index.
 *
 * @param <RK> The actual key of the entities being indexed.
 */
public abstract class Index<C, RK> 
{

	static String DYN_INDEX_MAP_SUFFIX = "Index";
	static String DYN_BAD_SYMBOL_MAP_SUFFIX = "BadSymbol";
	static String DYN_ATTRIBUTES_MAP_SUFFIX = "Attributes";
	static String DYN_COUNTER_MAP_SUFFIX = "Counter";
	
	static Logger logger = Logger.getLogger(Index.class.getName());
	IndexManager<C, RK> manager;
	
	String indexName;
	String attributeMapName;
	String attributeIndexMapName;
	String indexMapName;
	String badSymbolMapName;
	
	int maxMatches;
	
	TextIndexMBeanImpl mbean;

	/**
	 * Creates an index wrapper which only tracks entries with
	 * less than maxMatches duplicates.
	 * @param im
	 * @param indexName
	 * @param maxMatches
	 */
	Index(IndexManager<C,RK> im, String indexName, int maxMatches)
	{
		this.maxMatches = maxMatches;
		this.manager = im;
		this.indexName = indexName;
        attributeMapName = indexName + "_" + DYN_ATTRIBUTES_MAP_SUFFIX;
        attributeIndexMapName = indexName + "_" + DYN_INDEX_MAP_SUFFIX;
		indexMapName = indexName + "_" + DYN_INDEX_MAP_SUFFIX;
		badSymbolMapName = indexName + "_" + DYN_BAD_SYMBOL_MAP_SUFFIX;
		
		mbean = WXSUtils.getIndexMBeanManager().getBean(indexName);
		
		try
		{
			// make sure the maps based on the index name are dynamically created
			// a Dynamic Map won't exist unless a client calls Session#getMap using
			// the dynamic map name.
			Session s = manager.utils.getObjectGrid().getSession();
			ObjectMap m = s.getMap(attributeMapName);
			m = s.getMap(attributeIndexMapName);
			m = s.getMap(indexMapName);
			m = s.getMap(badSymbolMapName);
		}
		catch(Exception e)
		{
			logger.log(Level.SEVERE, "Cannot create dynamic maps for index", e);
			throw new ObjectGridRuntimeException(e);
		}
	}
	
	/**
	 * This adds index entries. It takes the key for the actual record and the
	 * value of the attribute for the record. The index can later be used to retrieve
	 * the keys for all entries with an attribute containing a symbol
	 * @param entries
	 */
    public void insert(Map<RK, String> entries)
    {
    	long start = System.nanoTime();
    	try
    	{
	    	ObjectGrid grid = manager.utils.getObjectGrid();
	
	        Session sess = grid.getSession();
	
	        int size = entries.size();
	        long longKey = getNameCounterRange(sess, size);
	
	        HashMap<Long, RK> allLongNames = new HashMap<Long, RK>();
	        HashMap<String, Set<Long>> indexMap = new HashMap<String, Set<Long>>();

	        for(Map.Entry<RK, String> e : entries.entrySet())
	        {
	            allLongNames.put(longKey, e.getKey());
	            Set<String> symbols = generate(e.getValue());
	
	            for (String a : symbols)
	            {
	                Set<Long> list = indexMap.get(a);
	                if (list == null)
	                    list = new HashSet<Long>();
	                list.add(longKey);
	                indexMap.put(a, list);
	            }
	            longKey++;
	        }
	        BackingMap bmap = grid.getMap(attributeMapName);
	        manager.utils.putAll(allLongNames, bmap);
	
	        Set<String> allSymbols = indexMap.keySet();
	        Map<String, IndexEntryUpdateAgent> batchAgents = new HashMap<String, IndexEntryUpdateAgent>();
	        for (String symbol : allSymbols)
	        {
	            Set<Long> keys = indexMap.get(symbol);
	            IndexEntryUpdateAgent agent = new IndexEntryUpdateAgent();
	            agent.maxMatches = maxMatches;
	            agent.nameKeys = new long[keys.size()];
	            agent.gridName = manager.utils.getObjectGrid().getName();
	            int idx = 0;
	            for (Long k : keys)
	            {
	                agent.nameKeys[idx++] = k;
	            }
	            agent.isAddOperation = true;
	            agent.indexName = indexName;
	            batchAgents.put(symbol, agent);
	        }
	        manager.utils.callMapAgentAll(batchAgents, grid.getMap(attributeIndexMapName));
	        mbean.getInsertMetrics().logTime(System.nanoTime() - start);
    	}
    	catch(Exception e)
    	{
    		mbean.getInsertMetrics().logException(e);
    		logger.log(Level.SEVERE, "Exception inserting index entries", e);
    		throw new ObjectGridRuntimeException(e);
    	}
    }
    
    /**
     * This retrieves the list of keys for all entries with an attribute
     * containing the symbol anywhere.
     * @param symbol
     * @return
     */
    public Collection<RK> contains(String symbol)
	{
    	long start = System.nanoTime();
    	try
    	{
			ObjectGrid grid = manager.utils.getObjectGrid();
			Session sess = grid.getSession();
			ObjectMap indexMap = sess.getMap(indexMapName);
			ObjectMap badSymbolMap = sess.getMap(badSymbolMapName);
			
			Collection<RK> rc = (Collection<RK>)Collections.EMPTY_LIST;
			
			if (!badSymbolMap.containsKey(symbol))
			{
				long[] keys = (long[]) indexMap.get(symbol);
				if (keys != null)
				{
					ArrayList<Long> listKeys = new ArrayList<Long>(keys.length);
					for (long k : keys)
					{
					    listKeys.add(k);
					}
					
					Map<Long, RK> all = manager.utils.getAll(listKeys, grid.getMap(attributeMapName));
					rc = all.values();
				}
			}
			mbean.getContainsMetrics().logTime(System.nanoTime() - start);
			return rc;
    	}
    	catch(ObjectGridException e)
    	{
    		mbean.getContainsMetrics().logException(e);
    		logger.log(Level.SEVERE, "Exception looking up substrings", e);
    		throw new ObjectGridRuntimeException(e);
    	}
	}
    
    /**
     * This removes the index entry for the record with the key key and the attribute value
     * attributeValue
     * @param key
     * @param attributeValue
     */
    public void remove(RK key, String attributeValue)
    {
    	long start = System.nanoTime();
    	try
    	{
	    	ObjectGrid grid = manager.utils.getObjectGrid();
	    	/*
	    	 * 1) Find the entry for attributeValue. If the attributevalue is in BadSymbol map, do nothing. 
	    	 * 2) Retrieve all the matched entries from Names map. 
	    	 *    Find out which long id represents the key ". 
	    	 * 3) Remove the long id from Attribute map.
	    	 * 4) Remove the long id for all the symbol genreated by the attribute name in Index map.  
	    	 */
	
	    	// 1) Find the entry for attributeValue. If the attributevalue is in BadSymbol map, do nothing. 
	    	Session sess = grid.getSession();
	    	ObjectMap badSymbolMap = sess.getMap(badSymbolMapName);
	    	if (!badSymbolMap.containsKey(key))
	    	{
		
		    	// 2) Retrieve all the matched entries from Names map. 
		    	// Find out which long id represents key. 
		
		    	// Get the ObjectMap representing the attribute index map
		    	ObjectMap indexMap = grid.getSession().getMap(indexMapName);
		
		    	long[] keys = (long[]) indexMap.get(attributeValue);
		    	if (keys != null)
		    	{
			    	logger.finest("removeAttribute - total " + keys.length + " matches for attribute value'" + attributeValue
			    			+ "'.");
			
			    	List<Long> listKeys = new ArrayList<Long>(keys.length);
			    	for (long k : keys)
			    	{
			    		listKeys.add(k);
			    	}
			
			    	ObjectMap attributeMap = grid.getSession().getMap(indexMapName);
			    	Map<Long, RK> all = manager.utils.getAll(listKeys, grid.getMap(attributeMapName));
			
			    	long id = 0;
			    	boolean found = false;
			    	for (Map.Entry<Long, RK> entry : all.entrySet())
			    	{
			    		if (entry.getValue().equals(key))
			    		{
			    			found = true;
			    			id = entry.getKey();
			    			break;
			    		}
			    	}
			
			    	if (found)
			    	{
				    	logger.finest("removeAttribute - Found the long id for attribute value'" + attributeValue + "': " + id);
				    	// 3) Remove the long id from Attribute map.
				    	attributeMap.remove(new Long(id));
				
				    	// 4) Remove the long id for all the symbol genreated by the attribute name in Index map.  
				
				    	Set<String> symbols = generate(attributeValue);
				
				    	ObjectMap names = sess.getMap(attributeMapName);
				    	names.remove(key);
				
				    	AgentManager am = sess.getMap(indexMapName).getAgentManager();
				
				    	for (String symbol : symbols)
				    	{
				    		IndexEntryUpdateAgent agent = new IndexEntryUpdateAgent();
				    		agent.maxMatches = maxMatches;
				    		agent.nameKeys = new long[1];
				    		agent.nameKeys[0] = id;
				    		agent.isAddOperation = false;
				    		agent.indexName = indexName;
				    		agent.gridName = manager.utils.getObjectGrid().getName();
				    		am.callMapAgent(agent, Collections.singletonList(symbol));
				    	}
			    	}
		    	}
	    	}
	    	mbean.getRemoveMetrics().logTime(System.nanoTime() - start);
    	}
    	catch(ObjectGridException e)
    	{
    		mbean.getRemoveMetrics().logException(e);
    		logger.log(Level.SEVERE, "Exception removing entries", e);
    		throw new ObjectGridRuntimeException(e);
    	}
    }

    /**
     * This is an internal method for allocating contiguous
     * ranges of longs.
     * @param sess
     * @param numKeys
     * @return
     * @throws ObjectGridException
     */
    long getNameCounterRange(Session sess, long numKeys) throws ObjectGridException
    {
        String counterKey = indexName + "_counter";

        sess.begin();
        ObjectMap counter = sess.getMap(DYN_COUNTER_MAP_SUFFIX);
        Long key = (Long) counter.getForUpdate(counterKey);
        Long next = null;
        if (key == null)
        {
            key = new Long(Long.MIN_VALUE);
            next = new Long(Long.MIN_VALUE + numKeys);
            counter.insert(counterKey, next);
        } else
        {
            next = new Long(key.longValue() + numKeys);
            counter.update(counterKey, next);
        }
        sess.commit();
        return key;
    }

    abstract Set<String> generate(String str);
}
