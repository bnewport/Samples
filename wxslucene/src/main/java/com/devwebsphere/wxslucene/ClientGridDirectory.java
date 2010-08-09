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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import com.devwebsphere.wxsutils.WXSUtils;

/**
 * This is a Directory to connect to the grid with a file name based parameter
 * without copying from disk to the grid. The GridDirectory(fileName) is typically
 * used to connect to a grid AND copy a local disk based directory in to it. If
 * you just want to connect to the grid the same way without copying the index again
 * then this class is just a way to use the same constructor and not do the copy
 * @author bnewport
 *
 */
public class ClientGridDirectory extends GridDirectory 
{
	static Logger logger = Logger.getLogger(ClientGridDirectory.class.getName());
	
	public ClientGridDirectory(String fileName)
		throws FileNotFoundException, URISyntaxException, IOException
	{
		super(WXSUtils.getDefaultUtils(), fileName);
	}
	
	public String toString()
	{
		return "ClientGridDirectory:<" + getName() + ">";
	}
}
