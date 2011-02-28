package com.devwebsphere.rediswxs.data.test;

import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.devwebsphere.rediswxs.R;


public class TestGrid 
{
	@BeforeClass
	public static void setupGrid()
	{
		R.initialize();
	}

	@Test
	public void testKVOperations()
	{
		// check set works
		String testValue = Long.toString(System.nanoTime());
		R.str_str.set("TestKey", testValue);
		String s = R.str_str.get("TestKey");
		Assert.assertEquals(testValue, s);
		// check update works
		testValue = Long.toString(System.nanoTime());
		R.str_str.set("TestKey", testValue);
		s = R.str_str.get("TestKey");
		Assert.assertEquals(testValue, s);
		// check remove works
		Assert.assertTrue(R.str_str.remove("TestKey"));
		// check removing something unknown works
		Assert.assertFalse(R.str_str.remove("TestKey"));
		// check looking up something unknown works
		s = R.str_str.get("TestKey");
		Assert.assertNull(s);
		
		R.str_long.set("TestKeySL", new Long(5));
		Long l5 = R.str_long.get("TestKeySL");
		Assert.assertEquals(new Long(5), l5);
		Long l6 = R.str_long.incr("TestKeySL");
		Assert.assertEquals(l6.intValue(), 6);
		Long l8 = R.str_long.incrby("TestKeySL", 2);
		Assert.assertEquals(l8.intValue(), 8);
		Assert.assertTrue(R.str_long.remove("TestKeySL"));
		
	}
	
	@Test
	public void testListOperations()
	{
		String key = "Test5";
		R.str_long.ltrim(key, 0);
		Assert.assertEquals(0, R.str_long.llen(key));
		
		Assert.assertNull(R.str_long.lpop(key));
		Assert.assertNull(R.str_long.rpop(key));

		final int numElements = 10;
		for(int i = 0; i < numElements;++i)
		{
			R.str_long.rpush(key, new Long(i));
			Assert.assertEquals(i+1, R.str_long.llen(key));
		}
		Assert.assertEquals(R.str_long.llen(key), numElements);
		
		List<Long> list = R.str_long.lrange(key, 0, numElements);
		for(int i = 0; i < numElements; ++i)
		{
			Assert.assertEquals(list.get(i).intValue(), i);
		}
		Assert.assertEquals(numElements, list.size());

		for(int i = 0; i < numElements; ++i)
		{
			Long ll = R.str_long.lpop(key);
			Assert.assertNotNull(ll);
			Assert.assertEquals(ll.intValue(), i);
			Assert.assertEquals(R.str_long.llen(key), numElements - i - 1);
		}
		Assert.assertEquals(R.str_long.llen(key), 0);
		
		for(int i = 0; i < numElements;++i)
		{
			R.str_long.lpush(key, new Long(i));
			Assert.assertEquals(i+1, R.str_long.llen(key));
		}
		list = R.str_long.lrange(key, 0, numElements);
		Iterator<Long> iter = list.iterator();
		for(int i = numElements - 1; i >= 0; --i)
		{
			Assert.assertEquals(iter.next().intValue(), i);
		}
		for(int i = 0; i < numElements; ++i)
		{
			Long ll = R.str_long.rpop(key);
			Assert.assertNotNull(ll);
			Assert.assertEquals(ll.intValue(), i);
		}
		Assert.assertEquals(0, R.str_long.llen(key));
	}
	
	@Test
	public void testPartitioning()
	{
		Person p1 = new Person();
		p1.firstName = "Billy";
		p1.surname = "Newport";
		p1.userId = "bnewport";
		p1.password = "password";
		
		R.multiPut("u:billy", p1);
		
		Person copy = R.multiGet("u:billy", Person.class);
		
		Assert.assertEquals(p1.firstName, copy.firstName);
		Assert.assertEquals(p1.surname, copy.surname);
		Assert.assertEquals(p1.userId, copy.userId);
		Assert.assertEquals(p1.password, copy.password);
		
		copy = R.multiGet("u:bobby", Person.class);
		Assert.assertNotNull(copy);
		Assert.assertNull(copy.firstName);
		Assert.assertNull(copy.surname);
		Assert.assertNull(copy.userId);
		Assert.assertNull(copy.password);
	}
	@Test
	public void testSetOperations()
		throws Throwable
	{
		String key = "S" + System.currentTimeMillis();

		for(int i = 0; i < 100; ++i)
		{
			Assert.assertTrue(R.str_long.sadd(key, new Long(i)));
			
			Assert.assertTrue(R.str_long.sismember(key, new Long(i)));
			Assert.assertEquals(i+1, R.str_long.scard(key));
			List<Long> contents = R.str_long.smembers(key);
			Assert.assertEquals(i+1, contents.size());
			for(int j = 0; j <= i; ++j)
			{
				Long jo = new Long(j);
				for(int k = 0; k < contents.size(); ++k)
				{
					if(contents.get(k).equals(jo))
					{
						contents.remove(k);
						break;
					}
				}
			}
			Assert.assertTrue(contents.isEmpty());
		}
		
		for(int i = 0; i < 100; ++i)
		{
			Assert.assertTrue(R.str_long.srem(key, new Long(i)));
			Assert.assertFalse(R.str_long.srem(key, new Long(i)));
		}
	}
	
}
