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

package com.devwebsphere.rediswxs.test;

import com.devwebsphere.rediswxs.RedisClient;

public class TestMapStuff {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		String s = RedisClient.getMapName(String.class, Long.class);
		System.out.println(s);
	}
}
