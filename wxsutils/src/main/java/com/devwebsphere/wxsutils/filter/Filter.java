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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.filter.path.PojoFieldPath;
import com.devwebsphere.wxsutils.filter.path.PojoPropertyPath;
import com.devwebsphere.wxsutils.utils.ClassSerializer;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;

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
			ClassNotFoundException {
		// TODO Auto-generated method stub
		
	}

	public void writeExternal(ObjectOutput arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	static public void writeValuePath(ObjectOutput out, ValuePath v)
		throws IOException
	{
		byte b = 0;
		if(v instanceof PojoFieldPath)
			b = 1;
		if(v instanceof PojoPropertyPath)
			b = 2;
		out.writeByte(b);
		if(b == 0)
			out.writeObject(v);
		else
		{
			Externalizable e = (Externalizable)v;
			e.writeExternal(out);
		}
	}
	
	static public ValuePath readValuePath(ObjectInput in)
		throws IOException, ClassNotFoundException
	{
		byte b = in.readByte();
		switch(b)
		{
		case 0:
			return (ValuePath)in.readObject();
		case 1:
		{
			PojoFieldPath p = new PojoFieldPath();
			p.readExternal(in);
			return p;
		}
		case 2:
		{
			PojoPropertyPath p = new PojoPropertyPath();
			p.readExternal(in);
			return p;
		}
		default:
			logger.log(Level.SEVERE, "Unknown value path type: " + b);
			throw new ObjectGridRuntimeException("Unknown value path type");
		}
	}
	
	static Map<Class<? extends Filter>, Byte> filterList = new HashMap<Class<? extends Filter>, Byte>();
	static Map<Byte, Class<? extends Filter>> idToFilterMap = new HashMap<Byte, Class<? extends Filter>>();
	
	static void storeFilterIdPair(Class<? extends Filter> f, Byte b)
	{
		filterList.put(f, b);
		idToFilterMap.put(b, f);
	}
	
	static ClassSerializer serializer = new FilterClassSerializer();
	
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
