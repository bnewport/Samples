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
 * This returns true IFF both Filters are true
 * @author bnewport
 *
 */
public class AndFilter extends BinaryLogicalFilter 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2814315378888140544L;

	public AndFilter(Filter left, Filter right)
	{
		super(left, right);
	}
	
	@Override
	public boolean filter(Object o) 
	{
		boolean l = left.filter(o);
		boolean r = right.filter(o);
		return l && r;
	}

}
