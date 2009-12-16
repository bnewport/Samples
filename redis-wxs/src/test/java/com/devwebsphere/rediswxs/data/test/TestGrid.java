package com.devwebsphere.rediswxs.data.test;

import java.util.List;

import com.devwebsphere.rediswxs.R;
import com.devwebsphere.rediswxs.RedisClient;


public class TestGrid {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		try
		{
			R.initialize(null);

			if(false)
			{
				R.str_str.set("TestKey", "Bobby");
				String s = R.str_str.get("TestKey");
				R.str_long.set("TestKeySL", new Long(5));
				Long l5 = R.str_long.get("TestKeySL");
			}
			if(true)
			{
				String key = "Test5";
				for(int i = 0; i < 10;++i)
				{
					R.str_long.rpush(key, new Long(i));
				}
		
				System.out.println("Inserted 10");
				while(true)
				{
					Long v = R.str_long.lpop(key);
					if(v == null)
					{
						System.out.println("Empty");
						break;
					}
					System.out.println("Popped " + v);
					List<Long> list = R.str_long.lrange(key, 0, 100);
					for(Long l : list)
					{
						System.out.println(l.toString());
					}
				}
			}
			{
				String key = "Test6";
				for(int i = 0; i < 10;++i)
				{
					R.str_long.lpush(key, new Long(i));
				}
		
				System.out.println("Inserted 10");
				while(true)
				{
					Long v = R.str_long.rpop(key);
					if(v == null)
					{
						System.out.println("Empty");
						break;
					}
					System.out.println("Popped " + v);
					List<Long> list = R.str_long.lrange(key, 0, 100);
					for(Long l : list)
					{
						System.out.println(l.toString());
					}
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
