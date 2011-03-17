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
package com.devwebsphere.wxsutils.wxsmap;

import com.devwebsphere.wxsutils.WXSUtils;

public class StartSingleCatalog {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		int i = 0;
		String catName = args[i++];
		String cep = args[i++];
		
		System.out.println("This catalog server name: " + catName);
		System.out.println("CEP: " + cep);
		WXSUtils.startCatalogServer(cep, catName);
	}

}
