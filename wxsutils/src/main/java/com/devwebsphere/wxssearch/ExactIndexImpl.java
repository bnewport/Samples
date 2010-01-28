package com.devwebsphere.wxssearch;

import java.util.HashSet;
import java.util.Set;

public class ExactIndexImpl<C,RK> extends Index<C,RK> {

	ExactIndexImpl(IndexManager<C,RK> im, String indexName, int maxMatches) {
		super(im, indexName, maxMatches);
	}

	@Override
	public Set<String> generate(String str) 
	{
		Set<String> rc = new HashSet<String>();
		rc.add(str);
		return rc;
	}

}
