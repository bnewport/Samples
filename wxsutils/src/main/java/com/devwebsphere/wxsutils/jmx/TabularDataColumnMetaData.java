//
//This sample program is provided AS IS and may be used, executed, copied and
//modified without royalty payment by customer (a) for its own instruction and 
//study, (b) in order to develop applications designed to run with an IBM 
//WebSphere product, either for customer's own internal use or for redistribution 
//by customer, as part of such an application, in customer's own products. "
//
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2005
//All Rights Reserved * Licensed Materials - Property of IBM
//
package com.devwebsphere.wxsutils.jmx;

import java.lang.reflect.Method;

import javax.management.openmbean.SimpleType;


public class TabularDataColumnMetaData
{
	String name;
	String description;
	SimpleType type;
	Method method;

	TabularDataColumnMetaData(String d, SimpleType t, Method m)
	{
		description = d;
		type = t;
		method = m;
		name = TabularDataMetaData.getAttributeFromMethod(m);
	}
	
	Object convertType(Object v)
	{
		SimpleType rc = TabularDataMetaData.typeConversion.get(v.getClass());
		if(rc.equals(type))
			return v;
		throw new IllegalArgumentException("Cannot convert type");
	}
}