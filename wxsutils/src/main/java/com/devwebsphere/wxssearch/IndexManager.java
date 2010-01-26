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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.WXSUtils;

/**
 * This is a client class for managing full search indexes. The application
 * should make one Index for each attribute that needs to be searchable.
 *
 */
public class IndexManager
{
    static Logger logger = Logger.getLogger(IndexManager.class.getName());

    WXSUtils utils;
    ConcurrentHashMap<String, Index<?>> indices = new ConcurrentHashMap<String, Index<?>>();
    
    /**
     * Create a WXSUtils and provide this. This is used for all bulk
     * operations.
     * @param utils
     */
    public IndexManager(WXSUtils utils)
    {
    	this.utils = utils;
    }

    /**
     * This returns and initializes the index for a specific attribute. The index
     * name should be a combination of the object name and attribute name. The object
     * map being indexed should have keys of type RK.
     * @param <RK> The type of the key from the ObjectMap being indexed
     * @param indexName The name of this index.
     * @return An index object
     */
    public <RK> Index<RK> getIndex(String indexName)
    {
    	Index<RK> rc = (Index<RK>)indices.get(indexName);
    	if(rc == null)
    	{
    		rc = new Index<RK>(this, indexName);
    		indices.put(indexName, rc);
    	}
    	return rc;
    }

    /**
     * This method searches across multiple indexes and combines the answers. For example:
     * The Map would look like [("FirstName", "Bil"), ("Surname", "Newpo")].
     * @param entries Each entry has the name of the index and the value to search for in that index
     * @param isAND TRUE if results are to be ANDed otherwise the results are ORed
     * @return The list of keys for the matching entries
     */
    public <RK> Collection<RK> searchMultipleIndexes(Map<String, String> entries, boolean isAND)
    {
    	Set<RK> result = null;
    	for(Map.Entry<String, String> e : entries.entrySet())
    	{
    		Index<RK> index = getIndex(e.getKey());
    		Collection<RK> r = index.contains(e.getValue());
    		if(result == null)
    			result = new HashSet<RK>(r);
    		else if(isAND)
    		{
    			LinkedList<RK> removeList = new LinkedList<RK>();
    			for(RK k : r)
    			{
    				if(!result.contains(k))
    					removeList.add(k);
    			}
    			result.removeAll(removeList);
    		}
    		else
    			result.addAll(r);
    	}
    	return (result != null) ? result : (Collection<RK>)(Collections.EMPTY_LIST);
    }
}
