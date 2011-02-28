package com.devwebsphere.rediswxs.test;

import java.util.List;

import com.devwebsphere.rediswxs.R;


public class BigListTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		R.initialize();

		long start = System.currentTimeMillis();
		R.str_long.ltrim("biglist", 0);
		int initialSize = R.str_long.llen("biglist");
		List<Long> value = R.str_long.lrange("biglist", 0, initialSize);
		System.out.println("llen is " + initialSize + " list size was " + value.size());
		for(int i = 0; i < 100000; ++i)
		{
			R.str_long.lpush("biglist", new Long(i));
			if(i % 100 == 0)
			{
				long now = System.currentTimeMillis();
				System.out.println("+100 to " + (i+initialSize) + " took another " + (now - start));
				start = now;
			}
		}
	}
}
