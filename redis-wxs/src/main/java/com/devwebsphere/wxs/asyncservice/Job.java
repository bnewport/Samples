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

import java.io.Serializable;

import com.ibm.websphere.objectgrid.Session;

public interface Job<V> extends Serializable
{
	/**
	 * This is called within the JVM hosting the partition primary this Job
	 * was assigned to. The session is a reference to a local session. It is
	 * not a 'client' session.
	 * @param localSession A session to the local shard for the partition
	 * @param MsgId
	 */
	V process(Session localSession, String MsgId);
}
