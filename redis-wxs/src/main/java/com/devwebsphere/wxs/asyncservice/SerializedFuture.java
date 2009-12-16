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
package com.devwebsphere.wxs.asyncservice;

import com.ibm.websphere.objectgrid.ObjectGrid;

/**
 * This is a serializable form of the Futures returned for exactly once messages. It contains
 * a single method that inflates the Future again.
 * @author bnewport
 *
 * @param <T>
 */
public interface SerializedFuture<T> extends java.io.Serializable
{
	/**
	 * This inflates the Future encoded by this serializable object.
	 * @param grid
	 * @return
	 */
	java.util.concurrent.Future<T> inflate(ObjectGrid grid);
}
