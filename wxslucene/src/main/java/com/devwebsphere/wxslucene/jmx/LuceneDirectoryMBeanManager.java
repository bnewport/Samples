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
package com.devwebsphere.wxslucene.jmx;

import javax.management.InstanceAlreadyExistsException;

import com.devwebsphere.wxsutils.jmx.MBeanGroupManager;

public class LuceneDirectoryMBeanManager extends MBeanGroupManager<LuceneDirectoryMBeanImpl>
{
	public LuceneDirectoryMBeanManager() 
		throws InstanceAlreadyExistsException
	{
		super(LuceneDirectoryMBeanImpl.class, LuceneDirectoryMBean.class, "com.devwebsphere.wxslucene", "grid", "LuceneDirectory", "DirectoryName");
	}

	@Override
	public LuceneDirectoryMBeanImpl createMBean(String grid, String directoryName) 
	{
		return new LuceneDirectoryMBeanImpl(grid, directoryName);
	}

}
