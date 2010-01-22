package com.devwebsphere.wxsutils.jmx.agent;

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
import com.devwebsphere.wxsutils.jmx.MBeanGroupManager;

/**
 * This tracks all AgentMBeans within a JMX and creates the SummaryMBean for those agents.
 *
 */
public final class AgentMBeanManager extends MBeanGroupManager<AgentMBeanImpl>
{
	public AgentMBeanManager(String gridName) {
		super(AgentMBeanImpl.class, AgentMBean.class, gridName, "Agent", "ClassName");
	}

	@Override
	public AgentMBeanImpl createMBean(String gridName, String className) 
	{
		return new AgentMBeanImpl(className);
	}
	
}
