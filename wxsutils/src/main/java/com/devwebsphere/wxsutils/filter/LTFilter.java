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
 * This checks if an attribute is < a scalar
 * @author bnewport
 *
 */
public class LTFilter extends CompareFilter 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5224402950812915172L;

	public LTFilter() {}
	
	public LTFilter(ValuePath v, Object o)
	{
		super(v, o);
	}
	
	@Override
	public boolean filter(Object fo) 
	{
		Comparable a = (Comparable)v.get(fo);
		return (a != null) ? a.compareTo(o) < 0 : true;
	}

	public String toString()
	{
		return createString(" < ");
	}
}
