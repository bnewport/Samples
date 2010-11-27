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
