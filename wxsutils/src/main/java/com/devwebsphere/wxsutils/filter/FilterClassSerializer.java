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

import com.devwebsphere.wxsutils.utils.ClassSerializer;

public class FilterClassSerializer extends ClassSerializer 
{
	FilterClassSerializer()
	{
		storeClass(AndFilter.class, OrFilter.class, EQFilter.class);
		storeClass(GTEQFilter.class, GTFilter.class, LTEFilter.class);
		storeClass(LTFilter.class, NEQFilter.class, FalseFilter.class, TrueFilter.class);
		storeClass(IsNullFilter.class, NotFilter.class);
	}

}
