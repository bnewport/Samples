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
import org.apache.lucene.store.LockFactory;

import com.devwebsphere.wxs.fs.MapNames;
import com.devwebsphere.wxsutils.WXSMap;
import com.devwebsphere.wxsutils.WXSUtils;

public class WXSLockFactory extends LockFactory 
{
	WXSUtils client;
	WXSMap<String, Boolean> lockMap;
	
	public WXSLockFactory(WXSUtils client)
	{
		this.client = client;
		lockMap = client.getCache(MapNames.LOCK_MAP);
	}

	@Override
	public void clearLock(String lockName) throws IOException 
	{
		Lock l = new WXSLock(client, lockName);
		l.release();
	}

	@Override
	public Lock makeLock(String lockName) 
	{
		return new WXSLock(client, lockName);
	}

}
