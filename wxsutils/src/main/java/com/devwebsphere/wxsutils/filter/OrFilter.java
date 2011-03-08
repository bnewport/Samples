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
 * This returns TRUE if any of the Filters are TRUE
 * @author bnewport
 *
 */
public class OrFilter extends BinaryLogicalFilter 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6535676992614300637L;

	public OrFilter(Filter... list)
	{
		super(list);
	}
	
	@Override
	public boolean filter(Object fo) 
	{
		boolean rc = false;
		for(Filter f : flist)
		{
			rc = rc || f.filter(fo);
			if(rc) break;
		}
		return rc;
	}

	public String toString()
	{
		return createString("OR");
	}
}
