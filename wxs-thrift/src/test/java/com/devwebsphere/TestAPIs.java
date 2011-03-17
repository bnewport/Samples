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
package com.devwebsphere;

import java.nio.ByteBuffer;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.devwebsphere.wxsthrift.gen.WxsGatewayService;


public class TestAPIs 
{
	static WxsGatewayService.Client client;
	
	@BeforeClass
	static public void initialize()
		throws TTransportException
	{
		TTransport port = new TSocket("localhost", 9100);
		TFramedTransport transport = new TFramedTransport(port);
		TCompactProtocol protocol = new TCompactProtocol(transport);
		client = new WxsGatewayService.Client(protocol);
		transport.open();
	}
	
	@Test
	public void testPut()
		throws TException
	{
		ByteBuffer original = ByteBuffer.wrap("Newport".getBytes());
		ByteBuffer key = ByteBuffer.wrap("Billy".getBytes());
		client.put("map", key, original);
		
		ByteBuffer value = client.get("map", key);
		
		Assert.assertArrayEquals(original.array(), value.array());
	}
}
