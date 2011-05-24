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
package com.devwebsphere.wxsutils;

import java.util.ArrayList;

import org.junit.Test;


public class TestUTF8Strings 
{
	@Test
	public void testSomeStrings()
	{
		
		ArrayList<String> names = new ArrayList<String>();
		for(int i = 0; i < 100000; ++i)
		{
			String name = "Billy Newport" + Integer.toString(i);
			names.add(name + name);
		}

		ArrayList<byte[]> bnames = new ArrayList<byte[]>();
		for(int i = 0; i < names.size(); ++i)
		{
			bnames.add(UTF8StringContainer.fromString(names.get(i)));
		}
		System.gc();
		byte[] b = UTF8StringContainer.fromString("BBB");
		System.out.println(b.length);
	}
}
