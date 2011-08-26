//
//This sample program is provided AS IS and may be used, executed, copied and
//modified without royalty payment by customer (a) for its own instruction and
//study, (b) in order to develop applications designed to run with an IBM
//WebSphere product, either for customer's own internal use or for redistribution
//by customer, as part of such an application, in customer's own products. "
//
//5724-J34 (C) COPYRIGHT International Business Machines Corp. 2009
//All Rights Reserved * Licensed Materials - Property of IBM
//
package com.devwebsphere.wxsutils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.Test;

public class TestResourceLoading {
	@Test
	public void testLoading() throws IOException {
		Properties props = new Properties();
		InputStream is = WXSUtils.class.getResourceAsStream("/wxsutils.properties");
		if (is == null) {
			is = WXSUtils.class.getResourceAsStream("/META-INF/wxsutils.properties");
		}
		if (is == null) {
			throw new FileNotFoundException("/[META-INF/]wxsutils.properties");
		}
		props.load(is);
		Assert.assertEquals(null, props.getProperty("cep"));
		is.close(); // BN added close
	}

}
