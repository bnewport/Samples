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

public class LuceneFileMBeanManager extends MBeanGroupManager<LuceneFileMBeanImpl>
{
	public LuceneFileMBeanManager() 
		throws InstanceAlreadyExistsException
	{
		super(LuceneFileMBeanImpl.class, LuceneFileMBean.class, "com.devwebsphere.wxslucene", "directory", "LuceneFile", "FileName");
	}

	@Override
	public LuceneFileMBeanImpl createMBean(String directoryName, String fileName) 
	{
		return new LuceneFileMBeanImpl(directoryName, fileName);
	}

}
