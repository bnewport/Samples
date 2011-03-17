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
import java.net.InetSocketAddress;
import java.net.URISyntaxException;

import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.server.TServer;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TTransportException;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.devwebsphere.wxsthrift.gen.WxsGatewayService;
import com.devwebsphere.wxsutils.WXSUtils;

public class WxsThriftServer 
{
	static Logger logger = LoggerFactory.getLogger(WxsThriftServer.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) 
		throws TTransportException, IOException, URISyntaxException, FileNotFoundException
	{
		Options options = new Options();
		CmdLineParser parser = new CmdLineParser(options);
		try
		{
			parser.parseArgument(args);
			WXSUtils client = WXSUtils.getDefaultUtils();
			if(client == null)
			{
				logger.error("Cannot connect to grid, check wxsutils.properties");
				return;
			}
			
			InetSocketAddress address = new InetSocketAddress(options.hostName, options.port);
			System.out.println("Listening for thrift requests @ " + options.hostName + ":" + options.port);
			final TNonblockingServerSocket socket = new TNonblockingServerSocket(address);
			final WxsGatewayService.Processor processor = new WxsGatewayService.Processor(
			        new Handler(client));
			final TServer server = new THsHaServer(processor, socket,
			        new TFramedTransport.Factory(), new TBinaryProtocol.Factory());
			
			server.serve();	
		}
		catch(CmdLineException e)
		{
			parser.printUsage(System.out);
		}
	}
}
