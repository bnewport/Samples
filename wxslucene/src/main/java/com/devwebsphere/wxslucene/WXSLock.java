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
package com.devwebsphere.wxslucene;

import java.io.IOException;

import org.apache.lucene.store.Lock;

import com.devwebsphere.wxs.fs.MapNames;
import com.devwebsphere.wxsutils.WXSMap;
import com.devwebsphere.wxsutils.WXSUtils;

public class WXSLock extends Lock 
{
	WXSMap<String, Boolean> lockMap;
	WXSUtils client;
	String name;

	public WXSLock(WXSUtils client, String name)
	{
		this.client = client;
		this.name = name;
		this.lockMap = client.getCache(MapNames.LOCK_MAP);
	}
	
	@Override
	public boolean isLocked() 
	{
		return lockMap.contains(name);
	}

	@Override
	public boolean obtain() throws IOException 
	{
		return lockMap.lock(name, Boolean.TRUE, 2000);
	}

	@Override
	public void release() throws IOException 
	{
		lockMap.unlock(name);
	}

}
