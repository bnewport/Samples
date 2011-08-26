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
package com.devwebsphere.wxsutils.filter;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.utils.ClassSerializer;

/**
 * This is an abstract filter. It defines a filter for testing
 * if a specified object passed the filter.
 * @author bnewport
 *
 */
public abstract class Filter implements Externalizable
{
	static Logger logger = Logger.getLogger(Filter.class.getName());
	/**
	 * This checks if the supplied object passes the filter
	 * @param o
	 * @return
	 */
	public abstract boolean filter(Object o);

	public void readExternal(ObjectInput arg0) throws IOException,
			ClassNotFoundException 
	{
	}

	public void writeExternal(ObjectOutput arg0) throws IOException 
	{
	}
	
	/**
	 * Register all the Filter classes on the serializer in the custom serializer.
	 */
	static ClassSerializer serializer = new FilterClassSerializer();

	static public ClassSerializer getSerializer()
	{
		return serializer;
	}
	
	static public void writeFilter(ObjectOutput out, Filter f)
		throws IOException
	{
		serializer.writeObject(out, f);
	}
	
	static public Filter readFilter(ObjectInput in)
		throws IOException, ClassNotFoundException
	{
		return (Filter)serializer.readObject(in);
	}
}
