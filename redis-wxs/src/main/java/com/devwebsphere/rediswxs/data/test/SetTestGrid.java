package com.devwebsphere.rediswxs.data.test;

import java.util.List;

import com.devwebsphere.rediswxs.R;
import com.devwebsphere.rediswxs.RedisClient;


public class SetTestGrid {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		try
		{
			R.initialize(null);
			String key = "Set";

			for(int i = 0; i < 100; ++i)
			{
				if(!R.str_long.sadd(key, new Long(i)))
				{
					System.out.println("Item wasn't added");
				}
				
				if(!R.str_long.sismember(key, new Long(i)))
				{
					System.out.println("Record missing in sismember");
				}
				if(R.str_long.scard(key) != (i + 1))
				{
					System.out.println("Set is the wrong size");
				}
				List<Long> contents = R.str_long.smembers(key);
				if(contents.size() != (i + 1))
					System.out.println("smembers wrong size");
			}
			
			for(int i = 0; i < 100; ++i)
			{
				if(!R.str_long.srem(key, new Long(i)))
				{
					System.out.println("Cannot remove item");
				}
				if(R.str_long.srem(key, new Long(i)))
				{
					System.out.println("Item was not removed");
				}
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		System.exit(0);
	}
}
