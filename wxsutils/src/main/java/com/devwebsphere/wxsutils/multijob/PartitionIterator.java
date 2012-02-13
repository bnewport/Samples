//
//This sample program is provided AS IS and may be used, executed, copied and
//modified without royalty payment by customer (a) for its own instruction and
//study, (b) in order to develop applications designed to run with an IBM
//WebSphere product, either for customer's own internal use or for redistribution
//by customer, as part of such an application, in customer's own products. "
//
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2012
//All Rights Reserved * Licensed Materials - Property of IBM
//
package com.devwebsphere.wxsutils.multijob;

import java.util.NoSuchElementException;

public interface PartitionIterator {	
	/**
	 * 
	 * @return <tt>true</tt> if the iterator has more partitions.
	 */
	boolean hasNext();

	/**
	 * @return the next partition number in the iteration.
	 * @exception NoSuchElementException
	 *                iteration has no more elements.
	 */
	int next();

	/**
	 * Reset the iterator to the original starting partition
	 */
	void reset();
}
