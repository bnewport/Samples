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
package com.devwebsphere.wxsutils.shell;

import org.kohsuke.args4j.Option;

public class Options 
{
	@Option(name="-grid",usage="Grid name")
	public String gridName = "Grid";
	
	@Option(name="-ch",usage="Catalog host name")
	public String ch="localhost";
	
	@Option(name="-cp",usage="Catalog port number")
	public Integer cp=2809;
}
