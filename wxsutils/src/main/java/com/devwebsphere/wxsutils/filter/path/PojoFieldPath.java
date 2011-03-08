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
package com.devwebsphere.wxsutils.filter.path;

import java.lang.reflect.Field;

import com.devwebsphere.wxsutils.filter.ValuePath;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;

/**
 * This fetches the named attributed from a POJO assuming
 * there is a public field on the class of that name.
 * @author bnewport
 *
 */
public class PojoFieldPath implements ValuePath 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6785462152045367579L;
	String propertyName;
	
	public PojoFieldPath(String propertyName)
	{
		try
		{
			this.propertyName = propertyName;
		}
		catch(Exception e)
		{
			throw new ObjectGridRuntimeException(e);
		}
	}
	
	public Object get(Object fo) {
		try
		{
			Field f = fo.getClass().getField(propertyName);
			return f.get(fo);
		}
		catch(Exception e)
		{
			throw new ObjectGridRuntimeException(e);
		}
	}
	public String toString()
	{
		return "." + propertyName;
	}
}
