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

/**
 * This listener is called when ever a message arrives on a queue
 * @author bnewport
 *
 */
public interface MessageProcessor<T>
{
	/**
	 * This method is called to process a message on a queue. The session is begun and committed by the caller.
	 * @param sessionForLocalShard A session which can be used to store extra state resulting from processing this message
	 * @param m The message itself.
	 */
	public T onMessage(Session sessionForLocalShard, Serializable MsgId, Serializable m);
}
