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

/**
 * This checks if an attribute is null
 * @author bnewport
 *
 */
public class IsNullFilter extends Filter 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8864323686268364722L;
	ValuePath v;

	public IsNullFilter(ValuePath value)
	{
		v = value;
	}
	
	@Override
	public boolean filter(Object fo) {
		return v.get(fo) == null;
	}

	public String toString()
	{
		return v.toString() + " ISNULL ";
	}
}
