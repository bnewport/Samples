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
import java.util.Map;

import com.devwebsphere.wxsutils.multijob.JobExecutor;
import com.devwebsphere.wxsutils.multijob.MultipartTask;
import com.devwebsphere.wxsutils.multijob.SinglePartTask;
import com.ibm.websphere.objectgrid.ObjectGrid;

/**
 * This implements a MultiTaskPart so users can run a query on all
 * data in the grid
 * @author bnewport
 *
 */
public class QueryAllPartitionsJSON implements MultipartTask<ArrayList<String>, ArrayList<String>>
{
	Map<String, Serializable> queryParameters;
	Map<String, Serializable> queryHints;
	int limit;
	String queryString;
	JobExecutor<ArrayList<String>, ArrayList<String>> je;

	/**
	 * Construct an instance. It's important that any query parameters or hints
	 * be specified using {@link #setQueryHints(Map)} and {@link #setQueryParameters(Map)}
	 * @param ogclient The objectgrid client to use
	 * @param queryString The OGQL query
	 * @param limit The maximum number of results to return at once
	 */
	public QueryAllPartitionsJSON(ObjectGrid ogclient, String queryString, int limit)
	{
		this.queryString = queryString;
		this.limit = limit;
		je = new JobExecutor<ArrayList<String>, ArrayList<String>>(ogclient, this);
	}
	
	/**
	 * This will return a new SingleTaskPart job if there are more blocks within the current partition
	 * otherwise it should return null to indicate all data can been consumed for the current
	 * partition
	 */
	public SinglePartTask<ArrayList<String>, ArrayList<String>> createTaskForPartition(SinglePartTask<ArrayList<String>, ArrayList<String>> previousTask)
	{
		// prevtask is null when called for first time for a partition
		if(previousTask == null)
		{
			QueryPartitionAgentJSON qpa = new QueryPartitionAgentJSON();
			qpa.limit = limit;
			qpa.queryString = queryString;
			qpa.queryHints = queryHints;
			qpa.queryParameters = queryParameters;
			qpa.offset = 0;
			return qpa;
		}
		else
		{
			// if last SinglePartTask wasn't the last one then just get the next
			// block of limit records
			QueryPartitionAgentJSON qpa = (QueryPartitionAgentJSON)previousTask;
			if(qpa.lastExtractWasFull)
			{
				qpa.offset += qpa.limit;
				return qpa;
			}
			else
				// otherwise we got all the records in this partition
				return null;
		}
	}

	/**
	 * This must be called before getNextResult is called if required.
	 * @param queryParameters
	 */
	public final void setQueryParameters(Map<String, Serializable> queryParameters) {
		this.queryParameters = queryParameters;
	}

	/**
	 * This must be called before getNextResult is called if required.
	 * @param queryHints
	 */
	public final void setQueryHints(Map<String, Serializable> queryHints) {
		this.queryHints = queryHints;
	}
	
	/**
	 * This is just a delegate to the JobExecutor. This can return arrays of zero length. Only
	 * a null return indicates the end of the operation.
	 * @return
	 */
	public ArrayList<String> getNextResult()
	{
		return je.getNextResult();
	}
}
