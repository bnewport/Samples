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

import com.devwebsphere.wxssearch.type.SubstringIndex;

public class SubstringIndexImpl<C, RK extends Serializable> extends Index<C, RK> 
{
	SubstringIndexImpl(IndexManager<C,RK> im, String indexName, SubstringIndex s) {
		super(im, indexName, s.maxMatches());
	}

	static public Set<String> sgenerate(String str)
	{
        HashSet<String> rc = new HashSet<String>();

        if (str != null)
        {
            String s = str.toUpperCase();

            for (int i = 0; i < s.length(); ++i)
            {
                for (int j = i; j <= s.length(); ++j)
                {
                    String v = s.substring(i, j);
                    rc.add(v);
                }
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
