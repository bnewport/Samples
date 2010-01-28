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
package com.devwebsphere.wxssearch.test;

import java.io.Serializable;

import com.devwebsphere.wxssearch.type.ExactIndex;
import com.devwebsphere.wxssearch.type.PrefixIndex;
import com.devwebsphere.wxssearch.type.SubstringIndex;

/**
 * Test business object with 3 indexed fields.
 *
 */
public class TestBusinessObject implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6204599238843897028L;
	
	/**
	 * Index firstName using a LIKE %XXX% style index
	 */
	@SubstringIndex
	String firstName;
	/**
	 * Index middle name using a LIKE XXXX% style index
	 */
	@PrefixIndex
	String middleName;
	/**
	 * Index surname using an exact index. Only track surnames
	 * with < 100 matches
	 */
	@ExactIndex(maxMatches=100)
	String surname;
}
