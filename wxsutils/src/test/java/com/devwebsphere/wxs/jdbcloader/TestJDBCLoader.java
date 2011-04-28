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
package com.devwebsphere.wxs.jdbcloader;

import java.sql.Connection;

import junit.framework.Assert;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.derby.jdbc.EmbeddedDriver;
import org.junit.BeforeClass;

public class TestJDBCLoader 
{
	@BeforeClass
	static void setup()
		throws Exception
	{
		Class.forName(EmbeddedDriver.class.getName());
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName(EmbeddedDriver.class.getName());
		ds.setUrl("jdbc:derby:derbyDB;create=true");
		
		Connection connTest = ds.getConnection();
		Assert.assertNotNull(connTest);
		connTest.close();
	}

//	void test
}
