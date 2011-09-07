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
import java.util.HashSet;
import java.util.Set;

import com.devwebsphere.wxssearch.type.ExactIndex;

public class ExactIndexImpl<C, RK extends Serializable> extends Index<C, RK> {

	ExactIndexImpl(IndexManager<C, RK> im, String indexName, ExactIndex e) {
		super(im, indexName, e.maxMatches());
	}

	@Override
	public Set<String> generate(String str) {
		Set<String> rc = new HashSet<String>();
		rc.add(str);
		return rc;
	}

}
