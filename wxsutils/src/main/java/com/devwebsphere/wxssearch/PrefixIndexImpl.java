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
