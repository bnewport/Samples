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

import java.nio.ByteBuffer;
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
	
	static ByteBuffer emptyByteBuffer = ByteBuffer.wrap(new byte[0]);
	
	public Handler(WXSUtils c)
	{
		client = c;
	}
	
	byte[] toByteArray(ByteBuffer a)
	{
		// not sure if thrift will tolerate us moving the position in the supplied bytebuffer
		// so use a readonly copy.
		ByteBuffer copy = a.asReadOnlyBuffer();
		byte[] rc = new byte[copy.remaining()];
		copy.get(rc);
		return rc;
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
	
	/**
	 * Can't return null here so return an empty ByteBuffer to indicate key not found
	 */
	public ByteBuffer get(String mapName, ByteBuffer key) throws TException 
	{
		WXSMap<ByteArrayKey, byte[]> map = getWXSMap(mapName);
		byte[] rc = map.get(new ByteArrayKey(toByteArray(key)));
		if(rc != null)
			return ByteBuffer.wrap(rc);
		else
			return emptyByteBuffer;
	}

	/**
	 * An empty ByteBuffer indicates key not found for that particular key
	 */
	public List<ByteBuffer> getAll(String mapName, List<ByteBuffer> keyList)
			throws TException 
	{
		WXSMap<ByteArrayKey, byte[]> map = getWXSMap(mapName);
		ArrayList<ByteArrayKey> kl = new ArrayList<ByteArrayKey>();
		for(ByteBuffer k : keyList) kl.add(new ByteArrayKey(toByteArray(k)));
		Map<ByteArrayKey, byte[]> rc = map.getAll(kl);
		
		ArrayList<ByteBuffer> results = new ArrayList<ByteBuffer>();
		for(ByteArrayKey k : kl)
		{
			byte[] v = rc.get(k);
			results.add((v != null) ? ByteBuffer.wrap(rc.get(k)) : emptyByteBuffer);
		}
		return results;
	}

	public void put(String mapName, ByteBuffer key, ByteBuffer value) throws TException {
		WXSMap<ByteArrayKey, byte[]> map = getWXSMap(mapName);
		map.put(new ByteArrayKey(toByteArray(key)), toByteArray(value));
	}

	public void putAll(String mapName, List<ByteBuffer> keys, List<ByteBuffer> values)
			throws TException {
		WXSMap<ByteArrayKey, byte[]> map = getWXSMap(mapName);
		Map<ByteArrayKey, byte[]> m = new HashMap<ByteArrayKey, byte[]>();
		Iterator<ByteBuffer> iter = values.iterator();
		for(ByteBuffer key : keys)
		{
			m.put(new ByteArrayKey(toByteArray(key)), toByteArray(iter.next()));
		}
		map.putAll(m);
	}

	public void remove(String mapName, ByteBuffer key) throws TException {
		WXSMap<ByteArrayKey, byte[]> map = getWXSMap(mapName);
		map.remove(new ByteArrayKey(toByteArray(key)));
	}

	public void removeAll(String mapName, List<ByteBuffer> keyList)
			throws TException {
		WXSMap<ByteArrayKey, byte[]> map = getWXSMap(mapName);
		ArrayList<ByteArrayKey> kl = new ArrayList<ByteArrayKey>();
		for(ByteBuffer k : keyList) kl.add(new ByteArrayKey(toByteArray(k)));
		map.removeAll(kl);
	}

}
