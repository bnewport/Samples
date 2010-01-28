package com.devwebsphere.wxssearch;

import java.util.HashSet;
import java.util.Set;

public class SubstringIndexImpl<C, RK> extends Index<C, RK> 
{
	SubstringIndexImpl(IndexManager<C,RK> im, String indexName, int maxMatches) {
		super(im, indexName, maxMatches);
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
