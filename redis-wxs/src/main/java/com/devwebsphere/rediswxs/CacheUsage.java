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

package com.devwebsphere.rediswxs;

/**
 * This flag tells the redis clients whether to use the WXS connection with
 * a near cache enabled or not.
 *
 */
public enum CacheUsage 
{
	/**
	 * Near cache is enabled (applies to get/set type methods)
	 */
	NEARCACHE,
	/**
	 * Near cache is disabled (all list/set operations)
	 */
	NONEARCACHE
}
