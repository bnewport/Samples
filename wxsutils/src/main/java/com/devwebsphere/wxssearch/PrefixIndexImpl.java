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

import java.util.HashSet;
import java.util.Set;

public class PrefixIndexImpl<C,RK> extends Index<C,RK> {

	PrefixIndexImpl(IndexManager<C,RK> im, String indexName, int maxMatches) {
		super(im, indexName, maxMatches);
		// TODO Auto-generated constructor stub
	}

	static public Set<String> sgenerate(String str)
	{
        HashSet<String> rc = new HashSet<String>();

        if (str != null)
        {
            String s = str.toUpperCase();

            for (int i = 1; i <= s.length(); ++i)
            {
                String v = s.substring(0, i);
                rc.add(v);
            }
        }
        return rc;
	}
	
	@Override
	public Set<String> generate(String str) 
	{
		return sgenerate(str);
	}

}
