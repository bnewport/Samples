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
package com.devwebsphere.wxs.fs;

import java.util.zip.Deflater;

public class ThreadLocalDeflator extends ThreadLocal<Deflater> 
{
	@Override
	protected Deflater initialValue() {
		Deflater d = new Deflater(Deflater.BEST_SPEED);
		return d;
	}

}
