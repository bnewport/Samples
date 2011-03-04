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
 * This is an easy way to build filter graphs using the common dot notation
 * @author bnewport
 *
 */
public class FilterBuilder 
{
	public FilterBuilder()
	{
	}
	
	public Filter lt(ValuePath v, Object o)
	{
		LTFilter f = new LTFilter(v, o);
		return f;
	}
	
	public Filter lte(ValuePath v, Object o)
	{
		LTEFilter f = new LTEFilter(v, o);
		return f;
	}
	
	public Filter gt(ValuePath v, Object o)
	{
		GTFilter f = new GTFilter(v, o);
		return f;
	}
	
	public Filter gte(ValuePath v, Object o)
	{
		GTEQFilter f = new GTEQFilter(v, o);
		return f;
	}
	
	public Filter eq(ValuePath v, Object o)
	{
		EQFilter f = new EQFilter(v, o);
		return f;
	}
	
	public Filter ne(ValuePath v, Object o)
	{
		NEQFilter f = new NEQFilter(v, o);
		return f;
	}
	
	public Filter isNull(ValuePath v)
	{
		IsNullFilter f = new IsNullFilter(v);
		return f;
	}
	
	public Filter and(Filter... conjList)
	{
		Filter a = new TrueFilter();
		for(Filter q : conjList)
		{
			a = new AndFilter(a, q);
		}
		return a;
	}
	
	public Filter or(Filter... orList)
	{
		Filter a = new FalseFilter();
		for(Filter q : orList)
		{
			a = new OrFilter(a, q);
		}
		return a;
	}
	
	public Filter not(Filter qb)
	{
		return new NotFilter(qb);
	}
	
}
