//
//This sample program is provided AS IS and may be used, executed, copied and
//modified without royalty payment by customer (a) for its own instruction and
//study, (b) in order to develop applications designed to run with an IBM
//WebSphere product, either for customer's own internal use or for redistribution
//by customer, as part of such an application, in customer's own products. "
//
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2009
//All Rights Reserved * Licensed Materials - Property of IBM
//
package com.devwebsphere.rediswxs;


/**
 * This is the main class. Call the initialize method with the catalog endpoints
 * or NULL at the start of your application and then use the X_X variables to
 * interact with the store or c_X_X for the near cached equivalent
 *
 */
public class R
{
	static public IRedisLowLevel r;
	
	/**
	 * This allows String/String entries to be worked with
	 */
	static public IRedis<String, String> str_str;
	
	/**
	 * This allows String/Long entries to be worked with
	 */
	static public IRedis<String, Long> str_long;
	/**
	 * This allows Long/String entries to be worked with
	 */
	static public IRedis<Long, String> long_str;
	/**
	 * This allows Long/Long entries to be worked with
	 */
	static public IRedis<Long, Long> long_long;

	static public IRedis<String, String> c_str_str;
	static public IRedis<String, Long> c_str_long;
	static public IRedis<Long, String> c_long_str;
	static public IRedis<Long, Long> c_long_long;

	/**
	 * This MUST be called when your application starts to initialize the client
	 * If the cep is null then a grid is started within the client JVM to host the
	 * grid servers locally. This is useful for debugging
	 * @param cep The catalog service end points (h:p,h:p,h:p)
	 */
	static public void initialize(String cep)
	{
		r = new RedisClient(cep);
		// create non caches classes
		str_str = new RedisMapAdapter<String, String>(false, String.class, String.class, r);
		str_long = new RedisMapAdapter<String, Long>(false, String.class, Long.class, r);
		long_str = new RedisMapAdapter<Long, String>(false, Long.class, String.class, r);
		long_long = new RedisMapAdapter<Long, Long>(false, Long.class, Long.class, r);
		
		// create the local caches versions
		c_str_str = new RedisMapAdapter<String, String>(true, String.class, String.class, r);
		c_str_long = new RedisMapAdapter<String, Long>(true, String.class, Long.class, r);
		c_long_str = new RedisMapAdapter<Long, String>(true, Long.class, String.class, r);
		c_long_long = new RedisMapAdapter<Long, Long>(true, Long.class, Long.class, r);
	}
}
