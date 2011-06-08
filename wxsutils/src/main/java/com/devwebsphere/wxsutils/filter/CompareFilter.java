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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * This is a base class for Filters comparing an attribute with a scalar
 * @author bnewport
 *
 */
public abstract class CompareFilter extends Filter 
{
	ValuePath v;
	Object o;
	
	public CompareFilter() {}
	
	public CompareFilter(ValuePath v, Object o)
	{
		this.v = v;
		this.o = o;
	}

	protected String createString(String op)
	{
		return v.toString() + " " + op + " " + o.toString();
	}
	public void readExternal(ObjectInput in) throws IOException,
	ClassNotFoundException 
	{
		super.readExternal(in);
		v = (ValuePath)serializer.readNullableObject(in);
		o = in.readObject();
	}
	
	public void writeExternal(ObjectOutput out) throws IOException 
	{
		super.writeExternal(out);
		serializer.writeNullableObject(out, v);
		out.writeObject(o);
	}
}
