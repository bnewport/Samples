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
package com.devwebsphere.rediswxs.agent.set;

import java.io.Serializable;
import java.util.HashSet;

import com.devwebsphere.rediswxs.data.list.ListItem;
import com.ibm.websphere.objectgrid.Session;

/**
 * This interface has some common methods between the purequery and jdbc versions
 * of the Loader. This applies only to the Loader for SetItems
 *
 */
public interface SetLoaderOperations 
{
	/**
	 * This fetches a single database record for a specific set member
	 * @param sess
	 * @param key
	 * @param value
	 * @return
	 */
	ListItem getSetRecord(Session sess, String key, Long value);
	/**
	 * This batch fetches all members of the specified set.
	 * @param sess
	 * @param key
	 * @return
	 */
	HashSet<Serializable> getAllMembers(Session sess, String key);
	
}
