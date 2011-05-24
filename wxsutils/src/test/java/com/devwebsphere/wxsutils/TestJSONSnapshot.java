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

import java.sql.Date;
import java.util.Calendar;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.devwebsphere.wxs.jdbcloader.Customer;
import com.devwebsphere.wxsutils.snapshot.CreateJSONSnapshotAgent;
import com.devwebsphere.wxsutils.snapshot.ReadJSONSnapshotAgent;
import com.ibm.websphere.objectgrid.BackingMap;
import com.ibm.websphere.objectgrid.ObjectGrid;
import com.ibm.websphere.objectgrid.ObjectGridException;


public class TestJSONSnapshot
{
	static ObjectGrid ogclient;
	static WXSUtils utils;
	static BackingMap bmFarMap3;
	
	@BeforeClass
	public static void setupTest()
		throws Exception
	{
		// do everything in one JVM for test
//		ogclient = WXSUtils.startTestServer("Grid", "/objectgrid.xml", "/deployment.xml");
		// switch to this to connect to remote grid instead.
		utils = WXSUtils.getDefaultUtils();
		ogclient = utils.getObjectGrid();
		bmFarMap3 = ogclient.getMap("FarMap3");
	}

	/**
	 * This clears the FarMap3 in preparation for any tests
	 */
	public static void clearMap()
	{
		try
		{
			ogclient.getSession().getMap(bmFarMap3.getName()).clear();
		}
		catch(ObjectGridException e)
		{
			Assert.fail("Exception during clear");
		}
	}

	@Test
	public void testCreateSnapshot()
		throws Exception
	{
		clearMap();
		
		WXSMap<String, Customer> map = utils.getCache(bmFarMap3.getName());

		Calendar calendar = Calendar.getInstance();
		
		for(int i = 0; i < 1000; ++i)
		{
			Customer c = new Customer();
			c.id = Integer.toString(i);
			calendar.set(1945, 3 /* Month - 1 */, 3 /* day */); // April 3, 1945
			c.dob = new Date(calendar.getTimeInMillis());
			c.firstName = "Billy";
			c.surname = "Newport";
			map.put(c.id, c);
		}
		
		CreateJSONSnapshotAgent.writeSnapshot(utils, bmFarMap3.getName(), "/tmp");
	}
	
	@Test
	public void testReadSnapshot()
		throws Exception
	{
		clearMap(); // read does not do a clear, do the clear first if you need it
		// Read the map bmFarMap3 from the remote snapshot
		ReadJSONSnapshotAgent.readSnapshot(utils, bmFarMap3.getName(), "/tmp", "localhost:2809");
	}
}
