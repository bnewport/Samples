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
 * This checks if an attribute equals a scalar
 * @author bnewport
 *
 */
public class EQFilter extends CompareFilter 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6919078613266693470L;
	public EQFilter(ValuePath value, Object o)
	{
		super(value, o);
	}
	@Override
	public boolean filter(Object fo) 
	{
		Object a = v.get(fo);
		return (a != null) ? a.equals(o) : false;
	}
}
