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
package com.devwebsphere.wxsutils;

import java.nio.charset.Charset;

/**
 * This is a helper class to work with UTF8 strings to save
 * space when there are lots of strings in memory
 * @author bnewport
 *
 */
public class UTF8StringContainer
{
	static Charset utf8 = Charset.forName("UTF-8");

	/**
	 * This converts a String to its UTF8 byte form.
	 * @param s
	 * @return
	 */
	public static byte[] fromString(String s)
	{
		byte[] value = s.getBytes(utf8);
		return value;
	}

	/**
	 * This converts a UTF8 byte to a String
	 * @param b
	 * @return
	 */
	public static String toString(byte[] b)
	{
		String s = new String(b, utf8);
		return s;
	}
}
