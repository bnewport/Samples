package com.devwebsphere.wxssearch;

import java.util.List;

public class SearchResult<V> 
{
	List<V> result;
	boolean tooManyMatches;

	public SearchResult()
	{
		tooManyMatches = true;
	}
	
	public SearchResult(List<V> a)
	{
		result = a;
		tooManyMatches = false;
	}
	
	public List<V> getResults()
	{
		return result;
	}
	
	public boolean isTooManyMatches()
	{
		return tooManyMatches;
	}
}
