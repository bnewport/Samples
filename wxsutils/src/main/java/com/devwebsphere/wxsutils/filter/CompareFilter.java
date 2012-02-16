//
//This sample program is provided AS IS and may be used, executed, copied and
//modified without royalty payment by customer (a) for its own instruction and
//study, (b) in order to develop applications designed to run with an IBM
//WebSphere product, either for customer's own internal use or for redistribution
//by customer, as part of such an application, in customer's own products. "
//
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2009, 2012
//All Rights Reserved * Licensed Materials - Property of IBM
//
package com.devwebsphere.wxsutils.filter;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.ibm.websphere.objectgrid.plugins.io.dataobject.DataObjectContextAware;

/**
 * This is a base class for Filters comparing an attribute with a scalar
 * 
 * @author bnewport
 * 
 */
public abstract class CompareFilter extends Filter {
	ValuePath v;
	Object o;

	transient private Boolean requiresContext = null;

	public CompareFilter() {
	}

	public CompareFilter(ValuePath v, Object o) {
		this.v = v;
		requiresContext = Boolean.valueOf(v instanceof DataObjectContextAware);
		this.o = o;
	}

	protected String createString(String op) {
		return v.toString() + " " + op + " " + o.toString();
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		v = (ValuePath) serializer.readObject(in);
		o = in.readObject();

		requiresContext = Boolean.valueOf(v instanceof DataObjectContextAware);
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		serializer.writeObject(out, v);
		out.writeObject(o);
	}

	@Override
	public Boolean requiresDataObjectContext() {
		return requiresContext;
	}
}
