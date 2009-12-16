package com.devwebsphere.purequery.loader;


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

/**
 * This is an abstract class that POJOs must implement to create a purequery key value loader
 * using ValueLoader.
 * @see LongLongProperty
 * @see StringLongProperty
 * @see ValueLoader
 */
public abstract class BaseKeyValue <K,V> 
{
	/**
	 * This should return the object representing the key in this key/value pair. Typically
	 * this is a string or a Long
	 * @return The Object representing the key
	 */
	abstract public K baseGetKey();
	/**
	 * This should return the object representing the value in this key/value pair. Typically
	 * this is a string or a Long. List values are not implemented using this system
	 * @return The Object representing the value
	 */
	abstract public V baseGetValue();
}

