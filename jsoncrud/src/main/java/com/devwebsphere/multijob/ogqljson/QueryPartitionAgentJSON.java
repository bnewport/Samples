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
package com.devwebsphere.multijob.ogqljson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import com.devwebsphere.wxsutils.multijob.SinglePartTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.query.ObjectQuery;

public class QueryPartitionAgentJSON implements SinglePartTask<ArrayList<String>, ArrayList<String>> 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2643446835456688514L;
	
	Map<String, Serializable> queryParameters;
	Map<String, Serializable> queryHints;
	int offset;
	int limit;
	String queryString;

	public ArrayList<String> process(Session sess) 
	{
		ObjectQuery q = sess.createObjectQuery(queryString);
		q.setFirstResult(offset);
		q.setMaxResults(limit);
		
		if(queryParameters != null)
			for(Map.Entry<String, Serializable> e : queryParameters.entrySet())
			{
				q.setParameter(e.getKey(), e.getValue());
			}
		if(queryHints != null)
			for(Map.Entry<String, Serializable> e : queryHints.entrySet())
			{
				q.setHint(e.getKey(), e.getValue());
			}
		
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		
		Iterator<Object> rc = q.getResultIterator();
		ArrayList<String> list = new ArrayList<String>();
		Class returnClass = null;
		while(rc.hasNext())
		{
			Object v = rc.next();
			if(returnClass == null)
			{
				returnClass = v.getClass();
			}
			list.add(gson.toJson(v, returnClass));
		}
		return list;
	}

	boolean lastExtractWasFull = false;
	
	public ArrayList<String> extractResult(ArrayList<String> rawRC)
	{
		ArrayList<String> rc = rawRC;
		// check if last block was < limit records and if it was then
		// assume there is no more data in this partition
		lastExtractWasFull = (rc.size() == limit);
		return rc;
	}
}
