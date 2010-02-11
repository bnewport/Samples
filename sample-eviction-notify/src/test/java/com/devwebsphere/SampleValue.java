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
package com.devwebsphere;

import java.io.Serializable;

public class SampleValue implements Serializable, Cloneable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2701749834220105039L;
	
	long time;

	/**
	 * Performance aid, avoids the need for Serialization to copy the object.
	 * Recommended
	 */
	public Object clone()
	{
		SampleValue copy = new SampleValue();
		copy.time = time;
		return copy;
	}
	
	public String toString()
	{
		return "SampleValue<" + time + ">";
	}
}
