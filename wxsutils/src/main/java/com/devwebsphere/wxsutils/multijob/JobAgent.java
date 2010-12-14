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


import java.util.Map;

import com.ibm.websphere.objectgrid.ObjectMap;
import com.ibm.websphere.objectgrid.Session;
import com.ibm.websphere.objectgrid.datagrid.MapGridAgent;

/**
 * This basically wraps a SinglePartTask with an agent so it can be executed on
 * the grid side. This is done to minimize the implementation of a SinglePartTask
 * @author bnewport
 *
 * @param <V> The return type from the SinglePartTask
 * @param <R> The user exposed type
 */
public class JobAgent<V,R> implements MapGridAgent
{
	SinglePartTask<V, R> task;
	public JobAgent(SinglePartTask<V,R> task)
	{
		this.task = task;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1628679561060400929L;

	public Object process(Session sess, ObjectMap routingMap, Object key) 
	{
		V v = task.process(sess);
		return v;
	}

	public Map processAllEntries(Session arg0, ObjectMap arg1) 
	{
		return null;
	}
	
}