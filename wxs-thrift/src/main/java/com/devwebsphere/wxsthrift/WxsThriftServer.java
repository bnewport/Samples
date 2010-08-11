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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;

import com.devwebsphere.wxsthrift.gen.WxsGatewayService;
import com.devwebsphere.wxsutils.WXSUtils;

public class WxsThriftServer 
{
	static Logger logger = Logger.getLogger(WxsThriftServer.class.getName());

	/**
	 * @param args
	 */
	public static void main(String[] args) 
		throws TTransportException, IOException, URISyntaxException, FileNotFoundException
	{
		WXSUtils client = WXSUtils.getDefaultUtils();
		if(client == null)
		{
			logger.log(Level.SEVERE, "Cannot connect to grid, check wxsutils.properties");
			return;
		}
		
		final TNonblockingServerSocket socket = new TNonblockingServerSocket(9100);
		final WxsGatewayService.Processor processor = new WxsGatewayService.Processor(
		        new Handler(client));
		final TServer server = new THsHaServer(processor, socket,
		        new TFramedTransport.Factory(), new TCompactProtocol.Factory());
		
		server.serve();	
	}
}
