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
package com.devwebsphere.purequery.loader;

import java.io.Serializable;
import java.util.StringTokenizer;

import com.ibm.websphere.objectgrid.plugins.PartitionableKey;

/**
 * This is a key class which uses the portion of the key
 * in braces {} as the portion for partition
 * routing. This makes collocating key/entrys easier as
 * related entries can use a style like this:
 * 	"{u:123}firstname"
 *  "{u:123}surname"
 *  "{u:123}password"
 * 
 * and so on.
 *
 */
public class ScalarKey implements PartitionableKey, Cloneable, Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8030915502041725254L;
	String key;

	public ScalarKey()
	{
	}
	
	public ScalarKey(String k)
	{
		key = k;
	}
	
	public ScalarKey(String prefix, String field)
	{
		key = "{" + prefix + "}" + field;
	}
	
	/**
	 * This is only called really one on the client
	 * so I don't think it's worth a lot of effort
	 * to cache this or anything. StringTokenizer
	 * is quick and dirty.
	 */
	public Object ibmGetPartition() 
	{
		if(key.startsWith("{"))
		{
			StringTokenizer tok = new StringTokenizer(key, "}");
			return tok.nextToken();
		}
		else
			return key;
	}
	
	public String getKey()
	{
		return key;
	}
	
	public Object clone()
	{
		return this;
	}
	
	public String toString()
	{
		return "SK<"+key+">";
	}
	
	@Override
	public int hashCode() {
		return key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ScalarKey other = (ScalarKey) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

}
