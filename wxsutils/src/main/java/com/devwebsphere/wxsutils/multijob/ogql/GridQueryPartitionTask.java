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
package com.devwebsphere.wxsutils.multijob.ogql;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import com.devwebsphere.wxsutils.multijob.SinglePartTask;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.query.ObjectQuery;

/**
 * This is used to execute a query on a partition. It runs a query and keeps all records from
 * offset to offset + limit - 1. Every time it is called within a single partition, it returns
 * the next logical block of records. It's possible for records to slip through without being
 * returned if the set of records being queried is changing all the time. If this agent returns
 * fewer than limit entries then it's assumed all the records for a partition have been returned
 * at that point.
 * @author bnewport
 *
 */
public class GridQueryPartitionTask implements SinglePartTask<ArrayList<Object>, ArrayList<Object>> 
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

	public ArrayList<Object> process(Session sess) 
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
		
		Iterator<Object> rc = q.getResultIterator();
		ArrayList<Object> list = new ArrayList<Object>();
		while(rc.hasNext())
		{
			list.add(rc.next());
		}
		return list;
	}

	public boolean isResultEmpty(ArrayList<Object> result) {
		return result.isEmpty();
	}
	
}
