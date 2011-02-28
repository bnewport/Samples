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
package com.devwebsphere.wxsthrift;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devwebsphere.wxssearch.ByteArrayKey;
import com.devwebsphere.wxsthrift.gen.WxsGatewayService;
import com.devwebsphere.wxsutils.WXSMap;
import com.devwebsphere.wxsutils.WXSUtils;

public class Handler implements WxsGatewayService.Iface
{
	static Logger logger = LoggerFactory.getLogger(Handler.class);
	
	WXSUtils client;
	Map<String, String> mapNames = new ConcurrentHashMap<String, String>();
	
	public Handler(WXSUtils c)
	{
		client = c;
	}
	
	WXSMap<ByteArrayKey, byte[]> getWXSMap(String mapName)
	{
		String name = mapNames.get(mapName);
		if(name == null)
		{
			if(client.getObjectGrid().getMap(mapName) != null)
				name = mapName;
			else
				name = "M." + mapName;
			mapNames.put(mapName, name);
		}
		WXSMap<ByteArrayKey, byte[]> map = client.getCache(name);
		return map;
	}
	
	public byte[] get(String mapName, byte[] key) throws TException 
	{
		WXSMap<ByteArrayKey, byte[]> map = getWXSMap(mapName);
		return map.get(new ByteArrayKey(key));
	}

	public List<byte[]> getAll(String mapName, List<byte[]> keyList)
			throws TException 
	{
		WXSMap<ByteArrayKey, byte[]> map = getWXSMap(mapName);
		ArrayList<ByteArrayKey> kl = new ArrayList<ByteArrayKey>();
		for(byte[] k : keyList) kl.add(new ByteArrayKey(k));
		Map<ByteArrayKey, byte[]> rc = map.getAll(kl);
		
		ArrayList<byte[]> results = new ArrayList<byte[]>();
		for(ByteArrayKey k : kl)
			results.add(rc.get(k));
		return results;
	}

	public void put(String mapName, byte[] key, byte[] value) throws TException {
		WXSMap<ByteArrayKey, byte[]> map = getWXSMap(mapName);
		map.put(new ByteArrayKey(key), value);
	}

	public void putAll(String mapName, List<byte[]> keys, List<byte[]> values)
			throws TException {
		WXSMap<ByteArrayKey, byte[]> map = getWXSMap(mapName);
		Map<ByteArrayKey, byte[]> m = new HashMap<ByteArrayKey, byte[]>();
		Iterator<byte[]> iter = values.iterator();
		for(byte[] key : keys)
		{
			m.put(new ByteArrayKey(key), iter.next());
		}
		map.putAll(m);
	}

	public void remove(String mapName, byte[] key) throws TException {
		WXSMap<ByteArrayKey, byte[]> map = getWXSMap(mapName);
		map.remove(new ByteArrayKey(key));
	}

	public void removeAll(String mapName, List<byte[]> keyList)
			throws TException {
		WXSMap<ByteArrayKey, byte[]> map = getWXSMap(mapName);
		ArrayList<ByteArrayKey> kl = new ArrayList<ByteArrayKey>();
		for(byte[] k : keyList) kl.add(new ByteArrayKey(k));
		map.removeAll(kl);
	}

}
