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
package com.devwebsphere.wxsutils.wxsmap;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.Beta;
import com.ibm.websphere.objectgrid.ObjectGridRuntimeException;
import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.ReduceGridAgent;

/**
 * This takes an list of Key/Agent pairs and then invokes the agent for each corresponding key and returns a Map with
 * the agent result for each key if not null
 */
@Beta
public class ReduceAgentNoKeysExecutor<K, A extends ReduceGridAgent, X> implements ReduceGridAgent {
	static Logger logger = Logger.getLogger(ReduceAgentNoKeysExecutor.class.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = 6568906743945108310L;

	public A agent;
	public String agentTargetMapName;

	public Object reduce(Session sess, ObjectMap map) {
		return null;
	}

	public Object reduce(Session sess, ObjectMap map, Collection arg2) {
		try {
			ObjectMap targetMap = sess.getMap(agentTargetMapName);
			X x = (X) agent.reduce(sess, targetMap);
			return x;
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception running application agent", e);
			throw new ObjectGridRuntimeException(e);
		}
	}

	public Object reduceResults(Collection arg0) {
		return agent.reduceResults(arg0);
	}

	public ReduceAgentNoKeysExecutor() {
	}
}
