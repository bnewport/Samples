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
 * This is a base class used for binary operators
 * @author bnewport
 *
 */
public abstract class BinaryLogicalFilter extends Filter 
{
	Filter[] flist;

	public BinaryLogicalFilter() {}
	
	public BinaryLogicalFilter(Filter... l)
	{
		flist = l;
	}
	
	protected String createString(String op)
	{
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < flist.length;++i)
		{
			sb.append(flist[i].toString());
			if(i != flist.length - 1)
				sb.append(" " + op + " ");
		}
		return sb.toString();
	}
	public void readExternal(ObjectInput in) 
		throws IOException,
		ClassNotFoundException 
	{
		super.readExternal(in);
		flist = new Filter[in.readInt()];
		for(int i = 0; i < flist.length; ++i)
			flist[i] = readFilter(in);
	}
	
	public void writeExternal(ObjectOutput out) throws IOException 
	{
		super.writeExternal(out);
		out.writeInt(flist.length);
		for(int i = 0; i < flist.length; ++i)
			writeFilter(out, flist[i]);
	}
}
