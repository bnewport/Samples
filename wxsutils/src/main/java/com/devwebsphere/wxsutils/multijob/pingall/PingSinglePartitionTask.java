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
package com.devwebsphere.wxsutils.multijob.pingall;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.multijob.SinglePartTask;
import com.ibm.websphere.objectgrid.Session;

public class PingSinglePartitionTask implements SinglePartTask<Boolean, Boolean>
{
	static Logger logger = Logger.getLogger(PingSinglePartitionTask.class.getName());
	private static final long serialVersionUID = 1722977140374061823L;
	
	public PingSinglePartitionTask() {}

	/**
	 * This can be called to check if a partition result is
	 * empty and not interesting for clients
	 */
	public boolean isResultEmpty(Boolean result) 
	{
		return true;
	}

	public Boolean process(Session sess) 
	{
		String aMap = (String)sess.getObjectGrid().getListOfMapNames().iterator().next();
		int partitionId = sess.getObjectGrid().getMap(aMap).getPartitionId();
		logger.log(Level.INFO, "Ping to partition " + partitionId);
		return Boolean.TRUE;
	}

}