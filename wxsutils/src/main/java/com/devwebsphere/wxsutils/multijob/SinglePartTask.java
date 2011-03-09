package com.devwebsphere.wxsutils.multijob;
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


import java.io.Serializable;

import com.ibm.websphere.objectgrid.Session;

public interface SinglePartTask<V,R> extends Serializable
{
	/**
	 * This is called on the grid side to process the next block within this partition
	 * @param sess The session to the local partition primary
	 * @return the next block of processing information for the user
	 */
	V process(Session sess);
	/**
	 * This is called on the client side to extract the actual value to return
	 * to the application from the value returned from process. This allows
	 * some post processing and detecting of conditions like no more data
	 * on the client side.
	 * @param rawRC The value returned from {@link #process(Session)}
	 * @return
	 */
	R extractResult(V rawRC);
	
	/**
	 * This tests of the result has any data
	 * @return
	 */
	boolean isResultEmpty(R result);
}
