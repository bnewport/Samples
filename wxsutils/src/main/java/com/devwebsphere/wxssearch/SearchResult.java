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

import java.io.Serializable;
import java.util.List;

/**
 * This is returned by the Index search routines. It is a wrapper for either the results
 * or a flag indicating too many entries matched the search.
 *
 * @param <V>
 */
public class SearchResult<V> implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8167726863844513209L;
	List<V> result;
	boolean tooManyMatches;

	/**
	 * Use this constructor for a SearchResult for too many matches
	 */
	public SearchResult()
	{
		tooManyMatches = true;
	}

	/**
	 * Use this constructor for a SearchResult for a set of matched records.
	 * @param a
	 */
	public SearchResult(List<V> a)
	{
		result = a;
		tooManyMatches = false;
	}

	/**
	 * Returns the list of results or null
	 * @return
	 */
	public List<V> getResults()
	{
		return result;
	}

	/**
	 * Returns true if there were too many matches
	 * @return
	 */
	public boolean isTooManyMatches()
	{
		return tooManyMatches;
	}
	
	public String toString()
	{
		if(isTooManyMatches())
			return "SR<Too many results>";
		else
			return "SR<" + result + ">";
	}
}
