package com.devwebsphere.wxs.jdbcloader;

import java.sql.Connection;
import java.sql.Statement;

import javax.sql.DataSource;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.derby.jdbc.EmbeddedDriver;
import org.junit.BeforeClass;
import org.junit.Test;

import com.devwebsphere.jdbc.loader.GenericJDBCLoader;
import com.devwebsphere.wxs.jdbcloader.telco.Device;


public class TestPreloaderFramework extends TestCase
{
	static DataSource dataSource;
	@BeforeClass
	static void setup()
		throws Exception
	{
		Class.forName(EmbeddedDriver.class.getName());
		BasicDataSource ds = new BasicDataSource();
		dataSource = ds;
		ds.setDriverClassName(EmbeddedDriver.class.getName());
		ds.setUrl("jdbc:derby:preloadDB;create=true");
		
		Connection connTest = ds.getConnection();
		Assert.assertNotNull(connTest);
		Statement stmt = connTest.createStatement();

		String sql = "DROP TABLE IF EXISTS `DEVICE`; ";
		sql += " CREATE TABLE IF NOT EXISTS `DEVICE` ( ";
		sql += "  `MDM` varchar(20) NOT NULL, ";
		sql += " `DEVICE_CODE` int(11) NOT NULL, ";
		sql += " `PHONE_NUMBER` varchar(15) NOT NULL, ";
		sql += " `ACTIVE` tinyint(1) NOT NULL, ";
		sql += " PRIMARY KEY (`MDM`) ";
		sql += ")";
		stmt.execute(sql);
		
		sql = "DROP TABLE IF EXISTS `DEVICEDATA`; ";
		sql += "CREATE TABLE IF NOT EXISTS `DEVICEDATA` ( ";
		sql += " `DEVICE_CODE` int(11) NOT NULL, ";
		sql += "`DEVICE_NAME` varchar(100) NOT NULL, ";
		sql += "  `IS3G` tinyint(1) NOT NULL,";
		sql += " `ISSMART` tinyint(1) NOT NULL,";
		sql += "  `VENDOR` varchar(100) NOT NULL,";
		sql += "  PRIMARY KEY (`DEVICE_CODE`)";
		sql += ")";
		stmt.execute(sql);
		
		sql = "DROP TABLE IF EXISTS `FEATURES`;";
		sql += "CREATE TABLE IF NOT EXISTS `FEATURES` (";
		sql += "  `MDM` varchar(15) NOT NULL,";
		sql += "  `FEATURE_CODE` varchar(10) NOT NULL,";
		sql += "  `ACTIVE_DATE` date NOT NULL,";
		sql += "  PRIMARY KEY (`MDM`,`FEATURE_CODE`)";
		sql += " )";
		stmt.execute(sql);
		stmt.close();
		connTest.close();
	}

	@Test
	public void testLoaderInitialization()
		throws Exception
	{
//		GenericJDBCLoader<Device> loader = new GenericJDBCLoader<Device>();
//		loader.setClassName(Device.class.getName());
	}
}
